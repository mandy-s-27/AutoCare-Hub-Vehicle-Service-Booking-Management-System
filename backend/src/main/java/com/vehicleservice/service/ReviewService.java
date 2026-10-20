package com.vehicleservice.service;

import com.vehicleservice.dto.ReviewRequest;
import com.vehicleservice.entity.Booking;
import com.vehicleservice.entity.Customer;
import com.vehicleservice.entity.Review;
import com.vehicleservice.exception.BadRequestException;
import com.vehicleservice.exception.ResourceNotFoundException;
import com.vehicleservice.repository.BookingRepository;
import com.vehicleservice.repository.CustomerRepository;
import com.vehicleservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public Review addReview(Long customerId, ReviewRequest req) {
        Booking booking = bookingRepository.findById(req.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (!booking.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("This booking does not belong to you");
        }
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Review review = Review.builder()
                .booking(booking)
                .customer(customer)
                .rating(req.getRating())
                .comment(req.getComment())
                .build();
        return reviewRepository.save(review);
    }
}
