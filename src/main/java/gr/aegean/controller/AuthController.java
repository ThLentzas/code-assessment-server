package gr.aegean.controller;

import gr.aegean.service.auth.CookieService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import gr.aegean.model.auth.AuthRequest;
import gr.aegean.model.auth.PasswordResetConfirmationRequest;
import gr.aegean.model.auth.PasswordResetRequest;
import gr.aegean.model.auth.PasswordResetResponse;
import gr.aegean.model.auth.AuthResponse;
import gr.aegean.model.auth.RegisterRequest;
import gr.aegean.service.auth.AuthService;
import gr.aegean.service.auth.PasswordResetService;

import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;

import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.time.Duration;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final CookieService cookieService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/signup")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody RegisterRequest request,
                                             UriComponentsBuilder uriBuilder) {
        AuthResponse authResponse = authService.registerUser(request);

        URI location = uriBuilder
                .path("/api/v1/users/{userID}")
                .buildAndExpand(authResponse.userId())
                .toUri();
        ResponseCookie cookie = cookieService.createHttpOnlyCookie(authResponse.token());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.LOCATION, location.toString());
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody AuthRequest request) {
        AuthResponse authResponse = authService.authenticateUser(request);
        ResponseCookie cookie = cookieService.createHttpOnlyCookie(authResponse.token());

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());

        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    @PostMapping("/password_reset")
    public ResponseEntity<PasswordResetResponse> resetPassword(@Valid @RequestBody
                                                               PasswordResetRequest passwordResetRequest) {
        PasswordResetResponse passwordResetResponse = passwordResetService.createPasswordResetToken(
                passwordResetRequest);

        return new ResponseEntity<>(passwordResetResponse, HttpStatus.ACCEPTED);
    }

    @GetMapping("/password_reset")
    public ResponseEntity<Void> resetPassword(@PathParam("token") String token) {
        passwordResetService.validatePasswordResetToken(token);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/password_reset/confirm")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody
                                              PasswordResetConfirmationRequest resetConfirmationRequest) {
        passwordResetService.resetPassword(resetConfirmationRequest);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}