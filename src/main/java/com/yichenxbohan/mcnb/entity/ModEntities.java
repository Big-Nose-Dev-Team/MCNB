package com.yichenxbohan.mcnb.entity;

import com.yichenxbohan.mcnb.Mcnb;
import com.yichenxbohan.mcnb.entity.forskills.DeathStareEntity; // 確保導包路徑正確
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    // 建立實體的延遲註冊器
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Mcnb.MODID);

    // 正式註冊你的眼睛（自訂投擲物）實體
    public static final RegistryObject<EntityType<DeathStareEntity>> DEATH_STARE =
            ENTITIES.register("death_stare", () -> EntityType.Builder.<DeathStareEntity>of(DeathStareEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F) // 設定眼睛碰撞箱的大小（寬0.5、高0.5）
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build("death_stare"));

    // 提供給主類別呼叫的註冊方法
    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}