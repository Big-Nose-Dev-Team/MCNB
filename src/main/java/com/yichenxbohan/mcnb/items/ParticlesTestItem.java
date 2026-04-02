package com.yichenxbohan.mcnb.items;

import mod.chloeprime.aaaparticles.AAAParticles;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ParticlesTestItem extends Item {

    public ParticlesTestItem(Properties properties) {
        super(properties);
    }

    private static final ParticleEmitterInfo SLASH = new ParticleEmitterInfo(ResourceLocation.fromNamespaceAndPath("mcnb", "slash"));

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        BlockPos pos = player.getOnPos();
        if(!level.isClientSide){

            float yaw = player.getYRot();
            float rotationY = (float) Math.toRadians(-yaw-90);

            AAALevel.addParticle(level, true, SLASH.clone()
                    .position(
                            pos.getX()+0d,
                            pos.getY()+0d,
                            pos.getZ()+0d
                    )
                    .rotation(0, rotationY, 0)
            );
        }
        return InteractionResultHolder.success(itemStack);
    }
}