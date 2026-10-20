package com.vehicleservice.repository;

import com.vehicleservice.entity.RepairNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepairNoteRepository extends JpaRepository<RepairNote, Long> {
    List<RepairNote> findByBookingIdOrderByCreatedAtAsc(Long bookingId);
}
