package com.tracksure_be.security;

import com.tracksure_be.entity.User;
import com.tracksure_be.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security UserDetails wrapper around the {@link User} entity.
 * Keeps the entity decoupled from the security layer.
 * Loads the role from the User entity and returns it as a GrantedAuthority.
 */
public class UserPrincipal implements UserDetails {

    @Getter
    private final Long userId;
    private final String username;
    private final String password;
    @Getter
    private final String email;
    @Getter
    private final Role role;

    private UserPrincipal(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.password = user.getPasswordHash();
        this.email = user.getEmail();
        this.role = user.getRole() != null ? user.getRole() : Role.CUSTOMER;
    }

    public static UserPrincipal of(User user) {
        return new UserPrincipal(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = "ROLE_" + role.name();
        return List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

