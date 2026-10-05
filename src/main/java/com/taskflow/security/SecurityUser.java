package com.taskflow.security;

import com.taskflow.entity.Role;
import com.taskflow.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * ADAPTER PATTERN: adapts our {@link User} entity to Spring Security's {@link UserDetails} interface.
 *
 * <p>Spring Security doesn't know about our User class. It speaks its own "language" via UserDetails.
 * This class translates between the two. It wraps our entity and delegates calls:
 * <ul>
 *   <li>{@code getUsername()} → {@code user.getEmail()}</li>
 *   <li>{@code getPassword()} → {@code user.getPassword()} (the BCrypt hash)</li>
 *   <li>{@code getAuthorities()} → role prefixed with "ROLE_"</li>
 * </ul>
 *
 * <p>DESIGN PATTERN: ADAPTER (structural).
 * "Convert the interface of a class into another interface that the client expects."
 * Here we adapt User (our interface) → UserDetails (Spring Security's expected interface).
 */
public class SecurityUser implements UserDetails {

    private final User user;

    public SecurityUser(User user) {
        this.user = user;
    }

    /** Spring Security uses this to check role-based access: @PreAuthorize("hasRole('ADMIN')") */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Spring's hasRole('ADMIN') internally looks for "ROLE_ADMIN"
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() { return user.getPassword(); }

    /** Spring Security treats "username" as the login identifier. We use email. */
    @Override
    public String getUsername() { return user.getEmail(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    /** Convenience: get our domain User from the security wrapper. */
    public User getUser() { return user; }

    public Long getUserId() { return user.getId(); }

    public Role getRole() { return user.getRole(); }
}
