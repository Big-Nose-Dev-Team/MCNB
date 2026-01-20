package com.yichenxbohan.mcnb.events;

import com.yichenxbohan.mcnb.Mcnb;
import com.yichenxbohan.mcnb.skill.SoulBowSkill;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Mcnb.MODID)
public class BowChargingEventHandler {

    // 存储正在拉弓的玩家及其拉弓时长
    private static final Map<UUID, Integer> chargingPlayers = new HashMap<>();

    // 粒子效果
    private static final ParticleEmitterInfo CHARGING_PARTICLE =
            new ParticleEmitterInfo(ResourceLocation.fromNamespaceAndPath("mcnb", "soul_bow"));

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // 只在服务端处理
        if (event.player.level().isClientSide()) {
            return;
        }

        // 只在阶段开始时处理
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        UUID playerId = player.getUUID();

        // 检查玩家是否激活了射魂长弓技能
        if (!SoulBowSkill.isActivated(playerId)) {
            // 如果没有激活技能，清除拉弓状态
            chargingPlayers.remove(playerId);
            return;
        }

        // 检查玩家是否正在使用弓
        ItemStack useItem = player.getUseItem();
        if (useItem.getItem() instanceof BowItem && player.isUsingItem()) {
            // 玩家正在拉弓
            int chargeTicks = chargingPlayers.getOrDefault(playerId, 0);
            chargingPlayers.put(playerId, chargeTicks + 1);

            // 每2个tick（0.1秒）生成一次粒子效果
            if (chargeTicks % 2 == 0) {
                spawnChargingParticles((ServerPlayer) player, chargeTicks);
            }
        } else {
            // 玩家没有在拉弓，清除拉弓状态
            chargingPlayers.remove(playerId);
        }
    }

    /**
     * 在玩家周围生成拉弓粒子效果
     */
    private static void spawnChargingParticles(ServerPlayer player, int chargeTicks) {
        ServerLevel level = player.serverLevel();
        Vec3 playerPos = player.getEyePosition(1.0F);
        Vec3 lookVec = player.getLookAngle();

        // 计算拉弓进度 (0.0 到 1.0)
        float chargeProgress = Math.min(chargeTicks / 20.0F, 1.0F);

        // 在玩家视线方向前方生成粒子
        double distance = 1.0 + chargeProgress * 0.5; // 随着拉弓进度增加距离
        Vec3 particlePos = playerPos.add(lookVec.scale(distance));

        // 生成螺旋粒子效果
        for (int i = 0; i < 2; i++) {
            double angle = (chargeTicks * 0.3 + i * Math.PI) % (Math.PI * 2);
            double radius = 0.3 * chargeProgress; // 半径随拉弓进度增加

            double offsetX = Math.cos(angle) * radius;
            double offsetY = Math.sin(angle) * radius;

            // 计算垂直于视线方向的偏移
            Vec3 rightVec = lookVec.cross(new Vec3(0, 1, 0)).normalize();
            Vec3 upVec = rightVec.cross(lookVec).normalize();

            Vec3 finalPos = particlePos
                    .add(rightVec.scale(offsetX))
                    .add(upVec.scale(offsetY));

            AAALevel.addParticle(level, false,
                    CHARGING_PARTICLE.clone().position(
                            finalPos.x, finalPos.y, finalPos.z
                    ));
        }

        // 当拉满弓时，在玩家周围生成额外的粒子环
        if (chargeProgress >= 1.0F && chargeTicks % 4 == 0) {
            for (int i = 0; i < 8; i++) {
                double angle = (i * Math.PI * 2) / 8.0;
                double radius = 0.8;

                double x = playerPos.x + Math.cos(angle) * radius;
                double y = playerPos.y;
                double z = playerPos.z + Math.sin(angle) * radius;

                AAALevel.addParticle(level, false,
                        CHARGING_PARTICLE.clone().position(x, y, z));
            }
        }
    }

    /**
     * 清除玩家的拉弓状态
     */
    public static void clearChargingState(UUID playerId) {
        chargingPlayers.remove(playerId);
    }

    /**
     * 清除所有拉弓状态
     */
    public static void clearAll() {
        chargingPlayers.clear();
    }
}

