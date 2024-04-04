package com.cl.publisher.online.iamservice.controllers;

import jakarta.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.cl.publisher.online.iamservice.dto.request.CredentialsDto;
import com.cl.publisher.online.iamservice.dto.request.LogoutRequestDto;
import com.cl.publisher.online.iamservice.dto.request.RefreshTokenRequestDto;
import com.cl.publisher.online.iamservice.dto.request.SignupRequestDto;
import com.cl.publisher.online.iamservice.dto.response.AuthDto;
import com.cl.publisher.online.iamservice.dto.response.RefreshTokenResponseDto;
import com.cl.publisher.online.iamservice.dto.response.ResponseServicesDto;
import com.cl.publisher.online.iamservice.services.AuthService;
import com.cl.publisher.online.iamservice.services.RefreshTokenService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Slf4j
public class AuthController {

    private String xsrfToken = "XSRF-TOKEN";
    private String xsrfTokenRefresh = "XSRF-TOKEN-REFRESH";

    @Autowired
    private AuthService authService;
    @Autowired
    private RefreshTokenService refreshTokenService;

    /**
     * It signs in the user.
     * 
     * @param credentialsDto The credentialsDto is the object that contains the
     *                       username and password.
     * @return The response is a ResponseServicesDto object.
     */
    @PostMapping("/signIn")
    public ResponseEntity<ResponseServicesDto<AuthDto>> signIn(@Valid @RequestBody CredentialsDto credentialsDto,
            HttpServletResponse servletResponse) {
        log.info("Trying to login {}", credentialsDto.getUsername());
        ResponseServicesDto<AuthDto> response = authService.signIn(credentialsDto);
        if (response.getErrors().isEmpty()) {
            log.info("login successfull");
            ResponseCookie cookieJwt = ResponseCookie.from(this.xsrfToken, response.getData().getToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(1 * 24 * 60 * 60)
                    .build();
            ResponseCookie cookieRefresh = ResponseCookie
                    .from(this.xsrfTokenRefresh, response.getData().getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();
            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, cookieJwt.toString())
                    .header(HttpHeaders.SET_COOKIE, cookieRefresh.toString())
                    .body(response);
        } else {
            log.info("error trying login");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * It refreshes the token.
     * 
     * @param request The request object that contains the refresh token.
     * @return The response is a ResponseServicesDto object.
     */
    @PostMapping("/refreshtoken")
    public ResponseEntity<ResponseServicesDto<RefreshTokenResponseDto>> refreshtoken(
            @Valid @RequestBody RefreshTokenRequestDto request,
            HttpServletResponse servletResponse) {
        ResponseServicesDto<RefreshTokenResponseDto> response = refreshTokenService.refresh(request);
        if (response.getErrors().isEmpty()) {
            log.info("refresh token successfull");
            ResponseCookie cookieJwt = ResponseCookie.from(this.xsrfToken, response.getData().getAccessToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(1 * 24 * 60 * 60)
                    .build();
            ResponseCookie cookieRefresh = ResponseCookie
                    .from(this.xsrfTokenRefresh, response.getData().getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.SET_COOKIE, cookieJwt.toString())
                    .header(HttpHeaders.SET_COOKIE2, cookieRefresh.toString())
                    .body(response);
        } else {
            log.info("refresh token login");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * This function is used to validate the token
     * 
     * @param token The token that you want to validate.
     * @return The response is a ResponseServicesDto object.
     */
    @GetMapping("/validateToken")
    public ResponseEntity<ResponseServicesDto<Object>> validateToken(
            @RequestHeader(name = "X-XSRF-TOKEN") String token) {
        log.info("Access Token: {}", token);
        ResponseServicesDto<Object> response = refreshTokenService.validate(token);
        if (response.getErrors().isEmpty()) {
            log.info("validate token successfull");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            log.info("error trying validate token");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * This function is used to logout the user
     * 
     * @param token         The token that was sent in the request header.
     * @param logOutRequest The request body is a LogoutRequestDto object.
     * @return The response is a ResponseServicesDto object.
     */
    @PostMapping("/signOut")
    public ResponseEntity<ResponseServicesDto<Object>> logout(
            @RequestHeader(name = "X-XSRF-TOKEN") String token,
            @Valid @RequestBody LogoutRequestDto logOutRequest) {
        log.info("Request {}", logOutRequest.toString());
        if (Boolean.TRUE.equals(refreshTokenService.validateToken(token))) {
            ResponseServicesDto<Object> response = refreshTokenService.deleteToken(logOutRequest.getUserId());
            if (response.getErrors().isEmpty()) {
                log.info("close session successfull");
                ResponseCookie cookieJwt = ResponseCookie.from(this.xsrfToken, "")
                        .build();
                ResponseCookie cookieRefresh = ResponseCookie.from(this.xsrfTokenRefresh, "")
                        .build();
                return ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.SET_COOKIE, cookieJwt.toString())
                        .header(HttpHeaders.SET_COOKIE2, cookieRefresh.toString())
                        .body(response);
            } else {
                log.info("error trying close session");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    /**
     * It creates a new user.
     * 
     * @param token         The token that was sent in the request header.
     * @param signUpRequest The request body that contains the user's information.
     * @return ResponseEntity<ResponseServicesDto>
     */
    @PostMapping("/signup")
    public ResponseEntity<ResponseServicesDto<Object>> signup(
            @RequestHeader(name = "X-XSRF-TOKEN") String token,
            @Valid @RequestBody SignupRequestDto signUpRequest) {
        log.info("{}", signUpRequest.getPassword());
        log.info("{}", signUpRequest.getUsername());
        log.info("{}", signUpRequest.getEmail());
        ResponseServicesDto<Object> response = authService.signup(signUpRequest);
        if (response.getErrors().isEmpty()) {
            log.info("create user successfull");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            log.info("error trying create user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}