package com.bookingapp.cabin.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;

public class FaviconController {
    @RequestMapping("favicon.ico")
    @ResponseBody
    void returnNoFavicon() {
        // for gcp, returner 200 OK og ikk error
    }
}
