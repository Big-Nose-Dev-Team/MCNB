package com.yichenxbohan.mcnb.level;

import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;

/**
 * 玩家等級系統的資料實作
 * 含 NBT 序列化，支援存檔讀取
 */
public class PlayerLevelData implements IPlayerLevel {

    public static final int MAX_LEVEL = 100;
    public static final int ATTRIBUTE_POINTS_PER_LEVEL = POINTS_PER_LEVEL;

    private int level = 1;
    private long exp = 0;
    private final int[] attributePoints = new int[PlayerAttributeType.values().length];
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
    public int getAllocatedAttributePoints() {
        int total = 0;
        for (int value : attributePoints) {
            total += Math.max(0, value);
        }
        return total;
    }

    @Override
    public int getAvailableAttributePoints() {
        return Math.max(0, getTotalAttributePoints() - getAllocatedAttributePoints());
    }

    @Override
    public int getAttributePoints(PlayerAttributeType type) {
        if (type == null) return 0;
        return Math.max(0, attributePoints[type.ordinal()]);
    }

    @Override
    public void setAttributePoints(PlayerAttributeType type, int points) {
        if (type == null) return;

        int idx = type.ordinal();
        int maxForThisType = Math.max(0, getTotalAttributePoints() - (getAllocatedAttributePoints() - Math.max(0, attributePoints[idx])));
        attributePoints[idx] = Math.max(0, Math.min(points, maxForThisType));
        markDirty();
    }

    @Override
    public void setAttributePoints(int strength, int constitution, int potential, int intelligence, int agility) {
        int total = getTotalAttributePoints();
        int s = Math.max(0, strength);
        int v = Math.max(0, constitution);
        int p = Math.max(0, potential);
        int i = Math.max(0, intelligence);
        int a = Math.max(0, agility);

        int spent = s + v + p + i + a;
        if (spent > total) {
            int overflow = spent - total;
            int[] values = { s, v, p, i, a };
            for (int idx = values.length - 1; idx >= 0 && overflow > 0; idx--) {
                int cut = Math.min(values[idx], overflow);
                values[idx] -= cut;
                overflow -= cut;
            }
            s = values[0];
            v = values[1];
            p = values[2];
            i = values[3];
            a = values[4];
        }

        attributePoints[PlayerAttributeType.STRENGTH.ordinal()] = s;
        attributePoints[PlayerAttributeType.CONSTITUTION.ordinal()] = v;
        attributePoints[PlayerAttributeType.POTENTIAL.ordinal()] = p;
        attributePoints[PlayerAttributeType.INTELLIGENCE.ordinal()] = i;
        attributePoints[PlayerAttributeType.AGILITY.ordinal()] = a;
        markDirty();
    }

    @Override
    public boolean adjustAttributePoints(PlayerAttributeType type, int delta) {
        if (type == null || delta == 0) return false;

        int idx = type.ordinal();
        int current = Math.max(0, attributePoints[idx]);
        int target = current + delta;
        if (target < 0) return false;

        int availableOther = Math.max(0, getTotalAttributePoints() - (getAllocatedAttributePoints() - current));
        if (target > availableOther) return false;

        if (target == current) return false;
        setAttributePoints(type, target);
        return true;
    }

    @Override
    public boolean resetAttributePoints() {
        if (getAllocatedAttributePoints() == 0) return false;
        Arrays.fill(attributePoints, 0);
        markDirty();
        return true;
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

    public int getTotalAttributePoints() {
        return Math.max(0, (level - 1) * ATTRIBUTE_POINTS_PER_LEVEL);
    }

    // ==================== NBT ====================

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("level", level);
        tag.putLong("exp", exp);
        tag.putInt("str", attributePoints[PlayerAttributeType.STRENGTH.ordinal()]);
        tag.putInt("vit", attributePoints[PlayerAttributeType.CONSTITUTION.ordinal()]);
        tag.putInt("pot", attributePoints[PlayerAttributeType.POTENTIAL.ordinal()]);
        tag.putInt("int", attributePoints[PlayerAttributeType.INTELLIGENCE.ordinal()]);
        tag.putInt("agi", attributePoints[PlayerAttributeType.AGILITY.ordinal()]);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.level = tag.contains("level") ? Math.max(1, Math.min(MAX_LEVEL, tag.getInt("level"))) : 1;
        this.exp   = tag.contains("exp")   ? Math.max(0, tag.getLong("exp")) : 0;
        attributePoints[PlayerAttributeType.STRENGTH.ordinal()]      = Math.max(0, tag.getInt("str"));
        attributePoints[PlayerAttributeType.CONSTITUTION.ordinal()]  = Math.max(0, tag.getInt("vit"));
        attributePoints[PlayerAttributeType.POTENTIAL.ordinal()]     = Math.max(0, tag.getInt("pot"));
        attributePoints[PlayerAttributeType.INTELLIGENCE.ordinal()]  = Math.max(0, tag.getInt("int"));
        attributePoints[PlayerAttributeType.AGILITY.ordinal()]       = Math.max(0, tag.getInt("agi"));

        int total = getTotalAttributePoints();
        int spent = getAllocatedAttributePoints();
        if (spent > total) {
            int overflow = spent - total;
            for (PlayerAttributeType type : PlayerAttributeType.orderedValues()) {
                if (overflow <= 0) break;
                int idx = type.ordinal();
                int cut = Math.min(attributePoints[idx], overflow);
                attributePoints[idx] -= cut;
                overflow -= cut;
            }
        }
    }
}
