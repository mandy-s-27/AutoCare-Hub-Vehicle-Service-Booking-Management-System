package com.vehicleservice.controller;

import com.vehicleservice.dto.ReviewRequest;
import com.vehicleservice.dto.VehicleRequest;
import com.vehicleservice.dto.booking.BookingRequest;
import com.vehicleservice.entity.Booking;
import com.vehicleservice.entity.Invoice;
import com.vehicleservice.entity.Review;
import com.vehicleservice.entity.Vehicle;
import com.vehicleservice.service.BookingService;
import com.vehicleservice.service.InvoiceService;
import com.vehicleservice.service.ReviewService;
import com.vehicleservice.service.VehicleService;
import com.vehicleservice.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final VehicleService vehicleService;
    private final BookingService bookingService;
    private final ReviewService reviewService;
    private final InvoiceService invoiceService;

    // ---- Vehicles ----
    @PostMapping("/vehicle")
    public ResponseEntity<Vehicle> addVehicle(@Valid @RequestBody VehicleRequest req) {
        return ResponseEntity.ok(vehicleService.addVehicle(SecurityUtils.currentUserId(), req));
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> myVehicles() {
        return ResponseEntity.ok(vehicleService.getVehiclesForCustomer(SecurityUtils.currentUserId()));
    }

    // ---- Bookings ----
    @PostMapping("/booking")
    public ResponseEntity<Booking> book(@Valid @RequestBody BookingRequest req) {
        return ResponseEntity.ok(bookingService.createBooking(SecurityUtils.currentUserId(), req));
    }

    @GetMapping("/booking/{id}")
    public ResponseEntity<Booking> getBooking(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingForCustomer(id, SecurityUtils.currentUserId()));
    }

    @GetMapping("/booking/history")
    public ResponseEntity<Page<Booking>> history(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(bookingService.getHistoryForCustomer(SecurityUtils.currentUserId(), pageable));
    }

    @DeleteMapping("/booking/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        bookingService.cancelBooking(id, SecurityUtils.currentUserId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/booking/{id}/invoice")
    public ResponseEntity<Invoice> invoice(@PathVariable Long id) {
        // Confirms ownership first, then returns the invoice
        bookingService.getBookingForCustomer(id, SecurityUtils.currentUserId());
        return ResponseEntity.ok(invoiceService.getByBookingId(id));
    }

    // ---- Reviews ----
    @PostMapping("/review")
    public ResponseEntity<Review> review(@Valid @RequestBody ReviewRequest req) {
        return ResponseEntity.ok(reviewService.addReview(SecurityUtils.currentUserId(), req));
    }
}
