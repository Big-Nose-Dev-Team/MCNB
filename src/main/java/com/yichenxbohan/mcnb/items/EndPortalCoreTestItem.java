package com.yichenxbohan.mcnb.items;

import com.yichenxbohan.mcnb.client.particle.EndPortalParticleHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

/**
 * 終界核心粒子測試道具
 *
 * 右鍵：在玩家腳下位置新增一個終界傳送門流動粒子核心
 * Shift+右鍵：清除所有活躍的粒子核心
 */
public class EndPortalCoreTestItem extends Item {

    public EndPortalCoreTestItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand) {

        // 只在客戶端執行粒子操作
        if (!level.isClientSide) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        if (FMLEnvironment.dist != Dist.CLIENT) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        if (player.isShiftKeyDown()) {
            // Shift+右鍵：清除所有核心
            EndPortalParticleHandler.clearCores();
            player.displayClientMessage(
                    Component.literal("§5[終界核心] §f已清除所有粒子核心"), true);
        } else {
            // 右鍵：在玩家腳下新增核心
            EndPortalParticleHandler.addCore(player.position());
            int count = EndPortalParticleHandler.getCoreCount();
            player.displayClientMessage(
                    Component.literal("§5[終界核心] §f已在腳下新增粒子核心 (共 " + count + " 個)"), true);
        }

        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}

