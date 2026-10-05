package com.taskflow.security;

import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security calls this service to load user data during authentication.
 * It bridges our UserRepository with Spring Security's UserDetailsService interface.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Called by Spring Security's AuthenticationManager during login.
     * @param email the "username" - we use email addresses
     * @return a {@link SecurityUser} wrapping our entity
     * @throws UsernameNotFoundException if no user has that email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new SecurityUser(user);
    }

    /** Used by JwtAuthenticationFilter to load a user by id from the JWT's "sub" claim. */
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        return new SecurityUser(user);
    }
}
