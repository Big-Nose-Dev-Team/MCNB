package com.yichenxbohan.mcnb.skill.capability;

import net.minecraft.nbt.CompoundTag;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PlayerSkillData implements IPlayerSkillData {

    private final Map<String, Integer> levels = new HashMap<>();
    private final Map<String, Long> cooldownEndTicks = new HashMap<>();
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
            cooldownEndTicks.remove(skillId);
        } else {
            levels.put(skillId, clamped);
        }
        markDirty();
    }

    @Override
    public long getSkillCooldownEndTick(String skillId) {
        return Math.max(0L, cooldownEndTicks.getOrDefault(skillId, 0L));
    }

    @Override
    public void setSkillCooldownEndTick(String skillId, long gameTick) {
        if (skillId == null || skillId.isEmpty()) {
            return;
        }
        long clamped = Math.max(0L, gameTick);
        if (clamped == 0L) {
            cooldownEndTicks.remove(skillId);
        } else {
            cooldownEndTicks.put(skillId, clamped);
        }
        markDirty();
    }

    @Override
    public int getRemainingCooldownTicks(String skillId, long currentGameTick) {
        long endTick = getSkillCooldownEndTick(skillId);
        long remain = endTick - Math.max(0L, currentGameTick);
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, remain));
    }

    @Override
    public boolean removeSkill(String skillId) {
        boolean changed = levels.remove(skillId) != null;
        changed |= cooldownEndTicks.remove(skillId) != null;
        if (changed) {
            markDirty();
        }
        return changed;
    }

    @Override
    public boolean resetAll() {
        if (levels.isEmpty() && cooldownEndTicks.isEmpty()) {
            return false;
        }
        levels.clear();
        cooldownEndTicks.clear();
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
    public Map<String, Long> getSkillCooldowns() {
        return Collections.unmodifiableMap(cooldownEndTicks);
    }

    @Override
    public void overwriteFrom(Map<String, Integer> newLevels, Map<String, Long> newCooldowns) {
        levels.clear();
        cooldownEndTicks.clear();
        for (Map.Entry<String, Integer> entry : newLevels.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null && entry.getValue() > 0) {
                levels.put(entry.getKey(), entry.getValue());
            }
        }
        for (Map.Entry<String, Long> entry : newCooldowns.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue() != null && entry.getValue() > 0L) {
                cooldownEndTicks.put(entry.getKey(), entry.getValue());
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
        CompoundTag cooldownTag = new CompoundTag();

        for (Map.Entry<String, Integer> entry : levels.entrySet()) {
            levelTag.putInt(entry.getKey(), Math.max(0, entry.getValue()));
        }
        for (Map.Entry<String, Long> entry : cooldownEndTicks.entrySet()) {
            cooldownTag.putLong(entry.getKey(), Math.max(0L, entry.getValue()));
        }

        tag.put("skillLevels", levelTag);
        tag.put("skillCooldownEndTicks", cooldownTag);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        levels.clear();
        cooldownEndTicks.clear();

        if (tag.contains("skillLevels")) {
            CompoundTag levelTag = tag.getCompound("skillLevels");
            for (String key : levelTag.getAllKeys()) {
                levels.put(key, Math.max(0, levelTag.getInt(key)));
            }
        }

        if (tag.contains("skillCooldownEndTicks")) {
            CompoundTag cooldownTag = tag.getCompound("skillCooldownEndTicks");
            for (String key : cooldownTag.getAllKeys()) {
                cooldownEndTicks.put(key, Math.max(0L, cooldownTag.getLong(key)));
            }
        }

        // Legacy compatibility: drop old branch data key if it exists in old saves.
        if (tag.contains("skillBranches")) {
            markDirty();
        }
    }
}

