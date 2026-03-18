package com.davidarbana.warehouse.service;

import com.davidarbana.warehouse.dto.request.OrderRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;
import com.davidarbana.warehouse.entity.*;
import com.davidarbana.warehouse.enums.OrderStatus;
import com.davidarbana.warehouse.enums.Role;
import com.davidarbana.warehouse.exception.InvalidOperationException;
import com.davidarbana.warehouse.exception.ResourceNotFoundException;
import com.davidarbana.warehouse.repository.*;
import com.davidarbana.warehouse.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private TruckRepository truckRepository;
    @Mock private DeliveryRepository deliveryRepository;

    private User clientUser;
    private Order order;
    private InventoryItem inventoryItem;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "maxPeriodDays", 30);

        clientUser = User.builder()
                .id(1L)
                .username("client")
                .fullName("Jane Client")
                .email("client@warehouse.com")
                .role(Role.CLIENT)
                .enabled(true)
                .build();

        inventoryItem = InventoryItem.builder()
                .id(1L)
                .itemName("Industrial Drill")
                .quantity(50)
                .unitPrice(299.99)
                .packageVolume(2.5)
                .build();

        order = Order.builder()
                .id(1L)
                .orderNumber("ORD-TEST0001")
                .client(clientUser)
                .status(OrderStatus.CREATED)
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void createOrder_ShouldReturnNewOrder() {
        when(userRepository.findByUsername("client")).thenReturn(Optional.of(clientUser));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        ResponseDtos.OrderDetailResponse response = orderService.createOrder(
                new OrderRequest.Create(LocalDate.now().plusDays(10)), "client");

        assertNotNull(response);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_UserNotFound_ShouldThrow() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                orderService.createOrder(new OrderRequest.Create(null), "unknown"));
    }

    @Test
    void submitOrder_WithItems_ShouldChangeStatusToAwaitingApproval() {
        OrderItem item = OrderItem.builder()
                .id(1L).order(order).inventoryItem(inventoryItem).requestedQuantity(2).build();
        order.getItems().add(item);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        ResponseDtos.OrderDetailResponse response = orderService.submitOrder(1L, "client");

        assertEquals(OrderStatus.AWAITING_APPROVAL, order.getStatus());
        assertNotNull(order.getSubmittedDate());
    }

    @Test
    void submitOrder_EmptyItems_ShouldThrow() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOperationException.class, () ->
                orderService.submitOrder(1L, "client"));
    }

    @Test
    void submitOrder_WrongStatus_ShouldThrow() {
        order.setStatus(OrderStatus.APPROVED);
        order.getItems().add(OrderItem.builder().id(1L).order(order)
                .inventoryItem(inventoryItem).requestedQuantity(1).build());

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOperationException.class, () ->
                orderService.submitOrder(1L, "client"));
    }

    @Test
    void cancelOrder_ShouldSetStatusToCanceled() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.cancelOrder(1L, "client");

        assertEquals(OrderStatus.CANCELED, order.getStatus());
    }

    @Test
    void cancelOrder_FulfilledOrder_ShouldThrow() {
        order.setStatus(OrderStatus.FULFILLED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOperationException.class, () ->
                orderService.cancelOrder(1L, "client"));
    }

    @Test
    void cancelOrder_UnderDelivery_ShouldThrow() {
        order.setStatus(OrderStatus.UNDER_DELIVERY);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOperationException.class, () ->
                orderService.cancelOrder(1L, "client"));
    }

    @Test
    void approveOrder_ShouldSetStatusToApproved() {
        order.setStatus(OrderStatus.AWAITING_APPROVAL);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.approveOrder(1L);

        assertEquals(OrderStatus.APPROVED, order.getStatus());
    }

    @Test
    void approveOrder_WrongStatus_ShouldThrow() {
        order.setStatus(OrderStatus.CREATED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOperationException.class, () ->
                orderService.approveOrder(1L));
    }

    @Test
    void declineOrder_ShouldSetStatusAndReason() {
        order.setStatus(OrderStatus.AWAITING_APPROVAL);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.declineOrder(1L, new OrderRequest.Decline("Insufficient stock"));

        assertEquals(OrderStatus.DECLINED, order.getStatus());
        assertEquals("Insufficient stock", order.getDeclineReason());
    }

    @Test
    void addItemToOrder_ShouldAddItemSuccessfully() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(inventoryItem));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        ResponseDtos.OrderDetailResponse response = orderService.addItemToOrder(
                1L, new OrderRequest.AddItem(1L, 3), "client");

        assertEquals(1, order.getItems().size());
        assertEquals(3, order.getItems().get(0).getRequestedQuantity());
    }

    @Test
    void addItemToOrder_WrongStatus_ShouldThrow() {
        order.setStatus(OrderStatus.AWAITING_APPROVAL);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOperationException.class, () ->
                orderService.addItemToOrder(1L, new OrderRequest.AddItem(1L, 2), "client"));
    }

    @Test
    void scheduleDelivery_OnWeekend_ShouldThrow() {
        order.setStatus(OrderStatus.APPROVED);
        // Find next Saturday
        LocalDate saturday = LocalDate.now();
        while (saturday.getDayOfWeek().getValue() != 6) saturday = saturday.plusDays(1);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        LocalDate finalSaturday = saturday;
        assertThrows(InvalidOperationException.class, () ->
                orderService.scheduleDelivery(1L, new OrderRequest.ScheduleDelivery(finalSaturday, List.of(1L))));
    }

    @Test
    void getAvailableDeliveryDates_ShouldSkipWeekends() {
        order.setStatus(OrderStatus.APPROVED);
        order.getItems().add(OrderItem.builder()
                .id(1L).order(order).inventoryItem(inventoryItem).requestedQuantity(1).build());

        Truck truck = Truck.builder().id(1L).chassisNumber("CH-001").licensePlate("AA-000-BB").containerVolume(100.0).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(truckRepository.findAvailableTrucksOnDate(any(LocalDate.class))).thenReturn(List.of(truck));

        List<LocalDate> dates = orderService.getAvailableDeliveryDates(1L, 7);

        assertTrue(dates.stream().noneMatch(d ->
                d.getDayOfWeek().getValue() == 6 || d.getDayOfWeek().getValue() == 7));
    }
}
