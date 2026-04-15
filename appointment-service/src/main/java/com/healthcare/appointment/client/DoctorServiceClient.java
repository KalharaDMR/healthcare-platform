package com.healthcare.appointment.client;

import com.healthcare.appointment.dto.DoctorSlotResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "doctor-service")
public interface DoctorServiceClient {

    @GetMapping("/internal/doctor/slots/{slotId}")
    DoctorSlotResponse getSlot(@PathVariable("slotId") Long slotId);

    @PostMapping("/internal/doctor/slots/validate/{slotId}")
    Boolean validate(@PathVariable("slotId") Long slotId);

    @PostMapping("/internal/doctor/slots/reserve/{slotId}")
    String reserve(@PathVariable("slotId") Long slotId);

    @PostMapping("/internal/doctor/slots/release/{slotId}")
    String release(@PathVariable("slotId") Long slotId);
}