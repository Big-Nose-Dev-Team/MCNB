package com.yichenxbohan.mcnb.skill.api;

import com.yichenxbohan.mcnb.playerclass.PlayerClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 技能定義，供 GUI、升級檢查與施放系統共用。
 */
public class SkillDefinition {
    private final String id;
    private final PlayerClass ownerClass;
    private final String displayName;
    private final String description;
    private final SkillCategory category;
    private final SkillCastType castType;
    private final SkillAimType aimType;
    private final int maxLevel;
    private final double baseMultiplier;
    private final double multiplierPerLevel;
    private final int baseDurationTicks;
    private final int durationPerLevel;
    private final double physicalScaling;
    private final double magicScaling;
    private final List<SkillPrerequisite> prerequisites;
    private final List<SkillBranch> branches;
    private final SkillExecutor customExecutor;

    private SkillDefinition(Builder builder) {
        this.id = builder.id;
        this.ownerClass = builder.ownerClass;
        this.displayName = builder.displayName;
        this.description = builder.description;
        this.category = builder.category;
        this.castType = builder.castType;
        this.aimType = builder.aimType;
        this.maxLevel = builder.maxLevel;
        this.baseMultiplier = builder.baseMultiplier;
        this.multiplierPerLevel = builder.multiplierPerLevel;
        this.baseDurationTicks = builder.baseDurationTicks;
        this.durationPerLevel = builder.durationPerLevel;
        this.physicalScaling = builder.physicalScaling;
        this.magicScaling = builder.magicScaling;
        this.prerequisites = Collections.unmodifiableList(new ArrayList<>(builder.prerequisites));
        this.branches = Collections.unmodifiableList(new ArrayList<>(builder.branches));
        this.customExecutor = builder.customExecutor;
    }

    public String getId() {
        return id;
    }

    public PlayerClass getOwnerClass() {
        return ownerClass;
    }

    /**
     * 這個技能隸屬的職業顯示名稱（給 GUI / 日誌快速使用）。
     */
    public String getOwnerClassDisplayName() {
        return ownerClass.displayName;
    }

    /**
     * 檢查技能是否屬於指定職業。
     */
    public boolean belongsTo(PlayerClass playerClass) {
        return playerClass != null && ownerClass == playerClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public SkillCategory getCategory() {
        return category;
    }

    public SkillCastType getCastType() {
        return castType;
    }

    public SkillAimType getAimType() {
        return aimType;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public double getBaseMultiplier() {
        return baseMultiplier;
    }

    public double getMultiplierPerLevel() {
        return multiplierPerLevel;
    }

    public int getBaseDurationTicks() {
        return baseDurationTicks;
    }

    public int getDurationPerLevel() {
        return durationPerLevel;
    }

    public double getPhysicalScaling() {
        return physicalScaling;
    }

    public double getMagicScaling() {
        return magicScaling;
    }

    public List<SkillPrerequisite> getPrerequisites() {
        return prerequisites;
    }

    public List<SkillBranch> getBranches() {
        return branches;
    }

    public SkillExecutor getCustomExecutor() {
        return customExecutor;
    }

    public double getMultiplierAtLevel(int level) {
        int lv = Math.max(1, Math.min(maxLevel, level));
        return baseMultiplier + (lv - 1) * multiplierPerLevel;
    }

    public int getDurationAtLevel(int level) {
        int lv = Math.max(1, Math.min(maxLevel, level));
        return baseDurationTicks + (lv - 1) * durationPerLevel;
    }

    public SkillBranch findBranch(String branchId) {
        for (SkillBranch branch : branches) {
            if (branch.getId().equals(branchId)) {
                return branch;
            }
        }
        return null;
    }

    public static Builder builder(String id, PlayerClass ownerClass) {
        return new Builder(id, ownerClass);
    }

    public static class Builder {
        private final String id;
        private final PlayerClass ownerClass;
        private String displayName = "";
        private String description = "";
        private SkillCategory category = SkillCategory.ATTACK;
        private SkillCastType castType = SkillCastType.INSTANT;
        private SkillAimType aimType = SkillAimType.LOOK_TARGET_BLOCK;
        private int maxLevel = 10;
        private double baseMultiplier = 1.0;
        private double multiplierPerLevel = 0.1;
        private int baseDurationTicks = 40;
        private int durationPerLevel = 10;
        private double physicalScaling = 1.0;
        private double magicScaling = 0.0;
        private final List<SkillPrerequisite> prerequisites = new ArrayList<>();
        private final List<SkillBranch> branches = new ArrayList<>();
        private SkillExecutor customExecutor = null;

        private Builder(String id, PlayerClass ownerClass) {
            this.id = id;
            this.ownerClass = ownerClass;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder category(SkillCategory category) {
            this.category = category;
            return this;
        }

        public Builder castType(SkillCastType castType) {
            this.castType = castType;
            return this;
        }

        public Builder aimType(SkillAimType aimType) {
            this.aimType = aimType;
            return this;
        }

        public Builder maxLevel(int maxLevel) {
            this.maxLevel = Math.max(1, maxLevel);
            return this;
        }

        public Builder multiplier(double baseMultiplier, double multiplierPerLevel) {
            this.baseMultiplier = baseMultiplier;
            this.multiplierPerLevel = multiplierPerLevel;
            return this;
        }

        public Builder duration(int baseDurationTicks, int durationPerLevel) {
            this.baseDurationTicks = Math.max(0, baseDurationTicks);
            this.durationPerLevel = Math.max(0, durationPerLevel);
            return this;
        }

        public Builder scaling(double physicalScaling, double magicScaling) {
            this.physicalScaling = Math.max(0, physicalScaling);
            this.magicScaling = Math.max(0, magicScaling);
            return this;
        }

        public Builder prerequisite(String requiredSkillId, int minLevel) {
            this.prerequisites.add(new SkillPrerequisite(requiredSkillId, minLevel));
            return this;
        }

        public Builder branch(SkillBranch branch) {
            this.branches.add(branch);
            return this;
        }

        public Builder customExecutor(SkillExecutor customExecutor) {
            this.customExecutor = customExecutor;
            return this;
        }

        public SkillDefinition build() {
            return new SkillDefinition(this);
        }
    }
}

