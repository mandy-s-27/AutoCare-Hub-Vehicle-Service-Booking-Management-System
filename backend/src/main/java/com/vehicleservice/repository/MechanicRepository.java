package com.vehicleservice.repository;

import com.vehicleservice.entity.Mechanic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MechanicRepository extends JpaRepository<Mechanic, Long> {
    Optional<Mechanic> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Mechanic> findByServiceCenterId(Long serviceCenterId);
}
