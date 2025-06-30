package com.cj.genieq.member.enums;

/**
 * 소셜 로그인 제공업체 열거형
 * 일반 회원가입과 소셜 로그인을 구분하기 위한 enum
 */
public enum SocialProvider {
    
    LOCAL("local", "일반 회원가입"),
    GOOGLE("google", "구글 로그인"),
    GITHUB("github", "깃허브 로그인"),
    NAVER("naver", "네이버 로그인"),
    KAKAO("kakao", "카카오 로그인");
    
    private final String value;
    private final String description;
    
    SocialProvider(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 문자열 값으로 SocialProvider 찾기
     * @param value 소셜 제공업체 문자열 값
     * @return 해당하는 SocialProvider (없으면 LOCAL)
     */
    public static SocialProvider fromValue(String value) {
        if (value == null) {
            return LOCAL;
        }
        
        for (SocialProvider provider : values()) {
            if (provider.getValue().equalsIgnoreCase(value)) {
                return provider;
            }
        }
        
        return LOCAL; // 기본값
    }
    
    /**
     * 일반 회원가입인지 확인
     * @return LOCAL이면 true
     */
    public boolean isLocal() {
        return this == LOCAL;
    }
    
    /**
     * 소셜 로그인인지 확인
     * @return LOCAL이 아니면 true
     */
    public boolean isSocial() {
        return this != LOCAL;
    }
}
