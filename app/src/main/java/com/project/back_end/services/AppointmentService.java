package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorService doctorService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              TokenService tokenService,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository,
                              DoctorService doctorService) {
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.doctorService = doctorService;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            Doctor doc = doctorRepository.findById(appointment.getDoctor().getId()).orElse(null);
            Patient pat = patientRepository.findById(appointment.getPatient().getId()).orElse(null);
            if (doc == null || pat == null) {
                return 0;
            }
            appointment.setDoctor(doc);
            appointment.setPatient(pat);
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public String updateAppointment(Long appointmentId, Appointment updated) {
        try {
            Appointment existing = appointmentRepository.findById(appointmentId).orElse(null);
            if (existing == null) {
                return "Appointment not found";
            }
            if (!existing.getPatient().getId().equals(updated.getPatient().getId())) {
                return "Patient ID mismatch";
            }
            Doctor doc = doctorRepository.findById(updated.getDoctor().getId()).orElse(null);
            if (doc == null) {
                return "Doctor not found";
            }
            String dateStr = updated.getAppointmentTime().toLocalDate().toString();
            List<String> available = doctorService.getDoctorAvailability(doc.getId(), dateStr);
            String slotHour = updated.getAppointmentTime().toLocalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            boolean isOwnSlot = existing.getDoctor().getId().equals(doc.getId()) && existing.getAppointmentTime().equals(updated.getAppointmentTime());

            boolean slotAvailable = false;
            if (isOwnSlot) {
                slotAvailable = true;
            } else {
                for (String slot : available) {
                    if (slot.startsWith(slotHour)) {
                        slotAvailable = true;
                        break;
                    }
                }
            }

            if (!slotAvailable) {
                return "Doctor is not available at the selected time";
            }

            existing.setDoctor(doc);
            existing.setAppointmentTime(updated.getAppointmentTime());
            existing.setStatus(updated.getStatus());
            appointmentRepository.save(existing);
            return "success";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @Transactional
    public String updateAppointment(Appointment appointment, String token) {
        String email = tokenService.extractEmail(token);
        if (email == null) return "Invalid token";
        Patient patient = patientRepository.findByEmail(email);
        if (patient == null) return "Patient not found";

        appointment.setPatient(patient);
        return updateAppointment(appointment.getId(), appointment);
    }

    @Transactional
    public boolean cancelAppointment(Long appointmentId, String token) {
        try {
            String email = tokenService.extractEmail(token);
            if (email == null) return false;
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) return false;

            Appointment app = appointmentRepository.findById(appointmentId).orElse(null);
            if (app == null) return false;

            if (!app.getPatient().getId().equals(patient.getId())) {
                return false;
            }

            appointmentRepository.delete(app);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointments(String date, String patientName, String token) {
        String email = tokenService.extractEmail(token);
        if (email == null) return new ArrayList<>();
        Doctor doctor = doctorRepository.findByEmail(email);
        if (doctor == null) return new ArrayList<>();

        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime start = localDate.atStartOfDay();
        LocalDateTime end = localDate.atTime(23, 59, 59);

        List<Appointment> list;
        if (patientName == null || patientName.isEmpty() || "null".equalsIgnoreCase(patientName)) {
            list = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), start, end);
        } else {
            list = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(doctor.getId(), patientName, start, end);
        }

        return list.stream().map(app -> new AppointmentDTO(
                app.getId(),
                app.getDoctor().getId(),
                app.getDoctor().getName(),
                app.getPatient().getId(),
                app.getPatient().getName(),
                app.getPatient().getEmail(),
                app.getPatient().getPhone(),
                app.getPatient().getAddress(),
                app.getAppointmentTime(),
                app.getStatus()
        )).collect(Collectors.toList());
    }

    @Transactional
    public void changeStatus(Long appointmentId, int status) {
        appointmentRepository.updateStatus(status, appointmentId);
    }
}
