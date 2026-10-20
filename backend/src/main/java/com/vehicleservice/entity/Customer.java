package com.vehicleservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // 🔥 IMPORTANT
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    private String password;

    private String phone;

    // 🔥 IMPORTANT: prevent loop
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @Builder.Default
    @JsonIgnore
    private List<Vehicle> vehicles = new ArrayList<>();
}