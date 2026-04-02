package com.yichenxbohan.mcnb.level;

/**
 * 玩家等級系統介面
 * 與原版 EXP 完全獨立
 */
public interface IPlayerLevel {

    /** 取得目前等級 */
    int getLevel();

    /** 設定等級 */
    void setLevel(int level);

    /** 取得目前經驗值 */
    long getExp();

    /** 設定目前經驗值 */
    void setExp(long exp);

    /** 增加經驗值（自動處理升級） */
    void addExp(long amount);

    /** 取得升到下一級所需的總經驗值 */
    long getExpToNextLevel();

    /** 取得目前等級的起始經驗值（已累積到此等級的總量） */
    long getExpForCurrentLevel();

    /** 取得等級上限 */
    int getMaxLevel();

    /** 是否為滿等 */
    boolean isMaxLevel();

    /** 取得當前等級的進度百分比 (0.0 ~ 1.0) */
    float getLevelProgress();

    /** 標記資料需要同步 */
    void markDirty();

    /** 是否需要同步 */
    boolean isDirty();

    /** 清除 dirty 標記 */
    void clearDirty();
}

