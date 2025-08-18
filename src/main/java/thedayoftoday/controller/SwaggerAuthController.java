package thedayoftoday.controller;

import thedayoftoday.dto.user.LoginRequestDto;
import thedayoftoday.security.CustomUserDetails;
import thedayoftoday.security.util.JWTUtil;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/swagger-auth")
public class SwaggerAuthController {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public SwaggerAuthController(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDto loginDto) {

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password());

        Authentication authentication = authenticationManager.authenticate(token);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String access = jwtUtil.createAccessToken("access", userDetails.getUsername(), userDetails.getRole(),
                userDetails.getUserId());

        Map<String, String> result = new HashMap<>();
        result.put("accessToken", access);
        return ResponseEntity.ok(result);
    }
}