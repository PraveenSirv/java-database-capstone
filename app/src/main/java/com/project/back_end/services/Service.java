package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
                   AdminRepository adminRepository,
                   DoctorRepository doctorRepository,
                   PatientRepository patientRepository,
                   AppointmentRepository appointmentRepository,
                   DoctorService doctorService,
                   PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    public ResponseEntity<Map<String, String>> validateToken(String token, String role) {
        if (!tokenService.validateToken(token, role)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Session expired or invalid login. Please log in again.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return null;
    }

    public ResponseEntity<Map<String, Object>> validateAdmin(Admin admin) {
        try {
            Admin existingAdmin = adminRepository.findByUsername(admin.getUsername());
            if (existingAdmin == null || !existingAdmin.getPassword().equals(admin.getPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Invalid credentials!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String token = tokenService.generateToken(admin.getUsername());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public List<Doctor> filterDoctor(String name, String time, String specialty) {
        return doctorService.filterDoctors(name, time, specialty);
    }

    public int validateAppointment(Long doctorId, String date, String time) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return -1;
        }
        List<String> availableSlots = doctorService.getDoctorAvailability(doctorId, date);
        for (String slot : availableSlots) {
            if (slot.startsWith(time) || slot.equals(time)) {
                return 1;
            }
        }
        return 0;
    }

    public boolean validatePatient(Patient patient) {
        Patient existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        return existing == null;
    }

    public ResponseEntity<Map<String, Object>> validatePatientLogin(Patient patient) {
        try {
            Patient existing = patientRepository.findByEmail(patient.getEmail());
            if (existing == null || !existing.getPassword().equals(patient.getPassword())) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Invalid credentials!");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String token = tokenService.generateToken(patient.getEmail());
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public List<AppointmentDTO> filterPatient(String token, String condition, String doctorName) {
        String email = tokenService.extractEmail(token);
        if (email == null) return null;
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) return null;
        return patientService.filterAppointments(patient.getId(), condition, doctorName);
    }
}
