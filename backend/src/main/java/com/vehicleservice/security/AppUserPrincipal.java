package com.vehicleservice.security;

import com.vehicleservice.entity.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * Custom UserDetails implementation carrying the underlying entity id and role,
 * since Customer, Mechanic and Admin are stored in separate tables.
 */
@Getter
public class AppUserPrincipal extends User {

    private final Long id;
    private final String name;
    private final Role role;

    public AppUserPrincipal(Long id, String name, String email, String password, Role role) {
        super(email, password, List.<GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_" + role.name())));
        this.id = id;
        this.name = name;
        this.role = role;
    }
}
