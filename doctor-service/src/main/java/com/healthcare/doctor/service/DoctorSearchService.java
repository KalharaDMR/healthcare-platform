package com.healthcare.doctor.service;

import com.healthcare.doctor.dto.DoctorSearchResponse;
import com.healthcare.doctor.entity.AvailabilitySlot;
import com.healthcare.doctor.entity.DoctorMeta;
import com.healthcare.doctor.repository.AvailabilityRepository;
import com.healthcare.doctor.repository.DoctorMetaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DoctorSearchService {

    private final DoctorMetaRepository doctorMetaRepository;
    private final AvailabilityRepository availabilityRepository;

    public DoctorSearchService(DoctorMetaRepository doctorMetaRepository,
                               AvailabilityRepository availabilityRepository) {
        this.doctorMetaRepository = doctorMetaRepository;
        this.availabilityRepository = availabilityRepository;
    }

    public List<DoctorSearchResponse> search(String name,
                                             String specialization,
                                             String location,
                                             Boolean available,
                                             LocalDate date) {

        List<DoctorMeta> doctors = doctorMetaRepository.findAll().stream()
                .filter(meta -> "VERIFIED".equalsIgnoreCase(meta.getVerificationStatus()))
                .filter(meta -> name == null || name.isBlank()
                        || meta.getDoctorName().toLowerCase().contains(name.trim().toLowerCase()))
                .filter(meta -> specialization == null || specialization.isBlank()
                        || meta.getSpecialization().equalsIgnoreCase(specialization.trim()))
                .filter(meta -> location == null || location.isBlank()
                        || meta.getLocation().equalsIgnoreCase(location.trim()))
                .toList();

        return doctors.stream()
                .map(meta -> {
                    boolean hasAvailability = hasAvailability(meta.getUsername(), date);
                    return new DoctorSearchResponse(
                            meta.getUserId(),
                            meta.getUsername(),
                            meta.getDoctorName(),
                            meta.getSpecialization(),
                            meta.getLocation(),
                            meta.getVerificationStatus(),
                            hasAvailability
                    );
                })
                .filter(response -> available == null || response.isAvailable() == available)
                .toList();
    }

    private boolean hasAvailability(String doctorUsername, LocalDate date) {
        List<AvailabilitySlot> slots =
                availabilityRepository.findByDoctorUsernameOrderByDateAscStartTimeAsc(doctorUsername);

        return slots.stream()
                .filter(AvailabilitySlot::isAvailable)
                .anyMatch(slot -> date == null || slot.getDate().equals(date));
    }
}