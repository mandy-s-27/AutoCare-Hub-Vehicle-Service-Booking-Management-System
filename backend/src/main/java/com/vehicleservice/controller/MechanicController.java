package com.vehicleservice.controller;

import com.vehicleservice.dto.booking.RepairNoteRequest;
import com.vehicleservice.dto.booking.StatusUpdateRequest;
import com.vehicleservice.entity.Booking;
import com.vehicleservice.entity.RepairNote;
import com.vehicleservice.service.BookingService;
import com.vehicleservice.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mechanic")
@RequiredArgsConstructor
public class MechanicController {

    private final BookingService bookingService;

    @GetMapping("/assigned-bookings")
    public ResponseEntity<List<Booking>> assignedBookings() {
        return ResponseEntity.ok(bookingService.getAssignedBookings(SecurityUtils.currentUserId()));
    }

    @PutMapping("/booking/status")
    public ResponseEntity<Booking> updateStatus(@Valid @RequestBody StatusUpdateRequest req) {
        return ResponseEntity.ok(bookingService.updateStatus(req, SecurityUtils.currentUserId()));
    }

    @PostMapping("/repair-note")
    public ResponseEntity<RepairNote> addRepairNote(@Valid @RequestBody RepairNoteRequest req) {
        return ResponseEntity.ok(bookingService.addRepairNote(req, SecurityUtils.currentUserId()));
    }

    @GetMapping("/completed-services")
    public ResponseEntity<List<Booking>> completedServices() {
        return ResponseEntity.ok(bookingService.getCompletedForMechanic(SecurityUtils.currentUserId()));
    }
}
