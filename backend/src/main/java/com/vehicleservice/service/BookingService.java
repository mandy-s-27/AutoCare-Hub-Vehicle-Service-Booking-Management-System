
package com.vehicleservice.service;

import com.vehicleservice.dto.AssignMechanicRequest;
import com.vehicleservice.dto.booking.BookingRequest;
import com.vehicleservice.dto.booking.RepairNoteRequest;
import com.vehicleservice.dto.booking.StatusUpdateRequest;
import com.vehicleservice.entity.*;
import com.vehicleservice.exception.BadRequestException;
import com.vehicleservice.exception.ResourceNotFoundException;
import com.vehicleservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceCenterRepository serviceCenterRepository;
    private final ServiceItemRepository serviceItemRepository;
    private final MechanicRepository mechanicRepository;
    private final CustomerRepository customerRepository;
    private final RepairNoteRepository repairNoteRepository;
    private final InvoiceService invoiceService;
    private final ComplaintAnalysisService complaintAnalysisService;

    // Booking statuses that occupy the requested appointment slot
    private static final List<BookingStatus> ACTIVE_STATUSES = List.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED,
            BookingStatus.RECEIVED,
            BookingStatus.INSPECTION_COMPLETED,
            BookingStatus.REPAIR_STARTED,
            BookingStatus.WAITING_FOR_SPARE_PARTS,
            BookingStatus.IN_PROGRESS
    );

    @Transactional
    public Booking createBooking(Long customerId, BookingRequest req) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found"));

        Vehicle vehicle = vehicleRepository.findById(req.getVehicleId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Vehicle not found"));

        if (!vehicle.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException(
                    "This vehicle does not belong to you");
        }

        ServiceCenter serviceCenter =
                serviceCenterRepository.findById(req.getServiceCenterId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Service center not found"));

        ServiceItem service =
                serviceItemRepository.findById(req.getServiceId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Service not found"));

        String complaint = req.getComplaintDescription();
        String category = complaintAnalysisService.analyze(complaint);

        // Find a suitable mechanic for this complaint and time slot
        Mechanic assignedMechanic = findAvailableMechanic(
                serviceCenter.getId(),
                category,
                req.getBookingDate(),
                req.getBookingTime()
        );

        // Confirm automatically only when a mechanic is available
        BookingStatus initialStatus = assignedMechanic != null
                ? BookingStatus.CONFIRMED
                : BookingStatus.PENDING;

        Booking booking = Booking.builder()
                .customer(customer)
                .vehicle(vehicle)
                .serviceCenter(serviceCenter)
                .service(service)
                .mechanic(assignedMechanic)
                .bookingDate(req.getBookingDate())
                .bookingTime(req.getBookingTime())
                .complaintDescription(complaint)
                .complaintCategory(category)
                .status(initialStatus)
                .build();

        booking = bookingRepository.save(booking);

        invoiceService.generateInvoiceForBooking(booking);

        return booking;
    }

    // Find a mechanic with the matching specialization and a free slot
    private Mechanic findAvailableMechanic(
            Long serviceCenterId,
            String category,
            java.time.LocalDate bookingDate,
            java.time.LocalTime bookingTime) {

        if (category == null
                || category.isBlank()
                || category.equalsIgnoreCase("OTHER")) {
            return null;
        }

        List<Mechanic> mechanics =
                mechanicRepository.findByServiceCenterId(serviceCenterId);

        for (Mechanic mechanic : mechanics) {

            String specialization = mechanic.getSpecialization();

            if (specialization == null
                    || !specialization.trim().equalsIgnoreCase(category.trim())) {
                continue;
            }

            boolean slotOccupied =
                    bookingRepository
                            .existsByMechanicIdAndBookingDateAndBookingTimeAndStatusIn(
                                    mechanic.getId(),
                                    bookingDate,
                                    bookingTime,
                                    ACTIVE_STATUSES
                            );

            if (!slotOccupied) {
                return mechanic;
            }
        }

        return null;
    }

    public List<Booking> getHistoryForCustomer(Long customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    public Page<Booking> getHistoryForCustomer(
            Long customerId, Pageable pageable) {
        return bookingRepository.findByCustomerId(customerId, pageable);
    }

    public Booking getBookingForCustomer(Long bookingId, Long customerId) {
        Booking booking = getById(bookingId);

        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException(
                    "This booking does not belong to you");
        }

        return booking;
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long customerId) {
        Booking booking = getBookingForCustomer(bookingId, customerId);

        if (booking.getStatus() == BookingStatus.DELIVERED
                || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BadRequestException(
                    "Cannot cancel a completed booking");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    public Booking getById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found"));
    }

    public List<Booking> getAssignedBookings(Long mechanicId) {
        return bookingRepository.findByMechanicId(mechanicId);
    }

    public List<Booking> getCompletedForMechanic(Long mechanicId) {
        return bookingRepository.findByMechanicIdAndStatus(
                mechanicId, BookingStatus.DELIVERED);
    }

    @Transactional
    public Booking updateStatus(StatusUpdateRequest req, Long mechanicId) {
        Booking booking = getById(req.getBookingId());

        if (mechanicId != null
                && (booking.getMechanic() == null
                || !booking.getMechanic().getId().equals(mechanicId))) {
            throw new BadRequestException(
                    "This booking is not assigned to you");
        }

        booking.setStatus(req.getStatus());
        booking = bookingRepository.save(booking);

        if (req.getStatus() == BookingStatus.REPAIR_COMPLETED
                && booking.getMechanic() != null) {
            assignNextPendingBooking(booking.getMechanic());
        }

        return booking;
    }

    private void assignNextPendingBooking(Mechanic mechanic) {
        List<Booking> pendingBookings =
                bookingRepository.findByStatusOrderByBookingDateAscBookingTimeAsc(
                        BookingStatus.PENDING);

        for (Booking pending : pendingBookings) {

            if (pending.getServiceCenter() == null
                    || mechanic.getServiceCenter() == null
                    || !pending.getServiceCenter().getId()
                    .equals(mechanic.getServiceCenter().getId())) {
                continue;
            }

            String specialization = mechanic.getSpecialization();
            String category = pending.getComplaintCategory();

            if (specialization == null || category == null
                    || category.equalsIgnoreCase("OTHER")
                    || !specialization.trim().equalsIgnoreCase(category.trim())) {
                continue;
            }

            boolean slotOccupied =
                    bookingRepository
                            .existsByMechanicIdAndBookingDateAndBookingTimeAndStatusIn(
                                    mechanic.getId(),
                                    pending.getBookingDate(),
                                    pending.getBookingTime(),
                                    ACTIVE_STATUSES);

            if (!slotOccupied) {
                pending.setMechanic(mechanic);
                pending.setStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(pending);
                return;
            }
        }
    }

    @Transactional
    public RepairNote addRepairNote(
            RepairNoteRequest req, Long mechanicId) {

        Booking booking = getById(req.getBookingId());

        if (booking.getMechanic() == null
                || !booking.getMechanic().getId().equals(mechanicId)) {
            throw new BadRequestException(
                    "This booking is not assigned to you");
        }

        RepairNote note = RepairNote.builder()
                .booking(booking)
                .note(req.getNote())
                .imageUrl(req.getImageUrl())
                .build();

        return repairNoteRepository.save(note);
    }

    @Transactional
    public Booking assignMechanic(AssignMechanicRequest req) {
        Booking booking = getById(req.getBookingId());

        Mechanic mechanic = mechanicRepository.findById(req.getMechanicId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Mechanic not found"));

        booking.setMechanic(mechanic);
        booking.setStatus(BookingStatus.CONFIRMED);

        return bookingRepository.save(booking);
    }

    public Page<Booking> getAllBookings(Pageable pageable) {
        return bookingRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
