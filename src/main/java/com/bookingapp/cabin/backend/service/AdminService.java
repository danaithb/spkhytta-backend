package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    @Autowired
    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public List<Booking> getAllBookings() {
        return adminRepository.findAll();
    }

    public Booking getBookingById(Long bookingId) {
        return adminRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking ikke funnet"));
    }

    public Booking updateBooking(Long bookingId, String status, LocalDate startDate, LocalDate endDate, Double price, Integer queuePosition) {
        Booking booking = getBookingById(bookingId);

        if (status != null) {
            booking.setStatus(status);
        }
        if (startDate != null) {
            booking.setStartDate(startDate);
        }
        if (endDate != null) {
            booking.setEndDate(endDate);
        }

        if (queuePosition != null) {
            booking.setQueuePosition(queuePosition);
        }

        return adminRepository.save(booking);
    }

    public void deleteBooking(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        adminRepository.delete(booking);
    }
}
