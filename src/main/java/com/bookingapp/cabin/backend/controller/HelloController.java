package com.bookingapp.cabin.backend.controller;

// klasse for lokaltesting
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")  //Setter base-path til rotnivå
public class HelloController {

    @GetMapping  //Når du besøker http://localhost:8080/
    public String helloWorld() {
        return "Hello world!";
    }
}