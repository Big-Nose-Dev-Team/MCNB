package com.yichenxbohan.mcnb.combat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.Holder;

public class ModDamageSources {
    public static final ResourceKey<DamageType> CUSTOM =
            ResourceKey.create(
                    Registries.DAMAGE_TYPE,
                    new ResourceLocation("mcnb", "custom")
            );

    public static DamageSource custom(ServerLevel level, LivingEntity attacker) {
        Holder<DamageType> holder =
                level.registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(CUSTOM);

        return new DamageSource(holder, attacker);
    }
}
