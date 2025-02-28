package com.example.thedayoftoday.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {


    @GetMapping
    public String home() {
        return "메인 페이지";
    }
}
