package com.healthcare.patient_service.controller;

import com.healthcare.patient_service.client.AuthClient;
import com.healthcare.patient_service.client.AuthUserResponse;
import com.healthcare.patient_service.dto.*;
import com.healthcare.patient_service.entity.Patient;
import com.healthcare.patient_service.service.MedicalReportService;
import com.healthcare.patient_service.service.PatientService;
import com.healthcare.patient_service.service.PrescriptionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final MedicalReportService reportService;
    private final PrescriptionService prescriptionService;
    private final AuthClient authClient;

    public PatientController(PatientService patientService,
                             MedicalReportService reportService,
                             PrescriptionService prescriptionService,
                             AuthClient authClient) {
        this.patientService = patientService;
        this.reportService = reportService;
        this.prescriptionService = prescriptionService;
        this.authClient = authClient;
    }

    // ----------------- PATIENT -----------------
    @PostMapping
    public PatientResponse createPatient(@RequestBody CreatePatientRequest request) {
        // Verify user exists in auth-service
        if (request.getUserId() != null && !request.getUserId().isEmpty()) {
            AuthUserResponse authUser = authClient.getUserById(request.getUserId());
            if (authUser == null) {
                throw new RuntimeException("User not found in auth-service");
            }
        }

        Patient patient = new Patient();
        patient.setUserId(request.getUserId());
        patient.setFirstName(request.getFirstName());
        patient.setLastName(request.getLastName());
        patient.setEmail(request.getEmail());
        patient.setPhone(request.getPhone());
        patient.setDateOfBirth(request.getDateOfBirth());

        Patient saved = patientService.save(patient);
        return patientService.toResponse(saved);
    }

    @GetMapping
    public List<PatientResponse> getAllPatients() {
        return patientService.toResponseList(patientService.getAll());
    }

    @GetMapping("/{id}")
    public PatientResponse getPatientById(@PathVariable Long id) {
        return patientService.toResponse(patientService.getById(id));
    }

    // ----------------- MEDICAL REPORT -----------------
    @PostMapping("/{id}/upload")
    public MedicalReportResponse uploadMedicalReport(@PathVariable Long id,
                                                     @RequestParam("file") MultipartFile file) {
        try {
            Patient patient = patientService.getById(id); // verify patient exists

            File uploadDir = new File("uploads/patient_" + id);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            File dest = new File(uploadDir, file.getOriginalFilename());
            file.transferTo(dest);

            com.healthcare.patient_service.entity.MedicalReport report =
                    new com.healthcare.patient_service.entity.MedicalReport(
                            null, id, file.getOriginalFilename(), dest.getAbsolutePath()
                    );

            com.healthcare.patient_service.entity.MedicalReport saved = reportService.save(report);
            return reportService.toResponse(saved);

        } catch (Exception e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/reports")
    public List<MedicalReportResponse> getMedicalReports(@PathVariable Long id) {
        patientService.getById(id); // verify patient exists
        return reportService.toResponseList(reportService.getByPatientId(id));
    }

    // ----------------- PRESCRIPTIONS -----------------
    @PostMapping("/{id}/prescriptions")
    public PrescriptionResponse addPrescription(@PathVariable Long id,
                                                @RequestBody CreatePrescriptionRequest request) {
        patientService.getById(id); // verify patient exists

        com.healthcare.patient_service.entity.Prescription prescription =
                new com.healthcare.patient_service.entity.Prescription();
        prescription.setPatientId(id);
        prescription.setDoctorId(request.getDoctorId());
        prescription.setMedications(request.getMedications());
        prescription.setNotes(request.getNotes());

        com.healthcare.patient_service.entity.Prescription saved =
                prescriptionService.save(prescription);

        return prescriptionService.toResponse(saved);
    }

    @GetMapping("/{id}/prescriptions")
    public List<PrescriptionResponse> getPrescriptions(@PathVariable Long id) {
        patientService.getById(id); // verify patient exists
        return prescriptionService.toResponseList(prescriptionService.getByPatientId(id));
    }
}