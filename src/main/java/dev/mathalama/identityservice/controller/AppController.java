package dev.mathalama.identityservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppController {
    @GetMapping("/health")
    public String getInfo() {
        return "ok";
    }
}
