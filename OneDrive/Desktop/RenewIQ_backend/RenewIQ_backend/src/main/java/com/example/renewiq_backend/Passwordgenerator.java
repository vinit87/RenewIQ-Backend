package com.example.renewiq_backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Passwordgenerator {
    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "123456";
        String hash = encoder.encode(password);

        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
    }
}