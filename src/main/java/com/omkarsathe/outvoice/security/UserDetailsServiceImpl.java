package com.omkarsathe.outvoice.security;

import com.omkarsathe.outvoice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Used by Spring Security's AuthenticationManager; JwtAuthFilter no longer queries the DB per request. */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmailOrMobileNumber(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
