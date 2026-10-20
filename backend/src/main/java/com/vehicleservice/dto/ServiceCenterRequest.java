package com.vehicleservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ServiceCenterRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String address;
    @NotBlank
    private String city;
}
