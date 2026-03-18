package com.davidarbana.warehouse.service;

import com.davidarbana.warehouse.dto.request.OrderRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;
import com.davidarbana.warehouse.enums.OrderStatus;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    // CLIENT operations
    ResponseDtos.OrderDetailResponse createOrder(OrderRequest.Create request, String username);
    ResponseDtos.OrderDetailResponse addItemToOrder(Long orderId, OrderRequest.AddItem request, String username);
    ResponseDtos.OrderDetailResponse removeItemFromOrder(Long orderId, Long orderItemId, String username);
    ResponseDtos.OrderDetailResponse updateItemQuantity(Long orderId, Long orderItemId, OrderRequest.UpdateItem request, String username);
    ResponseDtos.OrderDetailResponse submitOrder(Long orderId, String username);
    ResponseDtos.OrderDetailResponse cancelOrder(Long orderId, String username);
    List<ResponseDtos.OrderSummaryResponse> getClientOrders(String username, OrderStatus status);

    // WAREHOUSE_MANAGER operations
    List<ResponseDtos.OrderSummaryResponse> getAllOrders(OrderStatus status);
    ResponseDtos.OrderDetailResponse getOrderDetail(Long orderId);
    ResponseDtos.OrderDetailResponse approveOrder(Long orderId);
    ResponseDtos.OrderDetailResponse declineOrder(Long orderId, OrderRequest.Decline request);
    ResponseDtos.OrderDetailResponse scheduleDelivery(Long orderId, OrderRequest.ScheduleDelivery request);
    List<LocalDate> getAvailableDeliveryDates(Long orderId, int periodDays);
}
