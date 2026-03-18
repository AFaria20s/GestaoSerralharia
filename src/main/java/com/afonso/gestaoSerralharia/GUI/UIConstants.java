package com.afonso.gestaoSerralharia.GUI;

import java.awt.*;

public final class UIConstants {

    private UIConstants() {}

    // ── Sidebar ───────────────────────────────────────────────────────────────
    public static final int    SIDEBAR_WIDTH        = 200;
    public static final int    SIDEBAR_PAD_H        = 8;  // padding horizontal (esquerda/direita)
    public static final int    SIDEBAR_PAD_V        = 8;   // padding vertical dos botões
    public static final int    SIDEBAR_SECTION_TOP  = 12;  // espaço acima de cada secção
    public static final int    SIDEBAR_SECTION_BOT  = 3;   // espaço abaixo de cada secção
    public static final int    SIDEBAR_LOGO_PAD_V   = 14;  // padding vertical do logo
    public static final int    SIDEBAR_FOOTER_PAD_V = 10;  // padding vertical do footer
    public static final int    SIDEBAR_BTN_HEIGHT   = 36;
    public static final int    SIDEBAR_LOGO_HEIGHT  = 60;
    public static final int    SIDEBAR_FOOTER_HEIGHT= 56;

    // ── Content panels ────────────────────────────────────────────────────────
    public static final int    PANEL_PAD_TOP    = 28;
    public static final int    PANEL_PAD_SIDE   = 32;
    public static final int    PANEL_PAD_BOTTOM = 28;
    public static final int    HEADER_MARGIN_BOTTOM = 20;
    public static final int    TABLE_ROW_HEIGHT = 34;

    // ── Login ─────────────────────────────────────────────────────────────────
    public static final int    LOGIN_WIDTH        = 460;
    public static final int    LOGIN_HEIGHT       = 340;
    public static final int    LOGIN_ACCENT_WIDTH = 120;
    public static final int    LOGIN_FORM_PAD_TOP = 36;
    public static final int    LOGIN_FORM_PAD_H   = 32;
    public static final int    LOGIN_FIELD_GAP    = 10;
    public static final int    LOGIN_BTN_HEIGHT   = 34;

    // ── App window ────────────────────────────────────────────────────────────
    public static final int    APP_MIN_WIDTH  = 960;
    public static final int    APP_MIN_HEIGHT = 600;

    // ── Font sizes ────────────────────────────────────────────────────────────
    public static final float  FONT_PANEL_TITLE   = 20f;
    public static final float  FONT_PANEL_SUB     = 12f;
    public static final float  FONT_SIDEBAR_LOGO  = 14f;
    public static final float  FONT_SIDEBAR_ROLE  = 11f;
    public static final float  FONT_SIDEBAR_BTN   = 13f;
    public static final float  FONT_SIDEBAR_SEC   = 10f;
    public static final float  FONT_SIDEBAR_FOOT  = 12f;
    public static final float  FONT_SMALL         = 11f;
    public static final float  FONT_FIELD_LABEL   = 12f;
    public static final float  FONT_LOGIN_TITLE   = 20f;

    // ── Colors – role accents ─────────────────────────────────────────────────
    public static final Color  COLOR_ADMIN_ACCENT      = new Color(59, 130, 246);
    public static final Color  COLOR_ADMIN_HOVER_BG    = new Color(239, 246, 255);
    public static final Color  COLOR_ADMIN_ACTIVE_FG   = new Color(29, 78, 216);

    public static final Color  COLOR_FUNC_ACCENT       = new Color(22, 163, 74);
    public static final Color  COLOR_FUNC_HOVER_BG     = new Color(240, 253, 244);
    public static final Color  COLOR_FUNC_ACTIVE_FG    = new Color(21, 128, 61);

    // ── Colors – semantic ─────────────────────────────────────────────────────
    public static final Color  COLOR_DANGER   = new Color(220, 38, 38);
    public static final Color  COLOR_WARNING  = new Color(217, 119, 6);
    public static final Color  COLOR_SUCCESS  = new Color(22, 163, 74);
    public static final Color  COLOR_INFO     = new Color(37, 99, 235);

    // ── Colors – login accent panel ───────────────────────────────────────────
    public static final Color  COLOR_LOGIN_ACCENT_BG   = new Color(30, 41, 59);
    public static final Color  COLOR_LOGIN_ACCENT_ICON = new Color(148, 163, 184);

    // ── Search field default width ────────────────────────────────────────────
    public static final int    SEARCH_FIELD_WIDTH = 220;
    public static final int    SEARCH_FIELD_HEIGHT = 30;
}
