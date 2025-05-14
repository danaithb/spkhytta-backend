package com.bookingapp.cabin.backend.controller;

// klasse for lokaltesting
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")  //Setter base-path til rotnivå
public class HelloController {

    @GetMapping
    public String helloWorld() {
        return "Hello world!";
    }
}