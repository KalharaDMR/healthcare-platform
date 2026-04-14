package com.healthcare.appointment.service;

import com.healthcare.appointment.client.DoctorServiceClient;
import com.healthcare.appointment.dto.AppointmentCreateRequest;
import com.healthcare.appointment.dto.AppointmentRescheduleRequest;
import com.healthcare.appointment.dto.AppointmentResponse;
import com.healthcare.appointment.dto.DoctorSlotResponse;
import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.entity.AppointmentStatus;
import com.healthcare.appointment.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorServiceClient doctorServiceClient;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              DoctorServiceClient doctorServiceClient) {
        this.appointmentRepository = appointmentRepository;
        this.doctorServiceClient = doctorServiceClient;
    }

    @Transactional
    public AppointmentResponse book(String patientUsername, AppointmentCreateRequest request,Boolean isEnableVideo) {
        Boolean available = doctorServiceClient.validate(request.getSlotId());
        if (!Boolean.TRUE.equals(available)) {
            throw new RuntimeException("Selected slot is not available");
        }

        DoctorSlotResponse slot = doctorServiceClient.getSlot(request.getSlotId());
        if (slot == null) {
            throw new RuntimeException("Slot not found");
        }

        doctorServiceClient.reserve(request.getSlotId());

        try {
            Appointment appointment = new Appointment();
            appointment.setSlotId(slot.getId());
            appointment.setIsVideoConferencingAppointment(isEnableVideo);
            appointment.setPatientUsername(patientUsername);
            appointment.setDoctorUsername(slot.getDoctorUsername());
            appointment.setHospital(slot.getHospital());
            appointment.setAppointmentDate(slot.getDate());
            appointment.setStartTime(slot.getStartTime());
            appointment.setEndTime(slot.getEndTime());
            appointment.setStatus(AppointmentStatus.BOOKED);
            appointment.setNotes(request.getNotes());

            appointment = appointmentRepository.save(appointment);
            return toResponse(appointment);
        } catch (RuntimeException ex) {
            doctorServiceClient.release(request.getSlotId());
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(String patientUsername, AppointmentStatus status) {
        List<Appointment> appointments = status == null
                ? appointmentRepository.findByPatientUsernameOrderByCreatedAtDesc(patientUsername)
                : appointmentRepository.findByPatientUsernameAndStatusOrderByCreatedAtDesc(patientUsername, status);

        return appointments.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(String doctorUsername, AppointmentStatus status) {
        List<Appointment> appointments = status == null
                ? appointmentRepository.findByDoctorUsernameOrderByAppointmentDateAscStartTimeAsc(doctorUsername)
                : appointmentRepository.findByDoctorUsernameAndStatusOrderByAppointmentDateAscStartTimeAsc(doctorUsername, status);

        return appointments.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getMyAppointmentById(Long appointmentId, String patientUsername) {
        Appointment appointment = appointmentRepository.findByIdAndPatientUsername(appointmentId, patientUsername)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse cancelMyAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Appointment already cancelled");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Completed appointment cannot be cancelled");
        }

        doctorServiceClient.release(appointment.getSlotId());
        appointment.setStatus(AppointmentStatus.CANCELLED);

        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse rescheduleMyAppointment(Long appointmentId,
                                                       String patientUsername,
                                                       AppointmentRescheduleRequest request) {

        Appointment appointment = appointmentRepository.findByIdAndPatientUsername(appointmentId, patientUsername)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Cancelled appointment cannot be rescheduled");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new RuntimeException("Completed appointment cannot be rescheduled");
        }

        if (appointment.getSlotId().equals(request.getNewSlotId())) {
            throw new RuntimeException("New slot must be different from current slot");
        }

        Boolean available = doctorServiceClient.validate(request.getNewSlotId());
        if (!Boolean.TRUE.equals(available)) {
            throw new RuntimeException("New slot is not available");
        }

        DoctorSlotResponse newSlot = doctorServiceClient.getSlot(request.getNewSlotId());
        if (newSlot == null) {
            throw new RuntimeException("New slot not found");
        }

        doctorServiceClient.reserve(request.getNewSlotId());

        try {
            Long oldSlotId = appointment.getSlotId();

            appointment.setSlotId(newSlot.getId());
            appointment.setDoctorUsername(newSlot.getDoctorUsername());
            appointment.setHospital(newSlot.getHospital());
            appointment.setAppointmentDate(newSlot.getDate());
            appointment.setStartTime(newSlot.getStartTime());
            appointment.setEndTime(newSlot.getEndTime());
            appointment.setStatus(AppointmentStatus.RESCHEDULED);

            Appointment saved = appointmentRepository.save(appointment);

            doctorServiceClient.release(oldSlotId);

            return toResponse(saved);
        } catch (RuntimeException ex) {
            doctorServiceClient.release(request.getNewSlotId());
            throw ex;
        }
    }

    public AppointmentResponse getAppointment(Long appointmentId)
    {
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow(()->new RuntimeException("Appointment is not found"));
        return  toResponse(appointment);
    }
    @Transactional
    public AppointmentResponse updateStatusForDoctor(Long appointmentId,
                                                     String doctorUsername,
                                                     AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findByIdAndDoctorUsername(appointmentId, doctorUsername)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (status == AppointmentStatus.BOOKED || status == AppointmentStatus.RESCHEDULED) {
            throw new RuntimeException("Doctor cannot manually set this status");
        }

        appointment.setStatus(status);
        return toResponse(appointmentRepository.save(appointment));
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setSlotId(appointment.getSlotId());
        response.setPatientUsername(appointment.getPatientUsername());
        response.setDoctorUsername(appointment.getDoctorUsername());
        response.setHospital(appointment.getHospital());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setStartTime(appointment.getStartTime());
        response.setEndTime(appointment.getEndTime());
        response.setStatus(appointment.getStatus());
        response.setNotes(appointment.getNotes());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        return response;
    }
}