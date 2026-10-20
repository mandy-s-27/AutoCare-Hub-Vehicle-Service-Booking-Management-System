package com.vehicleservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "repair_note")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepairNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnoreProperties({"repairNotes", "customer", "vehicle"})
    private Booking booking;

    @Column(length = 2000)
    private String note;

    private String imageUrl;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
