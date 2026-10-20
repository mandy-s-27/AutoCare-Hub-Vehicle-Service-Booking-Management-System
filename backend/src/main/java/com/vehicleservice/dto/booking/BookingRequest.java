
package com.vehicleservice.dto.booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingRequest {

    @NotNull
    private Long vehicleId;

    @NotNull
    private Long serviceCenterId;

    @NotNull
    private Long serviceId;

    @NotNull
    private LocalDate bookingDate;

    @NotNull
    private LocalTime bookingTime;

    // Customer's description of the vehicle problem
    @Size(max = 2000, message = "Complaint must not exceed 2000 characters")
    private String complaintDescription;
}
