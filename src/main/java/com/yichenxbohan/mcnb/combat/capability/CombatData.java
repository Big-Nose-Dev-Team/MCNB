package com.yichenxbohan.mcnb.combat.capability;

import com.yichenxbohan.mcnb.combat.CombatStats;
import net.minecraft.nbt.CompoundTag;

public class CombatData implements ICombatData {

    // ── 攻擊 ──
    private double physicalAttack   = 0;
    private double magicAttack      = 0;
    private double energyAttack     = 0;
    private double soulAttack       = 0;
    private double chaosAttack      = 0;
    private double spatialAttack    = 0;
    private double temporalAttack   = 0;
    private double trueAttack       = 0;
    private double weaponMultiplier = 1.0;
    private double penetration      = 30;
    private double critChance       = 0.2;
    private double critDamage       = 0.5;
    private double damageBonus      = 1.0;
    private double finalDamageMultiplier = 1.0;

    // ── 防禦（統一） ──
    private float  defense          = 0;
    private double damageReduction  = 0.1;
    private double evasion          = 10;

    // ── 特殊 ──
    private float energyShield    = 0;
    private float maxEnergyShield = 100;
    private float soulIntegrity   = 100;

    // ── 恢復 ──
    private double healingBonus = 0;
    private double regen        = 0;

    private boolean dirty = false;

    // ==================== getStats / setStats (legacy bridge) ====================

    @Override
    public CombatStats getStats() {
        CombatStats s = new CombatStats();
        s.physicalAttack       = physicalAttack;
        s.magicAttack          = magicAttack;
        s.energyAttack         = energyAttack;
        s.soulAttack           = soulAttack;
        s.chaosAttack          = chaosAttack;
        s.spatialAttack        = spatialAttack;
        s.temporalAttack       = temporalAttack;
        s.trueAttack           = trueAttack;
        s.weaponMultiplier     = weaponMultiplier;
        s.penetration          = penetration;
        s.critChance           = critChance;
        s.critDamage           = critDamage;
        s.damageBonus          = damageBonus;
        s.finalDamageMultiplier = finalDamageMultiplier;
        s.defense              = defense;
        s.damageReduction      = damageReduction;
        s.evasion              = evasion;
        s.healingBonus         = healingBonus;
        s.regen                = regen;
        return s;
    }

    @Override
    public void setStats(CombatStats s) {
        physicalAttack       = s.physicalAttack;
        magicAttack          = s.magicAttack;
        energyAttack         = s.energyAttack;
        soulAttack           = s.soulAttack;
        chaosAttack          = s.chaosAttack;
        spatialAttack        = s.spatialAttack;
        temporalAttack       = s.temporalAttack;
        trueAttack           = s.trueAttack;
        weaponMultiplier     = s.weaponMultiplier;
        penetration          = s.penetration;
        critChance           = s.critChance;
        critDamage           = s.critDamage;
        damageBonus          = s.damageBonus;
        finalDamageMultiplier = s.finalDamageMultiplier;
        defense              = (float) s.defense;
        damageReduction      = s.damageReduction;
        evasion              = s.evasion;
        healingBonus         = s.healingBonus;
        regen                = s.regen;
        markDirty();
    }

    // ==================== 攻擊 ====================

    @Override public double getPhysicalAttack()  { return physicalAttack; }
    @Override public void   setPhysicalAttack(double v)  { physicalAttack = v;  markDirty(); }
    @Override public double getMagicAttack()     { return magicAttack; }
    @Override public void   setMagicAttack(double v)     { magicAttack = v;     markDirty(); }
    @Override public double getEnergyAttack()    { return energyAttack; }
    @Override public void   setEnergyAttack(double v)    { energyAttack = v;    markDirty(); }
    @Override public double getSoulAttack()      { return soulAttack; }
    @Override public void   setSoulAttack(double v)      { soulAttack = v;      markDirty(); }
    @Override public double getChaosAttack()     { return chaosAttack; }
    @Override public void   setChaosAttack(double v)     { chaosAttack = v;     markDirty(); }
    @Override public double getSpatialAttack()   { return spatialAttack; }
    @Override public void   setSpatialAttack(double v)   { spatialAttack = v;   markDirty(); }
    @Override public double getTemporalAttack()  { return temporalAttack; }
    @Override public void   setTemporalAttack(double v)  { temporalAttack = v;  markDirty(); }
    @Override public double getTrueAttack()      { return trueAttack; }
    @Override public void   setTrueAttack(double v)      { trueAttack = v;      markDirty(); }

    @Override public double getWeaponMultiplier()        { return weaponMultiplier; }
    @Override public void   setWeaponMultiplier(double v){ weaponMultiplier = v; markDirty(); }
    @Override public double getPenetration()             { return penetration; }
    @Override public void   setPenetration(double v)     { penetration = v;     markDirty(); }
    @Override public double getCritChance()              { return critChance; }
    @Override public void   setCritChance(double v)      { critChance = v;      markDirty(); }
    @Override public double getCritDamage()              { return critDamage; }
    @Override public void   setCritDamage(double v)      { critDamage = v;      markDirty(); }
    @Override public double getDamageBonus()             { return damageBonus; }
    @Override public void   setDamageBonus(double v)     { damageBonus = v;     markDirty(); }
    @Override public double getFinalDamageMultiplier()           { return finalDamageMultiplier; }
    @Override public void   setFinalDamageMultiplier(double v)   { finalDamageMultiplier = v; markDirty(); }

    // ==================== 防禦 ====================

    @Override public float  getDefense()         { return defense; }
    @Override public void   setDefense(float v)  { defense = v;  markDirty(); }
    @Override public double getDamageReduction()         { return damageReduction; }
    @Override public void   setDamageReduction(double v) { damageReduction = v;  markDirty(); }
    @Override public double getEvasion()                 { return evasion; }
    @Override public void   setEvasion(double v)         { evasion = v;          markDirty(); }

    // ==================== 特殊 ====================

    @Override public float getEnergyShield()             { return energyShield; }
    @Override public void  setEnergyShield(float v)      { energyShield = Math.max(0, Math.min(v, maxEnergyShield)); markDirty(); }
    @Override public float getMaxEnergyShield()          { return maxEnergyShield; }
    @Override public void  setMaxEnergyShield(float v)   { maxEnergyShield = Math.max(0, v); if (energyShield > maxEnergyShield) energyShield = maxEnergyShield; markDirty(); }
    @Override public float getSoulIntegrity()            { return soulIntegrity; }
    @Override public void  setSoulIntegrity(float v)     { soulIntegrity = Math.max(0, Math.min(v, 100f)); markDirty(); }

    // ==================== 恢復 ====================

    @Override public double getHealingBonus()            { return healingBonus; }
    @Override public void   setHealingBonus(double v)    { healingBonus = v; markDirty(); }
    @Override public double getRegen()                   { return regen; }
    @Override public void   setRegen(double v)           { regen = v;    markDirty(); }

    // ==================== dirty / sync ====================

    @Override public void markDirty() { dirty = true; }
    public boolean isDirty()          { return dirty; }
    public void clearDirty()          { dirty = false; }

    @Override
    public void sync() { clearDirty(); }

    // ==================== NBT ====================

    public CompoundTag serializeNBT() {
        CompoundTag t = new CompoundTag();
        t.putDouble("physAtk",  physicalAttack);
        t.putDouble("magAtk",   magicAttack);
        t.putDouble("eneAtk",   energyAttack);
        t.putDouble("souAtk",   soulAttack);
        t.putDouble("chaAtk",   chaosAttack);
        t.putDouble("spaAtk",   spatialAttack);
        t.putDouble("temAtk",   temporalAttack);
        t.putDouble("truAtk",   trueAttack);
        t.putDouble("wpnMul",   weaponMultiplier);
        t.putDouble("pen",      penetration);
        t.putDouble("crit",     critChance);
        t.putDouble("critDmg",  critDamage);
        t.putDouble("dmgBonus", damageBonus);
        t.putDouble("finalMul", finalDamageMultiplier);
        t.putFloat ("physDef",  defense);
        t.putDouble("dmgRed",   damageReduction);
        t.putDouble("evasion",  evasion);
        t.putFloat ("es",       energyShield);
        t.putFloat ("maxEs",    maxEnergyShield);
        t.putFloat ("soul",     soulIntegrity);
        t.putDouble("healBonus",healingBonus);
        t.putDouble("regen",    regen);
        return t;
    }

    public void deserializeNBT(CompoundTag t) {
        physicalAttack       = t.getDouble("physAtk");
        magicAttack          = t.getDouble("magAtk");
        energyAttack         = t.getDouble("eneAtk");
        soulAttack           = t.getDouble("souAtk");
        chaosAttack          = t.getDouble("chaAtk");
        spatialAttack        = t.getDouble("spaAtk");
        temporalAttack       = t.getDouble("temAtk");
        trueAttack           = t.getDouble("truAtk");
        weaponMultiplier     = t.contains("wpnMul")  ? t.getDouble("wpnMul")  : 1.0;
        penetration          = t.contains("pen")     ? t.getDouble("pen")     : 30;
        critChance           = t.contains("crit")    ? t.getDouble("crit")    : 0.2;
        critDamage           = t.contains("critDmg") ? t.getDouble("critDmg") : 0.5;
        damageBonus          = t.contains("dmgBonus")? t.getDouble("dmgBonus"): 1.0;
        finalDamageMultiplier= t.contains("finalMul")? t.getDouble("finalMul"): 1.0;
        defense              = t.getFloat("physDef");
        damageReduction      = t.contains("dmgRed")  ? t.getDouble("dmgRed")  : 0.1;
        evasion              = t.contains("evasion") ? t.getDouble("evasion") : 10;
        energyShield         = t.getFloat("es");
        maxEnergyShield      = t.contains("maxEs")   ? t.getFloat("maxEs")    : 100;
        soulIntegrity        = t.contains("soul")    ? t.getFloat("soul")     : 100;
        healingBonus         = t.getDouble("healBonus");
        regen                = t.getDouble("regen");
    }
}
