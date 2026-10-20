package com.vehicleservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MechanicCreateRequest {
    @NotBlank
    private String name;
    private String specialization;
    private String phone;
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;
    private Long serviceCenterId;
}
