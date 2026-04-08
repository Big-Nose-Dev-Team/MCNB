package com.yichenxbohan.mcnb.level;

/**
 * 玩家等級系統介面
 * 與原版 EXP 完全獨立
 */
public interface IPlayerLevel {

    /** 每升 1 級可獲得的屬性點數 */
    int POINTS_PER_LEVEL = 3;

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

    /** 取得已分配的屬性點數總量 */
    int getAllocatedAttributePoints();

    /** 取得尚未分配的屬性點數 */
    int getAvailableAttributePoints();

    /** 取得指定屬性的點數 */
    int getAttributePoints(PlayerAttributeType type);

    /** 直接設定指定屬性的點數 */
    void setAttributePoints(PlayerAttributeType type, int points);

    /** 直接設定五項屬性點數 */
    void setAttributePoints(int strength, int constitution, int potential, int intelligence, int agility);

    /** 嘗試對指定屬性增減點數，成功回傳 true */
    boolean adjustAttributePoints(PlayerAttributeType type, int delta);

    /** 將所有屬性點重置為 0 */
    boolean resetAttributePoints();

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

