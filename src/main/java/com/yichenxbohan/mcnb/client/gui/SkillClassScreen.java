package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import com.yichenxbohan.mcnb.skill.api.SkillDefinition;
import com.yichenxbohan.mcnb.skill.api.SkillRegistry;
import com.yichenxbohan.mcnb.skill.network.SkillPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class SkillClassScreen extends Screen {

    private static final int PANEL_W = 420;
    private static final int PANEL_H = 320;

    private static final int CARD_W = 188;
    private static final int CARD_H = 56;

    private int panelX;
    private int panelY;

    private final List<SkillDefinition> skills = new ArrayList<>();
    private final List<Rect> skillRects = new ArrayList<>();

    public SkillClassScreen() {
        super(Component.literal("技能"));
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;
        rebuildSkills();
    }

    private void rebuildSkills() {
        skills.clear();
        skillRects.clear();

        PlayerClass clazz = PlayerClass.NONE;
        var player = Minecraft.getInstance().player;
        if (player != null) {
            clazz = player.getCapability(ModCapabilities.PLAYER_CLASS)
                    .map(cap -> cap.getPlayerClass())
                    .orElse(PlayerClass.NONE);
        }

        skills.addAll(SkillRegistry.getSkills(clazz));

        int startX = panelX + 18;
        int startY = panelY + 56;
        int gapX = 12;
        int gapY = 10;
        for (int i = 0; i < skills.size(); i++) {
            int col = i % 2;
            int row = i / 2;
            int x = startX + col * (CARD_W + gapX);
            int y = startY + row * (CARD_H + gapY);
            skillRects.add(new Rect(x, y, CARD_W, CARD_H));
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        g.fill(0, 0, width, height, 0x88000000);
        drawPanel(g, mx, my);
        drawHoverTooltip(g, mx, my);
        super.render(g, mx, my, pt);
    }

    private void drawPanel(GuiGraphics g, int mx, int my) {
        int x = panelX;
        int y = panelY;

        g.fill(x - 1, y - 1, x + PANEL_W + 1, y + PANEL_H + 1, 0xFF3A4A6B);
        g.fill(x, y, x + PANEL_W, y + PANEL_H, 0xE8101014);
        g.fill(x, y, x + PANEL_W, y + 34, 0xCC1A2540);
        g.drawCenteredString(font, "職業技能", x + PANEL_W / 2, y + 11, 0xFFE8D87A);

        int points = getAvailableSkillPoints();
        g.drawString(font, "可用技能點: " + points, x + 12, y + 38, points > 0 ? 0xFF55FF88 : 0xFFAAAAAA, false);

        for (int i = 0; i < skills.size(); i++) {
            SkillDefinition skill = skills.get(i);
            Rect r = skillRects.get(i);
            boolean hovered = r.contains(mx, my);
            int level = getSkillLevel(skill.getId());

            g.fill(r.x, r.y, r.x + r.w, r.y + r.h, hovered ? 0xCC2A3A60 : 0xCC1C2030);
            drawBorder(g, r.x, r.y, r.x + r.w, r.y + r.h, hovered ? 0xFF7FA4FF : 0xFF3A4A6B);

            g.drawString(font, skill.getDisplayName(), r.x + 8, r.y + 8, 0xFFEEEEEE, false);
            g.drawString(font, "Lv." + level + " / " + skill.getMaxLevel(), r.x + 8, r.y + 22, 0xFFAACCEE, false);
            g.drawString(font, "左鍵升級  右鍵分支", r.x + 8, r.y + 38, 0xFF8899AA, false);
        }

        int bottomY = y + PANEL_H - 28;
        Rect reset = new Rect(x + 12, bottomY, 90, 18);
        Rect back = new Rect(x + PANEL_W - 92, bottomY, 80, 18);
        boolean resetHover = reset.contains(mx, my);
        boolean backHover = back.contains(mx, my);

        g.fill(reset.x, reset.y, reset.x + reset.w, reset.y + reset.h, resetHover ? 0xCC4A2A2A : 0xCC2B2020);
        drawBorder(g, reset.x, reset.y, reset.x + reset.w, reset.y + reset.h, 0xFF8A4A4A);
        g.drawCenteredString(font, "重置技能", reset.x + reset.w / 2, reset.y + 5, 0xFFFFBBBB);

        g.fill(back.x, back.y, back.x + back.w, back.y + back.h, backHover ? 0xCC2A3A60 : 0xCC1C2030);
        drawBorder(g, back.x, back.y, back.x + back.w, back.y + back.h, 0xFF3A4A6B);
        g.drawCenteredString(font, "返回", back.x + back.w / 2, back.y + 5, 0xFFCCCCDD);
    }

    private void drawHoverTooltip(GuiGraphics g, int mx, int my) {
        for (int i = 0; i < skillRects.size(); i++) {
            Rect r = skillRects.get(i);
            if (!r.contains(mx, my)) {
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
            lines.add(Component.literal("倍率(下級): x" + fmt(skill.getMultiplierAtLevel(nextLv))));
            lines.add(Component.literal("效果時長(下級): " + skill.getDurationAtLevel(nextLv) + " ticks"));
            g.renderComponentTooltip(font, lines, mx, my);
            break;
        }
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
        int lvl = player.getCapability(ModCapabilities.PLAYER_LEVEL).map(cap -> cap.getLevel()).orElse(1);
        return player.getCapability(ModCapabilities.PLAYER_SKILL)
                .map(cap -> cap.getAvailableSkillPoints(lvl))
                .orElse(0);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int x = panelX;
        int y = panelY;
        Rect reset = new Rect(x + 12, y + PANEL_H - 28, 90, 18);
        Rect back = new Rect(x + PANEL_W - 92, y + PANEL_H - 28, 80, 18);

        if (button == 0 && reset.contains((int) mx, (int) my)) {
            SkillPackets.requestResetAll();
            return true;
        }

        if (button == 0 && back.contains((int) mx, (int) my)) {
            if (minecraft != null) {
                minecraft.setScreen(new MainMenuScreen());
            }
            return true;
        }

        for (int i = 0; i < skillRects.size(); i++) {
            Rect r = skillRects.get(i);
            if (!r.contains((int) mx, (int) my)) {
                continue;
            }
            SkillDefinition skill = skills.get(i);
            if (button == 0) {
                SkillPackets.requestUpgrade(skill.getId());
                return true;
            }
            if (button == 1 && minecraft != null) {
                minecraft.setScreen(new SkillTreeScreen(skill.getId()));
                return true;
            }
        }

        return super.mouseClicked(mx, my, button);
    }

    private static String fmt(double value) {
        return String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    private static void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + 1, color);
        g.fill(x1, y2 - 1, x2, y2, color);
        g.fill(x1, y1, x1 + 1, y2, color);
        g.fill(x2 - 1, y1, x2, y2, color);
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
    }
}

