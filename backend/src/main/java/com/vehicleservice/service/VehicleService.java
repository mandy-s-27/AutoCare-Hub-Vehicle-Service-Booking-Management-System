package com.vehicleservice.service;

import com.vehicleservice.dto.VehicleRequest;
import com.vehicleservice.entity.Customer;
import com.vehicleservice.entity.Vehicle;
import com.vehicleservice.exception.BadRequestException;
import com.vehicleservice.exception.ResourceNotFoundException;
import com.vehicleservice.repository.CustomerRepository;
import com.vehicleservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public Vehicle addVehicle(Long customerId, VehicleRequest req) {
        if (vehicleRepository.existsByVehicleNumber(req.getVehicleNumber())) {
            throw new BadRequestException("Vehicle with this number is already registered");
        }
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Vehicle vehicle = Vehicle.builder()
                .customer(customer)
                .vehicleNumber(req.getVehicleNumber())
                .model(req.getModel())
                .company(req.getCompany())
                .fuelType(req.getFuelType())
                .build();
        return vehicleRepository.save(vehicle);
    }

    public List<Vehicle> getVehiclesForCustomer(Long customerId) {
        return vehicleRepository.findByCustomerId(customerId);
    }

    public Vehicle getOwnedVehicle(Long vehicleId, Long customerId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        if (!vehicle.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("This vehicle does not belong to you");
        }
        return vehicle;
    }
}
