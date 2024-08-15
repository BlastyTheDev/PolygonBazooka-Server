package me.blasty.polygonbazookaserver.api.security.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.blasty.polygonbazookaserver.api.security.LoginRequest;
import me.blasty.polygonbazookaserver.api.security.RegisterRequest;
import me.blasty.polygonbazookaserver.api.security.jwt.JWTService;
import me.blasty.polygonbazookaserver.api.security.user.User;
import me.blasty.polygonbazookaserver.api.security.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public void login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        var user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
        var jwt = jwtService.createToken(user);
        var tokenCookie = new Cookie("token", jwt);
        tokenCookie.setAttribute("SameSite", "Strict");
        response.addCookie(tokenCookie);
    }

    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest registerRequest, HttpServletResponse response) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return;
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return;
        }

        var user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .created(new Date(System.currentTimeMillis()))
                .timePlayed(0L)
                .build();

        userRepository.save(user);

        response.setStatus(HttpServletResponse.SC_CREATED);
        
        var tokenCookie = new Cookie("token", jwtService.createToken(user));
        tokenCookie.setAttribute("SameSite", "Strict");
        response.addCookie(tokenCookie);
    }

}
