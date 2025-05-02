package com.bookingapp.cabin.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FaviconController {

    @RequestMapping("favicon.ico")
    @ResponseBody
    void returnNoFavicon() {
        // Returner bare 200 OK uten innhold
    }
}
