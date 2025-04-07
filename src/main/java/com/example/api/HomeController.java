package com.example.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private static final String REDIRECT_INDEX = "redirect:/index.html";

    @GetMapping("/")
    public String home() {
        return REDIRECT_INDEX;
    }

}
