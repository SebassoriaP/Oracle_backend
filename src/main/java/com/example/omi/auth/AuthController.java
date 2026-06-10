package com.example.omi.auth;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.omi.role.RoleRepository;
import com.example.omi.user.UserDto;
import com.example.omi.user.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil,
                      RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return userRepository.findByEmail(req.email())
            .filter(u -> passwordEncoder.matches(req.password(), u.passwordHash()))
            .map(u -> {
                String token = jwtUtil.generate(u.id(), u.email(), u.roleId());
                return ResponseEntity.ok(Map.of(
                    "token", token,
                    "userId", u.id(),
                    "email", u.email(),
                    "name", u.name(),
                    "roleId", u.roleId()
                ));
            })
            .orElse(ResponseEntity.status(401).build());
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestBody RegisterRequest req) {

        if (userRepository.emailExists(req.email())) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error",
                    "Email already exists"
                ));
        }

        if (!roleRepository.exists(req.roleId())) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error",
                    "Role does not exist"
                ));
        }

        if (req.managerId() != null &&
            !userRepository.userExists(req.managerId())) {

            return ResponseEntity.badRequest()
                .body(Map.of(
                    "error",
                    "Manager does not exist"
                ));
        }

        String hash =
            passwordEncoder.encode(req.password());

        userRepository.createFromSignup(
            req.name(),
            req.email(),
            hash,
            req.workMode(),
            req.roleId(),
            req.managerId(),
            req.status(),
            req.chatId()
        );

        UserDto user =
            userRepository.findByEmail(req.email())
                .orElseThrow();

        String token =
            jwtUtil.generate(
                user.id(),
                user.email(),
                user.roleId()
            );

        return ResponseEntity.ok(
            Map.of(
                "token", token,
                "userId", user.id(),
                "email", user.email(),
                "name", user.name(),
                "roleId", user.roleId()
            )
        );
    }

    

    public record LoginRequest(String email, String password) {}
}