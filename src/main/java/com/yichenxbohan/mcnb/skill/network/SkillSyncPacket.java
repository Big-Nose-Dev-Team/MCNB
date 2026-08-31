package com.yichenxbohan.mcnb.skill.network;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.client.gui.SkillClassScreen; // 確保有導入你的技能畫面類別
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * S2C 技能資料同步封包。
 */
public class SkillSyncPacket {

    private final Map<String, Integer> levels;
    private final Map<String, Long> cooldowns;

    public SkillSyncPacket(Map<String, Integer> levels, Map<String, Long> cooldowns) {
        this.levels = new HashMap<>(levels);
        this.cooldowns = new HashMap<>(cooldowns);
    }

    public SkillSyncPacket(FriendlyByteBuf buf) {
        int levelSize = buf.readVarInt();
        this.levels = new HashMap<>();
        for (int i = 0; i < levelSize; i++) {
            String key = buf.readUtf(128);
            int value = buf.readVarInt();
            levels.put(key, value);
        }

        int cooldownSize = buf.readVarInt();
        this.cooldowns = new HashMap<>();
        for (int i = 0; i < cooldownSize; i++) {
            String key = buf.readUtf(128);
            long value = buf.readVarLong();
            cooldowns.put(key, value);
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(levels.size());
        for (Map.Entry<String, Integer> entry : levels.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }

        buf.writeVarInt(cooldowns.size());
        for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeVarLong(entry.getValue());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handleClient)
        );
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        Minecraft mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null) {
            return;
        }

        // 1. 先把伺服器發送過來的最新數據更新進 Capability
        player.getCapability(ModCapabilities.PLAYER_SKILL).ifPresent(cap -> {
            cap.overwriteFrom(levels, cooldowns);
            cap.clearDirty();
        });

        // 2. 數據同步完成後，檢查玩家當前是否正開著技能介面。如果是，立刻讓畫面重新整理
        if (mc.screen instanceof SkillClassScreen skillScreen) {
            skillScreen.refreshScreen();
        }
    }
}