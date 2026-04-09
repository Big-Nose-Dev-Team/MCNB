package com.yichenxbohan.mcnb.skill.api;

/**
 * 技能分支資料。
 */
public class SkillBranch {
    private final String id;
    private final String name;
    private final String description;
    private final double multiplierBonusPerLevel;
    private final int durationBonusPerLevel;
    private final SkillPrerequisite prerequisite;

    public SkillBranch(String id, String name, String description,
                       double multiplierBonusPerLevel,
                       int durationBonusPerLevel,
                       SkillPrerequisite prerequisite) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.multiplierBonusPerLevel = multiplierBonusPerLevel;
        this.durationBonusPerLevel = durationBonusPerLevel;
        this.prerequisite = prerequisite;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getMultiplierBonusPerLevel() {
        return multiplierBonusPerLevel;
    }

    public int getDurationBonusPerLevel() {
        return durationBonusPerLevel;
    }

    public SkillPrerequisite getPrerequisite() {
        return prerequisite;
    }
}

