package com.healthcare.auth.service;

import com.healthcare.auth.client.AdminServiceClient;
import com.healthcare.auth.dto.*;
import com.healthcare.auth.entity.Doctor;
import com.healthcare.auth.entity.Role;
import com.healthcare.auth.entity.User;
import com.healthcare.auth.repository.DoctorRepository;
import com.healthcare.auth.repository.RoleRepository;
import com.healthcare.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminServiceClient adminServiceClient;
    private final DoctorRepository doctorRepository;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AdminServiceClient adminServiceClient,
                       DoctorRepository doctorRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminServiceClient = adminServiceClient;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());

        String roleName = request.getRole() != null ? request.getRole().toUpperCase() : "PATIENT";
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));

        user.getRoles().add(role);
        if (roleName.equals("DOCTOR")) {
            user.setApproved(false);
        } else {
            user.setApproved(true);
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update basic fields
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        user = userRepository.save(user);

        // If the user is a doctor, update the doctor profile
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("DOCTOR"))) {
            Doctor doctor = doctorRepository.findByUserId(id)
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found for user: " + id));
            if (request.getSpecialization() != null) {
                doctor.setSpecialization(request.getSpecialization());
            }
            if (request.getLicenseNumber() != null) {
                doctor.setLicenseNumber(request.getLicenseNumber());
            }
            doctorRepository.save(doctor);
        }

        return user;
    }

    @Transactional
    public void deleteUser(Long id) {
        // First delete doctor record if it exists (to avoid foreign key constraint violation)
        doctorRepository.findByUserId(id).ifPresent(doctor -> doctorRepository.delete(doctor));

        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public User changeUserRole(Long id, RoleChangeRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Role newRole = roleRepository.findByName(request.getRoleName().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().clear();
        user.getRoles().add(newRole);

        return userRepository.save(user);
    }

    @Transactional
    public void approveDoctor(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setApproved(true);
        userRepository.save(user);
    }

    @Transactional
    public User registerDoctor(DoctorRegisterRequest request) {
        // 1. Validate specialization exists via admin service
        List<String> specializations = adminServiceClient.getSpecializations();
        if (!specializations.contains(request.getSpecialization())) {
            throw new RuntimeException("Invalid specialization: " + request.getSpecialization());
        }

        // 2. Create user with role DOCTOR
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role doctorRole = roleRepository.findByName("DOCTOR")
                .orElseThrow(() -> new RuntimeException("DOCTOR role not found"));
        user.getRoles().add(doctorRole);
        user.setApproved(false);  // doctors need approval

        user = userRepository.save(user);

        // 3. Create doctor profile
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctorRepository.save(doctor);

        return user;
    }
}