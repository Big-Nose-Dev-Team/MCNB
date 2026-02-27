package com.yichenxbohan.mcnb.combat.capability;

import com.yichenxbohan.mcnb.combat.CombatStats;

public class CombatData implements ICombatData {

    private CombatStats stats = new CombatStats();
    private boolean dirty = false;

    // 額外的屬性
    private float energyShield = 0.0f;
    private float maxEnergyShield = 100.0f;
    private float soulIntegrity = 100.0f;

    @Override
    public CombatStats getStats() {
        return stats;
    }

    @Override
    public void setStats(CombatStats stats) {
        this.stats = stats;
        markDirty();
    }

    @Override
    public float getPhysicalDefense() {
        return (float) stats.defense;
    }

    @Override
    public float getEnergyDefense() {
        return (float) stats.energyResistance;
    }

    @Override
    public float getMagicDefense() {
        return (float) stats.magicResistance;
    }

    @Override
    public float getChaosDefense() {
        return (float) stats.chaosResistance;
    }

    @Override
    public float getSoulDefense() {
        return (float) stats.soulResistance;
    }

    @Override
    public float getEnergyShield() {
        return energyShield;
    }

    @Override
    public void setEnergyShield(float value) {
        this.energyShield = Math.max(0, Math.min(value, maxEnergyShield));
        markDirty();
    }

    @Override
    public float getMaxEnergyShield() {
        return maxEnergyShield;
    }

    @Override
    public float getSoulIntegrity() {
        return soulIntegrity;
    }

    @Override
    public void setSoulIntegrity(float value) {
        this.soulIntegrity = Math.max(0, Math.min(value, 100.0f));
        markDirty();
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    @Override
    public void sync() {
        // TODO: 實現同步邏輯，將數據同步到客戶端
        // 這裡應該發送網路包到客戶端更新戰鬥數據
        clearDirty();
    }

    // 額外的工具方法
    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        dirty = false;
    }

    /**
     * 設置最大能量護盾值
     */
    public void setMaxEnergyShield(float maxEnergyShield) {
        this.maxEnergyShield = Math.max(0, maxEnergyShield);
        // 確保當前護盾值不超過最大值
        if (this.energyShield > this.maxEnergyShield) {
            this.energyShield = this.maxEnergyShield;
        }
        markDirty();
    }

    /**
     * 恢復能量護盾
     */
    public void restoreEnergyShield(float amount) {
        setEnergyShield(energyShield + amount);
    }

    /**
     * 消耗能量護盾
     */
    public float consumeEnergyShield(float damage) {
        float blocked = Math.min(damage, energyShield);
        setEnergyShield(energyShield - blocked);
        return damage - blocked; // 返回剩餘傷害
    }

    /**
     * 檢查能量護盾是否已滿
     */
    public boolean isEnergyShieldFull() {
        return energyShield >= maxEnergyShield;
    }

    /**
     * 檢查靈魂完整性是否完好
     */
    public boolean isSoulIntact() {
        return soulIntegrity >= 100.0f;
    }

    /**
     * 損害靈魂完整性
     */
    public void damageSoulIntegrity(float damage) {
        setSoulIntegrity(soulIntegrity - damage);
    }

    /**
     * 恢復靈魂完整性
     */
    public void healSoulIntegrity(float heal) {
        setSoulIntegrity(soulIntegrity + heal);
    }
}
