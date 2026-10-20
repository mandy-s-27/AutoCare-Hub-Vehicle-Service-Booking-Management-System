package com.vehicleservice.controller;

import com.vehicleservice.dto.*;
import com.vehicleservice.entity.*;
import com.vehicleservice.service.AdminService;
import com.vehicleservice.service.BookingService;
import com.vehicleservice.service.MechanicAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final MechanicAdminService mechanicAdminService;
    private final BookingService bookingService;

    // ---- Customers ----
    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> customers() {
        return ResponseEntity.ok(adminService.getAllCustomers());
    }

    @DeleteMapping("/customer/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        adminService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Mechanics ----
    @PostMapping("/mechanic")
    public ResponseEntity<Mechanic> createMechanic(@Valid @RequestBody MechanicCreateRequest req) {
        return ResponseEntity.ok(mechanicAdminService.create(req));
    }

    @GetMapping("/mechanics")
    public ResponseEntity<List<Mechanic>> mechanics() {
        return ResponseEntity.ok(mechanicAdminService.getAll());
    }

    @DeleteMapping("/mechanic/{id}")
    public ResponseEntity<Void> deleteMechanic(@PathVariable Long id) {
        mechanicAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/booking/assign-mechanic")
    public ResponseEntity<Booking> assignMechanic(@Valid @RequestBody AssignMechanicRequest req) {
        return ResponseEntity.ok(bookingService.assignMechanic(req));
    }

    // ---- Service Centers ----
    @PostMapping("/service-center")
    public ResponseEntity<ServiceCenter> createServiceCenter(@Valid @RequestBody ServiceCenterRequest req) {
        return ResponseEntity.ok(adminService.createServiceCenter(req));
    }

    @PutMapping("/service-center/{id}")
    public ResponseEntity<ServiceCenter> updateServiceCenter(@PathVariable Long id, @Valid @RequestBody ServiceCenterRequest req) {
        return ResponseEntity.ok(adminService.updateServiceCenter(id, req));
    }

    @DeleteMapping("/service-center/{id}")
    public ResponseEntity<Void> deleteServiceCenter(@PathVariable Long id) {
        adminService.deleteServiceCenter(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/service-centers")
    public ResponseEntity<List<ServiceCenter>> serviceCenters() {
        return ResponseEntity.ok(adminService.getAllServiceCenters());
    }

    // ---- Services & Prices ----
    @PostMapping("/service")
    public ResponseEntity<ServiceItem> createService(@Valid @RequestBody ServiceItemRequest req) {
        return ResponseEntity.ok(adminService.createService(req));
    }

    @PutMapping("/service/{id}")
    public ResponseEntity<ServiceItem> updateService(@PathVariable Long id, @Valid @RequestBody ServiceItemRequest req) {
        return ResponseEntity.ok(adminService.updateService(id, req));
    }

    @DeleteMapping("/service/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable Long id) {
        adminService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceItem>> services() {
        return ResponseEntity.ok(adminService.getAllServices());
    }

    // ---- Bookings & Reports ----
    @GetMapping("/all-bookings")
    public ResponseEntity<Page<Booking>> allBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(bookingService.getAllBookings(pageable));
    }

    @GetMapping("/reports")
    public ResponseEntity<Map<String, Object>> reports() {
        return ResponseEntity.ok(adminService.generateReports());
    }
}
