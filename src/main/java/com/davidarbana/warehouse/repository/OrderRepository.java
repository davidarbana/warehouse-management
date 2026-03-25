package com.davidarbana.warehouse.repository;

import com.davidarbana.warehouse.entity.Order;
import com.davidarbana.warehouse.entity.User;
import com.davidarbana.warehouse.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByClientOrderBySubmittedDateDesc(User client, Pageable pageable);
    Page<Order> findAllByClientAndStatusOrderBySubmittedDateDesc(User client, OrderStatus status, Pageable pageable);
    Page<Order> findAllByOrderBySubmittedDateDesc(Pageable pageable);
    Page<Order> findAllByStatusOrderBySubmittedDateDesc(OrderStatus status, Pageable pageable);
    Optional<Order> findByOrderNumber(String orderNumber);
}
