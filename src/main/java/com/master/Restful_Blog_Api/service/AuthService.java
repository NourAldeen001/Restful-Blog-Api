package com.master.Restful_Blog_Api.service;

import com.master.Restful_Blog_Api.dto.AuthResponse;
import com.master.Restful_Blog_Api.dto.LoginRequest;
import com.master.Restful_Blog_Api.dto.RegisterRequest;
import com.master.Restful_Blog_Api.entity.Role;
import com.master.Restful_Blog_Api.entity.User;
import com.master.Restful_Blog_Api.exception.EmailAlreadyExistsException;
import com.master.Restful_Blog_Api.exception.InvalidCredentialsException;
import com.master.Restful_Blog_Api.exception.UsernameAlreadyExistsException;
import com.master.Restful_Blog_Api.repository.UserRepository;
import com.master.Restful_Blog_Api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getRole().name()
        );

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();

    }
    // authenticationManager.authenticate -> DaoAuthenticationProvider.authenticate
    public AuthResponse login(LoginRequest request) {
        try {
            // Calls CustomUserDetailsService.loadUserByUsername(email)
            // Then get UserDetails Then Compares password with stored
            // fail throw BadCredentialsException
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException());

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(
                    user.getEmail(),
                    user.getId(),
                    user.getRole().name()
            );

            return AuthResponse.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();
        }
        catch(BadCredentialsException ex) {
            throw new InvalidCredentialsException();
        }

    }


}
