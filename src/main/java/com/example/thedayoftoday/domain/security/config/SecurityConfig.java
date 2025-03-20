package com.example.thedayoftoday.domain.security.config;

import com.example.thedayoftoday.domain.security.filter.CustomLogoutFilter;
import com.example.thedayoftoday.domain.security.filter.JWTFilter;
import com.example.thedayoftoday.domain.security.filter.LoginFilter;
import com.example.thedayoftoday.domain.security.repository.RefreshRepository;
import com.example.thedayoftoday.domain.security.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil,
                          RefreshRepository refreshRepository) {

        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/error",
                                "/login",
                                "/signup",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/reissue").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshRepository), LogoutFilter.class);
        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://thedayoftoday.kro.kr"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "access"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}