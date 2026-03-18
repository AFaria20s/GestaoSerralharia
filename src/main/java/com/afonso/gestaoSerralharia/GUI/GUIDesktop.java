package com.afonso.gestaoSerralharia.GUI;

import com.afonso.gestaoSerralharia.GUI.themes.ThemeType;
import com.afonso.gestaoSerralharia.GUI.utils.AppConfig;
import com.afonso.gestaoSerralharia.GUI.windows.AppFrame;
import com.afonso.gestaoSerralharia.services.AuthService;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class GUIDesktop {

    public GUIDesktop(AuthService authService) {
        setupLookAndFeel();
        new AppFrame(authService);
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
