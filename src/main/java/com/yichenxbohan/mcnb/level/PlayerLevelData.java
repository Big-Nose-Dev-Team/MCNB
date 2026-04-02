package com.yichenxbohan.mcnb.level;

import net.minecraft.nbt.CompoundTag;

/**
 * 玩家等級系統的資料實作
 * 含 NBT 序列化，支援存檔讀取
 */
public class PlayerLevelData implements IPlayerLevel {

    public static final int MAX_LEVEL = 100;

    private int level = 1;
    private long exp = 0;
    private boolean dirty = false;

    // ==================== 經驗公式 ====================
    // 從 level 升到 level+1 所需的經驗
    // 公式：floor( n² × 100 × 1.08^(n-1) )
    // Lv.1→2:    100    Lv.10→11:  2,159   Lv.30→31:  59,368
    // Lv.50→51:  469,016   Lv.80→81: 8,826,520   Lv.99→100: ~65,000,000
    // 調整：後期經驗成長更陡峭，將成長倍率改為 1.12
    public static long expNeededForLevel(int level) {
        double base = (double) level * level * 100.0;
        double scale = Math.pow(1.12, level - 1);
        return (long)Math.max(1L, base * scale);
    }

    // 累積到第 n 級所需的總經驗（從 Lv.1 起算）
    public static long totalExpForLevel(int level) {
        if (level <= 1) return 0;
        long total = 0;
        for (int i = 1; i < level; i++) {
            total += expNeededForLevel(i);
        }
        return total;
    }

    // ==================== IPlayerLevel ====================

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = Math.max(1, Math.min(MAX_LEVEL, level));
        markDirty();
    }

    @Override
    public long getExp() {
        return exp;
    }

    @Override
    public void setExp(long exp) {
        this.exp = Math.max(0, exp);
        markDirty();
    }

    @Override
    public void addExp(long amount) {
        if (amount <= 0) return;
        exp += amount;
        // 自動升級處理
        while (level < MAX_LEVEL) {
            long needed = expNeededForLevel(level);
            if (exp >= needed) {
                exp -= needed;
                level++;
            } else {
                break;
            }
        }
        // 滿等後多餘的 EXP 清零
        if (level >= MAX_LEVEL) {
            exp = 0;
        }
        markDirty();
    }

    @Override
    public long getExpToNextLevel() {
        if (level >= MAX_LEVEL) return 0;
        return expNeededForLevel(level);
    }

    @Override
    public long getExpForCurrentLevel() {
        return totalExpForLevel(level);
    }

    @Override
    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    @Override
    public boolean isMaxLevel() {
        return level >= MAX_LEVEL;
    }

    @Override
    public float getLevelProgress() {
        if (isMaxLevel()) return 1.0f;
        long needed = getExpToNextLevel();
        if (needed <= 0) return 1.0f;
        return (float) exp / needed;
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

    // ==================== NBT ====================

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("level", level);
        tag.putLong("exp", exp);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.level = tag.contains("level") ? Math.max(1, Math.min(MAX_LEVEL, tag.getInt("level"))) : 1;
        this.exp   = tag.contains("exp")   ? Math.max(0, tag.getLong("exp")) : 0;
    }
}
