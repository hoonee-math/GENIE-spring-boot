package com.cj.genieq.common.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtTokenProvider 단위 테스트
 * 태스크 2 검증 기준에 따른 토큰 생성 및 검증 테스트
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    
    // 테스트용 데이터
    private final String TEST_SECRET = "TestSecretKeyForJwtTokenProviderUnitTestMustBeAtLeast64Characters!@#$%";
    private final long ACCESS_TOKEN_VALIDITY = 3600000L; // 1시간
    private final long REFRESH_TOKEN_VALIDITY = 604800000L; // 7일
    
    private final Long TEST_MEM_CODE = 12345L;
    private final String TEST_MEM_EMAIL = "test@genieq.com";
    private final String TEST_ROLE = "ROLE_USER";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
            TEST_SECRET,
            ACCESS_TOKEN_VALIDITY,
            REFRESH_TOKEN_VALIDITY
        );
    }

    @Test
    @DisplayName("JWT 토큰 제공자 초기화 테스트")
    void testJwtTokenProviderInitialization() {
        // Given & When
        JwtTokenProvider provider = new JwtTokenProvider(
            TEST_SECRET,
            ACCESS_TOKEN_VALIDITY,
            REFRESH_TOKEN_VALIDITY
        );
        
        // Then
        assertNotNull(provider);
    }

    @Test
    @DisplayName("비밀키 길이 검증 테스트 - 64자 미만 시 예외 발생")
    void testSecretKeyValidation() {
        // Given
        String shortSecret = "TooShortSecret";
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            new JwtTokenProvider(shortSecret, ACCESS_TOKEN_VALIDITY, REFRESH_TOKEN_VALIDITY);
        });
    }

    @Test
    @DisplayName("액세스 토큰 생성 테스트")
    void testCreateAccessToken() {
        // When
        String accessToken = jwtTokenProvider.createAccessToken(TEST_MEM_CODE, TEST_ROLE);
        
        // Then
        assertNotNull(accessToken, "액세스 토큰이 null이면 안됩니다");
        assertFalse(accessToken.isEmpty(), "액세스 토큰이 비어있으면 안됩니다");
        assertTrue(accessToken.startsWith("eyJ"), "JWT 토큰은 'eyJ'로 시작해야 합니다");
        
        // 토큰에 점(.)이 3개 있는지 확인 (header.payload.signature)
        long dotCount = accessToken.chars().filter(ch -> ch == '.').count();
        assertEquals(2, dotCount, "JWT 토큰은 2개의 점(.)을 포함해야 합니다");
    }

    @Test
    @DisplayName("리프레시 토큰 생성 테스트")
    void testCreateRefreshToken() {
        // When
        String refreshToken = jwtTokenProvider.createRefreshToken(TEST_MEM_CODE);
        
        // Then
        assertNotNull(refreshToken, "리프레시 토큰이 null이면 안됩니다");
        assertFalse(refreshToken.isEmpty(), "리프레시 토큰이 비어있으면 안됩니다");
        assertTrue(refreshToken.startsWith("eyJ"), "JWT 토큰은 'eyJ'로 시작해야 합니다");
    }

    @Test
    @DisplayName("토큰 검증 테스트 - 유효한 토큰")
    void testValidateToken_ValidToken() {
        // Given
        String accessToken = jwtTokenProvider.createAccessToken(TEST_MEM_CODE, TEST_ROLE);
        
        // When
        boolean isValid = jwtTokenProvider.validateToken(accessToken);
        
        // Then
        assertTrue(isValid, "유효한 토큰은 검증을 통과해야 합니다");
    }

    @Test
    @DisplayName("토큰 검증 테스트 - 유효하지 않은 토큰")
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.jwt.token";
        
        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);
        
        // Then
        assertFalse(isValid, "유효하지 않은 토큰은 검증을 실패해야 합니다");
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출 테스트")
    void testGetMemberIdFromToken() {
        // Given
        String accessToken = jwtTokenProvider.createAccessToken(TEST_MEM_CODE, TEST_ROLE);
        
        // When
        Long extractedMemCode = jwtTokenProvider.getMemberIdFromToken(accessToken);
        
        // Then
        assertEquals(TEST_MEM_CODE, extractedMemCode, "토큰에서 추출한 사용자 ID가 일치해야 합니다");
    }

    @Test
    @DisplayName("토큰에서 권한 추출 테스트")
    void testGetRoleFromToken() {
        // Given
        String accessToken = jwtTokenProvider.createAccessToken(TEST_MEM_CODE, TEST_ROLE);
        
        // When
        String extractedRole = jwtTokenProvider.getRoleFromToken(accessToken);
        
        // Then
        assertEquals(TEST_ROLE, extractedRole, "토큰에서 추출한 권한이 일치해야 합니다");
    }

    @Test
    @DisplayName("토큰 만료 시간 확인 테스트")
    void testGetExpirationDateFromToken() {
        // Given
        String accessToken = jwtTokenProvider.createAccessToken(TEST_MEM_CODE, TEST_ROLE);
        
        // When
        var expirationDate = jwtTokenProvider.getExpirationDateFromToken(accessToken);
        
        // Then
        assertNotNull(expirationDate, "만료 시간이 null이면 안됩니다");
        assertTrue(expirationDate.after(new java.util.Date()), "만료 시간은 현재 시간보다 이후여야 합니다");
    }

    @Test
    @DisplayName("토큰 만료 여부 확인 테스트")
    void testIsTokenExpired() {
        // Given
        String accessToken = jwtTokenProvider.createAccessToken(TEST_MEM_CODE, TEST_ROLE);
        
        // When
        boolean isExpired = jwtTokenProvider.isTokenExpired(accessToken);
        
        // Then
        assertFalse(isExpired, "새로 생성된 토큰은 만료되지 않아야 합니다");
    }

    @Test
    @DisplayName("토큰 타입 확인 테스트")
    void testGetTokenType() {
        // Given
        String accessToken = jwtTokenProvider.createAccessToken(TEST_MEM_CODE, TEST_ROLE);
        String refreshToken = jwtTokenProvider.createRefreshToken(TEST_MEM_CODE);
        
        // When
        String accessTokenType = jwtTokenProvider.getTokenType(accessToken);
        String refreshTokenType = jwtTokenProvider.getTokenType(refreshToken);
        
        // Then
        assertEquals("access", accessTokenType, "액세스 토큰 타입이 'access'여야 합니다");
        assertEquals("refresh", refreshTokenType, "리프레시 토큰 타입이 'refresh'여야 합니다");
    }

    @Test
    @DisplayName("완전한 토큰 생성 및 검증 플로우 테스트")
    void testCompleteTokenFlow() {
        // Given
        Long memCode = 999L;
        String memEmail = "user@genieq.com";
        String role = "ROLE_ADMIN";
        
        // When - 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(memCode, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(memCode);
        
        // Then - 토큰 검증
        assertTrue(jwtTokenProvider.validateToken(accessToken), "액세스 토큰이 유효해야 합니다");
        assertTrue(jwtTokenProvider.validateToken(refreshToken), "리프레시 토큰이 유효해야 합니다");
        
        // 토큰에서 정보 추출 및 검증
        assertEquals(memCode, jwtTokenProvider.getMemberIdFromToken(accessToken));
        assertEquals(role, jwtTokenProvider.getRoleFromToken(accessToken));
        assertEquals(memCode, jwtTokenProvider.getMemberIdFromToken(refreshToken));
        
        // 토큰 타입 확인
        assertEquals("access", jwtTokenProvider.getTokenType(accessToken));
        assertEquals("refresh", jwtTokenProvider.getTokenType(refreshToken));
    }
}
