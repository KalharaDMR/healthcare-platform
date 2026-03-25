package com.healthcare.doctor.service;

import com.healthcare.doctor.client.AuthServiceClient;
import com.healthcare.doctor.dto.*;
import com.healthcare.doctor.entity.DoctorAvailabilitySlot;
import com.healthcare.doctor.entity.DoctorProfile;
import com.healthcare.doctor.entity.DoctorVerificationStatus;
import com.healthcare.doctor.exception.ResourceNotFoundException;
import com.healthcare.doctor.repository.DoctorAvailabilitySlotRepository;
import com.healthcare.doctor.repository.DoctorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorAvailabilitySlotRepository slotRepository;
    private final AuthServiceClient authServiceClient;

    @Value("${services.auth.internal-key}")
    private String authInternalKey;

    @Transactional
    public DoctorResponse registerDoctor(DoctorRegistrationRequest request) {
        if (doctorProfileRepository.existsByAuthUserId(request.getAuthUserId())) {
            throw new IllegalArgumentException("Doctor profile already exists for this auth user");
        }

        if (doctorProfileRepository.existsByLicenseNumber(request.getLicenseNumber())) {
            throw new IllegalArgumentException("License number already exists");
        }

        DoctorProfile doctor = new DoctorProfile();
        doctor.setAuthUserId(request.getAuthUserId());
        doctor.setFullName(request.getFullName());
        doctor.setLicenseNumber(request.getLicenseNumber());
        doctor.setPrimarySpecialization(request.getPrimarySpecialization());
        doctor.setSecondarySpecialization(request.getSecondarySpecialization());
        doctor.setLocation(request.getLocation());
        doctor.setHospitalName(request.getHospitalName());
        doctor.setBio(request.getBio());
        doctor.setYearsOfExperience(request.getYearsOfExperience());
        doctor.setConsultationFee(request.getConsultationFee());
        doctor.setVerificationStatus(DoctorVerificationStatus.PENDING);

        return mapDoctorResponse(doctorProfileRepository.save(doctor), true);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors() {
        return doctorProfileRepository.findAll().stream()
                .sorted(Comparator.comparing(DoctorProfile::getCreatedAt).reversed())
                .map(doctor -> mapDoctorResponse(doctor, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctor(Long doctorId) {
        return mapDoctorResponse(getDoctorEntity(doctorId), true);
    }

    @Transactional
    public DoctorResponse updateDoctor(Long doctorId, DoctorUpdateRequest request) {
        DoctorProfile doctor = getDoctorEntity(doctorId);

        if (request.getFullName() != null) doctor.setFullName(request.getFullName());
        if (request.getPrimarySpecialization() != null) doctor.setPrimarySpecialization(request.getPrimarySpecialization());
        if (request.getSecondarySpecialization() != null) doctor.setSecondarySpecialization(request.getSecondarySpecialization());
        if (request.getLocation() != null) doctor.setLocation(request.getLocation());
        if (request.getHospitalName() != null) doctor.setHospitalName(request.getHospitalName());
        if (request.getBio() != null) doctor.setBio(request.getBio());
        if (request.getYearsOfExperience() != null) doctor.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getConsultationFee() != null) doctor.setConsultationFee(request.getConsultationFee());
        if (request.getActive() != null) doctor.setActive(request.getActive());

        return mapDoctorResponse(doctorProfileRepository.save(doctor), true);
    }

    @Transactional
    public DoctorAvailabilitySlotResponse addAvailabilitySlot(Long doctorId, AvailabilitySlotRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        DoctorProfile doctor = getDoctorEntity(doctorId);

        List<DoctorAvailabilitySlot> existingSlots = slotRepository.findByDoctorIdOrderByStartTimeAsc(doctorId);

        boolean overlaps = existingSlots.stream().anyMatch(slot ->
                request.getStartTime().isBefore(slot.getEndTime()) &&
                        request.getEndTime().isAfter(slot.getStartTime())
        );

        if (overlaps) {
            throw new IllegalArgumentException("New slot overlaps with an existing slot");
        }

        DoctorAvailabilitySlot slot = new DoctorAvailabilitySlot();
        slot.setDoctor(doctor);
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setAvailable(request.isAvailable());
        slot.setNotes(request.getNotes());

        return mapSlotResponse(slotRepository.save(slot));
    }

    @Transactional
    public void deleteAvailabilitySlot(Long doctorId, Long slotId) {
        DoctorAvailabilitySlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability slot not found"));

        if (!slot.getDoctor().getId().equals(doctorId)) {
            throw new IllegalArgumentException("Slot does not belong to the given doctor");
        }

        slotRepository.delete(slot);
    }

    @Transactional(readOnly = true)
    public List<DoctorSearchResponse> searchDoctors(String specialization, String location, String availableFrom, String availableTo) {
        String specializationFilter = normalize(specialization);
        String locationFilter = normalize(location);

        return doctorProfileRepository.findAll().stream()
                .filter(DoctorProfile::isActive)
                .filter(doc -> doc.getVerificationStatus() == DoctorVerificationStatus.VERIFIED)
                .filter(doc -> matchesSpecialization(doc, specializationFilter))
                .filter(doc -> matchesLocation(doc, locationFilter))
                .map(doc -> buildSearchResponse(doc, availableFrom, availableTo))
                .filter(response -> response.getMatchingSlots() != null)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getPendingDoctors() {
        return doctorProfileRepository.findByVerificationStatus(DoctorVerificationStatus.PENDING)
                .stream()
                .map(doc -> mapDoctorResponse(doc, true))
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorResponse verifyDoctor(Long doctorId, DoctorVerificationRequest request) {
        DoctorProfile doctor = getDoctorEntity(doctorId);

        if (request.isApproved()) {
            doctor.setVerificationStatus(DoctorVerificationStatus.VERIFIED);
            authServiceClient.approveDoctor(doctor.getAuthUserId(), authInternalKey);
        } else {
            doctor.setVerificationStatus(DoctorVerificationStatus.REJECTED);
        }

        doctor.setVerificationRemarks(request.getRemarks());

        return mapDoctorResponse(doctorProfileRepository.save(doctor), true);
    }

    private DoctorProfile getDoctorEntity(Long doctorId) {
        return doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + doctorId));
    }

    private DoctorSearchResponse buildSearchResponse(DoctorProfile doctor, String availableFrom, String availableTo) {
        List<DoctorAvailabilitySlotResponse> slots;

        if (availableFrom != null && availableTo != null) {
            LocalDateTime from = LocalDateTime.parse(availableFrom);
            LocalDateTime to = LocalDateTime.parse(availableTo);

            slots = slotRepository
                    .findByDoctorIdAndAvailableTrueAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc(
                            doctor.getId(), from, to
                    )
                    .stream()
                    .map(this::mapSlotResponse)
                    .collect(Collectors.toList());

            if (slots.isEmpty()) {
                return DoctorSearchResponse.builder()
                        .matchingSlots(null)
                        .build();
            }
        } else {
            slots = slotRepository.findByDoctorIdOrderByStartTimeAsc(doctor.getId())
                    .stream()
                    .filter(DoctorAvailabilitySlot::isAvailable)
                    .map(this::mapSlotResponse)
                    .collect(Collectors.toList());
        }

        return DoctorSearchResponse.builder()
                .id(doctor.getId())
                .fullName(doctor.getFullName())
                .primarySpecialization(doctor.getPrimarySpecialization())
                .secondarySpecialization(doctor.getSecondarySpecialization())
                .location(doctor.getLocation())
                .hospitalName(doctor.getHospitalName())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .consultationFee(doctor.getConsultationFee())
                .matchingSlots(slots)
                .build();
    }

    private boolean matchesSpecialization(DoctorProfile doctor, String specializationFilter) {
        if (specializationFilter == null) return true;

        String primary = normalize(doctor.getPrimarySpecialization());
        String secondary = normalize(doctor.getSecondarySpecialization());

        return (primary != null && primary.contains(specializationFilter)) ||
                (secondary != null && secondary.contains(specializationFilter));
    }

    private boolean matchesLocation(DoctorProfile doctor, String locationFilter) {
        if (locationFilter == null) return true;

        String location = normalize(doctor.getLocation());
        String hospital = normalize(doctor.getHospitalName());

        return (location != null && location.contains(locationFilter)) ||
                (hospital != null && hospital.contains(locationFilter));
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private DoctorResponse mapDoctorResponse(DoctorProfile doctor, boolean includeSlots) {
        List<DoctorAvailabilitySlotResponse> slots = includeSlots
                ? slotRepository.findByDoctorIdOrderByStartTimeAsc(doctor.getId())
                .stream()
                .map(this::mapSlotResponse)
                .collect(Collectors.toList())
                : List.of();

        return DoctorResponse.builder()
                .id(doctor.getId())
                .authUserId(doctor.getAuthUserId())
                .fullName(doctor.getFullName())
                .licenseNumber(doctor.getLicenseNumber())
                .primarySpecialization(doctor.getPrimarySpecialization())
                .secondarySpecialization(doctor.getSecondarySpecialization())
                .location(doctor.getLocation())
                .hospitalName(doctor.getHospitalName())
                .bio(doctor.getBio())
                .yearsOfExperience(doctor.getYearsOfExperience())
                .consultationFee(doctor.getConsultationFee())
                .verificationStatus(doctor.getVerificationStatus())
                .verificationRemarks(doctor.getVerificationRemarks())
                .active(doctor.isActive())
                .createdAt(doctor.getCreatedAt())
                .updatedAt(doctor.getUpdatedAt())
                .availabilitySlots(slots)
                .build();
    }

    private DoctorAvailabilitySlotResponse mapSlotResponse(DoctorAvailabilitySlot slot) {
        return DoctorAvailabilitySlotResponse.builder()
                .id(slot.getId())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .available(slot.isAvailable())
                .notes(slot.getNotes())
                .build();
    }
}