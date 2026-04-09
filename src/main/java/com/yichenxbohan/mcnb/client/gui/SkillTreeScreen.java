package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.skill.api.SkillBranch;
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
public class SkillTreeScreen extends Screen {

    private static final int PANEL_W = 380;
    private static final int PANEL_H = 240;

    private final String skillId;
    private SkillDefinition skill;

    private int panelX;
    private int panelY;

    private Rect castRect;
    private Rect backRect;
    private final List<Rect> branchRects = new ArrayList<>();

    public SkillTreeScreen(String skillId) {
        super(Component.literal("技能分支"));
        this.skillId = skillId;
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;
        skill = SkillRegistry.getById(skillId);

        castRect = new Rect(panelX + 16, panelY + PANEL_H - 28, 100, 18);
        backRect = new Rect(panelX + PANEL_W - 96, panelY + PANEL_H - 28, 80, 18);

        branchRects.clear();
        if (skill != null) {
            int x = panelX + 30;
            int y = panelY + 78;
            for (int i = 0; i < skill.getBranches().size(); i++) {
                branchRects.add(new Rect(x + i * 160, y, 130, 70));
            }
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
        drawTooltip(g, mx, my);
        super.render(g, mx, my, pt);
    }

    private void drawPanel(GuiGraphics g, int mx, int my) {
        int x = panelX;
        int y = panelY;

        g.fill(x - 1, y - 1, x + PANEL_W + 1, y + PANEL_H + 1, 0xFF3A4A6B);
        g.fill(x, y, x + PANEL_W, y + PANEL_H, 0xE8101014);
        g.fill(x, y, x + PANEL_W, y + 34, 0xCC1A2540);

        if (skill == null) {
            g.drawCenteredString(font, "技能不存在", x + PANEL_W / 2, y + 90, 0xFFFF8888);
            return;
        }

        int level = getSkillLevel(skill.getId());
        String selectedBranch = getSelectedBranch(skill.getId());

        g.drawCenteredString(font, skill.getDisplayName() + " 分支", x + PANEL_W / 2, y + 11, 0xFFE8D87A);
        g.drawString(font, "目前等級: " + level + " / " + skill.getMaxLevel(), x + 12, y + 40, 0xFFAACCEE, false);
        g.drawString(font, "技能描述: " + skill.getDescription(), x + 12, y + 54, 0xFFAABBCC, false);

        for (int i = 0; i < skill.getBranches().size(); i++) {
            SkillBranch branch = skill.getBranches().get(i);
            Rect r = branchRects.get(i);
            boolean hovered = r.contains(mx, my);
            boolean selected = branch.getId().equals(selectedBranch);

            g.fill(r.x, r.y, r.x + r.w, r.y + r.h, hovered ? 0xCC2A3A60 : 0xCC1C2030);
            drawBorder(g, r.x, r.y, r.x + r.w, r.y + r.h, selected ? 0xFF55FF88 : 0xFF3A4A6B);
            g.drawString(font, branch.getName(), r.x + 8, r.y + 8, selected ? 0xFF55FF88 : 0xFFEEEEEE, false);
            g.drawString(font, "+倍率/級: " + fmt(branch.getMultiplierBonusPerLevel()), r.x + 8, r.y + 24, 0xFFAACCEE, false);
            g.drawString(font, "+時長/級: " + branch.getDurationBonusPerLevel(), r.x + 8, r.y + 38, 0xFFAACCEE, false);
            g.drawString(font, selected ? "已選擇" : "左鍵選擇", r.x + 8, r.y + 54, 0xFF99AABB, false);
        }

        boolean castHover = castRect.contains(mx, my);
        g.fill(castRect.x, castRect.y, castRect.x + castRect.w, castRect.y + castRect.h, castHover ? 0xCC2D4530 : 0xCC1D3020);
        drawBorder(g, castRect.x, castRect.y, castRect.x + castRect.w, castRect.y + castRect.h, 0xFF55AA66);
        g.drawCenteredString(font, "施放技能", castRect.x + castRect.w / 2, castRect.y + 5, 0xFFBBFFCC);

        boolean backHover = backRect.contains(mx, my);
        g.fill(backRect.x, backRect.y, backRect.x + backRect.w, backRect.y + backRect.h, backHover ? 0xCC2A3A60 : 0xCC1C2030);
        drawBorder(g, backRect.x, backRect.y, backRect.x + backRect.w, backRect.y + backRect.h, 0xFF3A4A6B);
        g.drawCenteredString(font, "返回", backRect.x + backRect.w / 2, backRect.y + 5, 0xFFCCCCDD);
    }

    private void drawTooltip(GuiGraphics g, int mx, int my) {
        if (skill == null) {
            return;
        }

        for (int i = 0; i < branchRects.size(); i++) {
            Rect r = branchRects.get(i);
            if (!r.contains(mx, my)) {
                continue;
            }
            SkillBranch branch = skill.getBranches().get(i);
            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(branch.getName()));
            lines.add(Component.literal(branch.getDescription()));
            lines.add(Component.literal("倍率加成/級: " + fmt(branch.getMultiplierBonusPerLevel())));
            lines.add(Component.literal("時長加成/級: " + branch.getDurationBonusPerLevel() + " ticks"));
            if (branch.getPrerequisite() != null) {
                lines.add(Component.literal("前置: " + branch.getPrerequisite().getRequiredSkillId() + " Lv." + branch.getPrerequisite().getMinLevel()));
            }
            g.renderComponentTooltip(font, lines, mx, my);
            return;
        }
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (button != 0 || skill == null) {
            return super.mouseClicked(mx, my, button);
        }

        if (backRect.contains((int) mx, (int) my)) {
            if (minecraft != null) {
                minecraft.setScreen(new SkillClassScreen());
            }
            return true;
        }

        if (castRect.contains((int) mx, (int) my)) {
            SkillPackets.requestCast(skill.getId());
            return true;
        }

        for (int i = 0; i < branchRects.size(); i++) {
            Rect r = branchRects.get(i);
            if (!r.contains((int) mx, (int) my)) {
                continue;
            }
            SkillBranch branch = skill.getBranches().get(i);
            SkillPackets.requestSelectBranch(skill.getId(), branch.getId());
            return true;
        }

        return super.mouseClicked(mx, my, button);
    }

    private int getSkillLevel(String id) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return 0;
        }
        return player.getCapability(ModCapabilities.PLAYER_SKILL).map(cap -> cap.getSkillLevel(id)).orElse(0);
    }

    private String getSelectedBranch(String id) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return "";
        }
        return player.getCapability(ModCapabilities.PLAYER_SKILL).map(cap -> {
            String b = cap.getSelectedBranch(id);
            return b == null ? "" : b;
        }).orElse("");
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.ROOT, "%.2f", v);
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

