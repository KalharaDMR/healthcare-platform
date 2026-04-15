package com.example.paymentService.client;

import com.example.paymentService.dto.AvailabilitySlot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "doctor-service")
public interface DoctorClient {

    @GetMapping("/internal/doctor/slots/{slotId}")
    public ResponseEntity<AvailabilitySlot> getSlot(@PathVariable Long slotId);
}
