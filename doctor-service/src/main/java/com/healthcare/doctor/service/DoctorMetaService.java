package com.healthcare.doctor.service;

import com.healthcare.doctor.dto.DoctorMetaUpsertRequest;
import com.healthcare.doctor.entity.DoctorMeta;
import com.healthcare.doctor.repository.DoctorMetaRepository;
import org.springframework.stereotype.Service;

@Service
public class DoctorMetaService {

    private final DoctorMetaRepository doctorMetaRepository;

    public DoctorMetaService(DoctorMetaRepository doctorMetaRepository) {
        this.doctorMetaRepository = doctorMetaRepository;
    }

    public DoctorMeta upsert(DoctorMetaUpsertRequest request) {
        DoctorMeta meta = doctorMetaRepository.findById(request.getUserId())
                .orElseGet(DoctorMeta::new);

        meta.setUserId(request.getUserId());
        meta.setUsername(request.getUsername().trim());
        meta.setDoctorName(request.getDoctorName().trim());
        meta.setSpecialization(request.getSpecialization().trim());
        meta.setLocation(request.getLocation().trim());
        meta.setVerificationStatus(request.getVerificationStatus().trim().toUpperCase());

        return doctorMetaRepository.save(meta);
    }

    public DoctorMeta getByUsername(String username) {
        return doctorMetaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor metadata not found"));
    }
}