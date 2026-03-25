package com.healthcare.patient_service.controller;

import com.healthcare.patient_service.entity.Patient;
import com.healthcare.patient_service.service.PatientService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService service;

    public PatientController(PatientService service) {
        this.service = service;
    }

    @PostMapping
    public Patient create(@RequestBody Patient patient) {
        return service.save(patient);
    }

    @GetMapping
    public List<Patient> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public Patient getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String path = "uploads/" + file.getOriginalFilename();
            file.transferTo(new java.io.File(path));
            return "Uploaded!";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}