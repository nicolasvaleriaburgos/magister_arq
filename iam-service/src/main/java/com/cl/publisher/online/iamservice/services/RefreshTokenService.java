package com.cl.publisher.online.iamservice.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;

import com.cl.publisher.online.iamservice.dto.request.RefreshTokenRequestDto;
import com.cl.publisher.online.iamservice.dto.response.ErrorDto;
import com.cl.publisher.online.iamservice.dto.response.RefreshTokenResponseDto;
import com.cl.publisher.online.iamservice.dto.response.ResponseServicesDto;
import com.cl.publisher.online.iamservice.entities.RefreshToken;
import com.cl.publisher.online.iamservice.entities.User;
import com.cl.publisher.online.iamservice.enums.LevelError;
import com.cl.publisher.online.iamservice.exceptions.TokenRefreshException;
import com.cl.publisher.online.iamservice.repositories.RefreshTokenRepository;
import com.cl.publisher.online.iamservice.repositories.UserRepository;
import com.cl.publisher.online.iamservice.security.JwtUtils;
import com.cl.publisher.online.iamservice.security.UserDetailsServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RefreshTokenService {
    @Value("${cdc.app.jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    JwtUtils jwtUtils;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) throws NotFoundException {
        RefreshToken refreshToken = new RefreshToken();
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException();
        }
        refreshToken.setUser(user.get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) throws NotFoundException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new NotFoundException();
        }
        return refreshTokenRepository.deleteByUser(user.get());
    }

    public ResponseServicesDto<RefreshTokenResponseDto> refresh(RefreshTokenRequestDto refreshTokenRequestDto) {
        ArrayList<ErrorDto> errors = new ArrayList<>();
        String refresh = refreshTokenRequestDto.getRefreshToken();
        try {
            return this.findByToken(refresh)
                    .map(this::verifyExpiration)
                    .map(RefreshToken::getUser)
                    .map(user -> {
                        String token = jwtUtils.generateTokenFromUsername(user.getUsername());
                        return new ResponseServicesDto<>(
                                new RefreshTokenResponseDto(token, refresh),
                                errors,
                                "Refresh OK");
                    })
                    .orElseThrow(() -> new TokenRefreshException(refresh,
                            "Refresh token is not in database!"));
        } catch (Exception e) {
            errors.add(new ErrorDto("100", e.getMessage(), LevelError.FATAL, "Invalid RefreshToken"));
            return new ResponseServicesDto<>(null, errors, "Invalid RefreshToken");
        }
    }

    public ResponseServicesDto<Object> validate(String token) {
        ArrayList<ErrorDto> errors = new ArrayList<>();
        String jwt = parseJwt(token);
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            String username = jwtUtils.getUserNameFromJwtToken(jwt);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                    null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return new ResponseServicesDto<>(null, errors, "Token Validate Ok");
        }
        errors.add(new ErrorDto("101", "Token Validate Nok", LevelError.FATAL, "Token Invalidate"));
        return new ResponseServicesDto<>(null, errors, "Token Validate Ok");
    }

    public Boolean validateToken(String token) {
        String jwt = parseJwt(token);
        return (jwt != null && jwtUtils.validateJwtToken(jwt));
    }

    private String parseJwt(String token) {
        if (StringUtils.hasText(token)) {
            return token;
        }
        return null;
    }

    @Transactional
    public ResponseServicesDto<Object> deleteToken(Long userId) {
        ArrayList<ErrorDto> errors = new ArrayList<>();
        try {
            this.deleteByUserId(userId);
            return new ResponseServicesDto<>(null, errors, "Logoff Ok");
        } catch (Exception e) {
            log.error("Error Authentication User: {}", e.getMessage());
            errors.add(
                    new ErrorDto("100", e.getMessage(), LevelError.FATAL,
                            "Invalid Token Session"));
            return new ResponseServicesDto<>(null, errors, "Invalid Token Session");
        }

    }

}
