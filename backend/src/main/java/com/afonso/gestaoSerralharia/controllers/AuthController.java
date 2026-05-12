package com.afonso.gestaoSerralharia.controllers;

import com.afonso.gestaoSerralharia.config.SessionManager;
import com.afonso.gestaoSerralharia.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        boolean ok = authService.login(email, password);
        if (!ok) return ResponseEntity.status(401).body(Map.of("erro", "Credenciais inválidas"));
        SessionManager sm = SessionManager.getInstance();
        return ResponseEntity.ok(Map.of(
                "dono", sm.getDono(),
                "nome", sm.getNome(),
                "role", sm.getRole(),
                "painelInicial", sm.getPainelInicial()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        SessionManager.getInstance().logout();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessao")
    public ResponseEntity<?> sessao() {
        SessionManager sm = SessionManager.getInstance();
        if (!sm.isAutenticado()) return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado"));
        return ResponseEntity.ok(Map.of(
                "dono", sm.getDono(),
                "nome", sm.getNome(),
                "role", sm.getRole(),
                "painelInicial", sm.getPainelInicial()
        ));
    }
}
