package com.healthcare.doctor.client;

import com.healthcare.doctor.dto.CreatePrescriptionPayload;
import com.healthcare.doctor.dto.MedicalReportDto;
import com.healthcare.doctor.dto.PrescriptionDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "patient-service")
public interface PatientClinicalClient {
    @PostMapping("/api/prescriptions")
    PrescriptionDto createPrescription(@RequestBody CreatePrescriptionPayload payload,
                                       @RequestHeader("X-INTERNAL-KEY") String internalKey);

    @GetMapping("/api/prescriptions/doctor/user/{userId}")
    List<PrescriptionDto> getPrescriptionsByUserForDoctor(@PathVariable("userId") Long userId,
                                                           @RequestHeader("X-INTERNAL-KEY") String internalKey);

    @GetMapping("/api/medical-reports/doctor/user/{userId}")
    List<MedicalReportDto> getReportsByUserForDoctor(@PathVariable("userId") Long userId,
                                                      @RequestHeader("X-INTERNAL-KEY") String internalKey);
}
