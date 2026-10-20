package com.vehicleservice.service;

import com.vehicleservice.dto.MechanicCreateRequest;
import com.vehicleservice.entity.Mechanic;
import com.vehicleservice.entity.ServiceCenter;
import com.vehicleservice.exception.BadRequestException;
import com.vehicleservice.exception.ResourceNotFoundException;
import com.vehicleservice.repository.MechanicRepository;
import com.vehicleservice.repository.ServiceCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Admin-side management of Mechanics (creation, deletion, listing). */
@Service
@RequiredArgsConstructor
public class MechanicAdminService {

    private final MechanicRepository mechanicRepository;
    private final ServiceCenterRepository serviceCenterRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Mechanic create(MechanicCreateRequest req) {
        if (mechanicRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered for a mechanic");
        }
        ServiceCenter center = null;
        if (req.getServiceCenterId() != null) {
            center = serviceCenterRepository.findById(req.getServiceCenterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Service center not found"));
        }
        Mechanic mechanic = Mechanic.builder()
                .name(req.getName())
                .specialization(req.getSpecialization())
                .phone(req.getPhone())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .serviceCenter(center)
                .build();
        return mechanicRepository.save(mechanic);
    }

    public List<Mechanic> getAll() {
        return mechanicRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        if (!mechanicRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mechanic not found");
        }
        mechanicRepository.deleteById(id);
    }
}
