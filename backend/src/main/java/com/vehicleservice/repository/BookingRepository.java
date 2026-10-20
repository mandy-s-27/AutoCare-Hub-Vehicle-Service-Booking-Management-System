
package com.vehicleservice.repository;

import com.vehicleservice.entity.Booking;
import com.vehicleservice.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerId(Long customerId);

    Page<Booking> findByCustomerId(Long customerId, Pageable pageable);

    List<Booking> findByMechanicId(Long mechanicId);

    List<Booking> findByMechanicIdAndStatus(
            Long mechanicId,
            BookingStatus status
    );

    Page<Booking> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Booking> findByServiceCenterId(Long serviceCenterId);

    long countByStatus(BookingStatus status);

    // Existing method preserved
    boolean existsByMechanicIdAndStatusIn(
            Long mechanicId,
            List<BookingStatus> statuses
    );

    // Check whether a mechanic already occupies the exact booking slot
    boolean existsByMechanicIdAndBookingDateAndBookingTimeAndStatusIn(
            Long mechanicId,
            LocalDate bookingDate,
            LocalTime bookingTime,
            List<BookingStatus> statuses
    );
    List<Booking> findByStatusOrderByBookingDateAscBookingTimeAsc(
            BookingStatus status
    );
}
