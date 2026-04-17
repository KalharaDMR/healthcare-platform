package com.example.paymentService.controller;

import com.example.paymentService.dto.BookRequest;
import com.example.paymentService.dto.PaymentRequest;
import com.example.paymentService.dto.PaymentResponse;
import com.example.paymentService.dto.RefundResponse;
import com.example.paymentService.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Locale;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    private List<String> normalizeRoles(String rolesHeader) {
        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .map(role -> role.toUpperCase(Locale.ROOT))
                .collect(Collectors.toList());
    }

    @PostMapping("/charge")
    public ResponseEntity<?> charge(@Valid @RequestBody BookRequest request,
                                    @RequestHeader("IS-enabled-video") Boolean enableVideo,
                                    @RequestHeader("X-User-Id") String userId,
                                    @RequestHeader("X-User-Role") String rolesHeader) throws Exception {
        List<String> roles = normalizeRoles(rolesHeader);
        if(!roles.contains("PATIENT"))
        {
            return  ResponseEntity.status(HttpStatus.FORBIDDEN).body("The user is not authorized");
        }
        return paymentService.chargePayment(request,userId,enableVideo);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,@RequestHeader("Stripe-Signature") String sigHeader)
    {
        return paymentService.handleWebHook(payload,sigHeader);
    }

    @PostMapping("/refund")
    public ResponseEntity<?> refund(@RequestParam Long slotId,@RequestParam Long AppointmentId,@RequestHeader("X-User-Role") String rolesHeader,
                                    @RequestHeader("X-User-Id") String userName) {
        List<String> roles = normalizeRoles(rolesHeader);
        if(!roles.contains("PATIENT"))
        {
            return  ResponseEntity.status(HttpStatus.FORBIDDEN).body("The user is not authorized");
        }
        return paymentService.patientRefund(slotId,AppointmentId,userName);
    }

    @PostMapping("/doctor-refund")
    public ResponseEntity<?> doctorRefund(@RequestParam Long appointmentId,@RequestParam Long slotId,@RequestHeader("X-User-Role") String rolesHeader,
                                    @RequestHeader("X-User-Id") String userName) {
        List<String> roles = normalizeRoles(rolesHeader);
        if(!roles.contains("DOCTOR"))
        {
            return  ResponseEntity.status(HttpStatus.FORBIDDEN).body("The user is not authorized");
        }
        return paymentService.DoctorRefund(slotId,appointmentId,userName);
    }

}
