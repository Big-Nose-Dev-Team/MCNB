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
}

