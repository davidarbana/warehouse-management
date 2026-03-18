package com.davidarbana.warehouse.repository;

import com.davidarbana.warehouse.entity.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TruckRepository extends JpaRepository<Truck, Long> {
    boolean existsByChassisNumber(String chassisNumber);
    boolean existsByLicensePlate(String licensePlate);

    @Query("SELECT t FROM Truck t WHERE t.id NOT IN (" +
           "SELECT tr.id FROM Delivery d JOIN d.trucks tr WHERE d.deliveryDate = :date)")
    List<Truck> findAvailableTrucksOnDate(@Param("date") LocalDate date);
}
