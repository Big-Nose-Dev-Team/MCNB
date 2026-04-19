package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.client.KeyBindings;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import com.yichenxbohan.mcnb.skill.api.SkillDefinition;
import com.yichenxbohan.mcnb.skill.api.SkillRegistry;
import com.yichenxbohan.mcnb.skill.network.SkillPackets;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SkillClassScreen extends Screen {

    private static final ResourceLocation BG_TEX = ResourceLocation.fromNamespaceAndPath("mcnb", "textures/gui/skill_class_gui.png");

    private static final int DESIGN_W = 1088;
    private static final int DESIGN_H = 991;
    private static final int TOP_SAFE = 10;
    private static final int BOTTOM_SAFE = 48;

    private static final int C_ROW_HOVER = 0x44745D3A;
    private static final int C_ROW_SELECTED = 0x4D9D7F52;
    private static final int C_TEXT = 0xFFE8E1D2;
    private static final int C_MUTED = 0xFFD0C4AE;

    private static final int LIST_X = 46;
    private static final int LIST_Y = 96;
    private static final int LIST_W = 1002;
    private static final int LIST_H = 325;
    private static final int BOTTOM_BAR_Y = 888;
    private static final int HOTKEY_Y = 892;
    private static final int LIST_TO_BOTTOM_GAP = 18;

    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private float textScale = 1.0f;
    private int layoutX;
    private int layoutY;

    private final List<SkillDefinition> skills = new ArrayList<>();
    private final List<RowLayout> rows = new ArrayList<>();

    private Rect listRect;
    private Rect leftActionRect;
    private Rect backRect;
    private final List<Rect> hotkeyRects = new ArrayList<>();

    private int scrollOffset;
    private int maxScroll;
    private int selectedIndex = -1;

    public SkillClassScreen() {
        super(Component.literal("技能"));
    }

    @Override
    protected void init() {
        int sideSafe = Math.max(8, width / 80);
        int topSafe = Math.max(TOP_SAFE, height / 80);
        int bottomSafe = Math.max(BOTTOM_SAFE, height / 20);
        int availH = height - topSafe - bottomSafe;
        int availW = width - sideSafe * 2;

        int panelW = Math.max(320, availW);
        int panelH = Math.max(260, availH);
        layoutX = (width - panelW) / 2;
        layoutY = topSafe + (availH - panelH) / 2;

        scaleX = panelW / (float) DESIGN_W;
        scaleY = panelH / (float) DESIGN_H;
        textScale = Math.max(0.72f, Math.min(1.08f, Math.min(scaleX, scaleY)));

        int listY = uy(LIST_Y);
        int bottomBarY = uy(BOTTOM_BAR_Y);
        int listH = Math.max(uh(LIST_H), bottomBarY - listY - uh(LIST_TO_BOTTOM_GAP));

        listRect = new Rect(ux(LIST_X), listY, uw(LIST_W), listH);
        leftActionRect = new Rect(ux(48), bottomBarY, uw(208), uh(82));
        backRect = new Rect(ux(884), uy(26), uw(164), uh(52));

        hotkeyRects.clear();
        int hx = ux(272);
        int hy = uy(HOTKEY_Y);
        int hw = uw(108);
        int gap = uw(12);
        for (int i = 0; i < 7; i++) {
            hotkeyRects.add(new Rect(hx + i * (hw + gap), hy, hw, uh(98)));
        }

        rebuildSkills();
    }

    private void rebuildSkills() {
        skills.clear();
        rows.clear();

        PlayerClass clazz = PlayerClass.NONE;
        var player = Minecraft.getInstance().player;
        if (player != null) {
            clazz = player.getCapability(ModCapabilities.PLAYER_CLASS)
                .map(cap -> cap.getPlayerClass())
                .orElse(PlayerClass.NONE);
        }

        skills.addAll(SkillRegistry.getSkills(clazz));

        int scaledLineHeight = Math.max(1, Math.round(font.lineHeight * textScale));
        int rowH = Math.max(uh(98), scaledLineHeight * 2 + uh(40));
        int gap = Math.max(uh(12), scaledLineHeight / 2);
        int leftSlotW = uw(98);
        int scrollW = uw(92);
        int rightW = uw(182);
        int woodX = listRect.x + leftSlotW + uw(12);
        int woodW = listRect.w - leftSlotW - scrollW - rightW - uw(36);
        for (int i = 0; i < skills.size(); i++) {
            int y = listRect.y + i * (rowH + gap);
            Rect icon = new Rect(listRect.x, y, leftSlotW, rowH);
            Rect info = new Rect(woodX, y, woodW, rowH);
            Rect scroll = new Rect(info.x + info.w + uw(8), y, scrollW, rowH);
            Rect upgrade = new Rect(scroll.x + scroll.w + uw(8), y, rightW, rowH);
            rows.add(new RowLayout(icon, info, scroll, upgrade));
        }

        int contentH = skills.size() * (rowH + gap);
        maxScroll = Math.max(0, contentH - listRect.h + 6);
        scrollOffset = Math.min(scrollOffset, maxScroll);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x93000000);
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(layoutX, layoutY, 0);
        pose.scale(scaleX, scaleY, 1.0f);
        // Draw the full source texture at design size, then scale once via matrix.
        g.blit(BG_TEX, 0, 0, 0, 0, DESIGN_W, DESIGN_H, DESIGN_W, DESIGN_H);
        pose.popPose();
        drawPanel(g, mx, my);
        drawList(g, mx, my);
        drawBottomBar(g, mx, my);
        drawTooltip(g, mx, my);
        super.render(g, mx, my, pt);
    }

    private void drawPanel(GuiGraphics g, int mx, int my) {
        drawHoverOverlay(g, backRect, backRect.contains(mx, my), 0x339E8A66);
        String title = "一轉-" + getPlayerClassName() + "之路";
        drawScaledCenteredString(g, title, ux(446), uy(36), C_TEXT, textScale);
        drawScaledCenteredString(g, "技能點: " + getAvailableSkillPoints(), ux(726), uy(36), C_TEXT, textScale);
        drawScaledCenteredString(g, "返回", backRect.x + backRect.w / 2, backRect.y + uh(18), C_TEXT, textScale);
    }

    private void drawList(GuiGraphics g, int mx, int my) {
        g.enableScissor(listRect.x, listRect.y, listRect.x + listRect.w, listRect.y + listRect.h);
        for (int i = 0; i < rows.size(); i++) {
            RowLayout row = rows.get(i);
            int yOffset = -scrollOffset;

            Rect icon = row.icon.offset(0, yOffset);
            Rect info = row.info.offset(0, yOffset);
            Rect scroll = row.scroll.offset(0, yOffset);
            Rect upgrade = row.upgrade.offset(0, yOffset);

            if (!isFullyInsideList(info)) {
                continue;
            }

            SkillDefinition skill = skills.get(i);
            boolean hoverInfo = info.contains(mx, my);
            boolean hoverScroll = scroll.contains(mx, my);
            boolean hoverUpgrade = upgrade.contains(mx, my);
            boolean selected = i == selectedIndex;

            int level = getSkillLevel(skill.getId());
            boolean canUpgrade = getAvailableSkillPoints() > 0 && level < skill.getMaxLevel();

            drawRowBlock(g, icon, info, scroll, upgrade, skill, level, hoverInfo, hoverScroll, hoverUpgrade, selected, canUpgrade);
        }
        g.disableScissor();
    }

    private void drawRowBlock(
        GuiGraphics g,
        Rect icon,
        Rect info,
        Rect scroll,
        Rect upgrade,
        SkillDefinition skill,
        int level,
        boolean hoverInfo,
        boolean hoverScroll,
        boolean hoverUpgrade,
        boolean selected,
        boolean canUpgrade
    ) {
        int textColor = selected ? 0xFFFFF3D8 : C_TEXT;
        int detailColor = selected ? 0xFFF2E6D0 : C_MUTED;

        drawHoverOverlay(g, icon, selected || hoverInfo, selected ? C_ROW_SELECTED : C_ROW_HOVER);
        drawHoverOverlay(g, info, hoverInfo, selected ? C_ROW_SELECTED : C_ROW_HOVER);
        drawHoverOverlay(g, scroll, hoverScroll, C_ROW_HOVER);
        drawHoverOverlay(g, upgrade, hoverUpgrade, C_ROW_HOVER);

        String iconText = skill.getDisplayName().isEmpty() ? "?" : skill.getDisplayName().substring(0, 1);
        drawScaledCenteredString(g, iconText, icon.x + icon.w / 2, icon.y + uh(42), 0xFFEFE4CC, textScale);

        int textX = info.x + uw(14);
        String levelText = "Lv." + level + "/" + skill.getMaxLevel();
        int contentWidth = Math.max(20, Math.round((info.w - uw(28)) / textScale));
        int maxNameWidth = Math.max(20, contentWidth - font.width("  " + levelText));
        String skillName = trimToWidth(skill.getDisplayName(), maxNameWidth);
        drawScaledString(g, skillName + "  " + levelText, textX, info.y + uh(28), textColor, textScale);
        drawScaledString(g, "類型: " + (skill.getCategory() == null ? "未知" : skill.getCategory().name().toLowerCase()), textX, info.y + uh(52), detailColor, textScale);

        drawScaledCenteredString(g, "卷", scroll.x + scroll.w / 2, scroll.y + uh(43), 0xFFE6D4B5, textScale);
        drawScaledCenteredString(g, canUpgrade ? "升級 +" : "已滿", upgrade.x + upgrade.w / 2, upgrade.y + uh(43), canUpgrade ? 0xFFE7E2D7 : 0xFF8F8680, textScale);
    }

    private void drawBottomBar(GuiGraphics g, int mx, int my) {
        boolean castHover = leftActionRect.contains(mx, my);
        drawHoverOverlay(g, leftActionRect, castHover, 0x338B6A44);
        drawScaledCenteredString(g, "施放技能", leftActionRect.x + leftActionRect.w / 2, leftActionRect.y + uh(32), C_TEXT, textScale);
        drawScaledCenteredString(g, "右鍵重置", leftActionRect.x + leftActionRect.w / 2, leftActionRect.y + uh(52), 0xFFBEAF95, textScale);

        String[] labels = getHotkeyLabels();
        for (int i = 0; i < hotkeyRects.size(); i++) {
            Rect r = hotkeyRects.get(i);
            boolean hover = r.contains(mx, my);
            drawHoverOverlay(g, r, hover, 0x33A18858);
            drawScaledCenteredString(g, labels[i], r.x + r.w / 2, r.y - uh(10), C_TEXT, textScale);
            drawScaledCenteredString(g, "+", r.x + r.w / 2, r.y + uh(42), 0xFF2B221A, textScale);
        }
    }

    private void drawTooltip(GuiGraphics g, int mx, int my) {
        for (int i = 0; i < rows.size(); i++) {
            RowLayout row = rows.get(i);
            Rect info = row.info.offset(0, -scrollOffset);
            if (!isFullyInsideList(info) || !info.contains(mx, my)) {
                continue;
            }

            SkillDefinition skill = skills.get(i);
            int level = getSkillLevel(skill.getId());
            int nextLv = Math.min(skill.getMaxLevel(), Math.max(1, level + 1));

            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(skill.getDisplayName()));
            lines.add(Component.literal("職業: " + skill.getOwnerClassDisplayName()));
            lines.add(Component.literal(skill.getDescription()));
            lines.add(Component.literal("類型: " + skill.getCategory()));
            lines.add(Component.literal("施放: " + skill.getCastType()));
            lines.add(Component.literal("瞄準: " + skill.getAimType()));
            lines.add(Component.literal("下級倍率: x" + fmt(skill.getMultiplierAtLevel(nextLv))));
            lines.add(Component.literal("下級持續: " + skill.getDurationAtLevel(nextLv) + " ticks"));
            g.renderComponentTooltip(font, lines, mx, my);
            return;
        }
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        if (!listRect.contains((int) mx, (int) my) || maxScroll <= 0) {
            return super.mouseScrolled(mx, my, amount);
        }
        int step = Math.max(12, uh(16));
        int delta = amount > 0 ? -step : step;
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset + delta));
        return true;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 && button != 1) {
            return super.mouseClicked(mx, my, button);
        }

        if (button == 0 && backRect.contains((int) mx, (int) my)) {
            if (minecraft != null) {
                minecraft.setScreen(new MainMenuScreen());
            }
            return true;
        }

        if (button == 0 && leftActionRect.contains((int) mx, (int) my)) {
            castSelectedSkill();
            return true;
        }

        if (button == 1 && leftActionRect.contains((int) mx, (int) my)) {
            SkillPackets.requestResetAll();
            return true;
        }

        for (int i = 0; i < rows.size(); i++) {
            RowLayout row = rows.get(i);
            Rect icon = row.icon.offset(0, -scrollOffset);
            Rect info = row.info.offset(0, -scrollOffset);
            Rect scroll = row.scroll.offset(0, -scrollOffset);
            Rect up = row.upgrade.offset(0, -scrollOffset);
            if (!isFullyInsideList(info)) {
                continue;
            }

            SkillDefinition skill = skills.get(i);
            if (icon.contains((int) mx, (int) my) || info.contains((int) mx, (int) my) || scroll.contains((int) mx, (int) my)) {
                selectedIndex = i;
                if (button == 1 && minecraft != null && info.contains((int) mx, (int) my)) {
                    minecraft.setScreen(new SkillTreeScreen(skill.getId()));
                }
                return true;
            }

            if (button == 0 && up.contains((int) mx, (int) my)) {
                selectedIndex = i;
                SkillPackets.requestUpgrade(skill.getId());
                return true;
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    private void castSelectedSkill() {
        SkillDefinition skill = resolveCastSkill();
        if (skill == null) {
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.displayClientMessage(Component.literal("沒有可施放的已學習技能"), true);
            }
            return;
        }
        SkillPackets.requestCast(skill.getId());
    }

    private SkillDefinition resolveCastSkill() {
        if (skills.isEmpty()) {
            return null;
        }
        if (selectedIndex >= 0 && selectedIndex < skills.size()) {
            SkillDefinition selected = skills.get(selectedIndex);
            if (getSkillLevel(selected.getId()) > 0) {
                return selected;
            }
        }
        for (SkillDefinition skill : skills) {
            if (getSkillLevel(skill.getId()) > 0) {
                return skill;
            }
        }
        return null;
    }

    private boolean intersectsList(Rect r) {
        return r.y + r.h > listRect.y && r.y < listRect.y + listRect.h;
    }

    private boolean isFullyInsideList(Rect r) {
        return r.y >= listRect.y && r.y + r.h <= listRect.y + listRect.h;
    }

    private int getSkillLevel(String skillId) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        return player.getCapability(ModCapabilities.PLAYER_SKILL)
            .map(cap -> cap.getSkillLevel(skillId))
            .orElse(0);
    }

    private int getAvailableSkillPoints() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        int lvl = player.getCapability(ModCapabilities.PLAYER_LEVEL)
            .map(cap -> cap.getLevel())
            .orElse(1);
        return player.getCapability(ModCapabilities.PLAYER_SKILL)
            .map(cap -> cap.getAvailableSkillPoints(lvl))
            .orElse(0);
    }

    private String getPlayerClassName() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return "未選擇";
        }
        return player.getCapability(ModCapabilities.PLAYER_CLASS)
            .map(cap -> cap.getPlayerClass().displayName)
            .orElse("未選擇");
    }


    private static String fmt(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private String trimToWidth(String value, int maxWidth) {
        if (font.width(value) <= maxWidth) {
            return value;
        }
        String ellipsis = "...";
        int textWidth = Math.max(0, maxWidth - font.width(ellipsis));
        return font.plainSubstrByWidth(value, textWidth) + ellipsis;
    }

    private void drawScaledString(GuiGraphics g, String text, int x, int y, int color, float scale) {
        if (scale >= 0.999f) {
            g.drawString(font, text, x, y, color, false);
            return;
        }
        PoseStack pose = g.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1.0f);
        g.drawString(font, text, 0, 0, color, false);
        pose.popPose();
    }

    private void drawScaledCenteredString(GuiGraphics g, String text, int centerX, int y, int color, float scale) {
        int drawX = centerX - Math.round(font.width(text) * scale / 2.0f);
        drawScaledString(g, text, drawX, y, color, scale);
    }

    private static void drawHoverOverlay(GuiGraphics g, Rect rect, boolean active, int color) {
        if (!active) {
            return;
        }
        g.fill(rect.x + 1, rect.y + 1, rect.x + rect.w - 1, rect.y + rect.h - 1, color);
    }

    private int ux(int baseX) {
        return layoutX + Math.round(baseX * scaleX);
    }

    private int uy(int baseY) {
        return layoutY + Math.round(baseY * scaleY);
    }

    private int uw(int baseWidth) {
        return Math.max(1, Math.round(baseWidth * scaleX));
    }

    private int uh(int baseHeight) {
        return Math.max(1, Math.round(baseHeight * scaleY));
    }

    private static String[] getHotkeyLabels() {
        return new String[]{
            keyLabel(KeyBindings.SKILL_CAST_3),
            keyLabel(KeyBindings.SKILL_CAST_2),
            keyLabel(KeyBindings.SKILL_CAST_1),
            keyLabel(KeyBindings.SKILL_CAST_6),
            keyLabel(KeyBindings.SKILL_CAST_5),
            keyLabel(KeyBindings.SKILL_CAST_4),
            "N"
        };
    }

    private static String keyLabel(KeyMapping key) {
        String value = key.getTranslatedKeyMessage().getString();
        return value.toUpperCase(java.util.Locale.ROOT);
    }


    private static final class RowLayout {
        final Rect icon;
        final Rect info;
        final Rect scroll;
        final Rect upgrade;

        private RowLayout(Rect icon, Rect info, Rect scroll, Rect upgrade) {
            this.icon = icon;
            this.info = info;
            this.scroll = scroll;
            this.upgrade = upgrade;
        }
    }

    private static final class Rect {
        final int x;
        final int y;
        final int w;
        final int h;

        private Rect(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        private boolean contains(int mx, int my) {
            return mx >= x && mx < x + w && my >= y && my < y + h;
        }

        private Rect offset(int ox, int oy) {
            return new Rect(x + ox, y + oy, w, h);
        }
    }
}

