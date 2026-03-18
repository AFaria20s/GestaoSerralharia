package com.afonso.gestaoSerralharia.services;

import com.afonso.gestaoSerralharia.config.SessionManager;
import com.afonso.gestaoSerralharia.models.Dono;
import com.afonso.gestaoSerralharia.models.Funcionario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final DonoService donoService;
    private final FuncionarioService funcionarioService;

    public boolean login(String email, String password) {
        Dono dono = donoService.autenticar(email, password);
        if (dono != null) {
            SessionManager.getInstance().loginDono(dono);
            return true;
        }

        Funcionario func = funcionarioService.autenticar(email, password);
        if (func != null) {
            SessionManager.getInstance().loginFuncionario(func);
            return true;
        }

        return false;
    }
}
