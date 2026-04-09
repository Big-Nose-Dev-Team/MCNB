package com.yichenxbohan.mcnb.skill;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.combat.StatsProvider;
import com.yichenxbohan.mcnb.combat.damage.DamageAPI;
import com.yichenxbohan.mcnb.network.ModNetworking;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import com.yichenxbohan.mcnb.skill.api.SkillAimType;
import com.yichenxbohan.mcnb.skill.api.SkillBranch;
import com.yichenxbohan.mcnb.skill.api.SkillCategory;
import com.yichenxbohan.mcnb.skill.api.SkillDefinition;
import com.yichenxbohan.mcnb.skill.api.SkillExecutionContext;
import com.yichenxbohan.mcnb.skill.api.SkillPrerequisite;
import com.yichenxbohan.mcnb.skill.api.SkillRegistry;
import com.yichenxbohan.mcnb.skill.capability.IPlayerSkillData;
import com.yichenxbohan.mcnb.skill.network.SkillSyncPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public final class SkillService {

    private SkillService() {
    }

    public static List<SkillDefinition> getClassSkills(ServerPlayer player) {
        PlayerClass clazz = player.getCapability(ModCapabilities.PLAYER_CLASS)
                .map(c -> c.getPlayerClass())
                .orElse(PlayerClass.NONE);
        return SkillRegistry.getSkills(clazz);
    }

    public static boolean upgradeSkill(ServerPlayer player, String skillId) {
        SkillDefinition def = SkillRegistry.getById(skillId);
        if (def == null) {
            return false;
        }

        PlayerClass clazz = player.getCapability(ModCapabilities.PLAYER_CLASS)
                .map(c -> c.getPlayerClass())
                .orElse(PlayerClass.NONE);
        if (!def.belongsTo(clazz)) {
            return false;
        }

        return player.getCapability(ModCapabilities.PLAYER_SKILL).map(skillData -> {
            int current = skillData.getSkillLevel(skillId);
            if (current >= def.getMaxLevel()) {
                return false;
            }
            if (!checkPrerequisites(skillData, def.getPrerequisites())) {
                return false;
            }

            int playerLevel = player.getCapability(ModCapabilities.PLAYER_LEVEL).map(c -> c.getLevel()).orElse(1);
            if (skillData.getAvailableSkillPoints(playerLevel) <= 0) {
                return false;
            }

            skillData.setSkillLevel(skillId, current + 1);
            return true;
        }).orElse(false);
    }

    public static boolean selectBranch(ServerPlayer player, String skillId, String branchId) {
        SkillDefinition def = SkillRegistry.getById(skillId);
        if (def == null || branchId == null || branchId.isEmpty()) {
            return false;
        }

        SkillBranch branch = def.findBranch(branchId);
        if (branch == null) {
            return false;
        }

        return player.getCapability(ModCapabilities.PLAYER_SKILL).map(skillData -> {
            if (skillData.getSkillLevel(skillId) <= 0) {
                return false;
            }

            SkillPrerequisite prerequisite = branch.getPrerequisite();
            if (prerequisite != null && skillData.getSkillLevel(prerequisite.getRequiredSkillId()) < prerequisite.getMinLevel()) {
                return false;
            }

            skillData.setSelectedBranch(skillId, branchId);
            return true;
        }).orElse(false);
    }

    public static boolean resetAll(ServerPlayer player) {
        return player.getCapability(ModCapabilities.PLAYER_SKILL)
                .map(IPlayerSkillData::resetAll)
                .orElse(false);
    }

    public static boolean castSkill(ServerPlayer player, String skillId) {
        SkillDefinition def = SkillRegistry.getById(skillId);
        if (def == null) {
            return false;
        }

        return player.getCapability(ModCapabilities.PLAYER_SKILL).map(skillData -> {
            int level = skillData.getSkillLevel(skillId);
            if (level <= 0) {
                return false;
            }

            if (def.getCastType() == com.yichenxbohan.mcnb.skill.api.SkillCastType.REQUIRES_BOW_DRAW) {
                if (!player.isUsingItem()) {
                    player.sendSystemMessage(Component.literal("技能需要拉弓中才能施放"));
                    return false;
                }
                if (!(player.getUseItem().getItem() instanceof BowItem) && !(player.getUseItem().getItem() instanceof CrossbowItem)) {
                    return false;
                }
            }

            SkillBranch selectedBranch = def.findBranch(skillData.getSelectedBranch(skillId));
            double base = computeBaseDamage(player, def);
            double multiplier = def.getMultiplierAtLevel(level) + (selectedBranch == null ? 0.0 : selectedBranch.getMultiplierBonusPerLevel() * level);
            int duration = def.getDurationAtLevel(level) + (selectedBranch == null ? 0 : selectedBranch.getDurationBonusPerLevel() * level);
            Vec3 targetPos = findTargetPosition(player, def.getAimType());
            double finalDamage = Math.max(0.0, base * multiplier);

            SkillExecutionContext ctx = new SkillExecutionContext(player, def, level, selectedBranch, targetPos, finalDamage, duration);

            if (def.getCustomExecutor() != null) {
                def.getCustomExecutor().execute(ctx);
            } else {
                executeDefault(ctx);
            }
            return true;
        }).orElse(false);
    }

    private static boolean checkPrerequisites(IPlayerSkillData skillData, List<SkillPrerequisite> prerequisites) {
        for (SkillPrerequisite prerequisite : prerequisites) {
            if (skillData.getSkillLevel(prerequisite.getRequiredSkillId()) < prerequisite.getMinLevel()) {
                return false;
            }
        }
        return true;
    }

    private static Vec3 findTargetPosition(ServerPlayer player, SkillAimType aimType) {
        if (aimType == SkillAimType.SELF) {
            return player.position();
        }

        HitResult result = player.pick(32.0, 0.0f, false);
        if (result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult blockHit) {
            return blockHit.getLocation();
        }
        return player.getEyePosition().add(player.getLookAngle().scale(10.0));
    }

    private static double computeBaseDamage(ServerPlayer player, SkillDefinition def) {
        var stats = StatsProvider.get(player);
        return stats.physicalAttack * def.getPhysicalScaling() + stats.magicAttack * def.getMagicScaling();
    }

    private static void executeDefault(SkillExecutionContext ctx) {
        ServerPlayer player = ctx.getPlayer();
        SkillDefinition def = ctx.getSkill();

        if (def.getCategory() == SkillCategory.BUFF) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, ctx.getComputedDuration(), 0));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, ctx.getComputedDuration(), 0));
            return;
        }

        double radius = 3.0;
        AABB hitBox = new AABB(
                ctx.getTargetPosition().x - radius, ctx.getTargetPosition().y - radius, ctx.getTargetPosition().z - radius,
                ctx.getTargetPosition().x + radius, ctx.getTargetPosition().y + radius, ctx.getTargetPosition().z + radius
        );

        List<LivingEntity> targets = player.serverLevel().getEntitiesOfClass(LivingEntity.class, hitBox,
                entity -> entity.isAlive() && entity != player && (!(entity instanceof net.minecraft.world.entity.player.Player)));

        if (targets.isEmpty()) {
            return;
        }

        double totalScaling = Math.max(0.0001, def.getPhysicalScaling() + def.getMagicScaling());
        float physicalDamage = (float) (ctx.getComputedDamage() * (def.getPhysicalScaling() / totalScaling));
        float magicDamage = (float) (ctx.getComputedDamage() * (def.getMagicScaling() / totalScaling));

        for (LivingEntity target : targets) {
            if (!(target instanceof Mob)) {
                continue;
            }
            if (physicalDamage > 0.01f) {
                DamageAPI.physical(player, target, physicalDamage);
            }
            if (magicDamage > 0.01f) {
                DamageAPI.magic(player, target, magicDamage);
            }
        }
    }

    public static void syncToClient(ServerPlayer player) {
        player.getCapability(ModCapabilities.PLAYER_SKILL).ifPresent(data -> {
            Map<String, Integer> levels = data.getSkillLevels();
            Map<String, String> branches = data.getSelectedBranches();
            ModNetworking.sendToPlayer(new SkillSyncPacket(levels, branches), player);
            data.clearDirty();
        });
    }
}

