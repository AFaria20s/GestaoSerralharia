package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.config.SessionManager;
import com.afonso.gestaoSerralharia.desktop.api.AuthApiResponse;
import com.afonso.gestaoSerralharia.desktop.api.DesktopApiSupport;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthService{

    private final DesktopApiSupport api;

    public AuthService(DesktopApiSupport api) {
        this.api = api;
    }
    public boolean login(String email, String password) {
        AuthApiResponse response = api.post("/api/auth/login", Map.of(
                "email", email,
                "password", password
        ), AuthApiResponse.class);

        if (response == null || response.dono() == null) {
            return false;
        }

        SessionManager.getInstance().loginDono(response.dono());
        return true;
    }
}
