package com.yichenxbohan.mcnb.client.gui;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.client.gui.Libs.HBox;
import com.yichenxbohan.mcnb.client.gui.Libs.ScaledLabel;
import com.yichenxbohan.mcnb.client.gui.Libs.ScrollVBox;
import com.yichenxbohan.mcnb.client.gui.Libs.ThemedButton;
import com.yichenxbohan.mcnb.client.gui.Libs.ThemedPanelScreen;
import com.yichenxbohan.mcnb.client.gui.Libs.UITheme;
import com.yichenxbohan.mcnb.level.PlayerAttributeType;
import com.yichenxbohan.mcnb.network.AttributePointPacket;
import com.yichenxbohan.mcnb.network.ModNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class StatsScreen extends ThemedPanelScreen {
    private String lastSnapshotKey = "";

    public StatsScreen() {
        super(Component.literal("屬性分配"), 760, 500, UITheme.DEFAULT);
    }

    @Override
    protected void buildContent(ScrollVBox content) {
        Snapshot data = captureSnapshot();
        populateContent(content, data);
        lastSnapshotKey = data.key();
    }

    @Override
    public void tick() {
        super.tick();
        if (contentBox == null) {
            return;
        }

        Snapshot current = captureSnapshot();
        String currentKey = current.key();
        if (!currentKey.equals(lastSnapshotKey)) {
            double scrollY = contentBox.getScrollY();
            contentBox.clearChildren();
            populateContent(contentBox, current);
            contentBox.setScrollY(scrollY);
            lastSnapshotKey = currentKey;
        }
    }

    private void populateContent(ScrollVBox content, Snapshot data) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            content.addChild(new ScaledLabel(0, 20, Component.literal("請先進入世界"), theme.labelText()));
            return;
        }

        HBox levelRow = new HBox(0, 0, 0, 30, 6, 2, 2);
        levelRow.addFill(new ScaledLabel(0, 18,
                Component.literal("Lv." + data.level + "  經驗: " + data.expText),
                theme.valueText()));
        content.addChild(levelRow);

        HBox summaryRow = new HBox(0, 0, 0, 30, 6, 2, 2);
        summaryRow.addFill(new ScaledLabel(0, 18,
                Component.literal("剩餘點數: " + data.availablePoints + "  已分配: " + data.allocatedPoints),
                data.availablePoints > 0 ? 0xFF4FC3F7 : theme.labelText()));
        ThemedButton reset = summaryRow.addRight(new ThemedButton(84, 24, Component.literal("重置"), theme,
                b -> {
                    ModNetworking.sendToServer(new AttributePointPacket(true));
                }));
        reset.active = data.allocatedPoints > 0;
        content.addChild(summaryRow);

        addSectionHeader(content, "戰鬥能力");
        addValueRow(content, "物理攻擊", fmt1(data.physicalAttack));
        addValueRow(content, "魔法攻擊", fmt1(data.magicAttack));
        addValueRow(content, "暴擊率", fmt1(data.critChance) + "%");
        addValueRow(content, "暴擊傷害", "+" + fmt1(data.critDamage) + "%");
        addValueRow(content, "穿透值", fmt1(data.penetration));
        addValueRow(content, "閃避值", fmt1(data.evasion));
        addValueRow(content, "防禦", fmt1(data.defense));
        addValueRow(content, "減傷", fmt1(data.damageReduction) + "%");
        addValueRow(content, "能量護盾", fmt1(data.energyShield) + " / " + fmt1(data.maxEnergyShield));
        addValueRow(content, "靈魂完整", fmt1(data.soulIntegrity) + "%");
        addValueRow(content, "生命回復", fmt2(data.regen) + "/s");
        addValueRow(content, "治療加成", "+" + fmt1(data.healingBonus) + "%");

        addSectionHeader(content, "屬性點分配");
        for (PlayerAttributeType type : PlayerAttributeType.orderedValues()) {
            int pts = data.attributePoints[type.ordinal()];
            addAttributeRow(content, type, pts, data.availablePoints);
        }
    }

    private void addSectionHeader(ScrollVBox content, String text) {
        content.addChild(new ScaledLabel(0, 18, Component.literal(text), theme.titleText()));
    }

    private void addValueRow(ScrollVBox content, String label, String value) {
        HBox row = new HBox(0, 0, 0, 24, 6, 2, 2);
        row.addLeft(new ScaledLabel(150, 16, Component.literal(label), theme.labelText()));
        row.addFill(new ScaledLabel(0, 16, Component.literal(value), theme.valueText()));
        content.addChild(row);
    }

    private void addAttributeRow(ScrollVBox content, PlayerAttributeType type, int points, int availablePoints) {
        HBox row = new HBox(0, 0, 0, 36, 6, 2, 2);
        row.addFill(new ScaledLabel(0, 16,
                Component.literal(type.getLabel() + "  點數:" + points + "  " + type.formatBonus(points)),
                type.getColor()));

        ThemedButton minus = row.addRight(new ThemedButton(28, 24, Component.literal("-"), theme,
                b -> {
                    ModNetworking.sendToServer(new AttributePointPacket(type, -1));
                }));
        minus.active = points > 0;

        ThemedButton plus = row.addRight(new ThemedButton(28, 24, Component.literal("+"), theme,
                b -> {
                    ModNetworking.sendToServer(new AttributePointPacket(type, 1));
                }));
        plus.active = availablePoints > 0;

        content.addChild(row);
        content.addChild(new ScaledLabel(0, 14, Component.literal("  " + type.getDescription()), theme.labelText()));
    }

    private Snapshot captureSnapshot() {
        Snapshot snapshot = new Snapshot();
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return snapshot;
        }

        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(cap -> {
            snapshot.level = cap.getLevel();
            int exp = (int) Math.min(cap.getExp(), Integer.MAX_VALUE);
            int expNext = (int) Math.min(cap.getExpToNextLevel(), Integer.MAX_VALUE);
            snapshot.expText = snapshot.level >= cap.getMaxLevel() ? "MAX" : exp + " / " + expNext;
            snapshot.allocatedPoints = cap.getAllocatedAttributePoints();
            snapshot.availablePoints = cap.getAvailableAttributePoints();
            for (PlayerAttributeType type : PlayerAttributeType.orderedValues()) {
                snapshot.attributePoints[type.ordinal()] = cap.getAttributePoints(type);
            }
        });

        player.getCapability(ModCapabilities.COMBAT_DATA).ifPresent(cap -> {
            snapshot.physicalAttack = cap.getPhysicalAttack();
            snapshot.magicAttack = cap.getMagicAttack();
            snapshot.critChance = cap.getCritChance() * 100.0;
            snapshot.critDamage = cap.getCritDamage() * 100.0;
            snapshot.penetration = cap.getPenetration();
            snapshot.evasion = cap.getEvasion();
            snapshot.defense = cap.getDefense();
            snapshot.damageReduction = cap.getDamageReduction() * 100.0;
            snapshot.energyShield = cap.getEnergyShield();
            snapshot.maxEnergyShield = cap.getMaxEnergyShield();
            snapshot.soulIntegrity = cap.getSoulIntegrity();
            snapshot.regen = cap.getRegen();
            snapshot.healingBonus = cap.getHealingBonus() * 100.0;
        });
        return snapshot;
    }

    private String fmt1(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private String fmt2(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static final class Snapshot {
        int level = 1;
        String expText = "0 / 0";
        int allocatedPoints = 0;
        int availablePoints = 0;
        final int[] attributePoints = new int[PlayerAttributeType.values().length];

        double physicalAttack;
        double magicAttack;
        double critChance;
        double critDamage;
        double penetration;
        double evasion;
        double defense;
        double damageReduction;
        double energyShield;
        double maxEnergyShield;
        double soulIntegrity;
        double regen;
        double healingBonus;

        String key() {
            return level + "|" + expText + "|" + allocatedPoints + "|" + availablePoints + "|"
                    + Arrays.toString(attributePoints) + "|"
                    + physicalAttack + "|" + magicAttack + "|" + critChance + "|" + critDamage + "|"
                    + penetration + "|" + evasion + "|" + defense + "|" + damageReduction + "|"
                    + energyShield + "|" + maxEnergyShield + "|" + soulIntegrity + "|" + regen + "|"
                    + healingBonus;
        }
    }
}
