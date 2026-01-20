package com.yichenxbohan.mcnb.combat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatsProvider {
    private static final Logger log = LoggerFactory.getLogger(StatsProvider.class);

    public static  CombatStats get(LivingEntity entity) {
        CombatStats stats = new CombatStats();

        //example
        stats.physicalAttack = entity.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
        stats.magicAttack = 0;
        stats.damageBonus = 1.0;
        stats.finalDamageMultiplier = 1.0;

        stats.weaponMultiplier = 1.0;
        stats.penetration = 30;

        stats.critChance = 0.2;
        stats.critDamage = 0.5;

        stats.defense = entity.getArmorValue();
        stats.damageReduction = 0.1;
        stats.evasion = 10;

        return stats;
    }
}
