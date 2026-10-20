package com.vehicleservice.repository;

import com.vehicleservice.entity.ServiceCenter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceCenterRepository extends JpaRepository<ServiceCenter, Long> {
}
