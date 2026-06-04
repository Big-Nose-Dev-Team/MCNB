package com.yichenxbohan.mcnb.skill.capability;

import java.util.Map;

public interface IPlayerSkillData {
    int SKILL_POINTS_PER_LEVEL = 1;

    int getSkillLevel(String skillId);

    void setSkillLevel(String skillId, int level);

    long getSkillCooldownEndTick(String skillId);

    void setSkillCooldownEndTick(String skillId, long gameTick);

    int getRemainingCooldownTicks(String skillId, long currentGameTick);

    boolean removeSkill(String skillId);

    boolean resetAll();

    int getSpentSkillPoints();

    int getTotalSkillPoints(int playerLevel);

    int getAvailableSkillPoints(int playerLevel);

    Map<String, Integer> getSkillLevels();

    Map<String, Long> getSkillCooldowns();

    void overwriteFrom(Map<String, Integer> levels, Map<String, Long> cooldowns);

    void markDirty();

    boolean isDirty();

    void clearDirty();
}

