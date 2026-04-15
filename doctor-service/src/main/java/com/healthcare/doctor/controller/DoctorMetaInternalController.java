package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.DoctorMetaUpsertRequest;
import com.healthcare.doctor.entity.DoctorMeta;
import com.healthcare.doctor.service.DoctorMetaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/doctor/meta")
public class DoctorMetaInternalController {

    private final DoctorMetaService doctorMetaService;

    public DoctorMetaInternalController(DoctorMetaService doctorMetaService) {
        this.doctorMetaService = doctorMetaService;
    }

    @PostMapping("/sync")
    public ResponseEntity<DoctorMeta> syncDoctorMeta(@Valid @RequestBody DoctorMetaUpsertRequest request) {
        return ResponseEntity.ok(doctorMetaService.upsert(request));
    }
}