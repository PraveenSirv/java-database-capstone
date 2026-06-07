package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.DTO.DoctorDTO;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import jakarta.validation.Valid;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctor")
@CrossOrigin
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> doctorLogin(@RequestBody Login login) {
        Map<String, Object> result = doctorService.validateDoctor(login.getEmail(), login.getPassword());
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctor() {
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctorService.getDoctors());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> saveDoctor(@Valid @RequestBody DoctorDTO doctorDTO, @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "admin");
        if (validation != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Session expired or invalid login. Please log in again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        int result = doctorService.saveDoctor(doctorDTO);
        Map<String, Object> response = new HashMap<>();
        if (result == -1) {
            response.put("message", "Doctor already exists!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else if (result == 1) {
            response.put("message", "Doctor added successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, Object>> updateDoctor(@Valid @RequestBody DoctorDTO doctorDTO, @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "admin");
        if (validation != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Session expired or invalid login. Please log in again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        int result = doctorService.updateDoctor(doctorDTO);
        Map<String, Object> response = new HashMap<>();
        if (result == -1) {
            response.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (result == 1) {
            response.put("message", "Doctor updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete/{id}/{token}")
    public ResponseEntity<Map<String, Object>> deleteDoctor(@PathVariable("id") Long id, @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "admin");
        if (validation != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Session expired or invalid login. Please log in again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        int result = doctorService.deleteDoctor(id);
        Map<String, Object> response = new HashMap<>();
        if (result == -1) {
            response.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (result == 1) {
            response.put("message", "Doctor deleted successfully");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/filter/{name}/{time}/{specialty}")
    public ResponseEntity<Map<String, Object>> filter(@PathVariable("name") String name,
                                                      @PathVariable("time") String time,
                                                      @PathVariable("specialty") String specialty) {
        List<DoctorDTO> doctors = service.filterDoctor(name, time, specialty);
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctors);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(@PathVariable("user") String user,
                                                                     @PathVariable("doctorId") Long doctorId,
                                                                     @PathVariable("date") String date,
                                                                     @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, user);
        if (validation != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Session expired or invalid login. Please log in again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, date);
        Map<String, Object> response = new HashMap<>();
        response.put("availableSlots", availableSlots);
        return ResponseEntity.ok(response);
    }
}
