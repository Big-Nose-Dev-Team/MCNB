package com.yichenxbohan.mcnb.client.gui;

public record UITheme(
        int bg, int border, int headerBg,
        int titleText, int labelText, int valueText,
        int btnBg, int btnHover, int btnDisabled
) {
    public static final UITheme DEFAULT = new UITheme(
            0xD0181C24, 0xFF3A4A6B, 0xCC1A2540,
            0xFFE8D87A, 0xFFAABBCC, 0xFFEEEEEE,
            0xCC1C2030, 0xCC2A3A60, 0xCC232323
    );
}