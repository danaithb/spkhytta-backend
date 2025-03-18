package com.bookingapp.cabin.backend.service;

import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

//Denne klassen er fikset
@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository usersRepository) {
        this.userRepository = usersRepository;
    }

    //Henter alle brukere
    public List<Users> getAllUsers() {
        return userRepository.findAll();
    }

    //Henter en bruker basert på email
    public Optional<Users> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    //Henter en bruker basert på firebase uid
    public Optional<Users> getUserByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid);
    }

}
