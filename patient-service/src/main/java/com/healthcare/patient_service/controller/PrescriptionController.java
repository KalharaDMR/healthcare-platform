package com.healthcare.patient_service.controller;

import com.healthcare.patient_service.dto.CreatePrescriptionRequest;
import com.healthcare.patient_service.dto.PrescriptionResponse;
import com.healthcare.patient_service.entity.Prescription;
import com.healthcare.patient_service.service.PatientService;
import com.healthcare.patient_service.service.PrescriptionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PatientService patientService;

    public PrescriptionController(PrescriptionService prescriptionService,
                                  PatientService patientService) {
        this.prescriptionService = prescriptionService;
        this.patientService = patientService;
    }

    // Create a prescription for a patient
    @PostMapping("/patient/{patientId}")
    public PrescriptionResponse addPrescription(@PathVariable Long patientId,
                                                @RequestBody CreatePrescriptionRequest request) {
        // Verify patient exists
        patientService.getById(patientId);

        Prescription prescription = new Prescription();
        prescription.setPatientId(patientId);
        prescription.setDoctorId(request.getDoctorId());
        prescription.setMedications(request.getMedications());
        prescription.setNotes(request.getNotes());

        Prescription saved = prescriptionService.save(prescription);

        return prescriptionService.toResponse(saved);
    }

    // Get all prescriptions
    @GetMapping
    public List<PrescriptionResponse> getAllPrescriptions() {
        return prescriptionService.toResponseList(prescriptionService.getAll());
    }

    // Get all prescriptions for a specific patient
    @GetMapping("/patient/{patientId}")
    public List<PrescriptionResponse> getPrescriptionsByPatient(@PathVariable Long patientId) {
        // Verify patient exists
        patientService.getById(patientId);

        return prescriptionService.toResponseList(
                prescriptionService.getByPatientId(patientId)
        );
    }

    // Get prescription by ID
    @GetMapping("/{id}")
    public PrescriptionResponse getPrescriptionById(@PathVariable Long id) {
        return prescriptionService.toResponse(prescriptionService.getById(id));
    }
}