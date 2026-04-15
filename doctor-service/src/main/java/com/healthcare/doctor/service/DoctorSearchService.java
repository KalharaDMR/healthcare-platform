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
                                             String hospital,
                                             Boolean available,
                                             LocalDate date) {

        List<DoctorMeta> doctors = doctorMetaRepository.findAll().stream()
                .filter(meta -> "VERIFIED".equalsIgnoreCase(meta.getVerificationStatus()))
                .filter(meta -> {
                    if (name == null || name.isBlank()) {
                        return true;
                    }

                    String keyword = name.trim().toLowerCase();

                    return (meta.getDoctorName() != null && meta.getDoctorName().toLowerCase().contains(keyword))
                            || (meta.getUsername() != null && meta.getUsername().toLowerCase().contains(keyword));
                })
                .filter(meta -> {
                    if (specialization == null || specialization.isBlank()) {
                        return true;
                    }

                    return meta.getSpecialization() != null
                            && meta.getSpecialization().toLowerCase().contains(specialization.trim().toLowerCase());
                })
                .toList();

        return doctors.stream()
                .map(meta -> {
                    List<AvailabilitySlot> slots = availabilityRepository
                            .findByDoctorUsernameOrderByDateAscStartTimeAsc(meta.getUsername())
                            .stream()
                            .filter(AvailabilitySlot::isAvailable)
                            .filter(slot -> hospital == null || hospital.isBlank()
                                    || (slot.getHospital() != null
                                    && slot.getHospital().toLowerCase().contains(hospital.trim().toLowerCase())))
                            .filter(slot -> date == null || slot.getDate().equals(date))
                            .toList();

                    boolean hasAvailability = !slots.isEmpty();

                    List<String> hospitals = slots.stream()
                            .map(AvailabilitySlot::getHospital)
                            .filter(h -> h != null && !h.isBlank())
                            .distinct()
                            .toList();

                    List<LocalDate> availableDates = slots.stream()
                            .map(AvailabilitySlot::getDate)
                            .distinct()
                            .toList();

                    return new DoctorSearchResponse(
                            meta.getUserId(),
                            meta.getUsername(),
                            meta.getDoctorName(),
                            meta.getSpecialization(),
                            meta.getLocation(),
                            meta.getVerificationStatus(),
                            hasAvailability,
                            hospitals,
                            availableDates
                    );
                })
                .filter(response -> available == null || response.isAvailable() == available)
                .filter(response -> {
                    if ((hospital != null && !hospital.isBlank()) || date != null) {
                        return response.isAvailable();
                    }
                    return true;
                })
                .toList();
    }
}