package com.yichenxbohan.mcnb.combat;

import java.util.Random;

import static net.minecraft.util.Mth.clamp;

public class DamageCalculator {
    private static final Random RANDOM = new Random();

    private static final double DEFENSE_K = 100.0;

    public static DamageResult calculate(
            CombatStats attacker,
            CombatStats target,
            boolean isMagic
    ){
        //命中判定
        double hitChance = attacker.penetration / (attacker.penetration + target.evasion);
        hitChance = clamp(hitChance, 0.2, 1.0);

        if(RANDOM.nextDouble() > hitChance) {
            return new DamageResult(false, false, 0);
        }

        //basic attack
        double baseAtk = isMagic ? attacker.magicAttack : attacker.physicalAttack;
        double damage = baseAtk * attacker.weaponMultiplier;

        //crit
        boolean crit = RANDOM.nextDouble() < clamp(attacker.critChance, 0, 0.75);
        if(crit){
            damage *= (1.5 + attacker.critDamage);
        }

        //pen & defense
        double penetrationRatio = attacker.penetration / (attacker.penetration + target.defense + DEFENSE_K);

        double effectiveDefense = target.defense - (1 - penetrationRatio);
        damage = damage * (100 / (100 + effectiveDefense));

        //damage bonus
        damage *= (1 + attacker.damageBonus);

        //damage reduction
        damage *= (1 - clamp(target.damageReduction, 0, 0.8));

        //final damage
        damage *= attacker.finalDamageMultiplier;

        return new DamageResult(true, crit, Math.max(0, damage));

    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
