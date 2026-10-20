package com.vehicleservice.service;

import com.vehicleservice.dto.ServiceCenterRequest;
import com.vehicleservice.dto.ServiceItemRequest;
import com.vehicleservice.entity.ServiceCenter;
import com.vehicleservice.entity.ServiceItem;
import com.vehicleservice.exception.ResourceNotFoundException;
import com.vehicleservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final ServiceCenterRepository serviceCenterRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final MechanicRepository mechanicRepository;

    // ---- Service Centers ----
    @Transactional
    public ServiceCenter createServiceCenter(ServiceCenterRequest req) {
        ServiceCenter sc = ServiceCenter.builder()
                .name(req.getName()).address(req.getAddress()).city(req.getCity()).build();
        return serviceCenterRepository.save(sc);
    }

    @Transactional
    public ServiceCenter updateServiceCenter(Long id, ServiceCenterRequest req) {
        ServiceCenter sc = serviceCenterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service center not found"));
        sc.setName(req.getName());
        sc.setAddress(req.getAddress());
        sc.setCity(req.getCity());
        return serviceCenterRepository.save(sc);
    }

    @Transactional
    public void deleteServiceCenter(Long id) {
        if (!serviceCenterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service center not found");
        }
        serviceCenterRepository.deleteById(id);
    }

    public List<ServiceCenter> getAllServiceCenters() {
        return serviceCenterRepository.findAll();
    }

    // ---- Services & Prices ----
    @Transactional
    public ServiceItem createService(ServiceItemRequest req) {
        ServiceItem item = ServiceItem.builder()
                .serviceName(req.getServiceName())
                .price(req.getPrice())
                .duration(req.getDuration())
                .build();
        return serviceItemRepository.save(item);
    }

    @Transactional
    public ServiceItem updateService(Long id, ServiceItemRequest req) {
        ServiceItem item = serviceItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        item.setServiceName(req.getServiceName());
        item.setPrice(req.getPrice());
        item.setDuration(req.getDuration());
        return serviceItemRepository.save(item);
    }

    @Transactional
    public void deleteService(Long id) {
        if (!serviceItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Service not found");
        }
        serviceItemRepository.deleteById(id);
    }

    public List<ServiceItem> getAllServices() {
        return serviceItemRepository.findAll();
    }

    // ---- Customers ----
    public List<com.vehicleservice.entity.Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer not found");
        }
        customerRepository.deleteById(id);
    }

    // ---- Reports ----
    public Map<String, Object> generateReports() {
        long totalCustomers = customerRepository.count();
        long totalMechanics = mechanicRepository.count();
        long totalServiceCenters = serviceCenterRepository.count();
        long totalBookings = bookingRepository.count();
        long pending = bookingRepository.countByStatus(com.vehicleservice.entity.BookingStatus.PENDING);
        long completed = bookingRepository.countByStatus(com.vehicleservice.entity.BookingStatus.COMPLETED);
        long delivered = bookingRepository.countByStatus(com.vehicleservice.entity.BookingStatus.DELIVERED);
        long cancelled = bookingRepository.countByStatus(com.vehicleservice.entity.BookingStatus.CANCELLED);

        return Map.of(
                "totalCustomers", totalCustomers,
                "totalMechanics", totalMechanics,
                "totalServiceCenters", totalServiceCenters,
                "totalBookings", totalBookings,
                "pendingBookings", pending,
                "completedBookings", completed,
                "deliveredBookings", delivered,
                "cancelledBookings", cancelled
        );
    }
}
