package com.davidarbana.warehouse.repository;

import com.davidarbana.warehouse.entity.Order;
import com.davidarbana.warehouse.entity.User;
import com.davidarbana.warehouse.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByClientOrderBySubmittedDateDesc(User client);
    List<Order> findAllByClientAndStatusOrderBySubmittedDateDesc(User client, OrderStatus status);
    List<Order> findAllByOrderBySubmittedDateDesc();
    List<Order> findAllByStatusOrderBySubmittedDateDesc(OrderStatus status);
    Optional<Order> findByOrderNumber(String orderNumber);
}
