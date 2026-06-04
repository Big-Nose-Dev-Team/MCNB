package com.yichenxbohan.mcnb.skill.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 技能執行時上下文。
 */
public class SkillExecutionContext {
    public enum ComputedDamageType {
        PHYSICAL,
        MAGIC,
        TOTAL
    }

    private final ServerPlayer player;
    private final SkillDefinition skill;
    private final int level;
    private final Vec3 targetPosition;
    private final double computedDamage;
    private final double computedPhysicalDamage;
    private final double computedMagicDamage;
    private final int computedDuration;

    public SkillExecutionContext(ServerPlayer player,
                                 SkillDefinition skill,
                                 int level,
                                 Vec3 targetPosition,
                                 double computedDamage,
                                 double computedPhysicalDamage,
                                 double computedMagicDamage,
                                 int computedDuration) {
        this.player = player;
        this.skill = skill;
        this.level = level;
        this.targetPosition = targetPosition;
        this.computedDamage = computedDamage;
        this.computedPhysicalDamage = computedPhysicalDamage;
        this.computedMagicDamage = computedMagicDamage;
        this.computedDuration = computedDuration;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public SkillDefinition getSkill() {
        return skill;
    }

    public int getLevel() {
        return level;
    }


    public Vec3 getTargetPosition() {
        return targetPosition;
    }

    public double getComputedDamage() {
        return computedDamage;
    }

    public double getComputedPhysicalDamage() {
        return computedPhysicalDamage;
    }

    public double getComputedMagicDamage() {
        return computedMagicDamage;
    }

    public double getComputedDamage(ComputedDamageType type) {
        return switch (type) {
            case PHYSICAL -> computedPhysicalDamage;
            case MAGIC -> computedMagicDamage;
            case TOTAL -> computedDamage;
        };
    }

    public int getComputedDuration() {
        return computedDuration;
    }
}

