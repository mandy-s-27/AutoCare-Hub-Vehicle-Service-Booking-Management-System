package com.vehicleservice.controller;

import com.vehicleservice.repository.ServiceCenterRepository;
import com.vehicleservice.repository.ServiceItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Read-only endpoints anyone can view before logging in (browse centers/services/prices). */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final ServiceCenterRepository serviceCenterRepository;
    private final ServiceItemRepository serviceItemRepository;

    @GetMapping("/service-centers")
    public List<?> serviceCenters() {
        return serviceCenterRepository.findAll();
    }

    @GetMapping("/services")
    public List<?> services() {
        return serviceItemRepository.findAll();
    }
}
