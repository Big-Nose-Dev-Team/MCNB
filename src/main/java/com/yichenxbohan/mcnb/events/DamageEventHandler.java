package com.yichenxbohan.mcnb.events;

import com.yichenxbohan.mcnb.combat.*;
import com.yichenxbohan.mcnb.combat.damage.DamageHandler;
import com.yichenxbohan.mcnb.combat.damage.DamagePacket;
import com.yichenxbohan.mcnb.combat.damage.DamageTypeEx;
import net.minecraft.server.level.ServerLevel;
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

        // 獲取戰鬥屬性
        CombatStats atkStats = StatsProvider.get(attacker);
        CombatStats defStats = StatsProvider.get(target);

        // 判斷傷害類型
        DamageTypeEx damageType = getDamageType(event.getSource());

        // 使用新的傷害系統
        DamagePacket packet = new DamagePacket(attacker, target)
            .set(damageType, event.getAmount()) // 使用原始傷害值
            .withCrit((float) atkStats.critChance, (float) (1.5 + atkStats.critDamage))
            .withPenetration((float) atkStats.penetration)
            .withDamageBonus((float) atkStats.damageBonus)
            .withFinalMultiplier((float) atkStats.finalDamageMultiplier);

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
            case "lightningBolt" -> DamageTypeEx.ENERGY;
            case "wither" -> DamageTypeEx.SOUL;
            case "sonic_boom" -> DamageTypeEx.CHAOS; // 監守者的音波攻擊
            case "freeze" -> DamageTypeEx.SPATIAL;
            case "outOfWorld" -> DamageTypeEx.TRUE; // 虛空傷害
            case "fall", "flyIntoWall" -> DamageTypeEx.PHYSICAL; // 摔落傷害
            case "onFire", "inFire", "lava" -> DamageTypeEx.ENERGY; // 火焰傷害
            case "drown", "starve" -> DamageTypeEx.TRUE; // 溺水、飢餓
            case "cactus", "sweetBerryBush", "stalagmite" -> DamageTypeEx.PHYSICAL; // 環境傷害
            default -> DamageTypeEx.PHYSICAL;
        };
    }
}
