package com.cj.genieq.common.auth;

import com.cj.genieq.member.entity.MemberEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Spring Security 인증 정보 유틸리티 클래스
 * 현재 인증된 사용자 정보를 편리하게 조회
 */
public class AuthenticationUtil {

    /**
     * 현재 인증된 사용자의 MemberEntity 조회
     * @return 현재 인증된 사용자 (인증되지 않은 경우 null)
     */
    public static MemberEntity getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof MemberEntity) {
            return (MemberEntity) authentication.getPrincipal();
        }
        
        return null;
    }

    /**
     * 현재 인증된 사용자의 memCode 조회
     * @return 사용자 ID (인증되지 않은 경우 null)
     */
    public static Long getCurrentMemCode() {
        MemberEntity member = getCurrentMember();
        return member != null ? member.getMemCode() : null;
    }

    /**
     * 현재 인증된 사용자의 이메일 조회
     * @return 사용자 이메일 (인증되지 않은 경우 null)
     */
    public static String getCurrentMemEmail() {
        MemberEntity member = getCurrentMember();
        return member != null ? member.getMemEmail() : null;
    }

    /**
     * 현재 사용자가 인증되었는지 확인
     * @return 인증되었으면 true
     */
    public static boolean isAuthenticated() {
        return getCurrentMember() != null;
    }

    /**
     * 현재 사용자가 특정 권한을 가지고 있는지 확인
     * @param role 확인할 권한 (예: "ROLE_ADMIN")
     * @return 권한이 있으면 true
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals(role));
        }
        
        return false;
    }

    /**
     * 현재 사용자가 관리자 권한을 가지고 있는지 확인
     * @return 관리자 권한이 있으면 true
     */
    public static boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
}
