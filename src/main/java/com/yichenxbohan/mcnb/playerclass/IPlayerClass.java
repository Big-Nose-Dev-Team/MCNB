package com.yichenxbohan.mcnb.playerclass;

public interface IPlayerClass {
    PlayerClass getPlayerClass();
    void setPlayerClass(PlayerClass cls);
    boolean hasClass();
    void markDirty();
    boolean isDirty();
    void clearDirty();
}

