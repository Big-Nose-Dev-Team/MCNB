package com.yichenxbohan.mcnb.skill.api;

/**
 * 技能前置條件。
 */
public class SkillPrerequisite {
    private final String requiredSkillId;
    private final int minLevel;

    public SkillPrerequisite(String requiredSkillId, int minLevel) {
        this.requiredSkillId = requiredSkillId;
        this.minLevel = Math.max(1, minLevel);
    }

    public String getRequiredSkillId() {
        return requiredSkillId;
    }

    public int getMinLevel() {
        return minLevel;
    }
}

