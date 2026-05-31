package com.project.back_end.controllers;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/patient")
@CrossOrigin
public class PatientController {

    private final PatientService patientService;
    private final Service service;

    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (validation != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Session expired or invalid login. Please log in again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        Patient patient = patientService.getPatientDetails(token);
        Map<String, Object> response = new HashMap<>();
        response.put("patient", patient);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPatient(@RequestBody Patient patient) {
        if (!service.validatePatient(patient)) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Patient with this email or phone number already exists!");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        int res = patientService.createPatient(patient);
        Map<String, Object> response = new HashMap<>();
        if (res == 1) {
            response.put("message", "Patient registered successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.put("message", "Failed to register patient");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Login login) {
        Patient temp = new Patient();
        temp.setEmail(login.getEmail());
        temp.setPassword(login.getPassword());
        return service.validatePatientLogin(temp);
    }

    @GetMapping("/{id}/{user}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(@PathVariable("id") Long id,
                                                                     @PathVariable("user") String user,
                                                                     @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, user);
        if (validation != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Session expired or invalid login. Please log in again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        List<AppointmentDTO> appointments = patientService.getPatientAppointment(id);
        Map<String, Object> response = new HashMap<>();
        response.put("appointments", appointments);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(@PathVariable("condition") String condition,
                                                                        @PathVariable("name") String name,
                                                                        @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (validation != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Session expired or invalid login. Please log in again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        List<AppointmentDTO> rawAppointments = service.filterPatient(token, condition, name);
        // Map raw appointments to AppointmentDTO if filterPatient returns raw Appointments
        // Wait, in Service.java, we defined: public List<Appointment> filterPatient
        // We can convert those to AppointmentDTO or let patientService do it.
        // Let's call patientService.filterAppointments(patientId, condition, name)
        Patient patient = patientService.getPatientDetails(token);
        List<AppointmentDTO> dtoList = patientService.filterAppointments(patient.getId(), condition, name);
        Map<String, Object> response = new HashMap<>();
        response.put("appointments", dtoList);
        return ResponseEntity.ok(response);
    }
}
