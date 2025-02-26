package com.example.thedayoftoday.domain.security;

import com.example.thedayoftoday.domain.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
public class CustomUserDetails implements UserDetails {

    private final String email;
    private final String password;
    private final String role;

    // 기존 생성자 (User 객체를 받음)
    public CustomUserDetails(User user) {
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.role = user.getRole().toString();
    }

    // ✅ 새로 추가: JWT 인증 시 email, role만 사용할 수 있도록!
    public CustomUserDetails(String email, String role) {
        this.email = email;
        this.password = null; // JWT 인증에는 비밀번호가 필요 없음
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return password; // JWT 인증에서는 사용되지 않음
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}