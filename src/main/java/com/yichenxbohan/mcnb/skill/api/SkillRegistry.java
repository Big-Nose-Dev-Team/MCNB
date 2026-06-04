package com.yichenxbohan.mcnb.skill.api;

import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import com.yichenxbohan.mcnb.skill.SoulBowSkill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 技能註冊中心：
 * - 每個職業預設 5 個技能
 * - 每個技能可帶前置、描述與冷卻資料
 */
public final class SkillRegistry {

    private static final Map<PlayerClass, List<SkillDefinition>> CLASS_SKILLS = new EnumMap<>(PlayerClass.class);
    private static final Map<String, SkillDefinition> BY_ID = new HashMap<>();

    static {
        bootstrap();
    }

    private SkillRegistry() {
    }

    private static void bootstrap() {
        for (PlayerClass clazz : PlayerClass.values()) {
            if (clazz == PlayerClass.NONE) {
                continue;
            }
            registerDefaultsFor(clazz);
        }
    }

    private static void registerDefaultsFor(PlayerClass clazz) {
        List<SkillDefinition> defs = new ArrayList<>();

        switch (clazz) {
            case ARCHER -> registerArcherSkills(defs);
            default -> registerGenericFiveSkills(defs, clazz);
        }

        CLASS_SKILLS.put(clazz, Collections.unmodifiableList(defs));
    }


    private static void registerArcherSkills(List<SkillDefinition> defs) {
        SkillDefinition soulBow = SkillDefinition.builder("archer_skill_1", PlayerClass.ARCHER)
                .displayName(PlayerClass.ARCHER.displayName + "技能 1")
                .description("召喚靈魂箭強化你的下一發箭矢，命中時造成額外靈魂傷害。")
                .category(SkillCategory.ATTACK)
                .castType(SkillCastType.REQUIRES_BOW_DRAW)
                .aimType(SkillAimType.LOOK_TARGET_BLOCK)
                .maxLevel(10)
                .multiplier(1.1, 0.12)
                .duration(50, 6)
                .scaling(1.1, 0.6)
                .cooldownTicks(80)
                .customExecutor(ctx -> SoulBowSkill.activate(ctx.getPlayer()))
                .build();
        register(defs, soulBow);

        String previousId = soulBow.getId();
        for (int i = 2; i <= 5; i++) {
            SkillDefinition generic = buildGenericSkill(PlayerClass.ARCHER, i, previousId);
            register(defs, generic);
            previousId = generic.getId();
        }
    }

    private static void registerGenericFiveSkills(List<SkillDefinition> defs, PlayerClass clazz) {
        String previousId = null;
        for (int i = 1; i <= 5; i++) {
            SkillDefinition def = buildGenericSkill(clazz, i, previousId);
            register(defs, def);
            previousId = def.getId();
        }
    }

    private static SkillDefinition buildGenericSkill(PlayerClass clazz, int index, String prerequisiteSkillId) {
        String base = clazz.name().toLowerCase();
        String id = base + "_skill_" + index;

        SkillDefinition.Builder builder = SkillDefinition.builder(id, clazz)
                .displayName(clazz.displayName + "技能 " + index)
                .description("這是「" + clazz.displayName + "技能 " + index + "」的自訂技能描述。")
                .category(index % 2 == 0 ? SkillCategory.BUFF : SkillCategory.ATTACK)
                .castType(SkillCastType.INSTANT)
                .aimType(index % 2 == 0 ? SkillAimType.SELF : SkillAimType.LOOK_TARGET_BLOCK)
                .maxLevel(10)
                .multiplier(1.0 + index * 0.1, 0.12)
                .duration(40 + index * 10, 6)
                .scaling(index % 2 == 0 ? 0.3 : 1.1, index % 2 == 0 ? 0.9 : 0.6)
                .cooldownTicks(60 + index * 10);

        if (prerequisiteSkillId != null && !prerequisiteSkillId.isEmpty()) {
            builder.prerequisite(prerequisiteSkillId, 1);
        }
        return builder.build();
    }

    private static void register(List<SkillDefinition> defs, SkillDefinition def) {
        if (def == null) {
            return;
        }
        if (BY_ID.containsKey(def.getId())) {
            throw new IllegalStateException("Duplicate skill id: " + def.getId());
        }
        defs.add(def);
        BY_ID.put(def.getId(), def);
    }

    public static List<SkillDefinition> getSkills(PlayerClass clazz) {
        return CLASS_SKILLS.getOrDefault(clazz, Collections.emptyList());
    }

    public static SkillDefinition getById(String skillId) {
        return BY_ID.get(skillId);
    }

    /**
     * 依技能 ID 取得該技能所屬職業。
     */
    public static PlayerClass getOwnerClass(String skillId) {
        SkillDefinition def = BY_ID.get(skillId);
        return def == null ? PlayerClass.NONE : def.getOwnerClass();
    }
}

