package com.davidarbana.warehouse.controller;

import com.davidarbana.warehouse.dto.request.OrderRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;
import com.davidarbana.warehouse.enums.OrderStatus;
import com.davidarbana.warehouse.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders")
public class OrderController {

    private final OrderService orderService;

    // ── CLIENT ─────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ResponseDtos.OrderDetailResponse> createOrder(
            @RequestBody OrderRequest.Create request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(request, principal.getName()));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Page<ResponseDtos.OrderSummaryResponse>> getMyOrders(
            @RequestParam(required = false) OrderStatus status,
            Principal principal,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getClientOrders(principal.getName(), status, pageable));
    }

    @PostMapping("/{orderId}/items")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ResponseDtos.OrderDetailResponse> addItem(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderRequest.AddItem request,
            Principal principal) {
        return ResponseEntity.ok(orderService.addItemToOrder(orderId, request, principal.getName()));
    }

    @DeleteMapping("/{orderId}/items/{orderItemId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ResponseDtos.OrderDetailResponse> removeItem(
            @PathVariable Long orderId, @PathVariable Long orderItemId, Principal principal) {
        return ResponseEntity.ok(orderService.removeItemFromOrder(orderId, orderItemId, principal.getName()));
    }

    @PatchMapping("/{orderId}/items/{orderItemId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ResponseDtos.OrderDetailResponse> updateItemQty(
            @PathVariable Long orderId,
            @PathVariable Long orderItemId,
            @Valid @RequestBody OrderRequest.UpdateItem request,
            Principal principal) {
        return ResponseEntity.ok(orderService.updateItemQuantity(orderId, orderItemId, request, principal.getName()));
    }

    @PostMapping("/{orderId}/submit")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ResponseDtos.OrderDetailResponse> submit(
            @PathVariable Long orderId, Principal principal) {
        return ResponseEntity.ok(orderService.submitOrder(orderId, principal.getName()));
    }

    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ResponseDtos.OrderDetailResponse> cancel(
            @PathVariable Long orderId, Principal principal) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, principal.getName()));
    }

    // ── WAREHOUSE MANAGER ──────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<Page<ResponseDtos.OrderSummaryResponse>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            Pageable pageable
            ) {
        return ResponseEntity.ok(orderService.getAllOrders(status, pageable));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ResponseDtos.OrderDetailResponse> getDetail(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }

    @PostMapping("/{orderId}/approve")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ResponseDtos.OrderDetailResponse> approve(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.approveOrder(orderId));
    }

    @PostMapping("/{orderId}/decline")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ResponseDtos.OrderDetailResponse> decline(
            @PathVariable Long orderId, @RequestBody OrderRequest.Decline request) {
        return ResponseEntity.ok(orderService.declineOrder(orderId, request));
    }

    @Operation(summary = "Schedule delivery - selects date and trucks, deducts inventory")
    @PostMapping("/{orderId}/schedule-delivery")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<ResponseDtos.OrderDetailResponse> scheduleDelivery(
            @PathVariable Long orderId, @Valid @RequestBody OrderRequest.ScheduleDelivery request) {
        return ResponseEntity.ok(orderService.scheduleDelivery(orderId, request));
    }

    @GetMapping("/{orderId}/available-dates")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    public ResponseEntity<List<LocalDate>> getAvailableDates(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "30") int periodDays) {
        return ResponseEntity.ok(orderService.getAvailableDeliveryDates(orderId, periodDays));
    }
}
