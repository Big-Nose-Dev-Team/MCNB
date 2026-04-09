package com.yichenxbohan.mcnb.skill.capability;

import java.util.Map;

public interface IPlayerSkillData {
    int SKILL_POINTS_PER_LEVEL = 1;

    int getSkillLevel(String skillId);

    void setSkillLevel(String skillId, int level);

    String getSelectedBranch(String skillId);

    void setSelectedBranch(String skillId, String branchId);

    boolean removeSkill(String skillId);

    boolean resetAll();

    int getSpentSkillPoints();

    int getTotalSkillPoints(int playerLevel);

    int getAvailableSkillPoints(int playerLevel);

    Map<String, Integer> getSkillLevels();

    Map<String, String> getSelectedBranches();

    void overwriteFrom(Map<String, Integer> levels, Map<String, String> branches);

    void markDirty();

    boolean isDirty();

    void clearDirty();
}

