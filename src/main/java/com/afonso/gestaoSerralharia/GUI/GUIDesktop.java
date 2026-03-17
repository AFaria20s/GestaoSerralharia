package com.afonso.gestaoSerralharia.GUI;

import com.afonso.gestaoSerralharia.GUI.themes.ThemeType;
import com.afonso.gestaoSerralharia.GUI.utils.AppConfig;
import com.afonso.gestaoSerralharia.GUI.windows.MainWindow;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class GUIDesktop {
    public GUIDesktop() {
        FlatLightLaf.setup();
        setupLookAndFeel();

        MainWindow mainWindow = new MainWindow();
        mainWindow.init();
    }

    private void setupLookAndFeel() {
        try {
            ThemeType theme = AppConfig.getInstance().getTheme();
            switch (theme) {
                case LIGHT -> UIManager.setLookAndFeel(new FlatLightLaf());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
