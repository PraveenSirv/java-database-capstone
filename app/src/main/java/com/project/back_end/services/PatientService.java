package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getPatientAppointment(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> filterByCondition(Long patientId, String condition) {
        int status = "past".equalsIgnoreCase(condition) ? 1 : 0;
        List<Appointment> appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status);
        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> filterByDoctor(Long patientId, String doctorName) {
        List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientId(doctorName, patientId);
        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> filterByDoctorAndCondition(Long patientId, String doctorName, String condition) {
        int status = "past".equalsIgnoreCase(condition) ? 1 : 0;
        List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, status);
        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Patient getPatientDetails(String token) {
        String email = tokenService.extractEmail(token);
        if (email == null) {
            return null;
        }
        return patientRepository.findByEmail(email);
    }

    public List<AppointmentDTO> filterAppointments(Long patientId, String condition, String doctorName) {
        boolean hasCondition = (condition != null && !condition.isEmpty() && !"null".equalsIgnoreCase(condition) && !"all".equalsIgnoreCase(condition));
        boolean hasDoctor = (doctorName != null && !doctorName.isEmpty() && !"null".equalsIgnoreCase(doctorName));

        List<Appointment> list;
        if (hasCondition && hasDoctor) {
            int status = "past".equalsIgnoreCase(condition) ? 1 : 0;
            list = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, status);
        } else if (hasCondition) {
            int status = "past".equalsIgnoreCase(condition) ? 1 : 0;
            list = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status);
        } else if (hasDoctor) {
            list = appointmentRepository.filterByDoctorNameAndPatientId(doctorName, patientId);
        } else {
            list = appointmentRepository.findByPatientId(patientId);
        }
        return list.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private AppointmentDTO convertToDTO(Appointment app) {
        return new AppointmentDTO(
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
        );
    }
}
