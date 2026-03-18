package com.davidarbana.warehouse.repository;

import com.davidarbana.warehouse.entity.Delivery;
import com.davidarbana.warehouse.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    @Query("SELECT d FROM Delivery d WHERE d.deliveryDate <= :today AND d.order.status = 'UNDER_DELIVERY'")
    List<Delivery> findDeliveriesDueByDate(@Param("today") LocalDate today);
}
