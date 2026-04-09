package com.yichenxbohan.mcnb.skill.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * 技能執行時上下文。
 */
public class SkillExecutionContext {
    private final ServerPlayer player;
    private final SkillDefinition skill;
    private final int level;
    private final SkillBranch selectedBranch;
    private final Vec3 targetPosition;
    private final double computedDamage;
    private final int computedDuration;

    public SkillExecutionContext(ServerPlayer player,
                                 SkillDefinition skill,
                                 int level,
                                 SkillBranch selectedBranch,
                                 Vec3 targetPosition,
                                 double computedDamage,
                                 int computedDuration) {
        this.player = player;
        this.skill = skill;
        this.level = level;
        this.selectedBranch = selectedBranch;
        this.targetPosition = targetPosition;
        this.computedDamage = computedDamage;
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

    public SkillBranch getSelectedBranch() {
        return selectedBranch;
    }

    public Vec3 getTargetPosition() {
        return targetPosition;
    }

    public double getComputedDamage() {
        return computedDamage;
    }

    public int getComputedDuration() {
        return computedDuration;
    }
}

