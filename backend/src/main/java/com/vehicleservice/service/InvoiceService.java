package com.vehicleservice.service;

import com.vehicleservice.entity.Booking;
import com.vehicleservice.entity.Invoice;
import com.vehicleservice.exception.ResourceNotFoundException;
import com.vehicleservice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final double GST_RATE = 0.18;

    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Invoice generateInvoiceForBooking(Booking booking) {
        double price = booking.getService().getPrice();
        double gst = Math.round(price * GST_RATE * 100.0) / 100.0;
        Invoice invoice = Invoice.builder()
                .booking(booking)
                .totalAmount(price + gst)
                .gst(gst)
                .paymentStatus("PENDING")
                .build();
        return invoiceRepository.save(invoice);
    }

    public Invoice getByBookingId(Long bookingId) {
        return invoiceRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for this booking"));
    }
}
