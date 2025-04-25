package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.AdminRepository;
import com.bookingapp.cabin.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private UserRepository userRepository;

    @Autowired
    public AdminService(AdminRepository adminRepository, UserRepository userRepository) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
    }

    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
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
