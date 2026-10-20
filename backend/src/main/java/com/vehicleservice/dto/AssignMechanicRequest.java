package com.vehicleservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignMechanicRequest {
    @NotNull
    private Long bookingId;
    @NotNull
    private Long mechanicId;
}
