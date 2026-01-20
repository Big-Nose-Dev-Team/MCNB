package com.yichenxbohan.mcnb.combat;

public class DamageResult {
    public final boolean hit;
    public final boolean critical;
    public final double damage;

    public DamageResult(boolean hit, boolean critical, double damage){
        this.hit = hit;
        this.critical = critical;
        this.damage = damage;
    }
}
