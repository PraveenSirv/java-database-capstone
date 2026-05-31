package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long doctorId, String date) {
        Doctor doctor = doctorRepository.findById(doctorId).orElse(null);
        if (doctor == null) {
            return new ArrayList<>();
        }

        List<String> allSlots = new ArrayList<>(doctor.getAvailableTimes());
        LocalDate localDate = LocalDate.parse(date);
        LocalDateTime start = localDate.atStartOfDay();
        LocalDateTime end = localDate.atTime(23, 59, 59);

        List<Appointment> bookedAppointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        List<String> bookedTimes = bookedAppointments.stream()
                .map(app -> app.getAppointmentTime().toLocalTime().format(formatter))
                .collect(Collectors.toList());

        return allSlots.stream()
                .filter(slot -> {
                    String slotStart = slot.split("-")[0];
                    return !bookedTimes.contains(slotStart);
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1; // Conflict
            }
            doctorRepository.save(doctor);
            return 1; // Success
        } catch (Exception e) {
            return 0; // Failure
        }
    }

    @Transactional
    public int updateDoctor(Doctor doctor) {
        try {
            Doctor existing = doctorRepository.findById(doctor.getId()).orElse(null);
            if (existing == null) {
                return -1;
            }
            existing.setName(doctor.getName());
            existing.setEmail(doctor.getEmail());
            existing.setSpecialty(doctor.getSpecialty());
            existing.setPhone(doctor.getPhone());
            existing.setAvailableTimes(doctor.getAvailableTimes());
            if (doctor.getPassword() != null && !doctor.getPassword().isEmpty()) {
                existing.setPassword(doctor.getPassword());
            }
            doctorRepository.save(existing);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional
    public int deleteDoctor(Long doctorId) {
        try {
            if (!doctorRepository.existsById(doctorId)) {
                return -1;
            }
            appointmentRepository.deleteAllByDoctorId(doctorId);
            doctorRepository.deleteById(doctorId);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public Map<String, Object> validateDoctor(String email, String password) {
        Map<String, Object> response = new HashMap<>();
        Doctor doctor = doctorRepository.findByEmail(email);
        if (doctor == null || !doctor.getPassword().equals(password)) {
            response.put("success", false);
            response.put("message", "Invalid credentials!");
            return response;
        }
        String token = tokenService.generateToken(email);
        response.put("success", true);
        response.put("token", token);
        return response;
    }

    @Transactional(readOnly = true)
    public List<Doctor> findDoctorByName(String name) {
        return doctorRepository.findByNameLike(name);
    }

    private boolean isAvailableDuring(List<String> availableTimes, String timeFilter) {
        if (timeFilter == null || timeFilter.isEmpty() || "null".equalsIgnoreCase(timeFilter)) {
            return true;
        }
        for (String slot : availableTimes) {
            try {
                String startHourStr = slot.split(":")[0];
                int startHour = Integer.parseInt(startHourStr);
                if ("am".equalsIgnoreCase(timeFilter) && startHour < 12) {
                    return true;
                }
                if ("pm".equalsIgnoreCase(timeFilter) && startHour >= 12) {
                    return true;
                }
            } catch (Exception e) {
                // ignore
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctors(String name, String time, String specialty) {
        boolean hasName = (name != null && !name.isEmpty() && !"null".equalsIgnoreCase(name));
        boolean hasTime = (time != null && !time.isEmpty() && !"null".equalsIgnoreCase(time));
        boolean hasSpecialty = (specialty != null && !specialty.isEmpty() && !"null".equalsIgnoreCase(specialty));

        List<Doctor> doctors;
        if (hasName && hasSpecialty) {
            doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        } else if (hasName) {
            doctors = doctorRepository.findByNameLike(name);
        } else if (hasSpecialty) {
            doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        } else {
            doctors = doctorRepository.findAll();
        }

        if (hasTime) {
            doctors = doctors.stream()
                    .filter(d -> isAvailableDuring(d.getAvailableTimes(), time))
                    .collect(Collectors.toList());
        }

        return doctors;
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorsByNameSpecilityandTime(String name, String specialty, String time) {
        return filterDoctors(name, time, specialty);
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByTime(List<Doctor> doctors, String time) {
        return doctors.stream()
                .filter(d -> isAvailableDuring(d.getAvailableTimes(), time))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByNameAndTime(String name, String time) {
        return filterDoctors(name, time, null);
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByNameAndSpecility(String name, String specialty) {
        return filterDoctors(name, null, specialty);
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorByTimeAndSpecility(String time, String specialty) {
        return filterDoctors(null, time, specialty);
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorBySpecility(String specialty) {
        return filterDoctors(null, null, specialty);
    }

    @Transactional(readOnly = true)
    public List<Doctor> filterDoctorsByTime(String time) {
        return filterDoctors(null, time, null);
    }
}
