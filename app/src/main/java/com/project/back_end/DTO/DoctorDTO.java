package com.project.back_end.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.back_end.models.Doctor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class DoctorDTO {

    private Long id;

    @NotNull(message = "Name cannot be null")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    @NotNull(message = "Specialty cannot be null")
    @Size(min = 3, max = 50, message = "Specialty must be between 3 and 50 characters")
    private String specialty;

    @NotNull(message = "Email cannot be null")
    @Email(message = "Email must be valid")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull(message = "Phone number cannot be null")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;

    private List<String> availableTimes = new ArrayList<>();

    // Default constructor
    public DoctorDTO() {
    }

    // Parameterized constructor
    public DoctorDTO(Long id, String name, String specialty, String email, String password, String phone, List<String> availableTimes) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.availableTimes = availableTimes != null ? new ArrayList<>(availableTimes) : new ArrayList<>();
    }

    // Convert Entity to DTO
    public static DoctorDTO fromEntity(Doctor doctor) {
        if (doctor == null) {
            return null;
        }
        return new DoctorDTO(
            doctor.getId(),
            doctor.getName(),
            doctor.getSpecialty(),
            doctor.getEmail(),
            null, // Do not expose password to client
            doctor.getPhone(),
            doctor.getAvailableTimes() != null ? new ArrayList<>(doctor.getAvailableTimes()) : new ArrayList<>()
        );
    }

    // Convert DTO to Entity
    public Doctor toEntity() {
        Doctor doctor = new Doctor();
        doctor.setId(this.id);
        doctor.setName(this.name);
        doctor.setSpecialty(this.specialty);
        doctor.setEmail(this.email);
        doctor.setPassword(this.password);
        doctor.setPhone(this.phone);
        doctor.setAvailableTimes(this.availableTimes != null ? new ArrayList<>(this.availableTimes) : new ArrayList<>());
        return doctor;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getAvailableTimes() {
        return availableTimes;
    }

    public void setAvailableTimes(List<String> availableTimes) {
        this.availableTimes = availableTimes;
    }
}
