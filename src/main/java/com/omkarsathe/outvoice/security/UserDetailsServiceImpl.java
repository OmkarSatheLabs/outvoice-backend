package com.omkarsathe.outvoice.security;

import com.omkarsathe.outvoice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UUID userId = UUID.fromString(username);
        return userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public UserDetails loadUserByUsernameAndPhoneCode(String mobile, UUID phoneCodeId) {
        return userRepository.findByMobileAndPhoneCodeId(mobile, phoneCodeId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + mobile));
    }
}
