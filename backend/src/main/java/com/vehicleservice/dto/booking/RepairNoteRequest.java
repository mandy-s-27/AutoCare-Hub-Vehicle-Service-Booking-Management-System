package com.vehicleservice.dto.booking;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepairNoteRequest {
    @NotNull
    private Long bookingId;

    private String note;

    private String imageUrl;
}
