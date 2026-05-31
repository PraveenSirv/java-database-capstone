package com.project.back_end.controllers;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
@CrossOrigin
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(AppointmentService appointmentService, Service service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<?> getAppointments(@PathVariable("date") String date,
                                             @PathVariable("patientName") String patientName,
                                             @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "doctor");
        if (validation != null) {
            return validation;
        }
        List<AppointmentDTO> list = appointmentService.getAppointments(date, patientName, token);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> bookAppointment(@RequestBody Appointment appointment,
                                                               @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (validation != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Session expired or invalid login. Please log in again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String dateStr = appointment.getAppointmentTime().toLocalDate().toString();
        String timeStr = appointment.getAppointmentTime().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        int validity = service.validateAppointment(appointment.getDoctor().getId(), dateStr, timeStr);

        Map<String, Object> response = new HashMap<>();
        if (validity == -1) {
            response.put("message", "Doctor not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else if (validity == 0) {
            response.put("message", "Doctor not available at the specified time");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        int res = appointmentService.bookAppointment(appointment);
        if (res == 1) {
            response.put("message", "Appointment booked successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Failed to book appointment");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, Object>> updateAppointment(@RequestBody Appointment appointment,
                                                                 @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (validation != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Session expired or invalid login. Please log in again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String result = appointmentService.updateAppointment(appointment, token);
        Map<String, Object> response = new HashMap<>();
        if ("success".equals(result)) {
            response.put("message", "Appointment updated successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", result);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> cancelAppointment(@PathVariable("id") Long id,
                                                                 @PathVariable("token") String token) {
        ResponseEntity<Map<String, String>> validation = service.validateToken(token, "patient");
        if (validation != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Session expired or invalid login. Please log in again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        boolean success = appointmentService.cancelAppointment(id, token);
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("message", "Appointment cancelled successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Failed to cancel appointment");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
