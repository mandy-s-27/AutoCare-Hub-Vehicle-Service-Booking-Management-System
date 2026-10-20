package com.vehicleservice.service;

import com.vehicleservice.dto.auth.AuthResponse;
import com.vehicleservice.dto.auth.LoginRequest;
import com.vehicleservice.dto.auth.RegisterRequest;
import com.vehicleservice.entity.*;
import com.vehicleservice.exception.BadRequestException;
import com.vehicleservice.repository.AdminRepository;
import com.vehicleservice.repository.CustomerRepository;
import com.vehicleservice.repository.MechanicRepository;
import com.vehicleservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomerRepository customerRepository;
    private final MechanicRepository mechanicRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    /** Public self-registration is only for Customers; mechanics/admins are created by an Admin. */
    public AuthResponse registerCustomer(RegisterRequest req) {
        if (customerRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        Customer customer = Customer.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .build();
        customer = customerRepository.save(customer);

        String token = jwtUtil.generateToken(customer.getEmail(), customer.getId(), Role.CUSTOMER.name());
        return AuthResponse.builder()
                .token(token).id(customer.getId()).name(customer.getName())
                .email(customer.getEmail()).role(Role.CUSTOMER.name()).build();
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        // Authentication succeeded - look the identity up across the three tables
        // to build the response (id/name/role) and issue the JWT.
        return customerRepository.findByEmail(req.getEmail())
                .<AuthResponse>map(c -> respond(c.getId(), c.getName(), c.getEmail(), Role.CUSTOMER))
                .or(() -> mechanicRepository.findByEmail(req.getEmail())
                        .map(m -> respond(m.getId(), m.getName(), m.getEmail(), Role.MECHANIC)))
                .or(() -> adminRepository.findByEmail(req.getEmail())
                        .map(a -> respond(a.getId(), a.getName(), a.getEmail(), Role.ADMIN)))
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    private AuthResponse respond(Long id, String name, String email, Role role) {
        String token = jwtUtil.generateToken(email, id, role.name());
        return AuthResponse.builder().token(token).id(id).name(name).email(email).role(role.name()).build();
    }
}
