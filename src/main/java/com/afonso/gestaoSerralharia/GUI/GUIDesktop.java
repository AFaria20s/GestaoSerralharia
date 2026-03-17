package com.afonso.gestaoSerralharia.GUI;

import com.afonso.gestaoSerralharia.GUI.themes.ThemeType;
import com.afonso.gestaoSerralharia.GUI.utils.AppConfig;
import com.afonso.gestaoSerralharia.GUI.windows.LoginDialog;
import com.afonso.gestaoSerralharia.GUI.windows.MainWindow;
import com.afonso.gestaoSerralharia.services.AuthService;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class GUIDesktop {

    public GUIDesktop(AuthService authService) {
        setupLookAndFeel();
        SwingUtilities.invokeLater(() -> iniciar(authService));
    }

    private void iniciar(AuthService authService) {
        LoginDialog login = new LoginDialog(authService);
        login.setVisible(true);

        if (!login.isAutenticado()) {
            System.exit(0);
            return;
        }

        new MainWindow().init();
    }

    private void setupLookAndFeel() {
        try {
            ThemeType theme = AppConfig.getInstance().getTheme();
            if (theme == ThemeType.LIGHT)
                UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}