package com.yichenxbohan.mcnb.events;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.combat.StatsProvider;
import com.yichenxbohan.mcnb.level.EntityLevelData;
import com.yichenxbohan.mcnb.level.EntityLevelProvider;
import com.yichenxbohan.mcnb.level.LevelDamageModifier;
import com.yichenxbohan.mcnb.level.PlayerAttributeType;
import com.yichenxbohan.mcnb.level.PlayerLevelProvider;
import com.yichenxbohan.mcnb.network.ClassSyncPacket;
import com.yichenxbohan.mcnb.network.LevelSyncPacket;
import com.yichenxbohan.mcnb.network.ModNetworking;
import com.yichenxbohan.mcnb.network.StatsSyncPacket;
import com.yichenxbohan.mcnb.playerclass.IPlayerClass;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "mcnb")
public class PlayerLevelEvents {

    // ==================== 附加 Capability ====================

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        // 玩家 → 附加 PLAYER_LEVEL
        if (event.getObject() instanceof Player player) {
            if (!player.getCapability(ModCapabilities.PLAYER_LEVEL).isPresent()) {
                event.addCapability(PlayerLevelProvider.ID, new PlayerLevelProvider());
            }
        }
        // 非玩家生物 → 附加 ENTITY_LEVEL
        else if (event.getObject() instanceof Mob mob) {
            if (!mob.getCapability(ModCapabilities.ENTITY_LEVEL).isPresent()) {
                event.addCapability(EntityLevelProvider.ID, new EntityLevelProvider());
            }
        }
    }

    // ==================== 怪物生成時初始化等級 ====================

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        mob.getCapability(ModCapabilities.ENTITY_LEVEL).ifPresent(cap -> {
            if (cap.getLevel() == 1) {
                // 傳入維度 ResourceKey，讓等級範圍依維度限制
                cap.setLevel(EntityLevelData.inferLevel(mob, event.getLevel().dimension()));
            }
        });
    }

    // ==================== 玩家登入時同步 ====================

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            syncToClient(sp);
            syncStatsToClient(sp);
            syncClassToClient(sp);
        }
    }

    // ==================== 玩家重生後複製資料並同步 ====================

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(oldCap ->
                event.getEntity().getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(newCap -> {
                    newCap.setLevel(oldCap.getLevel());
                    newCap.setExp(oldCap.getExp());
                    newCap.setAttributePoints(
                            oldCap.getAttributePoints(PlayerAttributeType.STRENGTH),
                            oldCap.getAttributePoints(PlayerAttributeType.CONSTITUTION),
                            oldCap.getAttributePoints(PlayerAttributeType.POTENTIAL),
                            oldCap.getAttributePoints(PlayerAttributeType.INTELLIGENCE),
                            oldCap.getAttributePoints(PlayerAttributeType.AGILITY)
                    );
                })
            );
            // 複製職業
            event.getOriginal().reviveCaps();
            IPlayerClass oldClass = ModCapabilities.getPlayerClass(event.getOriginal());
            IPlayerClass newClass = ModCapabilities.getPlayerClass(event.getEntity());
            if (oldClass != null && newClass != null) {
                newClass.setPlayerClass(oldClass.getPlayerClass());
            }
            event.getOriginal().invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            syncToClient(sp);
            syncStatsToClient(sp);
            syncClassToClient(sp);
        }
    }

    // ==================== 切換維度後同步 ====================

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            syncToClient(sp);
            syncStatsToClient(sp);
            syncClassToClient(sp);
        }
    }

    // ==================== 擊殺怪物獲得經驗 ====================

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;
        if (victim instanceof Player) return;

        long expGain = calculateExpGain(victim, killer);
        if (expGain <= 0) return;

        killer.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(cap -> {
            int levelBefore = cap.getLevel();
            cap.addExp(expGain);
            int levelAfter = cap.getLevel();
            syncToClient(killer);
            if (levelAfter > levelBefore) {
                sendLevelUpMessage(killer, levelAfter);
            }
        });
    }

    // ==================== 工具方法 ====================

    /**
     * 計算擊殺怪物所獲得的經驗
     *
     * 基礎公式：怪物最大血量 × 2
     * 怪物等級係數：怪物等級每高1等，EXP × 1.05（最高 ×10 封頂）
     * 等級差距懲罰：玩家等級 > 怪物等級 + 10 時，EXP × 0.5^(超出等差/10)
     * 例：怪物 Lv.10，玩家 Lv.30 → 超出 20 等 → EXP × 0.25
     */
    public static long calculateExpGain(LivingEntity mob, LivingEntity killer) {
        float hp = mob.getMaxHealth();
        long baseExp = Math.max(1L, (long)(hp * 2));

        // 怪物等級係數
        int mobLevel = LevelDamageModifier.getLevel(mob);
        double levelScale = Math.min(10.0, Math.pow(1.05, mobLevel - 1));

        // 等級差距懲罰（玩家遠高於怪物時減少 EXP）
        int killerLevel = LevelDamageModifier.getLevel(killer);
        int overLevel = killerLevel - mobLevel - 10; // 超過 10 等差才懲罰
        double penaltyScale = overLevel > 0 ? Math.pow(0.5, overLevel / 10.0) : 1.0;

        return Math.max(1L, (long)(baseExp * levelScale * penaltyScale));
    }

    private static void sendLevelUpMessage(ServerPlayer player, int newLevel) {
        Component msg = Component.empty()
                .append(Component.literal("★ ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal("等級提升！").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                .append(Component.literal(" 現在是 ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("Lv." + newLevel).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
                .append(Component.literal(" ★").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(msg);
    }

    public static void syncToClient(ServerPlayer player) {
        player.getCapability(ModCapabilities.PLAYER_LEVEL).ifPresent(cap -> {
            ModNetworking.sendToPlayer(new LevelSyncPacket(
                    cap.getLevel(), cap.getExp(),
                    cap.getAttributePoints(PlayerAttributeType.STRENGTH),
                    cap.getAttributePoints(PlayerAttributeType.CONSTITUTION),
                    cap.getAttributePoints(PlayerAttributeType.POTENTIAL),
                    cap.getAttributePoints(PlayerAttributeType.INTELLIGENCE),
                    cap.getAttributePoints(PlayerAttributeType.AGILITY)
            ), player);
            cap.clearDirty();
        });
    }

    public static void syncStatsToClient(ServerPlayer player) {
        StatsProvider.apply(player);
        StatsSyncPacket pkt = StatsSyncPacket.from(player);
        if (pkt != null) ModNetworking.sendToPlayer(pkt, player);
    }

    public static void syncClassToClient(ServerPlayer player) {
        player.getCapability(ModCapabilities.PLAYER_CLASS).ifPresent(cap ->
            ModNetworking.sendToPlayer(new ClassSyncPacket(cap.getPlayerClass()), player)
        );
    }
}
