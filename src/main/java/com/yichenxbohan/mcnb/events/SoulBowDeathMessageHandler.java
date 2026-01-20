package com.yichenxbohan.mcnb.events;

import com.yichenxbohan.mcnb.skill.SoulBowSkill;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "mcnb", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SoulBowDeathMessageHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource damageSource = event.getSource();

        // 检查是否是箭矢伤害
        if (damageSource.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Arrow arrow) {
            // 检查箭矢是否由射魂长弓技能发射
            if (SoulBowSkill.isSoulBowArrow(arrow.getUUID())) {
                // 获取发射箭矢的玩家
                ServerPlayer shooter = SoulBowSkill.getShooterByArrow(arrow.getUUID());

                if (shooter != null) {
                    // 创建自定义死亡消息
                    Component deathMessage;

                    if (victim instanceof ServerPlayer) {
                        // 玩家被击杀的消息
//                        deathMessage = Component.literal(victim.getName().getString() + " §c被 §6" + shooter.getName().getString() + " §c的§d射魂长弓§c擊穿了灵魂");
                        deathMessage = Component.literal(victim.getName().getString() + " §c被 §6" + shooter.getName().getString() + " §c擊穿了靈魂");
                    } else {
                        // 怪物被击杀的消息
                        deathMessage = Component.literal(victim.getName().getString() + " §c被 §6" + shooter.getName().getString() + " §c擊穿了靈魂");
                    }

                    // 向所有玩家广播死亡消息
                    if (victim.level().getServer() != null) {
                        victim.level().getServer().getPlayerList().broadcastSystemMessage(deathMessage, false);
                    }
                }
            }
        }
    }
}

