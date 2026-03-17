package com.afonso.gestaoSerralharia.GUI.utils;

import com.afonso.gestaoSerralharia.GUI.themes.ThemeType;

public class AppConfig {
    private static AppConfig instance;

    /** Construtor privado para impedir a instanciação externa. */
    private AppConfig() {}

    /**
     * Obtem a instancia unica da configuracao.
     * Se nao existir, cria uma nova (Lazy Initialization).
     * @return A instancia global de AppConfig.
     */
    public static AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    // --- Configs ---
    private ThemeType theme = ThemeType.LIGHT;

    public void setTheme(ThemeType theme) {
        this.theme = theme;
    }

    public ThemeType getTheme() {
        return this.theme;
    }
}
