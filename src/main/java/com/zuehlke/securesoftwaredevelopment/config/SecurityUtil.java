package com.zuehlke.securesoftwaredevelopment.config;

import com.zuehlke.securesoftwaredevelopment.domain.Role;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import com.zuehlke.securesoftwaredevelopment.repository.RoleRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SecurityUtil {

    private final RoleRepository roleRepository;

    public SecurityUtil(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            return ((User) authentication.getPrincipal()).getUsername();
        }

        return null;
    }

    public static Integer getCurrentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            return ((User) authentication.getPrincipal()).getId();
        }
        return null;
    }

    public  boolean hasPermission(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null) {
            return false;
        }

        List<Role> roles = roleRepository.findByUserId(getCurrentId());

        if(roles.stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            return true;
        }

        if(roles.stream().noneMatch(role -> role.getName().equals("ADMIN"))) {
            return username.equals(getCurrentUsername());
        }
        return false;
    }

    public  boolean hasPermissionCustomerEdition(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null) {
            return false;
        }

        List<Role> roles = roleRepository.findByUserId(getCurrentId());

        if(roles.stream().anyMatch(role -> role.getName().equals("CUSTOMER"))) {
            return true;
        }

        return false;
    }
    public  boolean hasPermissionAdminEdition(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null) {
            return false;
        }

        List<Role> roles = roleRepository.findByUserId(getCurrentId());

        if(roles.stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            return true;
        }

        return false;
    }
}
