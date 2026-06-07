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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@DisplayName("AuthService Unit Tests")
@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    // Dependencies
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    // Test Fixtures
    RegisterRequest testRegisterRequest;
    LoginRequest testLoginRequest;
    User sampleUser;

    @BeforeEach
    void setUp() {

        testRegisterRequest = RegisterRequest.builder()
                .username("nour")
                .email("nour@example.com")
                .password("12344")
                .build();

        testLoginRequest = LoginRequest.builder()
                .email("nour@example.com")
                .password("12344")
                .build();

        sampleUser = User.builder()
                .id(1L)
                .username("nour")
                .email("nour@example.com")
                .password("hashed_password")
                .role(Role.USER)
                .build();

    }

    @Nested
    @DisplayName("register() Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should Throw EmailAlreadyExistsException When Email Already Exists")
        void should_throwEmailAlreadyExistsException_whenEmailAlreadyExists() {
            // Given
            String email = "nour@example.com";
            given(userRepository.existsByEmail(email)).willReturn(true);

            // When && Then
            assertThatThrownBy(() -> authService.register(testRegisterRequest))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            then(userRepository).should(times(1)).existsByEmail(email);
            then(userRepository).should(never()).save(any(User.class));
            then(passwordEncoder).should(never()).encode(any());
            then(userRepository).shouldHaveNoMoreInteractions();
            then(passwordEncoder).shouldHaveNoMoreInteractions();
            then(jwtTokenProvider).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("Should Throw UsernameAlreadyExistsException When Username Already Exists")
        void should_throwUsernameAlreadyExistsException_whenUsernameAlreadyExists() {
            // Given
            String email = "nour@example.com";
            String username = "nour";
            given(userRepository.existsByEmail(email)).willReturn(false);
            given(userRepository.existsByUsername(username)).willReturn(true);

            // When && Then
            assertThatThrownBy(() -> authService.register(testRegisterRequest))
                    .isInstanceOf(UsernameAlreadyExistsException.class);

            then(userRepository).should(times(1)).existsByEmail(email);
            then(userRepository).should(times(1)).existsByUsername(username);
            then(userRepository).should(never()).save(any(User.class));
            then(passwordEncoder).should(never()).encode(any());
            then(userRepository).shouldHaveNoMoreInteractions();
            then(passwordEncoder).shouldHaveNoMoreInteractions();
            then(jwtTokenProvider).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("Should Return AuthResponse When Register New User")
        void should_returnAuthResponse_whenRegisterNewUser() {
            // Given
            String email = "nour@example.com";
            String username = "nour";
            Long userId = 1L;
            Role role = Role.USER;

            given(userRepository.existsByEmail(email)).willReturn(false);
            given(userRepository.existsByUsername(username)).willReturn(false);
            given(passwordEncoder.encode("12344")).willReturn("hashed_password");
            given(userRepository.save(any(User.class))).willReturn(sampleUser);
            given(jwtTokenProvider.generateToken(email, userId, role.name())).willReturn("fake-jwt-token");

            // When
            AuthResponse result = authService.register(testRegisterRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("fake-jwt-token");
            assertThat(result.getRole()).isEqualTo(Role.USER.name());
            assertThat(result.getUsername()).isEqualTo("nour");
            assertThat(result.getEmail()).isEqualTo("nour@example.com");
            assertThat(result.getUserId()).isEqualTo(1L);

            then(userRepository).should(times(1)).existsByEmail(email);
            then(userRepository).should(times(1)).existsByUsername(username);
            then(passwordEncoder).should(times(1)).encode("12344");
            then(userRepository).should(times(1)).save(any(User.class));
            then(jwtTokenProvider).should(times(1)).generateToken(email, userId, role.name());

        }
    }

    @Nested
    @DisplayName("Login() Tests")
    class LoginTests {

        @Test
        @DisplayName("Should Return AuthResponse When Credentials Are Valid")
        void should_returnAuthResponse_whenCredentialsAreValid() {
            // Given
            String email = "nour@example.com";
            Long userId = 1L;
            String role = Role.USER.name();
            Authentication mockAuth = mock(Authentication.class);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(mockAuth);
            given(userRepository.findByEmail(email)).willReturn(Optional.of(sampleUser));
            given(jwtTokenProvider.generateToken(email, userId, role)).willReturn("fake-jwt-token");

            // When
            AuthResponse result = authService.login(testLoginRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getRole()).isEqualTo(role);

            then(authenticationManager).should(times(1))
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
            then(userRepository).should(times(1)).findByEmail(email);
            then(jwtTokenProvider).should(times(1)).generateToken(email, userId, role);
            then(authenticationManager).shouldHaveNoMoreInteractions();
            then(userRepository).shouldHaveNoMoreInteractions();
            then(jwtTokenProvider).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("Should Throw InvalidCredentialsException When User Not Found After Authentication")
        void should_throwInvalidCredentialsException_whenUserNotFoundAfterAuthentication() {
            // Given
            String email = "nour@example.com";
            Long userId = 1L;
            String role = Role.USER.name();
            Authentication mockAuth = mock(Authentication.class);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(mockAuth);
            given(userRepository.findByEmail(email)).willReturn(Optional.empty());

            // When && Then
            assertThatThrownBy(() -> authService.login(testLoginRequest))
                    .isInstanceOf(InvalidCredentialsException.class);

            then(authenticationManager).should(times(1))
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
            then(userRepository).should(times(1)).findByEmail(email);
            then(jwtTokenProvider).should(never()).generateToken(any(), any(), any());
            then(authenticationManager).shouldHaveNoMoreInteractions();
            then(userRepository).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("Should Throw InvalidCredentialsException When Password is Wrong")
        void should_throwInvalidCredentialsException_whenPasswordIsWrong() {
            // Given
            String email = "nour@example.com";
            Long userId = 1L;
            String role = Role.USER.name();

            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new BadCredentialsException("Bad Credentials"));

            // When && Then
            assertThatThrownBy(() -> authService.login(testLoginRequest))
                    .isInstanceOf(InvalidCredentialsException.class);

            then(authenticationManager).should(times(1))
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
            then(userRepository).should(never()).findByEmail(any());
            then(jwtTokenProvider).should(never()).generateToken(any(), any(), any());
            then(authenticationManager).shouldHaveNoMoreInteractions();
            then(userRepository).shouldHaveNoMoreInteractions();
            then(jwtTokenProvider).shouldHaveNoMoreInteractions();
        }
    }

}