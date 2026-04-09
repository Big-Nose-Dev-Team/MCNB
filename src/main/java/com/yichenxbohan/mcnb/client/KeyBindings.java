package com.yichenxbohan.mcnb.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static final String KEY_CATEGORY = "key.categories.mcnb";

    // 射魂长弓技能按键（默认 X 键）
    public static final KeyMapping SOUL_BOW_KEY = new KeyMapping(
            "key.mcnb.soul_bow",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            KEY_CATEGORY
    );

    // 屬性面板（預設 C 鍵）
    public static final KeyMapping STATS_PANEL_KEY = new KeyMapping(
            "key.mcnb.stats_panel",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            KEY_CATEGORY
    );

    // 大選單（預設 M 鍵）
    public static final KeyMapping MAIN_MENU_KEY = new KeyMapping(
            "key.mcnb.main_menu",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            KEY_CATEGORY
    );

    // 技能施放快捷鍵（預設 P O I L K J）
    public static final KeyMapping SKILL_CAST_1 = new KeyMapping(
            "key.mcnb.skill_cast_1",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            KEY_CATEGORY
    );

    public static final KeyMapping SKILL_CAST_2 = new KeyMapping(
            "key.mcnb.skill_cast_2",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_O,
            KEY_CATEGORY
    );

    public static final KeyMapping SKILL_CAST_3 = new KeyMapping(
            "key.mcnb.skill_cast_3",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_I,
            KEY_CATEGORY
    );

    public static final KeyMapping SKILL_CAST_4 = new KeyMapping(
            "key.mcnb.skill_cast_4",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            KEY_CATEGORY
    );

    public static final KeyMapping SKILL_CAST_5 = new KeyMapping(
            "key.mcnb.skill_cast_5",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            KEY_CATEGORY
    );

    public static final KeyMapping SKILL_CAST_6 = new KeyMapping(
            "key.mcnb.skill_cast_6",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            KEY_CATEGORY
    );
}
