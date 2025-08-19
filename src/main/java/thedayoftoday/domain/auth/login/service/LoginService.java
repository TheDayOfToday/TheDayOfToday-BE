package thedayoftoday.domain.auth.login.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import thedayoftoday.domain.auth.login.dto.LoginRequestDto;
import thedayoftoday.security.CustomUserDetails;
import thedayoftoday.security.util.JWTUtil;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public String login(LoginRequestDto loginDto) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password());

        Authentication authentication = authenticationManager.authenticate(authToken);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return jwtUtil.createAccessToken("access", userDetails.getUsername(), userDetails.getRole(),
                userDetails.getUserId());
    }
}
