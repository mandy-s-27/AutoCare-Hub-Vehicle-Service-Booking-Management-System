package com.vehicleservice.util;

import com.vehicleservice.security.AppUserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    private SecurityUtils() {}

    public static AppUserPrincipal currentUser() {
        return (AppUserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }
}
