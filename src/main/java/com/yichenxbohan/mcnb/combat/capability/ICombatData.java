package com.yichenxbohan.mcnb.combat.capability;

import com.yichenxbohan.mcnb.combat.CombatStats;

public interface ICombatData {
    CombatStats getStats();

    void setStats(CombatStats stats);

    float getPhysicalDefense();
    float getEnergyDefense();
    float getMagicDefense();
    float getChaosDefense();
    float getSoulDefense();

    float getEnergyShield();
    void setEnergyShield(float value);

    float getMaxEnergyShield();

    float getSoulIntegrity();
    void setSoulIntegrity(float value);

    void markDirty();

    void sync();
}

