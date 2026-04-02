package com.yichenxbohan.mcnb.events;

import com.yichenxbohan.mcnb.combat.*;
import com.yichenxbohan.mcnb.combat.damage.DamageHandler;
import com.yichenxbohan.mcnb.combat.damage.DamagePacket;
import com.yichenxbohan.mcnb.combat.damage.DamageTypeEx;
import com.yichenxbohan.mcnb.level.LevelDamageModifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DamageEventHandler {

    /**
     * 攔截所有傷害事件，使用自定義傷害系統處理
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        // 只在服務端處理
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        LivingEntity target = event.getEntity();

        // 跳過我們自己的傷害來源，避免無限循環
        if (event.getSource().getMsgId().equals("custom")) {
            return;
        }

        // 確保是 ServerLevel
        if (!(attacker.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        event.setCanceled(true);

        // ===== 等級差距檢查 =====
        float levelMult = LevelDamageModifier.getMultiplier(attacker, target);
        if (levelMult <= 0f) {
            // 等級差距過大，無法造成傷害
            // 若攻擊者是玩家則提示
            if (attacker instanceof ServerPlayer sp) {
                int atkLv = LevelDamageModifier.getLevel(attacker);
                int defLv = LevelDamageModifier.getLevel(target);
                sp.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                        "§c等級差距過大（你 Lv." + atkLv + " / 目標 Lv." + defLv + "），無法造成傷害！"
                    )
                );
            }
            return;
        }

        // 獲取戰鬥屬性
        CombatStats atkStats = StatsProvider.get(attacker);
        CombatStats defStats = StatsProvider.get(target);

        // 判斷傷害類型
        DamageTypeEx damageType = getDamageType(event.getSource());

        // 物理傷害沿用原版攻擊值；非物理類型取 atkStats 對應的攻擊力
        // （其他類型攻擊力已在 StatsProvider 中以 magicAttack 為基礎計算好）
        float damageValue = (damageType == DamageTypeEx.PHYSICAL)
                ? event.getAmount()
                : (float) atkStats.getAttackFor(damageType);

        // 使用新的傷害系統
        DamagePacket packet = new DamagePacket(attacker, target)
            .set(damageType, damageValue)
            .withCrit((float) atkStats.critChance, (float) (1.5 + atkStats.critDamage))
            .withPenetration((float) atkStats.penetration)
            .withDamageBonus((float) atkStats.damageBonus)
            .withFinalMultiplier((float)(atkStats.finalDamageMultiplier * levelMult)); // 考慮等級差距

        // 計算傷害（不直接造成，我們自己處理）
        DamageResult result = DamageHandler.calculate(packet, atkStats, defStats);

        if (!result.hit || result.damage <= 0) {
            return;
        }

        // 使用自定義傷害來源造成傷害
        DamageSource source = ModDamageSources.custom(serverLevel, attacker);

        target.invulnerableTime = 0;
        target.hurtMarked = true;
        target.hurt(source, (float) result.damage);

        // 應用特殊效果（禁療、緩速等）
        DamageHandler.applyEffects(result, target, defStats);

        // 發送傷害數字到客戶端（確保一定發送）
        DamageHandler.sendDamageNumber(target, result.damage, damageType, result.critical);
    }

    /**
     * 監聽最終傷害事件 - 確保所有傷害都顯示數字（包括環境傷害、摔落傷害等）
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        // 只在服務端處理
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        LivingEntity target = event.getEntity();
        float damage = event.getAmount();

        // 如果傷害為0或以下，不顯示
        if (damage <= 0) {
            return;
        }

        // 確保是 ServerLevel
        if (!(target.level() instanceof ServerLevel)) {
            return;
        }

        // 判斷傷害類型
        DamageTypeEx damageType = getDamageType(event.getSource());

        // 判斷是否暴擊（如果有攻擊者且是我們的自定義傷害，暴擊已經在前面處理了）
        // 這裡主要處理環境傷害等，所以不算暴擊
        boolean isCrit = false;

        // 如果是我們自己的傷害來源，不重複發送（已經在 onLivingHurt 中發送了）
        if (event.getSource().getMsgId().equals("custom")) {
            return;
        }

        // 發送傷害數字
        DamageHandler.sendDamageNumber(target, damage, damageType, isCrit);
    }

    /**
     * 根據原版傷害來源判斷傷害類型
     */
    private static DamageTypeEx getDamageType(DamageSource source) {
        String msgId = source.getMsgId();

        return switch (msgId) {
            case "magic", "indirectMagic", "witherSkull", "dragonBreath" -> DamageTypeEx.MAGIC;
            case "lightningBolt", "onFire", "inFire", "lava"             -> DamageTypeEx.ENERGY;
            case "wither"                                                  -> DamageTypeEx.SOUL;
            case "sonic_boom"                                              -> DamageTypeEx.CHAOS;
            case "freeze"                                                  -> DamageTypeEx.SPATIAL;
            case "outOfWorld", "drown", "starve"                          -> DamageTypeEx.TRUE;
            default                                                        -> DamageTypeEx.PHYSICAL;
        };
    }
}
