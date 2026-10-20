package com.vehicleservice.repository;

import com.vehicleservice.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
}
