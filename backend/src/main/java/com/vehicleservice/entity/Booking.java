
package com.vehicleservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonIgnoreProperties({"vehicles", "password"})
    private Customer customer;

    // Vehicle
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    @JsonIgnoreProperties({"customer"})
    private Vehicle vehicle;

    // Service Center
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_center_id", nullable = false)
    @JsonIgnoreProperties({"bookings"})
    private ServiceCenter serviceCenter;

    // Service Item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    @JsonIgnoreProperties({"bookings"})
    private ServiceItem service;

    // Assigned Mechanic
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mechanic_id")
    @JsonIgnoreProperties({"password", "serviceCenter"})
    private Mechanic mechanic;

    // Booking date and time
    @Column(nullable = false)
    private LocalDate bookingDate;

    @Column(nullable = false)
    private LocalTime bookingTime;

    // Customer's description of the vehicle problem
    @Column(length = 2000)
    private String complaintDescription;

    @Column(length = 50)
    private String complaintCategory;
    // Booking status
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    // Booking creation timestamp
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Repair Notes
    @OneToMany(
            mappedBy = "booking",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    @JsonIgnoreProperties({"booking"})
    private List<RepairNote> repairNotes = new ArrayList<>();
}
