package Polyakov.Bank.Card.Management.Systems.controller.impl;

import Polyakov.Bank.Card.Management.Systems.controller.AuthController;
import Polyakov.Bank.Card.Management.Systems.exception.TokenRefreshException;
import Polyakov.Bank.Card.Management.Systems.model.dto.request.LoginRequest;
import Polyakov.Bank.Card.Management.Systems.model.dto.request.RegisterRequest;
import Polyakov.Bank.Card.Management.Systems.model.dto.request.TokenRefreshRequest;
import Polyakov.Bank.Card.Management.Systems.model.dto.response.JwtResponse;
import Polyakov.Bank.Card.Management.Systems.model.dto.response.TokenRefreshResponse;
import Polyakov.Bank.Card.Management.Systems.model.entity.RefreshToken;
import Polyakov.Bank.Card.Management.Systems.model.entity.User;
import Polyakov.Bank.Card.Management.Systems.security.SecurityUserDetails;
import Polyakov.Bank.Card.Management.Systems.service.AuthService;
import Polyakov.Bank.Card.Management.Systems.service.RefreshTokenService;
import Polyakov.Bank.Card.Management.Systems.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AuthControllerImpl implements AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Override
    public ResponseEntity<JwtResponse> authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        SecurityUserDetails userDetails = (SecurityUserDetails) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        return ResponseEntity.ok(new JwtResponse(jwt, refreshToken.getToken(), userDetails.getUsername()));
    }

    @Override
    public ResponseEntity<?> refreshToken(TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();
        RefreshToken refreshToken = refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration) // Проверяет и бросает исключение, если истек
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token is not in database!"));
        User tokenUser = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateToken(new SecurityUserDetails(tokenUser)); // Создаем UserDetails для генерации

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(tokenUser.getId());
        return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, newRefreshToken.getToken()));
    }

    @Override
    public ResponseEntity<?> logoutUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }
        SecurityUserDetails userDetails = (SecurityUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getId();
        refreshTokenService.deleteByUserId(userId);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("User logged out successfully!");
    }

    @Override
    public ResponseEntity<?> registerUser(RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully!");
    }
}
