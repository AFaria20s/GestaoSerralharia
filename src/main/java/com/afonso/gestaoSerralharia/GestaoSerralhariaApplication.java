package com.afonso.gestaoSerralharia;

import com.afonso.gestaoSerralharia.GUI.GUIDesktop;
import com.afonso.gestaoSerralharia.services.AuthService;
import com.afonso.gestaoSerralharia.services.CargoService;
import com.afonso.gestaoSerralharia.services.FuncionarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.swing.*;

@SpringBootApplication
@RequiredArgsConstructor
public class GestaoSerralhariaApplication implements CommandLineRunner {

    private final AuthService authService;

    public static void main(String[] args) {
        new SpringApplicationBuilder(GestaoSerralhariaApplication.class)
                .headless(false)
                .run(args);
    }

    @Override
    public void run(String[] args) {
        SwingUtilities.invokeLater(() ->
                new GUIDesktop(authService)
        );
    }
}