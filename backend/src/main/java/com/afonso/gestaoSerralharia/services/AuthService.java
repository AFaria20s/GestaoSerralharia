package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.config.SessionManager;
import com.afonso.gestaoSerralharia.models.Dono;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final DonoService donoService;

    public boolean login(String email, String password) {
        Dono dono = donoService.autenticar(email, password);
        if (dono != null) {
            SessionManager.getInstance().loginDono(dono);
            return true;
        }

        return false;
    }
}
