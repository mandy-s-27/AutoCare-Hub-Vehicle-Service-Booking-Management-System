package com.vehicleservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "vehicle")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 🔥 IMPORTANT
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 Prevent circular JSON
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"vehicles", "password"})
    private Customer customer;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String vehicleNumber;

    @NotBlank
    private String model;

    @NotBlank
    private String company;

    private String fuelType;
}