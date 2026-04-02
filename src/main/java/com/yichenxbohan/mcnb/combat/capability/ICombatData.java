package com.yichenxbohan.mcnb.combat.capability;

import com.yichenxbohan.mcnb.combat.CombatStats;

public interface ICombatData {
    CombatStats getStats();
    void setStats(CombatStats stats);

    // ── 攻擊 ──
    double getPhysicalAttack();
    void   setPhysicalAttack(double v);
    double getMagicAttack();
    void   setMagicAttack(double v);
    double getEnergyAttack();
    void   setEnergyAttack(double v);
    double getSoulAttack();
    void   setSoulAttack(double v);
    double getChaosAttack();
    void   setChaosAttack(double v);
    double getSpatialAttack();
    void   setSpatialAttack(double v);
    double getTemporalAttack();
    void   setTemporalAttack(double v);
    double getTrueAttack();
    void   setTrueAttack(double v);

    double getWeaponMultiplier();
    void   setWeaponMultiplier(double v);
    double getPenetration();
    void   setPenetration(double v);
    double getCritChance();
    void   setCritChance(double v);
    double getCritDamage();
    void   setCritDamage(double v);
    double getDamageBonus();
    void   setDamageBonus(double v);
    double getFinalDamageMultiplier();
    void   setFinalDamageMultiplier(double v);

    // ── 防禦 ──
    float getDefense();
    void  setDefense(float v);
    double getDamageReduction();
    void   setDamageReduction(double v);
    double getEvasion();
    void   setEvasion(double v);

    // ── 特殊 ──
    float getEnergyShield();
    void  setEnergyShield(float value);
    float getMaxEnergyShield();
    void  setMaxEnergyShield(float value);
    float getSoulIntegrity();
    void  setSoulIntegrity(float value);

    // ── 恢復 ──
    double getHealingBonus();
    void   setHealingBonus(double v);
    double getRegen();
    void   setRegen(double v);

    void markDirty();
    void sync();
}
