package com.davidarbana.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "truck")
public class Truck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String chassisNumber;

    @Column(unique = true, nullable = false)
    private String licensePlate;

    private double containerVolume;
}
