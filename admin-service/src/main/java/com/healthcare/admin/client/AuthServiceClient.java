package com.healthcare.admin.client;
import com.healthcare.admin.dto.RoleChangeRequest;
import com.healthcare.admin.dto.UpdateUserRequest;
import com.healthcare.admin.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "auth-service")
public interface AuthServiceClient {

    @GetMapping("/internal/users")
    List<UserResponse> getAllUsers(@RequestHeader("X-INTERNAL-KEY") String apiKey);

    @GetMapping("/internal/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long id,
                             @RequestHeader("X-INTERNAL-KEY") String apiKey);

    @PutMapping("/internal/users/{id}")
    UserResponse updateUser(@PathVariable("id") Long id,
                            @RequestBody UpdateUserRequest request,
                            @RequestHeader("X-INTERNAL-KEY") String apiKey);

    @DeleteMapping("/internal/users/{id}")
    void deleteUser(@PathVariable("id") Long id,
                    @RequestHeader("X-INTERNAL-KEY") String apiKey);


    @PutMapping("/internal/users/{id}/approve")
    void approveDoctor(@PathVariable("id") Long id,
                       @RequestHeader("X-INTERNAL-KEY") String apiKey);
}