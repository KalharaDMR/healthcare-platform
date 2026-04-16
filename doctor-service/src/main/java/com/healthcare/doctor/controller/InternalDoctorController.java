package com.healthcare.doctor.controller;

import com.healthcare.doctor.entity.AvailabilitySlot;
import com.healthcare.doctor.repository.AvailabilityRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/doctor")
public class InternalDoctorController {

    private final AvailabilityRepository repository;

    public InternalDoctorController(AvailabilityRepository repository) {
        this.repository = repository;
    }

    //  Check if slot is available
    @PostMapping("/slots/validate/{slotId}")
    public ResponseEntity<Boolean> validate(@PathVariable Long slotId) {

        AvailabilitySlot slot = repository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        return ResponseEntity.ok(slot.isAvailable());
    }

    //  Reserve slot (booking)
    @PostMapping("/slots/reserve/{slotId}")
    public ResponseEntity<String> reserve(@PathVariable Long slotId) {

        AvailabilitySlot slot = repository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (!slot.isAvailable()) {
            throw new RuntimeException("Slot already booked");
        }

        slot.setAvailable(false);
        repository.save(slot);

        return ResponseEntity.ok("Slot reserved");
    }

    //  Release slot (cancel)
    @PostMapping("/slots/release/{slotId}")
    public ResponseEntity<String> release(@PathVariable Long slotId) {

        AvailabilitySlot slot = repository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        slot.setAvailable(true);
        repository.save(slot);

        return ResponseEntity.ok("Slot released");
    }

    @GetMapping("/slots/{slotId}")
    public ResponseEntity<AvailabilitySlot> getSlot(@PathVariable Long slotId) {
        AvailabilitySlot slot = repository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));
        return ResponseEntity.ok(slot);
    }

}