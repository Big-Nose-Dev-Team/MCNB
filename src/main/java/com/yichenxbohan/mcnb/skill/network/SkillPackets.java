package com.yichenxbohan.mcnb.skill.network;

import com.yichenxbohan.mcnb.network.ModNetworking;

/**
 * API 封裝：GUI 只需呼叫這裡即可完成封包收發。
 */
public final class SkillPackets {

    private SkillPackets() {
    }

    public static void requestUpgrade(String skillId) {
        ModNetworking.sendToServer(new SkillActionPacket(SkillActionType.UPGRADE, skillId, ""));
    }

    public static void requestSelectBranch(String skillId, String branchId) {
        ModNetworking.sendToServer(new SkillActionPacket(SkillActionType.SELECT_BRANCH, skillId, branchId));
    }

    public static void requestCast(String skillId) {
        ModNetworking.sendToServer(new SkillActionPacket(SkillActionType.CAST, skillId, ""));
    }

    public static void requestResetAll() {
        ModNetworking.sendToServer(new SkillActionPacket(SkillActionType.RESET, "", ""));
    }
}

