package com.davidarbana.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private LocalDate deliveryDate;

    @ManyToMany
    @JoinTable(
            name = "delivery_truck",
            joinColumns = @JoinColumn(name = "delivery_id"),
            inverseJoinColumns = @JoinColumn(name = "truck_id")
    )
    @Builder.Default
    private List<Truck> trucks = new ArrayList<>();
}
