package com.yichenxbohan.mcnb.skill;

import com.mojang.logging.LogUtils;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SoulBowSkill {

    private static final Logger LOGGER = LogUtils.getLogger();

    // 追踪需要生成粒子的箭矢 UUID（线程安全）
    private static final Set<UUID> trackedArrows = ConcurrentHashMap.newKeySet();

    // 追踪箭矢的發射時間（用於5秒後自動消失）
    private static final ConcurrentHashMap<UUID, Long> arrowSpawnTime = new ConcurrentHashMap<>();

    // 追踪射魂长弓箭矢，用于自定义死亡消息
    private static final ConcurrentHashMap<UUID, UUID> soulBowArrows = new ConcurrentHashMap<>(); // 箭矢UUID -> 玩家UUID

    // 缓存服务器实例（从 Tick 事件中获取）
    private static MinecraftServer cachedServer = null;

    // 存储已激活技能等待射箭的玩家
    private static final Set<UUID> activatedPlayers = new HashSet<>();

    // 可自定义的箭矢属性
    private static double arrowSpeed = 3.0; // 箭矢速度（默认3.0，普通弓是3.0）
    private static double arrowDamage = 15.0; // 箭矢伤害（默认10.0）
    private static byte arrowPierceLevel = 3; // 穿透等级（默认3）
    private static boolean arrowCrit = true; // 是否暴击箭（默认true）

    // 粒子效果
    private static final ParticleEmitterInfo SOUL_BOW;
    private static final ParticleEmitterInfo ARROW01;
    // 你可以在這裡添加更多粒子效果，例如：
    // private static final ParticleEmitterInfo SOUL_BOW_TRAIL;
    // private static final ParticleEmitterInfo SOUL_BOW_HIT;

    static {
        ResourceLocation rl = ResourceLocation.tryParse("mcnb:soulbow");
        if (rl == null) {
            // 最后手段回退（大多数环境下 tryParse 不会返回 null）
            throw new IllegalStateException("無法解析粒子效果資源位置: mcnb:soulbow");
        }
        SOUL_BOW = new ParticleEmitterInfo(rl);

        ResourceLocation arrowRl = ResourceLocation.tryParse("mcnb:arrow_1");
        if (arrowRl == null) {
            throw new IllegalStateException("無法解析粒子效果資源位置: mcnb:arrow_1");
        }
        ARROW01 = new ParticleEmitterInfo(arrowRl);

        // === 如何添加更多粒子效果範例 ===
        // 1. 首先在上面宣告粒子變數（已註解範例）
        // 2. 在這裡初始化它們：
        // ResourceLocation trailRl = ResourceLocation.tryParse("mcnb:soulbow_trail");
        // if (trailRl == null) throw new IllegalStateException("無法解析粒子: mcnb:soulbow_trail");
        // SOUL_BOW_TRAIL = new ParticleEmitterInfo(trailRl);
        //
        // ResourceLocation hitRl = ResourceLocation.tryParse("mcnb:soulbow_hit");
        // if (hitRl == null) throw new IllegalStateException("無法解析粒子: mcnb:soulbow_hit");
        // SOUL_BOW_HIT = new ParticleEmitterInfo(hitRl);
        //
        // 3. 然後在需要的地方使用（例如在 onServerTick 中用 SOUL_BOW_TRAIL，
        //    或在箭矢擊中目標時用 SOUL_BOW_HIT）

        // 注册服务器 Tick 事件监听器，用于在每个服务器刻生成被追踪箭矢的粒子
        MinecraftForge.EVENT_BUS.addListener(SoulBowSkill::onServerTick);
    }

    // 设置箭矢速度
    public static void setArrowSpeed(double speed) {
        arrowSpeed = speed;
        LOGGER.info("射魂长弓箭矢速度设置为: {}", speed);
    }

    // 设置箭矢伤害
    public static void setArrowDamage(double damage) {
        arrowDamage = damage;
        LOGGER.info("射魂长弓箭矢伤害设置为: {}", damage);
    }

    // 设置穿透等级
    public static void setArrowPierceLevel(int level) {
        arrowPierceLevel = (byte) level;
        LOGGER.info("射魂长弓箭矢穿透等级设置为: {}", level);
    }

    // 设置是否暴击
    public static void setArrowCrit(boolean crit) {
        arrowCrit = crit;
        LOGGER.info("射魂长弓箭矢暴击设置为: {}", crit);
    }

    // 获取当前箭矢速度
    public static double getArrowSpeed() {
        return arrowSpeed;
    }

    // 获取当前箭矢伤害
    public static double getArrowDamage() {
        return arrowDamage;
    }

    // 获取当前穿透等级
    public static byte getArrowPierceLevel() {
        return arrowPierceLevel;
    }

    // 获取当前暴击状态
    public static boolean isArrowCrit() {
        return arrowCrit;
    }

    /**
     * 激活技能 - 按下按键时调用
     */
    public static void activate(ServerPlayer player) {
        UUID playerId = player.getUUID();

        if (activatedPlayers.contains(playerId)) {
            // 如果已经激活，取消激活
            activatedPlayers.remove(playerId);
            player.sendSystemMessage(Component.literal("§c哈哈哈，遺憾哪"));
        } else {
            // 激活技能
            activatedPlayers.add(playerId);
            player.sendSystemMessage(Component.literal("§e魔術技巧"));

            // 播放激活音效
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1.0F, 1.5F);
        }
    }

    /**
     * 检查玩家是否已激活技能
     */
    public static boolean isActivated(UUID playerId) {
        return activatedPlayers.contains(playerId);
    }

    /**
     * 当玩家射箭时调用此方法来应用技能效果
     */
    public static void onArrowShoot(ServerPlayer player, Arrow arrow) {
        UUID playerId = player.getUUID();

        // 检查玩家是否激活了技能
        if (!activatedPlayers.contains(playerId)) {
            return; // 没有激活技能，不做任何处理
        }

        // 移除激活状态
        activatedPlayers.remove(playerId);

        // 应用技能效果
        applySkillToArrow(player, arrow);
    }

    /**
     * 应用技能效果到箭矢
     */
    private static void applySkillToArrow(ServerPlayer player, Arrow arrow) {
        ServerLevel level = player.serverLevel();

        // 修改箭矢属性
        arrow.setBaseDamage(arrowDamage);
        arrow.setCritArrow(arrowCrit);
        arrow.setPierceLevel(arrowPierceLevel);

        // 取消箭矢重力，讓箭矢直線飛行
        arrow.setNoGravity(true);

        // 重新设置箭矢速度
        Vec3 motion = arrow.getDeltaMovement();
        double currentSpeed = motion.length();
        if (currentSpeed > 0) {
            Vec3 direction = motion.normalize();
            arrow.setDeltaMovement(direction.scale(arrowSpeed));
        }

        // 播放特效音效
        level.playSound(null, player.blockPosition(),
                SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS, 1.0F, 1.2F);

        player.sendSystemMessage(Component.literal("§a直擊靈魂"));

        // 在玩家位置生成粒子效果
        spawnActivationParticles(player);

        // 将箭矢加入被追踪集合，由服务器 tick 事件统一生成粒子
        trackedArrows.add(arrow.getUUID());
        // 记录箭矢的發射時間
        arrowSpawnTime.put(arrow.getUUID(), System.currentTimeMillis());
        // 将箭矢与玩家的关系记录下来，用于自定义死亡消息
        soulBowArrows.put(arrow.getUUID(), player.getUUID());
    }

    /**
     * 在玩家位置生成激活粒子效果
     */
    private static void spawnActivationParticles(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 playerPos = player.position();

        new Thread(() -> {
            try {
                for (int i = 0; i < 20; i++) {
                    double angle = (i * Math.PI * 2) / 10.0;
                    double radius = 1.5;

                    double x = playerPos.x + Math.cos(angle) * radius;
                    double y = playerPos.y + 1.0;
                    double z = playerPos.z + Math.sin(angle) * radius;

                    level.getServer().execute(() ->
                            AAALevel.addParticle(level, true,
                                    SOUL_BOW.clone().position(x, y, z))
                    );

                    Thread.sleep(25);
                }
            } catch (InterruptedException e) {
                LOGGER.error("Activation particles interrupted", e);
            }
        }).start();
    }

    // 使用服务器 Tick 事件在每个刻生成被追踪箭矢的粒子
    private static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // 缓存服务器实例供其他方法使用
        cachedServer = event.getServer();

        // 遍历所有 ServerLevel
        event.getServer().getAllLevels().forEach(level -> {
            // 为每个在当前刻仍在追踪集合中的箭矢生成粒子
            level.getAllEntities().forEach(entity -> {
                if (!(entity instanceof Arrow arrow)) return;
                UUID arrowId = arrow.getUUID();
                if (!trackedArrows.contains(arrowId)) return;

                // 獲取箭矢發射時間
                Long spawnTime = arrowSpawnTime.get(arrowId);
                if (spawnTime == null) {
                    // 如果沒有記錄時間，停止追蹤
                    trackedArrows.remove(arrowId);
                    return;
                }

                // 檢查箭矢是否已經飛行超過5秒
                long currentTime = System.currentTimeMillis();
                boolean timeExpired = (currentTime - spawnTime) > 5000;

                // 如果箭矢已移除、死亡、落地，或時間超過5秒，則停止追蹤並移除箭矢
                if (arrow.isRemoved() || !arrow.isAlive() || arrow.onGround() || timeExpired) {
                    trackedArrows.remove(arrowId);
                    arrowSpawnTime.remove(arrowId);
                    soulBowArrows.remove(arrowId);

                    // 如果是因為時間到期，主動移除箭矢實體
                    if (timeExpired && !arrow.isRemoved()) {
                        arrow.discard();
                    }
                    return;
                }

                // 箭矢仍在飛行中，持續生成粒子
                Vec3 p = arrow.position();
                AAALevel.addParticle(level, true,
                        SOUL_BOW.clone().position(p.x, p.y, p.z));
                AAALevel.addParticle(level, true,
                        ARROW01.clone().position(p.x, p.y, p.z));

                // === 添加區域傷害邏輯 ===
                // 檢測箭矢周圍一格（1.0方塊）範圍內的敵對生物
                double damageRadius = 3.0;
                AABB boundingBox = new AABB(
                        p.x - damageRadius, p.y - damageRadius, p.z - damageRadius,
                        p.x + damageRadius, p.y + damageRadius, p.z + damageRadius
                );

                // 獲取範圍內的所有生物實體
                List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(
                        LivingEntity.class,
                        boundingBox,
                        e -> e.isAlive() && !e.isRemoved()
                );

                // 對範圍內的敵對生物造成傷害
                for (LivingEntity target : nearbyEntities) {
                    // 跳過箭矢的發射者（避免誤傷）
                    if (arrow.getOwner() != null && target.getUUID().equals(arrow.getOwner().getUUID())) {
                        continue;
                    }

                    // 只對怪物或敵對生物造成傷害
                    // 可以根據需要調整條件（例如只傷害怪物，或者也傷害其他玩家）
                    if (target instanceof Monster || (target instanceof Mob mob && mob.getTarget() != null)) {
                        // 創建傷害源
                        DamageSource damageSource;
                        if (arrow.getOwner() instanceof LivingEntity owner) {
                            damageSource = level.damageSources().arrow(arrow, owner);
                        } else {
                            damageSource = level.damageSources().arrow(arrow, arrow);
                        }

                        // 造成傷害（使用設定的箭矢傷害值）
                        float damage = (float) arrowDamage;
                        boolean damaged = target.hurt(damageSource, damage);

                        // 如果成功造成傷害，播放音效
                        if (damaged) {
                            level.playSound(null, target.blockPosition(),
                                    SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS,
                                    0.5F, 1.0F);
                        }
                    }
                }
            });
        });
    }

    /**
     * 取消玩家的技能激活状态
     */
    public static void cancelActivation(UUID playerId) {
        activatedPlayers.remove(playerId);
    }

    /**
     * 清除所有激活状态（用于玩家退出等情况）
     */
    public static void clearAll() {
        activatedPlayers.clear();
    }

    /**
     * 检查箭矢是否是射魂长弓技能发射的箭矢
     * @param arrowId 箭矢的UUID
     * @return 如果是射魂长弓箭矢返回true，否则返回false
     */
    public static boolean isSoulBowArrow(UUID arrowId) {
        return soulBowArrows.containsKey(arrowId);
    }

    /**
     * 根据箭矢UUID获取发射该箭矢的玩家
     * @param arrowId 箭矢的UUID
     * @return 发射箭矢的玩家，如果找不到返回null
     */
    public static ServerPlayer getShooterByArrow(UUID arrowId) {
        UUID playerId = soulBowArrows.get(arrowId);
        if (playerId == null) {
            return null;
        }

        // 使用缓存的服务器实例获取玩家
        if (cachedServer == null) {
            return null;
        }

        return cachedServer.getPlayerList().getPlayer(playerId);
    }
}
