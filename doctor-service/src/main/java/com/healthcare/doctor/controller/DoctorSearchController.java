package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.DoctorSearchResponse;
import com.healthcare.doctor.service.DoctorSearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/doctor")
public class DoctorSearchController {

    private final DoctorSearchService doctorSearchService;

    public DoctorSearchController(DoctorSearchService doctorSearchService) {
        this.doctorSearchService = doctorSearchService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<DoctorSearchResponse>> searchDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) LocalDate date) {

        return ResponseEntity.ok(
                doctorSearchService.search(name, specialization, location, available, date)
        );
    }
}