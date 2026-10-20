package com.vehicleservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ServiceItemRequest {
    @NotBlank
    private String serviceName;
    @NotNull
    private Double price;
    private Integer duration;
}
