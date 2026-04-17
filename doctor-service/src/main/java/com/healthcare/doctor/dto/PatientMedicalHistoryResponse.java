package com.healthcare.doctor.dto;

import java.util.List;

public class PatientMedicalHistoryResponse {
    private Long patientUserId;
    private List<MedicalReportDto> reports;
    private List<PrescriptionDto> prescriptions;

    public Long getPatientUserId() {
        return patientUserId;
    }

    public void setPatientUserId(Long patientUserId) {
        this.patientUserId = patientUserId;
    }

    public List<MedicalReportDto> getReports() {
        return reports;
    }

    public void setReports(List<MedicalReportDto> reports) {
        this.reports = reports;
    }

    public List<PrescriptionDto> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(List<PrescriptionDto> prescriptions) {
        this.prescriptions = prescriptions;
    }
}
