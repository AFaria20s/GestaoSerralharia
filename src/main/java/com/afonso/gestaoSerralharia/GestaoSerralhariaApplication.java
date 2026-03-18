package com.afonso.gestaoSerralharia;

import com.afonso.gestaoSerralharia.GUI.GUIDesktop;
import com.afonso.gestaoSerralharia.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

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
        SwingUtilities.invokeLater(() -> new GUIDesktop(authService));
    }
}
