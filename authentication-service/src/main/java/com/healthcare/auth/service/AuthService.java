package com.healthcare.auth.service;

import com.healthcare.auth.client.AdminServiceClient;
import com.healthcare.auth.client.DoctorServiceClient;
import com.healthcare.auth.dto.DoctorMetaSyncRequest;
import com.healthcare.auth.dto.DoctorProfileResponse;
import com.healthcare.auth.dto.DoctorProfileUpdateRequest;
import com.healthcare.auth.dto.DoctorRegisterRequest;
import com.healthcare.auth.dto.RegisterRequest;
import com.healthcare.auth.dto.RoleChangeRequest;
import com.healthcare.auth.dto.UpdateUserRequest;
import com.healthcare.auth.entity.Doctor;
import com.healthcare.auth.entity.Role;
import com.healthcare.auth.entity.User;
import com.healthcare.auth.repository.DoctorRepository;
import com.healthcare.auth.repository.RoleRepository;
import com.healthcare.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.healthcare.auth.dto.UserResponse;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminServiceClient adminServiceClient;
    private final DoctorRepository doctorRepository;
    private final DoctorServiceClient doctorServiceClient;

    @Value("${internal.api.key}")
    private String internalApiKey;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       AdminServiceClient adminServiceClient,
                       DoctorRepository doctorRepository,
                       DoctorServiceClient doctorServiceClient) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminServiceClient = adminServiceClient;
        this.doctorRepository = doctorRepository;
        this.doctorServiceClient = doctorServiceClient;
    }

    @Transactional
    public UserResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = new User();
        user.setUsername(request.getUsername());

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

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

        user = userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setStatus(user.getStatus());
        response.setApproved(user.isApproved());
        response.setCreatedAt(user.getCreatedAt());
        response.setRoles(user.getRoles());

        return response;
    }

    @Transactional
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

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

        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("DOCTOR"))) {
            Doctor doctor = doctorRepository.findByUserId(id)
                    .orElseThrow(() -> new RuntimeException("Doctor profile not found for user: " + id));

            if (request.getSpecialization() != null) {
                doctor.setSpecialization(request.getSpecialization());
            }

            if (request.getLicenseNumber() != null) {
                doctor.setLicenseNumber(request.getLicenseNumber());
            }

            doctor = doctorRepository.save(doctor);
            syncDoctorMeta(user, doctor);
        }

        return user;
    }

    @Transactional(readOnly = true)
    public DoctorProfileResponse getDoctorProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        validateDoctorUser(user);

        Doctor doctor = doctorRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for user: " + user.getId()));

        return mapToDoctorProfileResponse(user, doctor);
    }

    @Transactional
    public DoctorProfileResponse updateDoctorProfileByUsername(String username, DoctorProfileUpdateRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        validateDoctorUser(user);

        Long userId = user.getId();

        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for user: " + userId));

        if (request.getEmail() != null
                && !request.getEmail().isBlank()
                && !request.getEmail().equals(user.getEmail())) {

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getDoctorName() != null && !request.getDoctorName().isBlank()) {
            doctor.setDoctorName(request.getDoctorName());
        }

        if (request.getSpecialization() != null && !request.getSpecialization().isBlank()) {
            doctor.setSpecialization(request.getSpecialization());
        }

        if (request.getLicenseNumber() != null && !request.getLicenseNumber().isBlank()) {
            doctor.setLicenseNumber(request.getLicenseNumber());
        }

        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            doctor.setLocation(request.getLocation());
        }

        user = userRepository.save(user);
        doctor = doctorRepository.save(doctor);

        syncDoctorMeta(user, doctor);

        return mapToDoctorProfileResponse(user, doctor);
    }

    @Transactional
    public void deleteUser(Long id) {
        doctorRepository.findByUserId(id).ifPresent(doctorRepository::delete);

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
        user = userRepository.save(user);

        Doctor doctor = doctorRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("Doctor profile not found for user: " + id));

        syncDoctorMeta(user, doctor);
    }

    @Transactional
    public User registerDoctor(DoctorRegisterRequest request) {
        List<String> specializations = adminServiceClient.getSpecializations();
        if (!specializations.contains(request.getSpecialization())) {
            throw new RuntimeException("Invalid specialization: " + request.getSpecialization());
        }

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
        user.setApproved(false);

        user = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setDoctorName(request.getDoctorName());
        doctor.setSpecialization(request.getSpecialization());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setLocation(request.getLocation());
        doctor = doctorRepository.save(doctor);

        syncDoctorMeta(user, doctor);

        return user;
    }

    private void validateDoctorUser(User user) {
        boolean isDoctor = user.getRoles().stream()
                .anyMatch(role -> "DOCTOR".equalsIgnoreCase(role.getName()));

        if (!isDoctor) {
            throw new RuntimeException("User is not a doctor");
        }
    }

    private DoctorProfileResponse mapToDoctorProfileResponse(User user, Doctor doctor) {
        DoctorProfileResponse response = new DoctorProfileResponse();
        response.setDoctorId(doctor.getId());
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setDoctorName(doctor.getDoctorName());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setStatus(user.getStatus());
        response.setApproved(user.isApproved());
        response.setCreatedAt(user.getCreatedAt());
        response.setSpecialization(doctor.getSpecialization());
        response.setLicenseNumber(doctor.getLicenseNumber());
        response.setLocation(doctor.getLocation());
        return response;
    }

    private void syncDoctorMeta(User user, Doctor doctor) {
        DoctorMetaSyncRequest request = new DoctorMetaSyncRequest();
        request.setUserId(user.getId());
        request.setUsername(user.getUsername());
        request.setDoctorName(
                doctor.getDoctorName() != null && !doctor.getDoctorName().isBlank()
                        ? doctor.getDoctorName()
                        : user.getUsername()
        );
        request.setSpecialization(doctor.getSpecialization());
        request.setLocation(
                doctor.getLocation() != null && !doctor.getLocation().isBlank()
                        ? doctor.getLocation()
                        : "Unknown"
        );
        request.setVerificationStatus(user.isApproved() ? "VERIFIED" : "PENDING");

        doctorServiceClient.syncDoctorMeta(request, internalApiKey);
    }
}