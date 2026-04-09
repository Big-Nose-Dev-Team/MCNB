package com.yichenxbohan.mcnb.skill.capability;

import net.minecraft.nbt.CompoundTag;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PlayerSkillData implements IPlayerSkillData {

    private final Map<String, Integer> levels = new HashMap<>();
    private final Map<String, String> branches = new HashMap<>();
    private boolean dirty;

    @Override
    public int getSkillLevel(String skillId) {
        return Math.max(0, levels.getOrDefault(skillId, 0));
    }

    @Override
    public void setSkillLevel(String skillId, int level) {
        if (skillId == null || skillId.isEmpty()) {
            return;
        }
        int clamped = Math.max(0, level);
        if (clamped == 0) {
            levels.remove(skillId);
            branches.remove(skillId);
        } else {
            levels.put(skillId, clamped);
        }
        markDirty();
    }

    @Override
    public String getSelectedBranch(String skillId) {
        return branches.get(skillId);
    }

    @Override
    public void setSelectedBranch(String skillId, String branchId) {
        if (skillId == null || skillId.isEmpty()) {
            return;
        }
        if (branchId == null || branchId.isEmpty()) {
            branches.remove(skillId);
        } else {
            branches.put(skillId, branchId);
        }
        markDirty();
    }

    @Override
    public boolean removeSkill(String skillId) {
        boolean changed = levels.remove(skillId) != null;
        changed |= branches.remove(skillId) != null;
        if (changed) {
            markDirty();
        }
        return changed;
    }

    @Override
    public boolean resetAll() {
        if (levels.isEmpty() && branches.isEmpty()) {
            return false;
        }
        levels.clear();
        branches.clear();
        markDirty();
        return true;
    }

    @Override
    public int getSpentSkillPoints() {
        int total = 0;
        for (int value : levels.values()) {
            total += Math.max(0, value);
        }
        return total;
    }

    @Override
    public int getTotalSkillPoints(int playerLevel) {
        return Math.max(0, playerLevel - 1) * SKILL_POINTS_PER_LEVEL;
    }

    @Override
    public int getAvailableSkillPoints(int playerLevel) {
        return Math.max(0, getTotalSkillPoints(playerLevel) - getSpentSkillPoints());
    }

    @Override
    public Map<String, Integer> getSkillLevels() {
        return Collections.unmodifiableMap(levels);
    }

    @Override
    public Map<String, String> getSelectedBranches() {
        return Collections.unmodifiableMap(branches);
    }

    @Override
    public void overwriteFrom(Map<String, Integer> newLevels, Map<String, String> newBranches) {
        levels.clear();
        branches.clear();
        for (Map.Entry<String, Integer> entry : newLevels.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null && entry.getValue() > 0) {
                levels.put(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<String, String> entry : newBranches.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null && !entry.getValue().isEmpty()) {
                branches.put(entry.getKey(), entry.getValue());
            }
        }
        markDirty();
    }

    @Override
    public void markDirty() {
        dirty = true;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void clearDirty() {
        dirty = false;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        CompoundTag levelTag = new CompoundTag();
        CompoundTag branchTag = new CompoundTag();

        for (Map.Entry<String, Integer> entry : levels.entrySet()) {
            levelTag.putInt(entry.getKey(), Math.max(0, entry.getValue()));
        }
        for (Map.Entry<String, String> entry : branches.entrySet()) {
            if (entry.getValue() != null) {
                branchTag.putString(entry.getKey(), entry.getValue());
            }
        }

        tag.put("skillLevels", levelTag);
        tag.put("skillBranches", branchTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        levels.clear();
        branches.clear();

        if (tag.contains("skillLevels")) {
            CompoundTag levelTag = tag.getCompound("skillLevels");
            for (String key : levelTag.getAllKeys()) {
                levels.put(key, Math.max(0, levelTag.getInt(key)));
            }
        }

        if (tag.contains("skillBranches")) {
            CompoundTag branchTag = tag.getCompound("skillBranches");
            for (String key : branchTag.getAllKeys()) {
                branches.put(key, branchTag.getString(key));
            }
        }
    }
}

