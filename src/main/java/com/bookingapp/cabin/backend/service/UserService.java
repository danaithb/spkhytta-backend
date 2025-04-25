package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.dtos.BookingSummaryDTO;
import com.bookingapp.cabin.backend.model.Booking;
import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.BookingRepository;
import com.bookingapp.cabin.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final WaitListService waitListService;

    @Autowired
    public UserService(UserRepository usersRepository, BookingRepository bookingRepository, WaitListService waitListService) {
        this.userRepository = usersRepository;
        this.bookingRepository = bookingRepository;
        this.waitListService = waitListService;
    }


    public List<BookingSummaryDTO> getMyBookingSummaries(String firebaseUid) {
        List<Booking> bookings = bookingRepository.findByUser_FirebaseUidOrderByStartDateDesc(firebaseUid);

        return bookings.stream().map(booking -> new BookingSummaryDTO(
                booking.getBookingCode(),
                booking.getCabin().getCabinName(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus()
        )).toList();
    }


    public List<Booking> getBookingsForUser(String firebaseUid) {
        return bookingRepository.findByUser_FirebaseUidOrderByStartDateDesc(firebaseUid);
    }


}



