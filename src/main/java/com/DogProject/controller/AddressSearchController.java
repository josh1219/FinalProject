package com.DogProject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AddressSearchController {
    
    @GetMapping("/address-search")
    public String addressSearch() {
        return "address-search";
    }
}
