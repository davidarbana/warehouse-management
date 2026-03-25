package com.davidarbana.warehouse.service.impl;

import com.davidarbana.warehouse.dto.request.OrderRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;
import com.davidarbana.warehouse.entity.*;
import com.davidarbana.warehouse.enums.OrderStatus;
import com.davidarbana.warehouse.exception.InvalidOperationException;
import com.davidarbana.warehouse.exception.ResourceNotFoundException;
import com.davidarbana.warehouse.repository.*;
import com.davidarbana.warehouse.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final TruckRepository truckRepository;
    private final DeliveryRepository deliveryRepository;

    @Value("${delivery.scheduling.max-period}")
    private int maxPeriodDays;

    // ── CLIENT ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ResponseDtos.OrderDetailResponse createOrder(OrderRequest.Create request, String username) {
        User client = getUser(username);

        if (request.getDeadlineDate() != null && request.getDeadlineDate().isBefore(LocalDate.now())) {
            throw new InvalidOperationException("Deadline date cannot be in the past");
        }

        Order order = Order.builder()
                .client(client)
                .status(OrderStatus.CREATED)
                .deadlineDate(request.getDeadlineDate())
                .items(new ArrayList<>())
                .build();

        orderRepository.save(order);
        log.info("Order {} created by {}", order.getOrderNumber(), username);
        return toDetailResponse(order);
    }

    @Override
    @Transactional
    public ResponseDtos.OrderDetailResponse addItemToOrder(Long orderId, OrderRequest.AddItem request, String username) {
        Order order = getOrder(orderId);
        checkOwnership(order, username);
        checkEditable(order);

        InventoryItem invItem = inventoryItemRepository.findById(request.getInventoryItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + request.getInventoryItemId()));

        // if item already exists in order add the 2nd quantity
        order.getItems().stream()
                .filter(i -> i.getInventoryItem().getId().equals(invItem.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setRequestedQuantity(existing.getRequestedQuantity() + request.getQuantity()),
                        () -> order.getItems().add(OrderItem.builder()
                                .order(order)
                                .inventoryItem(invItem)
                                .requestedQuantity(request.getQuantity())
                                .build())
                );

        orderRepository.save(order);
        return toDetailResponse(order);
    }

    @Override
    @Transactional
    public ResponseDtos.OrderDetailResponse removeItemFromOrder(Long orderId, Long orderItemId, String username) {
        Order order = getOrder(orderId);
        checkOwnership(order, username);
        checkEditable(order);

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found: " + orderItemId));

        order.getItems().remove(item);
        orderRepository.save(order);
        return toDetailResponse(order);
    }

    @Override
    @Transactional
    public ResponseDtos.OrderDetailResponse updateItemQuantity(Long orderId, Long orderItemId, OrderRequest.UpdateItem request, String username) {
        Order order = getOrder(orderId);
        checkOwnership(order, username);
        checkEditable(order);

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found: " + orderItemId));

        item.setRequestedQuantity(request.getQuantity());
        orderRepository.save(order);
        return toDetailResponse(order);
    }

    @Override
    @Transactional
    public ResponseDtos.OrderDetailResponse submitOrder(Long orderId, String username) {
        Order order = getOrder(orderId);
        checkOwnership(order, username);

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.DECLINED) {
            throw new InvalidOperationException("Can only submit orders in CREATED or DECLINED status");
        }
        if (order.getItems().isEmpty()) {
            throw new InvalidOperationException("Cannot submit an empty order");
        }

        order.setStatus(OrderStatus.AWAITING_APPROVAL);
        order.setSubmittedDate(LocalDateTime.now());
        orderRepository.save(order);
        log.info("Order {} submitted", order.getOrderNumber());
        return toDetailResponse(order);
    }

    @Override
    @Transactional
    public ResponseDtos.OrderDetailResponse cancelOrder(Long orderId, String username) {
        Order order = getOrder(orderId);
        checkOwnership(order, username);

        if (order.getStatus() == OrderStatus.FULFILLED
                || order.getStatus() == OrderStatus.UNDER_DELIVERY
                || order.getStatus() == OrderStatus.CANCELED) {
            throw new InvalidOperationException("Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
        return toDetailResponse(order);
    }

    @Override
    public Page<ResponseDtos.OrderSummaryResponse> getClientOrders(String username, OrderStatus status, Pageable pageable) {
        User client = getUser(username);
        Page<Order> orders = status != null
                ? orderRepository.findAllByClientAndStatusOrderBySubmittedDateDesc(client, status, pageable)
                : orderRepository.findAllByClientOrderBySubmittedDateDesc(client, pageable);
        return orders.map(this::toSummaryResponse);
    }

    // ── WAREHOUSE MANAGER ──────────────────────────────────────────────────────

    @Override
    public Page<ResponseDtos.OrderSummaryResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> orders = status != null
                ? orderRepository.findAllByStatusOrderBySubmittedDateDesc(status, pageable)
                : orderRepository.findAllByOrderBySubmittedDateDesc(pageable);
        return orders.map(this::toSummaryResponse);
    }

    @Override
    public ResponseDtos.OrderDetailResponse getOrderDetail(Long orderId) {
        return toDetailResponse(getOrder(orderId));
    }

    @Override
    @Transactional
    public ResponseDtos.OrderDetailResponse approveOrder(Long orderId) {
        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.AWAITING_APPROVAL) {
            throw new InvalidOperationException("Order is not awaiting approval");
        }

        order.setStatus(OrderStatus.APPROVED);
        orderRepository.save(order);
        log.info("Order {} approved", order.getOrderNumber());
        return toDetailResponse(order);
    }

    @Override
    @Transactional
    public ResponseDtos.OrderDetailResponse declineOrder(Long orderId, OrderRequest.Decline request) {
        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.AWAITING_APPROVAL) {
            throw new InvalidOperationException("Order is not awaiting approval");
        }

        order.setStatus(OrderStatus.DECLINED);
        order.setDeclineReason(request.getReason());
        orderRepository.save(order);
        log.info("Order {} declined, reason: {}", order.getOrderNumber(), request.getReason());
        return toDetailResponse(order);
    }

    @Override
    @Transactional
    public ResponseDtos.OrderDetailResponse scheduleDelivery(Long orderId, OrderRequest.ScheduleDelivery request) {
        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new InvalidOperationException("Order must be APPROVED before scheduling delivery");
        }

        LocalDate date = request.getDeliveryDate();

        if (date.isBefore(LocalDate.now())) {
            throw new InvalidOperationException("Delivery date cannot be in the past");
        }
        if (isWeekend(date)) {
            throw new InvalidOperationException("Cannot schedule delivery on weekends");
        }

        List<Truck> availableOnDate = truckRepository.findAvailableTrucksOnDate(date);

        List<Truck> selectedTrucks = new ArrayList<>();
        for (Long truckId : request.getTruckIds()) {
            Truck truck = truckRepository.findById(truckId)
                    .orElseThrow(() -> new ResourceNotFoundException("Truck not found: " + truckId));
            if (availableOnDate.stream().noneMatch(t -> t.getId().equals(truckId))) {
                throw new InvalidOperationException("Truck " + truck.getLicensePlate() + " is not available on " + date);
            }
            selectedTrucks.add(truck);
        }

        double orderVolume = order.getItems().stream()
                .mapToDouble(i -> i.getInventoryItem().getPackageVolume() * i.getRequestedQuantity())
                .sum();

        double truckCapacity = selectedTrucks.stream().mapToDouble(Truck::getContainerVolume).sum();

        if (orderVolume > truckCapacity) {
            throw new InvalidOperationException(
                    String.format("Order volume %.2f exceeds truck capacity %.2f", orderVolume, truckCapacity));
        }

        // deduct stock
        for (OrderItem orderItem : order.getItems()) {
            InventoryItem invItem = orderItem.getInventoryItem();
            if (invItem.getQuantity() < orderItem.getRequestedQuantity()) {
                throw new InvalidOperationException("Not enough stock for: " + invItem.getItemName());
            }
            invItem.setQuantity(invItem.getQuantity() - orderItem.getRequestedQuantity());
            inventoryItemRepository.save(invItem);
        }

        Delivery delivery = Delivery.builder()
                .order(order)
                .deliveryDate(date)
                .trucks(selectedTrucks)
                .build();

        deliveryRepository.save(delivery);

        order.setDelivery(delivery);
        order.setStatus(OrderStatus.UNDER_DELIVERY);
        orderRepository.save(order);

        log.info("Delivery scheduled for order {} on {}", order.getOrderNumber(), date);
        return toDetailResponse(order);
    }

    @Override
    public List<LocalDate> getAvailableDeliveryDates(Long orderId, int periodDays) {
        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.APPROVED) {
            throw new InvalidOperationException("Order must be APPROVED to check delivery dates");
        }

        int days = Math.min(periodDays, maxPeriodDays);

        double orderVolume = order.getItems().stream()
                .mapToDouble(i -> i.getInventoryItem().getPackageVolume() * i.getRequestedQuantity())
                .sum();

        List<LocalDate> available = new ArrayList<>();
        LocalDate cursor = LocalDate.now().plusDays(1);

        for (int i = 0; i < days; i++, cursor = cursor.plusDays(1)) {
            if (isWeekend(cursor)) continue;

            double capacity = truckRepository.findAvailableTrucksOnDate(cursor)
                    .stream().mapToDouble(Truck::getContainerVolume).sum();

            if (capacity >= orderVolume) {
                available.add(cursor);
            }
        }

        return available;
    }

    // ── HELPERS ────────────────────────────────────────────────────────────────

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private void checkOwnership(Order order, String username) {
        if (!order.getClient().getUsername().equals(username)) {
            throw new InvalidOperationException("You don't have access to this order");
        }
    }

    private void checkEditable(Order order) {
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.DECLINED) {
            throw new InvalidOperationException("Order cannot be modified in status: " + order.getStatus());
        }
    }

    private Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    private ResponseDtos.OrderSummaryResponse toSummaryResponse(Order order) {
        return ResponseDtos.OrderSummaryResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .submittedDate(order.getSubmittedDate())
                .createdAt(order.getCreatedAt())
                .deadlineDate(order.getDeadlineDate())
                .totalItems(order.getItems().size())
                .build();
    }

    private ResponseDtos.OrderDetailResponse toDetailResponse(Order order) {
        List<ResponseDtos.OrderItemResponse> items = order.getItems().stream()
                .map(i -> ResponseDtos.OrderItemResponse.builder()
                        .id(i.getId())
                        .inventoryItemId(i.getInventoryItem().getId())
                        .itemName(i.getInventoryItem().getItemName())
                        .requestedQuantity(i.getRequestedQuantity())
                        .unitPrice(i.getInventoryItem().getUnitPrice())
                        .packageVolume(i.getInventoryItem().getPackageVolume())
                        .build())
                .collect(Collectors.toList());

        ResponseDtos.DeliveryResponse deliveryResponse = null;
        if (order.getDelivery() != null) {
            Delivery d = order.getDelivery();
            deliveryResponse = ResponseDtos.DeliveryResponse.builder()
                    .id(d.getId())
                    .deliveryDate(d.getDeliveryDate())
                    .trucks(d.getTrucks().stream()
                            .map(t -> ResponseDtos.TruckResponse.builder()
                                    .id(t.getId())
                                    .chassisNumber(t.getChassisNumber())
                                    .licensePlate(t.getLicensePlate())
                                    .containerVolume(t.getContainerVolume())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
        }

        return ResponseDtos.OrderDetailResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .clientUsername(order.getClient().getUsername())
                .status(order.getStatus())
                .submittedDate(order.getSubmittedDate())
                .createdAt(order.getCreatedAt())
                .deadlineDate(order.getDeadlineDate())
                .declineReason(order.getDeclineReason())
                .items(items)
                .delivery(deliveryResponse)
                .build();
    }
}
