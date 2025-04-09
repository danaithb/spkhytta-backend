package com.bookingapp.cabin.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

public class FaviconController {
    @RequestMapping("favicon.ico")
    @ResponseBody
    void returnNoFavicon() {
        // Gjør ingenting, bare returner 200 OK og ikk error
    }
}
