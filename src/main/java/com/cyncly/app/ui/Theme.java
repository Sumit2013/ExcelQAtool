package com.cyncly.app.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * Shared visual design tokens used by both FileDropScreen and QAWindow.
 * Edit this one file to retheme the entire application.
 */
public final class Theme {

    private Theme() {}

    // ── Background layers ────────────────────────────────────────────────────
    public static final Color BG_DARK      = new Color(18,  22,  36);
    public static final Color CARD_BG      = new Color(26,  31,  50);
    public static final Color FIELD_BG     = new Color(34,  41,  64);
    public static final Color ROW_ALT      = new Color(22,  27,  44);   // alternating row tint

    // ── Borders ───────────────────────────────────────────────────────────────
    public static final Color BORDER       = new Color(50,  62,  100);
    public static final Color BORDER_FOCUS = new Color(99, 179, 237);

    // ── Accent (blue) ─────────────────────────────────────────────────────────
    public static final Color ACCENT       = new Color(99,  179, 237);
    public static final Color ACCENT_HOVER = new Color(144, 205, 244);
    public static final Color ACCENT_DIM   = new Color(60,  110, 160);

    // ── Secondary button (ghost) ──────────────────────────────────────────────
    public static final Color BTN_GHOST    = new Color(40,  50,  80);
    public static final Color BTN_GHOST_HV = new Color(55,  68, 108);

    // ── Text ──────────────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY = new Color(230, 235, 255);
    public static final Color TEXT_MUTED   = new Color(130, 140, 170);
    public static final Color TEXT_SUCCESS = new Color(100, 220, 140);
    public static final Color TEXT_ERROR   = new Color(255, 110, 110);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final String FONT_FAMILY = "Segoe UI";

    public static Font bold(float size)  { return new Font(FONT_FAMILY, Font.BOLD,  (int) size); }
    public static Font plain(float size) { return new Font(FONT_FAMILY, Font.PLAIN, (int) size); }
}
