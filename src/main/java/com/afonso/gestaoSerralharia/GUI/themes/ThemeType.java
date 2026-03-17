package com.afonso.gestaoSerralharia.GUI.themes;

public enum ThemeType {
    LIGHT("Flat Light");

    private final String label;

    ThemeType(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

}
