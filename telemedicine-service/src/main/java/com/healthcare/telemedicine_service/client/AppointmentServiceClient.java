package com.healthcare.telemedicine_service.client;
import com.healthcare.telemedicine_service.dto.AppointmentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "appointment-service")
public interface AppointmentServiceClient {

    @GetMapping("/internal/appointments/{id}")
    AppointmentResponse getAppointmentById(@PathVariable("id") Long id,
                                           @RequestHeader("X-INTERNAL-KEY") String internalKey);
}