package thedayoftoday.domain.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import thedayoftoday.domain.auth.dto.SignupRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import thedayoftoday.domain.auth.dto.LoginRequestDto;
import thedayoftoday.domain.user.service.UserService;
import thedayoftoday.domain.auth.security.CustomUserDetails;
import thedayoftoday.domain.auth.security.util.JWTUtil;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserService userService;

    public Long join(SignupRequestDto user) {
        return userService.createUser(user);
    }

    public String login(LoginRequestDto loginDto) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password());

        Authentication authentication = authenticationManager.authenticate(authToken);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return jwtUtil.createAccessToken("access", userDetails.getUsername(), userDetails.getRole(), userDetails.getUserId());
    }
}
