package com.variopool.dashboard.controller;

import com.variopool.dashboard.common.Result;
import com.variopool.dashboard.config.AuthTokenStore;
import com.variopool.dashboard.config.DashboardProperties;
import com.variopool.dashboard.dto.LoginRequest;
import com.variopool.dashboard.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final DashboardProperties properties;
    private final AuthTokenStore tokenStore;

    public AuthController(DashboardProperties properties, AuthTokenStore tokenStore) {
        this.properties = properties;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        DashboardProperties.Auth auth = properties.getAuth();
        if (!auth.getUsername().equals(request.getUsername()) || !auth.getPassword().equals(request.getPassword())) {
            return Result.fail("用户名或密码错误");
        }
        String token = tokenStore.createToken();
        return Result.success(new LoginResponse(token, request.getUsername()));
    }

    @GetMapping("/user")
    public Result<Map<String, String>> user() {
        return Result.success(Map.of("username", properties.getAuth().getUsername(), "role", "admin"));
    }
}
