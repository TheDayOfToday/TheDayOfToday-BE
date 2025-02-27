package com.example.thedayoftoday.domain.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.thedayoftoday.domain.dto.LoginRequestDto;
import com.example.thedayoftoday.domain.entity.User;
import com.example.thedayoftoday.domain.repository.UserRepository;
import com.example.thedayoftoday.domain.security.service.AuthService;
import com.example.thedayoftoday.domain.security.util.JWTUtil;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {


}