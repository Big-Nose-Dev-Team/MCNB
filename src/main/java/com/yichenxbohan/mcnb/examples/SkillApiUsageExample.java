package com.yichenxbohan.mcnb.examples;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.level.PlayerAttributeType;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import com.yichenxbohan.mcnb.skill.SkillService;
import com.yichenxbohan.mcnb.skill.api.SkillBranch;
import com.yichenxbohan.mcnb.skill.api.SkillCategory;
import com.yichenxbohan.mcnb.skill.api.SkillCastType;
import com.yichenxbohan.mcnb.skill.api.SkillDefinition;
import com.yichenxbohan.mcnb.skill.api.SkillRegistry;
import com.yichenxbohan.mcnb.skill.api.SkillAimType;
import com.yichenxbohan.mcnb.skill.network.SkillPackets;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;

/**
 * 技能 API 使用範例。
 *
 * 這個類只示範「怎麼用」，不會自動註冊到遊戲流程。
 */
public final class SkillApiUsageExample {

    private SkillApiUsageExample() {
    }

    /**
     * 範例 1：建立一個技能定義（含職業、分支、倍率、時長）。
     */
    public static SkillDefinition createMageFireballDefinition() {
        return SkillDefinition.builder("mage_fireball", PlayerClass.MAGE)
                .displayName("火球術")
                .description("向視線目標施放爆裂火球。")
                .category(SkillCategory.ATTACK)
                .castType(SkillCastType.INSTANT)
                .aimType(SkillAimType.LOOK_TARGET_BLOCK)
                .maxLevel(10)
                .multiplier(1.2, 0.18)
                .duration(20, 4)
                .scaling(0.2, 1.4)
                .prerequisite("mage_skill_1", 1)
                .branch(new SkillBranch("burst", "爆裂", "提高瞬間倍率", 0.10, 0, null))
                .branch(new SkillBranch("burn", "灼燒", "提高效果時長", 0.03, 12, null))
                .customExecutor(ctx -> {
                    // 在 customExecutor 內，直接從 ctx 取玩家，接著讀 Capability。
                    ServerPlayer self = ctx.getPlayer();

                    PlayerClass myClass = self.getCapability(ModCapabilities.PLAYER_CLASS)
                            .map(cap -> cap.getPlayerClass())
                            .orElse(PlayerClass.NONE);

                    int myLevel = self.getCapability(ModCapabilities.PLAYER_LEVEL)
                            .map(cap -> cap.getLevel())
                            .orElse(1);

                    double myMagicAttack = self.getCapability(ModCapabilities.COMBAT_DATA)
                            .map(cap -> cap.getMagicAttack())
                            .orElse(0.0);

                    int myFireballLv = self.getCapability(ModCapabilities.PLAYER_SKILL)
                            .map(cap -> cap.getSkillLevel("mage_fireball"))
                            .orElse(0);

                    // 你可以把玩家資料和技能計算值一起用在自訂邏輯：
                    // ctx.getComputedDamage()、ctx.getComputedDuration()、ctx.getTargetPosition()
                    // myClass、myLevel、myMagicAttack、myFireballLv
                })
                .build();
    }

    /**
     * 範例 2：查詢技能屬於哪個職業。
     */
    public static PlayerClass queryOwnerClass(String skillId) {
        return SkillRegistry.getOwnerClass(skillId);
    }

    /**
     * 範例 3：客戶端透過 API 封包請求施放技能。
     */
    public static void clientRequestCast(String skillId) {
        SkillPackets.requestCast(skillId);
    }

    /**
     * 範例 4：伺服端直接呼叫服務層。
     */
    public static void serverSideFlow(ServerPlayer player, String skillId) {
        boolean upgraded = SkillService.upgradeSkill(player, skillId);
        boolean selected = SkillService.selectBranch(player, skillId, "burst");
        boolean casted = SkillService.castSkill(player, skillId);

        if (upgraded || selected || casted) {
            SkillService.syncToClient(player);
        }
    }

    /**
     * 範例 5：客戶端取得「玩家自身資料」。
     */
    public static void readMyDataClientSide() {
        var self = Minecraft.getInstance().player;
        if (self == null) {
            return;
        }

        PlayerClass myClass = self.getCapability(ModCapabilities.PLAYER_CLASS)
                .map(cap -> cap.getPlayerClass())
                .orElse(PlayerClass.NONE);

        int myLevel = self.getCapability(ModCapabilities.PLAYER_LEVEL)
                .map(cap -> cap.getLevel())
                .orElse(1);

        int myStrength = self.getCapability(ModCapabilities.PLAYER_LEVEL)
                .map(cap -> cap.getAttributePoints(PlayerAttributeType.STRENGTH))
                .orElse(0);

        double myPhysicalAttack = self.getCapability(ModCapabilities.COMBAT_DATA)
                .map(cap -> cap.getPhysicalAttack())
                .orElse(0.0);

        int myFireballLv = self.getCapability(ModCapabilities.PLAYER_SKILL)
                .map(cap -> cap.getSkillLevel("mage_fireball"))
                .orElse(0);

        // 這些變數就是你可直接拿去顯示在 GUI / HUD 的玩家自身資料
        // myClass, myLevel, myStrength, myPhysicalAttack, myFireballLv
    }

    /**
     * 範例 6：伺服端取得「玩家自身資料」。
     */
    public static void readMyDataServerSide(ServerPlayer player) {
        PlayerClass myClass = player.getCapability(ModCapabilities.PLAYER_CLASS)
                .map(cap -> cap.getPlayerClass())
                .orElse(PlayerClass.NONE);

        int myLevel = player.getCapability(ModCapabilities.PLAYER_LEVEL)
                .map(cap -> cap.getLevel())
                .orElse(1);

        int myIntelligence = player.getCapability(ModCapabilities.PLAYER_LEVEL)
                .map(cap -> cap.getAttributePoints(PlayerAttributeType.INTELLIGENCE))
                .orElse(0);

        double myMagicAttack = player.getCapability(ModCapabilities.COMBAT_DATA)
                .map(cap -> cap.getMagicAttack())
                .orElse(0.0);

        int mySkillLv = player.getCapability(ModCapabilities.PLAYER_SKILL)
                .map(cap -> cap.getSkillLevel("mage_fireball"))
                .orElse(0);

        // 你可以在這裡把資料做伺服端判定、計算或同步給客戶端
        // myClass, myLevel, myIntelligence, myMagicAttack, mySkillLv
    }
}

