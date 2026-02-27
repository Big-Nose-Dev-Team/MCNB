package com.yichenxbohan.mcnb.items;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

    private static final ParticleEmitterInfo HERALD = new ParticleEmitterInfo(ResourceLocation.fromNamespaceAndPath("mcnb", "skybreaking"));

    // 设置最大射线距离（你可以调整这个值）
    private static final double MAX_REACH_DISTANCE = 200.0;

    public ParticlesTestItem(Properties properties) {
        super(properties);
    }

    /**
     * 当玩家右键使用物品时调用此方法
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // 执行射线追踪，找到玩家准心指向的方块
        BlockHitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE, MAX_REACH_DISTANCE);

        // 检查是否击中了方块
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = hitResult.getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);

            // 只在服务端执行
            if (!level.isClientSide) {
                // 獲取玩家的旋轉角度
                float yaw = player.getYRot();     // 水平旋轉（左右看）
                float pitch = player.getXRot();   // 垂直旋轉（上下看）

                // 將 Minecraft 的角度轉換為弧度
                // 注意：Minecraft 的 yaw 0 是朝南，需要調整讓粒子朝向玩家面對的方向
                float rotationY = (float) Math.toRadians(-yaw);      // Y軸旋轉（水平）
                float rotationX = (float) Math.toRadians(-pitch);    // X軸旋轉（俯仰）
                float rotationZ = 0.0f;                               // Z軸旋轉（翻滾）

                // 在方块上方生成粒子效果，並設置旋轉
                AAALevel.addParticle(level, true, HERALD.clone()
                        .position(
                                blockPos.getX() + 0.5d,
                                blockPos.getY() + 128.0d,
                                blockPos.getZ() + 0.5d
                        )
                        .rotation(0, rotationY+80, rotationZ)
                );

                // 给玩家发送消息（可选）
//                player.sendSystemMessage(
//                    Component.literal("在方块 " + blockState.getBlock().getName().getString() +
//                                    " 上生成粒子效果！位置: " + blockPos.toShortString())
//                );
            }

            return InteractionResultHolder.success(itemStack);
        }

        // 如果没有击中方块，返回失败
        return InteractionResultHolder.fail(itemStack);
    }

    /**
     * 自定义射线追踪方法，支持自定义距离
     */
    private BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Fluid fluidMode, double distance) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 viewVector = player.getViewVector(1.0F);
        Vec3 targetPosition = eyePosition.add(viewVector.x * distance, viewVector.y * distance, viewVector.z * distance);

        return level.clip(new ClipContext(
                eyePosition,
                targetPosition,
                ClipContext.Block.OUTLINE,
                fluidMode,
                player
        ));
    }
}
