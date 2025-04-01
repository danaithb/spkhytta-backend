/*package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.UserRepository;

import java.util.List;

public class PointService {
    private final UserRepository userRepository;

    public PointService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void resetPointsYearly() {
        List<Users> users = userRepository.findAll();
        for (Users user : users) {
            user.setPoints(12);
            userRepository.save(user);
        }
    }

}*/
