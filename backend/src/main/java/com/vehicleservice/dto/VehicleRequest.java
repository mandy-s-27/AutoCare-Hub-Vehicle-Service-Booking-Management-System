package com.vehicleservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VehicleRequest {
    @NotBlank
    private String vehicleNumber;
    @NotBlank
    private String model;
    @NotBlank
    private String company;
    private String fuelType;
}
