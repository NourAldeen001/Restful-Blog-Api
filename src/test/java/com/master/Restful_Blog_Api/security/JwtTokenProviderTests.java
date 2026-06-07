package com.master.Restful_Blog_Api.security;

import com.master.Restful_Blog_Api.config.JwtConfig;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTests {

    private JwtTokenProvider jwtTokenProvider;

    // Test Constants
    private static final String TEST_SECRET = "test-secret-key-is-at-least-32-chars-long!";
    private static final Long EXPIRATION_MS = 3_600_000L; // 1 hour
    private static final Long EXPIRED_MS = -1000L;

    private static final String TEST_EMAIL = "nour@example.com";
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_ROLE = "USER";

    // Helper Builder Method
    private JwtTokenProvider buildProvider(long expirationMs) {
        JwtConfig jwtConfig = new JwtConfig();
        try {
            var secretField = JwtConfig.class.getDeclaredField("jwtSecret");
            var expirationField = JwtConfig.class.getDeclaredField("jwtExpiration");
            secretField.setAccessible(true);
            expirationField.setAccessible(true);
            secretField.set(jwtConfig, TEST_SECRET);
            expirationField.set(jwtConfig, expirationMs);
        }
        catch (Exception ex) {
            throw new RuntimeException("Failed to configure JwtConfig for test", ex);
        }
        jwtTokenProvider = new JwtTokenProvider(jwtConfig);
        jwtTokenProvider.init();
        return jwtTokenProvider;
    }

    @BeforeEach
    void setUp() {
        jwtTokenProvider = buildProvider(EXPIRATION_MS);
    }

    @Nested
    @DisplayName("generateToken() Tests")
    class GenerateToken {

        @Test
        @DisplayName("Should Return Non-Blank Token When Called With Valid Args")
        void should_returnNonBlankToken_whenCalledWithValidArgs() {
            // When
            String token = jwtTokenProvider.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);

            // Then
            assertThat(token)
                    .isNotNull()
                    .isNotBlank()
                    .contains(".");
        }

        @Test
        @DisplayName("Should Return Different Tokens For Different Users")
        void should_returnDifferentTokens_whenCalledByDifferentUsers() {
            // When
            String token1 = jwtTokenProvider.generateToken("nour@example.com", 1L, "USER");
            String token2 = jwtTokenProvider.generateToken("hossam@example.com", 2L, "USER");

            // Then
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("getEmailFromToken() Tests")
    class GetEmailFromToken {

        @Test
        @DisplayName("Should Return Correct Email From Valid Token")
        void should_returnEmail_whenTokenIsValid() {
            // Given
            String token = jwtTokenProvider.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);

            // When
            String email = jwtTokenProvider.getEmailFromToken(token);

            // Then
            assertThat(email).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("Should Throw JwtException When Token Is Malformed")
        void should_throwJwtException_whenTokenIsMalformed() {
            // When && Then
            assertThatThrownBy(() -> jwtTokenProvider.getEmailFromToken("this.is.not.a.jwt"))
                    .isInstanceOf(MalformedJwtException.class);
        }

        @Test
        @DisplayName("Should Throw JwtException When Token Is Tampered")
        void should_throwJwtException_whenTokenIsTampered() {

            String validToken = jwtTokenProvider.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);
            String tamperedToken = validToken.substring(0, validToken.lastIndexOf(".") + 1) + "invalidSignatureXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

            // When && Then
            assertThatThrownBy(() -> jwtTokenProvider.getEmailFromToken(tamperedToken))
                    .isInstanceOf(SignatureException.class);
        }
    }

    @Nested
    @DisplayName("getUserIdFromToken() Tests")
    class GetUserIdFromToken {

        @Test
        @DisplayName("Should Return Correct UserId From Valid Token")
        void should_returnUserId_whenTokenIsValid() {
            // Given
            String token = jwtTokenProvider.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);

            // When
            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            // Then
            assertThat(userId).isEqualTo(TEST_USER_ID);
        }
    }

    @Nested
    @DisplayName("getRoleFromToken() Tests")
    class GetRoleFromToken {

        @Test
        @DisplayName("Should Return Correct User Role From Valid Token")
        void should_returnUserRole_whenTokenIsValid() {
            // Given
            String token = jwtTokenProvider.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);

            // When
            String userRole = jwtTokenProvider.getRoleFromToken(token);

            // Then
            assertThat(userRole)
                    .isEqualTo("USER")
                    .isNotEqualTo("ADMIN");
        }
    }

    @Nested
    @DisplayName("getExpirationDateFromToken() Tests")
    class GetExpirationDateFromToken {

        @Test
        @DisplayName("Should Return Expiration Date In Future For Fresh Token")
        void should_returnFutureDate_whenTokenIsFresh() {
            // Given
            String token = jwtTokenProvider.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);

            // When
            Date expiration = jwtTokenProvider.getExpirationDateFromToken(token);

            // Then
            assertThat(expiration)
                    .isAfter(new Date());
        }


        @Test
        @DisplayName("Should Expire In One Hour From Now")
        void should_expireInOneHour_whenDefaultProviderProvided() {
            // Given
            long before = System.currentTimeMillis();
            String token = jwtTokenProvider.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);
            long after = System.currentTimeMillis();

            // When
            Date expiration = jwtTokenProvider.getExpirationDateFromToken(token);

            // Then
            assertThat(expiration.getTime())
                    .isGreaterThanOrEqualTo(before + EXPIRATION_MS - 5000)
                    .isLessThanOrEqualTo(after + EXPIRATION_MS + 5000);
        }
    }

    @Nested
    @DisplayName("parseToken() Tests")
    class ParseTokenTests {

        @Test
        @DisplayName("Should Return TokenData With All Three Fields Correctly When Token Is Valid")
        void should_returnTokenDataWithAllFields_whenTokenIsValid() {
            // Given
            String token = jwtTokenProvider.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);

            // When
            JwtTokenProvider.TokenData data = jwtTokenProvider.parseToken(token);

            // Then
            assertThat(data)
                    .extracting(
                            JwtTokenProvider.TokenData::email,
                            JwtTokenProvider.TokenData::userId,
                            JwtTokenProvider.TokenData::role
                    )
                    .containsExactly(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);
        }
    }

    @Nested
    @DisplayName("validateToken() Tests")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should Return True When Token Is Valid And Email Matches")
        void should_returnTrue_whenTokenIsValidAndEmailMatches() {
            // Given
            String token = jwtTokenProvider.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);
            String email = jwtTokenProvider.getEmailFromToken(token);

            // When
            Boolean result = jwtTokenProvider.validateToken(token, email);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should Return False When Email Dismatches")
        void should_returnFalse_whenEmailDismatches() {
            // Given
            String token = jwtTokenProvider.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);

            // When
            Boolean result = jwtTokenProvider.validateToken(token, "hossam@exmaple.com");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should Return False When Token Expired")
        void should_returnFalse_whenTokenExpired() {
            // Given
            JwtTokenProvider expiredProvider = buildProvider(EXPIRED_MS);
            String expiredToken = expiredProvider.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);

            // When
            Boolean result = jwtTokenProvider.validateToken(expiredToken, TEST_EMAIL);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should Return False When Token Signature Tampered")
        void should_returnFalse_whenTokenSignatureTampered() {
            // Given
            String validToken = jwtTokenProvider.generateToken(TEST_EMAIL, TEST_USER_ID, TEST_ROLE);
            String tamperedToken = validToken.substring(0, validToken.lastIndexOf(".") + 1) + "dcvfvfvfsvdfdfbdgdfdgfffff";
            // When
            Boolean result = jwtTokenProvider.validateToken(tamperedToken, TEST_EMAIL);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should Return False When Token Completely Malformed")
        void should_returnFalse_whenTokenCompletelyMalformed() {
            // Given
            // When
            Boolean result = jwtTokenProvider.validateToken("not.a.real.jwt.at.all", TEST_EMAIL);

            // Then
            assertThat(result).isFalse();
        }
    }

}