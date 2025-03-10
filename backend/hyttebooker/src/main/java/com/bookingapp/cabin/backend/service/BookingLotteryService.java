package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class BookingLotteryService {

    @Autowired
    private BookingRepository bookingRepository;

    public Booking conductLottery(List<Booking> pendingBookings) {
        if (pendingBookings.isEmpty()) return null;

        Booking selectedBooking = pendingBookings.get(new Random().nextInt(pendingBookings.size()));
        selectedBooking.setStatus("booked");
        bookingRepository.save(selectedBooking);

        return selectedBooking;
    }
}

