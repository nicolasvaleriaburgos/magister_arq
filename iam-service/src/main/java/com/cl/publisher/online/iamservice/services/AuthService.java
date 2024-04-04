package com.cl.publisher.online.iamservice.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.cl.publisher.online.iamservice.dto.request.CredentialsDto;
import com.cl.publisher.online.iamservice.dto.request.SignupRequestDto;
import com.cl.publisher.online.iamservice.dto.response.AuthDto;
import com.cl.publisher.online.iamservice.dto.response.ErrorDto;
import com.cl.publisher.online.iamservice.dto.response.ResponseServicesDto;
import com.cl.publisher.online.iamservice.entities.RefreshToken;
import com.cl.publisher.online.iamservice.entities.Role;
import com.cl.publisher.online.iamservice.entities.User;
import com.cl.publisher.online.iamservice.enums.LevelError;
import com.cl.publisher.online.iamservice.enums.Roles;
import com.cl.publisher.online.iamservice.repositories.RoleRepository;
import com.cl.publisher.online.iamservice.repositories.UserRepository;
import com.cl.publisher.online.iamservice.security.JwtUtils;
import com.cl.publisher.online.iamservice.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired(required = true)
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    RefreshTokenService refreshTokenService;

    public ResponseServicesDto<AuthDto> signIn(CredentialsDto credentialsDto) {
        ArrayList<ErrorDto> errors = new ArrayList<>();
        try {
            log.info("Authentication user {}", credentialsDto.getUsername());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(credentialsDto.getUsername(),
                            credentialsDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwtToken(userDetails);

            List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            log.info("Authentication User Ok");
            return new ResponseServicesDto<>(
                    new AuthDto(jwt, refreshToken.getToken(), userDetails.getId(),
                            userDetails.getUsername(),
                            userDetails.getEmail(), roles),
                    errors, "OK");

        } catch (Exception e) {
            log.error("Error Authentication User: {}", e.getMessage());
            errors.add(
                    new ErrorDto("100", e.getMessage(), LevelError.FATAL,
                            "Invalid Client/Credentials"));
            return new ResponseServicesDto<>(null, errors, "Invalid Client/Credentials");
        }
    }

    public ResponseServicesDto<Object> signup(SignupRequestDto signUpRequest) {
        ArrayList<ErrorDto> errors = new ArrayList<>();
        try {
            if (Boolean.TRUE.equals(userRepository.existsByUsername(signUpRequest.getUsername()))) {
                throw new Exception("Error: Username is already taken!");
            }

            if (Boolean.TRUE.equals(userRepository.existsByEmail(signUpRequest.getEmail()))) {
                throw new Exception("Error: Email is already in use!");
            }

            // Create new user's account
            User user = new User(signUpRequest.getUsername(), signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()));

            Set<String> strRoles = signUpRequest.getRole();
            Set<Role> roles = new HashSet<>();

            if (strRoles == null) {
                Role userRole = roleRepository.findByName(Roles.USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            } else {
                strRoles.forEach(role -> {
                    switch (role) {
                        case "admin":
                            Role adminRole = roleRepository.findByName(Roles.ADMIN)
                                    .orElseThrow(() -> new RuntimeException(
                                            "Error: Role is not found."));
                            roles.add(adminRole);

                            break;
                        default:
                            Role userRole = roleRepository.findByName(Roles.USER)
                                    .orElseThrow(() -> new RuntimeException(
                                            "Error: Role is not found."));
                            roles.add(userRole);
                    }
                });
            }
            user.setRoles(roles);
            userRepository.save(user);
            return new ResponseServicesDto<>(null, errors, "User Created Ok");
        } catch (Exception e) {
            errors.add(new ErrorDto("102", "User Created NOK", LevelError.FATAL, e.getMessage()));
            return new ResponseServicesDto<>(null, errors, "User Created NOk");
        }
    }

}
