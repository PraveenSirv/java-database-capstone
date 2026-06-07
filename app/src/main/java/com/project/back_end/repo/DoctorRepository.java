package com.project.back_end.repo;

import com.project.back_end.models.Doctor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Override
    @EntityGraph(attributePaths = "availableTimes")
    List<Doctor> findAll();

    @Override
    @EntityGraph(attributePaths = "availableTimes")
    Optional<Doctor> findById(Long id);

    @EntityGraph(attributePaths = "availableTimes")
    Doctor findByEmail(String email);

    @EntityGraph(attributePaths = "availableTimes")
    @Query("SELECT d FROM Doctor d WHERE d.name LIKE CONCAT('%', :name, '%')")
    List<Doctor> findByNameLike(@Param("name") String name);

    @EntityGraph(attributePaths = "availableTimes")
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(String name, String specialty);

    @EntityGraph(attributePaths = "availableTimes")
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}