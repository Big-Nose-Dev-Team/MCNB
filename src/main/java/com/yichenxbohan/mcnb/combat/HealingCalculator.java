package com.yichenxbohan.mcnb.combat;

public class HealingCalculator {
    public static double calculateHeal(double baseHeal, CombatStats target){
        return baseHeal * (1+ target.healingBonus);
    }

    public static double getRegenPerSecond(CombatStats target){
        return target.regen;
    }
}
