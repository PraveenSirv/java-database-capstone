package com.project.back_end.mvc;

import com.project.back_end.services.Service;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DashboardController {

    private final Service service;

    public DashboardController(Service service) {
        this.service = service;
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable("token") String token) {
        if (service.validateToken(token, "admin") != null) {
            return "redirect:/";
        }
        return "admin/adminDashboard";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable("token") String token) {
        if (service.validateToken(token, "doctor") != null) {
            return "redirect:/";
        }
        return "doctor/doctorDashboard";
    }
}
