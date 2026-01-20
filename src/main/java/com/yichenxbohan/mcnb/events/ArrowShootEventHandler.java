package com.yichenxbohan.mcnb.events;

import com.yichenxbohan.mcnb.Mcnb;
import com.yichenxbohan.mcnb.skill.SoulBowSkill;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Mcnb.MODID)
public class ArrowShootEventHandler {

    @SubscribeEvent
    public static void onArrowShoot(EntityJoinLevelEvent event) {
        // 检查是否是箭矢实体
        if (!(event.getEntity() instanceof Arrow arrow)) {
            return;
        }

        // 检查箭矢是否由玩家射出
        if (!(arrow.getOwner() instanceof ServerPlayer player)) {
            return;
        }

        // 在服务端处理
        if (event.getLevel().isClientSide()) {
            return;
        }

        // 清除玩家的拉弓状态
        BowChargingEventHandler.clearChargingState(player.getUUID());

        // 调用技能系统检查并应用效果
        SoulBowSkill.onArrowShoot(player, arrow);
    }
}
