package com.vehicleservice.dto.booking;

import com.vehicleservice.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusUpdateRequest {
    @NotNull
    private Long bookingId;

    @NotNull
    private BookingStatus status;
}
