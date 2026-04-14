package com.example.paymentService.client;

import com.example.paymentService.dto.AppointmentCreateRequest;
import com.example.paymentService.dto.AppointmentResponse;
import com.example.paymentService.dto.BookRequest;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "appointment-service")
public interface AppointmentsClient {

    @PostMapping("/appointments")
    AppointmentResponse book(
            @RequestParam("username") String username,
            @RequestBody AppointmentCreateRequest request,
            @RequestParam("isEnableVideo") Boolean isEnableVideo);

    @GetMapping("/appointments/myAppointment")
    AppointmentResponse myAppointment(
            @RequestParam("appointmentId") Long appointmentId);

    @PutMapping("/appointments/my/{appointmentId}/cancel")
    AppointmentResponse cancelMyAppointment(
            @PathVariable Long appointmentId) ;
}
