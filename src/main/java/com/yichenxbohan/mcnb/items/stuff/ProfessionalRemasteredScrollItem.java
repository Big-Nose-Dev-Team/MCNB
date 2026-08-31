package com.yichenxbohan.mcnb.items.stuff;

import com.yichenxbohan.mcnb.network.ClassResetPacket;
import com.yichenxbohan.mcnb.network.ModNetworking;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ProfessionalRemasteredScrollItem extends Item {

    public ProfessionalRemasteredScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            ModNetworking.sendToServer(new ClassResetPacket());
        }

        return InteractionResultHolder.success(itemStack);
    }

}
