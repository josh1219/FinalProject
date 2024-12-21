package com.DogProject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/terms")
public class TermsController {

    @GetMapping("/terms")
    public String terms() {
        return "terms/terms";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "terms/privacy";
    }

    @GetMapping("/location")
    public String location() {
        return "terms/location";
    }
}
