package com.creditmodule.ing.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UserUtils {

    public String getCurrentUsername() {
        // Get the Authentication object from the SecurityContext
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // If the principal is a UserDetails object (which it will be if you're using Spring Security's default setup)
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            return userDetails.getUsername();
        }

        // If principal is not a UserDetails object, return null or handle accordingly
        return null;
    }
}
