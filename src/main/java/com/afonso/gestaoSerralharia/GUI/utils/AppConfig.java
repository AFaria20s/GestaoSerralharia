package com.afonso.gestaoSerralharia.GUI.utils;

import com.afonso.gestaoSerralharia.GUI.themes.ThemeType;

public class AppConfig {
    private static AppConfig instance;

    private AppConfig() {}

    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private ThemeType theme = ThemeType.LIGHT;

    public void setTheme(ThemeType theme) {
        this.theme = theme;
    }

    public ThemeType getTheme() {
        return this.theme;
    }
}
