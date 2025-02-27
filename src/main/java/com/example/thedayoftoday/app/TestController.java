package com.example.thedayoftoday.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController

public class TestController {

    @GetMapping("/hi")
    public String testController()
    {
        return "ok";
    }

}
