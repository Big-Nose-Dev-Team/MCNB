package com.yichenxbohan.mcnb.skill.skills.mage;

import com.yichenxbohan.mcnb.entity.ModEntities;
import com.yichenxbohan.mcnb.entity.forskills.DeathStareEntity;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import com.yichenxbohan.mcnb.skill.api.SkillAimType;
import com.yichenxbohan.mcnb.skill.api.SkillCastType;
import com.yichenxbohan.mcnb.skill.api.SkillCategory;
import com.yichenxbohan.mcnb.skill.api.SkillDefinition;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DeathStare {
    public static SkillDefinition createDefinition() {
        return SkillDefinition.builder("death_stare", PlayerClass.MAGE)
                .displayName("死神的凝視")
                .description("對目標施加死亡凝視效果，降低其攻擊力和移動速度。")
                .category(SkillCategory.ATTACK)
                .castType(SkillCastType.INSTANT)
                .aimType(SkillAimType.SELF)
                .maxLevel(10)
                .multiplier(2.0, 0.2)
                .duration(200, 0)
                .cooldownTicks(200)
                .scaling(0.1, 0.5)
                .customExecutor(ctx -> {
                    // 這裡放技能的具體執行邏輯，例如對目標施加效果等。
                    // 你可以從 ctx 取玩家、目標、計算傷害等。
                    ServerPlayer self = ctx.getPlayer();

                    final ParticleEmitterInfo DEATH_STARE = new ParticleEmitterInfo(ResourceLocation.fromNamespaceAndPath("mcnb", "deathstare"));

                    BlockPos pos = self.getOnPos();

                    AAALevel.addParticle(self.level(), true, DEATH_STARE.clone()
                            .position(
                                    pos.getX() + 0.0,
                                    pos.getY() + 1.1,
                                    pos.getZ() + 0.0
                            )
                            .scale(100.0f)
                    );

                    if (!self.level().isClientSide()) {
                        ServerLevel serverLevel = (ServerLevel) self.level();

                        DeathStareEntity eye = new DeathStareEntity(ModEntities.DEATH_STARE.get(), serverLevel);

                        eye.setPos(self.getX(), self.getY()+15, self.getZ());
                        eye.setOwner(self);

                        serverLevel.addFreshEntity(eye);

                        CompletableFuture.runAsync(() -> {
                            // 這裡寫時間到之後要做的事情：
                            // ⚠️ 注意：Minecraft 的實體操作必須回到主線程執行，所以用 server.execute() 包裹起來
                            serverLevel.getServer().execute(() -> {
                                if (eye.isAlive()) {
                                    // 💥 在眼睛消失前，你甚至可以在這裡追加一波技能結束的爆炸或傷害
                                    // serverLevel.explode(eye, eye.getX(), eye.getY(), eye.getZ(), 1.5F, Level.ExplosionInteraction.NONE);

                                    eye.discard(); // ⏱️ 時間到，讓這隻眼睛在世界中消失
                                }
                            });
                        }, CompletableFuture.delayedExecutor(20, TimeUnit.SECONDS));
                    }





                })
                .build();

    }
}
