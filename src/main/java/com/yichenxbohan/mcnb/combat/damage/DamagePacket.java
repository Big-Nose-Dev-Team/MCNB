package com.yichenxbohan.mcnb.combat.damage;

import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;

/**
 * 傷害數據包 - 封裝一次傷害事件的所有信息
 */
public class DamagePacket {

    /** 攻擊者 */
    public LivingEntity attacker;

    /** 目標 */
    public LivingEntity target;

    /** 各類型傷害數值 */
    public EnumMap<DamageTypeEx, Float> damage = new EnumMap<>(DamageTypeEx.class);

    /** 暴擊率 (0.0 - 1.0) */
    public float critRate;

    /** 暴擊倍率 (例如 1.5 = 150% 傷害) */
    public float critMultiplier = 1.5f;

    /** 是否可以暴擊 */
    public boolean canCrit = true;

    /** 是否無視無敵幀 */
    public boolean bypassInvul = false;

    /** 穿透值 - 用於計算命中和減少防禦效果 */
    public float penetration = 0f;

    /** 傷害加成倍率 */
    public float damageBonus = 0f;

    /** 最終傷害倍率 */
    public float finalDamageMultiplier = 1.0f;

    public DamagePacket(LivingEntity attacker, LivingEntity target) {
        this.attacker = attacker;
        this.target = target;

        for (DamageTypeEx type : DamageTypeEx.values()) {
            damage.put(type, 0.0f);
        }
    }

    /**
     * 獲取指定類型的傷害值
     */
    public float get(DamageTypeEx type) {
        return damage.getOrDefault(type, 0.0f);
    }

    /**
     * 添加指定類型的傷害
     */
    public DamagePacket add(DamageTypeEx type, float value) {
        damage.put(type, get(type) + value);
        return this;
    }

    /**
     * 設置指定類型的傷害
     */
    public DamagePacket set(DamageTypeEx type, float value) {
        damage.put(type, value);
        return this;
    }

    /**
     * 獲取總傷害值（未經計算的原始值）
     */
    public float getTotalRawDamage() {
        float total = 0f;
        for (Float dmg : damage.values()) {
            total += dmg;
        }
        return total;
    }

    /**
     * 檢查是否包含指定類型的傷害
     */
    public boolean hasDamageType(DamageTypeEx type) {
        return get(type) > 0;
    }

    /**
     * 檢查是否包含任何會造成禁療的傷害類型
     */
    public boolean hasAntiHeal() {
        for (DamageTypeEx type : DamageTypeEx.values()) {
            if (type.antiHeal && hasDamageType(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 檢查是否包含任何會造成緩速的傷害類型
     */
    public boolean hasSlow() {
        for (DamageTypeEx type : DamageTypeEx.values()) {
            if (type.causesSlow && hasDamageType(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 建造者模式 - 設置暴擊參數
     */
    public DamagePacket withCrit(float rate, float multiplier) {
        this.critRate = rate;
        this.critMultiplier = multiplier;
        return this;
    }

    /**
     * 建造者模式 - 設置穿透值
     */
    public DamagePacket withPenetration(float penetration) {
        this.penetration = penetration;
        return this;
    }

    /**
     * 建造者模式 - 設置傷害加成
     */
    public DamagePacket withDamageBonus(float bonus) {
        this.damageBonus = bonus;
        return this;
    }

    /**
     * 建造者模式 - 設置最終傷害倍率
     */
    public DamagePacket withFinalMultiplier(float multiplier) {
        this.finalDamageMultiplier = multiplier;
        return this;
    }
}
