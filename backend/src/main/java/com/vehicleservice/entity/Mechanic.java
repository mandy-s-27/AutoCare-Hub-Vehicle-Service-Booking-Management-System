package com.vehicleservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "mechanic")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mechanic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    private String specialization;

    private String phone;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @JsonIgnore
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_center_id")
    @JsonIgnoreProperties({"mechanics"})
    private ServiceCenter serviceCenter;
}
