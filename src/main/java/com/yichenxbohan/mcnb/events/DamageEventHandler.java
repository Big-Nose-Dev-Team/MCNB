package com.yichenxbohan.mcnb.events;

import com.yichenxbohan.mcnb.combat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class DamageEventHandler {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {

        if(!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        LivingEntity target = event.getEntity();

        if (event.getSource().getMsgId().equals("custom")) {
            return;
        }

        event.setCanceled(true);

        // damage logic
        CombatStats atkStats = StatsProvider.get(attacker);
        CombatStats defStats = StatsProvider.get(target);

        boolean isMagic = event.getSource().getMsgId().equals("magic")
                || event.getSource().getMsgId().equals("indirectMagic");

        DamageResult result = DamageCalculator.calculate(
                atkStats,
                defStats,
                isMagic
        );

        if (!result.hit || result.damage <= 0) {
            return;
        }

        DamageSource source = ModDamageSources.custom((ServerLevel) attacker.level(), attacker);

        target.invulnerableTime = 0;
        target.hurtMarked = true;

        target.hurt(source, (float) result.damage);
    }
}
