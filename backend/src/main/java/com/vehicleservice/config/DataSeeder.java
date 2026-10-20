package com.vehicleservice.config;

import com.vehicleservice.entity.Admin;
import com.vehicleservice.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds a default Admin login on first startup:
 *   email:    admin@vehicleservice.com
 *   password: Admin@123
 * (data.sql cannot do this because the password must be BCrypt-hashed by the app.)
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminRepository.findByEmail("admin@vehicleservice.com").isEmpty()) {
            Admin admin = Admin.builder()
                    .name("System Admin")
                    .email("admin@vehicleservice.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .build();
            adminRepository.save(admin);
            System.out.println(">>> Seeded default admin: admin@vehicleservice.com / Admin@123");
        }
    }
}
