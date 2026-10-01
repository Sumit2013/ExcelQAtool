package com.cyncly.app.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * Shared visual design tokens used by both FileDropScreen and QAWindow.
 * Edit this one file to retheme the entire application.
 */
public final class Theme {

    private Theme() {}

    // ── Purple / seashell design palette ─────────────────────────────────────
    public static final Color PURPLE      = new Color(106, 93, 222);  // #6A5DDE
    public static final Color PURPLE_HV   = new Color(124, 110, 230);
    public static final Color PURPLE_DIM  = new Color(83, 72, 180);
    public static final Color SEA_SHELL   = new Color(255, 244, 236); // #FFF4EC

    // ── Background layers ────────────────────────────────────────────────────
    public static final Color BG_DARK      = SEA_SHELL;
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color FIELD_BG     = new Color(255, 250, 247);
    public static final Color ROW_ALT      = new Color(252, 247, 243);

    // ── Borders ───────────────────────────────────────────────────────────────
    public static final Color BORDER       = new Color(228, 220, 236);
    public static final Color BORDER_FOCUS = PURPLE;

    // ── Accent ────────────────────────────────────────────────────────────────
    public static final Color ACCENT       = PURPLE;
    public static final Color ACCENT_HOVER = PURPLE_HV;
    public static final Color ACCENT_DIM   = PURPLE_DIM;

    // ── Secondary button (ghost) ──────────────────────────────────────────────
    public static final Color BTN_GHOST    = Color.WHITE;
    public static final Color BTN_GHOST_HV = new Color(246, 240, 252);

    // ── Text ──────────────────────────────────────────────────────────────────
    public static final Color TEXT_PRIMARY = new Color(45, 35, 64);
    public static final Color TEXT_MUTED   = new Color(117, 106, 133);
    public static final Color TEXT_SUCCESS = new Color(38, 138, 91);
    public static final Color TEXT_ERROR   = new Color(201, 76, 76);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final String FONT_FAMILY = "Segoe UI";

    public static Font bold(float size)  { return new Font(FONT_FAMILY, Font.BOLD,  (int) size); }
    public static Font plain(float size) { return new Font(FONT_FAMILY, Font.PLAIN, (int) size); }
}
