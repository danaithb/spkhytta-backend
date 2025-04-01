package com.bookingapp.cabin.backend.controller;

import com.bookingapp.cabin.backend.model.Users;
import com.bookingapp.cabin.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    //Hent alle brukere
    @GetMapping
    public ResponseEntity<List<Users>> getAllUsers() {
        //List<Users> users = userService.getAllUsers();
        return ResponseEntity.ok(userService.getAllUsers());
    }

    //Hent en bruker baser på email
    @GetMapping("/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Bruker ikke funnet"));
    }
}
