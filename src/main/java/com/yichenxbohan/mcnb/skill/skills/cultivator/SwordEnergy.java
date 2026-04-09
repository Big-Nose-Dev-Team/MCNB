package com.yichenxbohan.mcnb.skill.skills.cultivator;

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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

import static com.yichenxbohan.mcnb.ModSounds.SWORD_ENERGY;

public class SwordEnergy {

    public static SkillDefinition SwordEnergy() {
        return SkillDefinition.builder("cultivator_sword_energy", PlayerClass.CULTIVATOR)
                .displayName("劍氣")
                .description("測試而已")
                .category(SkillCategory.ATTACK)
                .castType(SkillCastType.INSTANT)
                .aimType(SkillAimType.SELF)
                .maxLevel(10)
                .multiplier(2.0, 0.3)
                .scaling(1.0, 1.0)
                .customExecutor(ctx ->{
                    ServerPlayer self = ctx.getPlayer();
                    ServerLevel level = self.serverLevel();
                    ParticleEmitterInfo SLASH = new ParticleEmitterInfo(ResourceLocation.fromNamespaceAndPath("mcnb", "slash"));

                    level.playSound(null, self.blockPosition(), SWORD_ENERGY.get(), SoundSource.HOSTILE, 1.0F, 1.0F);

                    BlockPos pos = self.getOnPos();

                        float yaw = self.getYRot();
                        float z = self.getXRot();
                        Random rand = new Random();
                        int randomInt = rand.nextInt(181);
                        float rotationY = (float) Math.toRadians(-yaw-90);
                        float rotationZ = (float) Math.toRadians(z);

                        AAALevel.addParticle(level, true, SLASH.clone()
                                .position(
                                        pos.getX()+0d,
                                        pos.getY()+0d,
                                        pos.getZ()+0d
                                )
                                .rotation(0, rotationY, -rotationZ)
                        );


                })
                .build();
    }
}
