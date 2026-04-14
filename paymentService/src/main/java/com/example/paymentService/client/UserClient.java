package com.example.paymentService.client;

import com.example.paymentService.dto.DoctorProfileResponse;
import com.example.paymentService.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "auth-service")
public interface UserClient {

    @GetMapping("/internal/getDoctor")
    public ResponseEntity<?> getDoctorProfile(@RequestParam("doctorUserName") String doctorUserName);

    @GetMapping("/internal/users/{username}")
    public UserResponse getUserByUserName(@PathVariable String username);

}
