package com.yichenxbohan.mcnb.items;

import com.yichenxbohan.mcnb.Mcnb;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        // 添加到战斗标签页
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(PARTICLE_TEST_ITEM);
        }

        // 添加到杂项标签页
//        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
//            event.accept(PARTICLE_TEST_ITEM);
//            // 在这里添加 ModItems 中的其他物品
//        }
    }

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Mcnb.MODID);

    public static final RegistryObject<Item> PARTICLE_TEST_ITEM = ITEMS.register("particles_test_item",
            () -> new ParticlesTestItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

}
