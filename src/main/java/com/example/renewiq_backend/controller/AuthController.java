package com.example.renewiq_backend.controller;

import com.example.renewiq_backend.dto.LoginRequest;
import com.example.renewiq_backend.entity.User;
import com.example.renewiq_backend.repository.UserRepository;
import com.example.renewiq_backend.dto.*;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404)
                    .body(Map.of("message", "User not found"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("businessId", user.getBusinessId());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Invalid credentials"));
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "User is inactive"));
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            return ResponseEntity.status(401)
                    .body(Map.of("message", "Invalid credentials"));
        }

        Map<String, Object> response = new HashMap<>();

        response.put("message", "Login successful");
        response.put("userId", user.getUserId());
        response.put("businessId", user.getBusinessId());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }


    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {

        // Basic validation
        if (request.getBusinessName() == null || request.getBusinessName().isBlank()
                || request.getFullName() == null || request.getFullName().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().length() < 6) {

            return ResponseEntity.badRequest()
                    .body(Map.of("message", "All fields are required. Password must be at least 6 characters"));
        }

        // Email already exists?
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(409)
                    .body(Map.of("message", "Email already registered"));
        }

        // create new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword())); // BCrypt hash
        user.setRole("ADMIN");
        user.setIsActive(true);
        // user.setBusinessId(...);  // niche dekho

        User saved = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Account created successfully");
        response.put("userId", saved.getUserId());
        response.put("businessId", saved.getBusinessId());
        response.put("fullName", saved.getFullName());
        response.put("email", saved.getEmail());
        response.put("role", saved.getRole());

        return ResponseEntity.status(201).body(response);
    }
}