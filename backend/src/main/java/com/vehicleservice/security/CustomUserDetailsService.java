package com.vehicleservice.security;

import com.vehicleservice.entity.Admin;
import com.vehicleservice.entity.Customer;
import com.vehicleservice.entity.Mechanic;
import com.vehicleservice.entity.Role;
import com.vehicleservice.repository.AdminRepository;
import com.vehicleservice.repository.CustomerRepository;
import com.vehicleservice.repository.MechanicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Looks up a login identity (email) across the three separate user tables:
 * Customer, Mechanic, Admin - since the system does not use one unified user table.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private final MechanicRepository mechanicRepository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return customerRepository.findByEmail(email)
                .map(this::toPrincipal)
                .or(() -> mechanicRepository.findByEmail(email).map(this::toPrincipal))
                .or(() -> adminRepository.findByEmail(email).map(this::toPrincipal))
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));
    }

    private AppUserPrincipal toPrincipal(Customer c) {
        return new AppUserPrincipal(c.getId(), c.getName(), c.getEmail(), c.getPassword(), Role.CUSTOMER);
    }

    private AppUserPrincipal toPrincipal(Mechanic m) {
        return new AppUserPrincipal(m.getId(), m.getName(), m.getEmail(), m.getPassword(), Role.MECHANIC);
    }

    private AppUserPrincipal toPrincipal(Admin a) {
        return new AppUserPrincipal(a.getId(), a.getName(), a.getEmail(), a.getPassword(), Role.ADMIN);
    }
}
