package com.yichenxbohan.mcnb.client.damage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yichenxbohan.mcnb.combat.damage.DamageTypeEx;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 傷害數字渲染器 - 在敵人身上顯示浮動傷害數字
 */
@OnlyIn(Dist.CLIENT)
public class DamageNumberRenderer {

    // 使用線程安全的列表，避免網路線程和渲染線程衝突
    private static final List<DamageNumber> activeNumbers = new CopyOnWriteArrayList<>();
    private static final Random RANDOM = new Random();

    // 數字存活時間（tick）
    private static final int LIFETIME = 40; // 2秒

    // 數字上升速度
    private static final double RISE_SPEED = 0.04;

    // 減小基礎縮放，讓字體線條更細
    private static final float BASE_SCALE = 0.0125f;

    // ==================== 自訂樣式系統 ====================

    /**
     * 傷害數字樣式配置
     */
    public static class DamageStyle {
        public int color;           // 基礎顏色 (RGB)
        public int critColor;       // 暴擊顏色 (RGB)
        public String prefix;       // 前綴符號
        public String suffix;       // 後綴符號
        public String critPrefix;   // 暴擊前綴
        public String critSuffix;   // 暴擊後綴
        public float scale;         // 縮放倍率
        public float critScale;     // 暴擊縮放倍率
        public boolean shake;       // 是否抖動
        public boolean rainbow;     // 是否彩虹色
        public boolean glow;        // 是否發光效果
        public double riseSpeed;    // 上升速度倍率

        public DamageStyle() {
            this.color = 0xFFFFFF;
            this.critColor = 0xFFFFFF;
            this.prefix = "";
            this.suffix = "";
            this.critPrefix = "! ";
            this.critSuffix = "";
            this.scale = 1.0f;
            this.critScale = 1.8f;
            this.shake = false;
            this.rainbow = false;
            this.glow = false;
            this.riseSpeed = 1.0;
        }

        // 建造者模式方法
        public DamageStyle color(int color) { this.color = color; return this; }
        public DamageStyle critColor(int color) { this.critColor = color; return this; }
        public DamageStyle prefix(String prefix) { this.prefix = prefix; return this; }
        public DamageStyle suffix(String suffix) { this.suffix = suffix; return this; }
        public DamageStyle critPrefix(String prefix) { this.critPrefix = prefix; return this; }
        public DamageStyle critSuffix(String suffix) { this.critSuffix = suffix; return this; }
        public DamageStyle scale(float scale) { this.scale = scale; return this; }
        public DamageStyle critScale(float scale) { this.critScale = scale; return this; }
        public DamageStyle shake(boolean shake) { this.shake = shake; return this; }
        public DamageStyle rainbow(boolean rainbow) { this.rainbow = rainbow; return this; }
        public DamageStyle glow(boolean glow) { this.glow = glow; return this; }
        public DamageStyle riseSpeed(double speed) { this.riseSpeed = speed; return this; }
    }

    // 每種傷害類型的樣式配置
    private static final EnumMap<DamageTypeEx, DamageStyle> DAMAGE_STYLES = new EnumMap<>(DamageTypeEx.class);

    static {
        // 初始化默認樣式
        initDefaultStyles();
    }

    /**
     * 初始化默認樣式
     */
    private static void initDefaultStyles() {
        // 物理傷害
        DAMAGE_STYLES.put(DamageTypeEx.PHYSICAL, new DamageStyle()
            .color(0xFFFFFF).critColor(0xFFFF55)
            .prefix("").suffix("").critPrefix("✦ ").critSuffix("")
            .scale(1.0f).critScale(1.5f).glow(false)
        );
        // 魔法傷害
        DAMAGE_STYLES.put(DamageTypeEx.MAGIC, new DamageStyle()
            .color(0xAA55FF).critColor(0xDD88FF)
            .prefix("").suffix("").critPrefix("✦ ").critSuffix("")
            .scale(1.0f).critScale(1.5f).glow(false)
        );
        // 能量傷害
        DAMAGE_STYLES.put(DamageTypeEx.ENERGY, new DamageStyle()
            .color(0xFFD700).critColor(0xFFFF00)
            .prefix("").suffix("").critPrefix("✦ ").critSuffix("")
            .scale(1.0f).critScale(1.5f).glow(false)
        );
        // 真實傷害
        DAMAGE_STYLES.put(DamageTypeEx.TRUE, new DamageStyle()
            .color(0xFF3333).critColor(0xFF0000)
            .prefix("").suffix("").critPrefix("✦ ").critSuffix("")
            .scale(1.0f).critScale(1.5f).shake(true).glow(false)
        );
        // 靈魂傷害
        DAMAGE_STYLES.put(DamageTypeEx.SOUL, new DamageStyle()
            .color(0x00FFCC).critColor(0x55FFDD)
            .prefix("").suffix("").critPrefix("✦ ").critSuffix("")
            .riseSpeed(0.7).scale(1.0f).critScale(1.5f).glow(false)
        );
        // 混沌傷害
        DAMAGE_STYLES.put(DamageTypeEx.CHAOS, new DamageStyle()
            .color(0xFF55FF).critColor(0xFF88FF)
            .prefix("").suffix("").critPrefix("✦ ").critSuffix("")
            .rainbow(true).shake(true).scale(1.0f).critScale(1.5f).glow(false)
        );
        // 空間傷害
        DAMAGE_STYLES.put(DamageTypeEx.SPATIAL, new DamageStyle()
            .color(0x55CCFF).critColor(0x88DDFF)
            .prefix("").suffix("").critPrefix("✦ ").critSuffix("")
            .riseSpeed(0.5).scale(1.0f).critScale(1.5f).glow(false)
        );
        // 時空傷害
        DAMAGE_STYLES.put(DamageTypeEx.TEMPORAL, new DamageStyle()
            .color(0xDDDDDD).critColor(0xFFFFFF)
            .prefix("").suffix("").critPrefix("✦ ").critSuffix("")
            .riseSpeed(1.5).scale(1.0f).critScale(1.5f).glow(false)
        );
    }

    /**
     * 獲取傷害類型的樣式
     */
    public static DamageStyle getStyle(DamageTypeEx type) {
        return DAMAGE_STYLES.getOrDefault(type, new DamageStyle());
    }

    /**
     * 設置傷害類型的樣式
     */
    public static void setStyle(DamageTypeEx type, DamageStyle style) {
        DAMAGE_STYLES.put(type, style);
    }

    /**
     * 重置所有樣式為默認
     */
    public static void resetStyles() {
        DAMAGE_STYLES.clear();
        initDefaultStyles();
    }

    // ==================== 原有方法 ====================

    /**
     * 添加一個傷害數字
     */
    public static void addDamageNumber(double x, double y, double z, double damage,
                                        DamageTypeEx type, boolean isCrit) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        DamageStyle style = getStyle(type);

        // 添加隨機偏移，避免數字重疊
        double offsetX = (RANDOM.nextDouble() - 0.5) * 0.8;
        double offsetZ = (RANDOM.nextDouble() - 0.5) * 0.8;
        double offsetY = RANDOM.nextDouble() * 0.5;

        activeNumbers.add(new DamageNumber(
            x + offsetX,
            y + offsetY,
            z + offsetZ,
            damage,
            type,
            isCrit,
            style
        ));
    }

    /**
     * 添加多種傷害類型的數字
     */
    public static void addDamageNumbers(double x, double y, double z,
                                         Map<DamageTypeEx, Double> damages,
                                         boolean isCrit) {
        int index = 0;
        for (var entry : damages.entrySet()) {
            if (entry.getValue() > 0) {
                double offsetY = index * 0.4;
                addDamageNumber(x, y + offsetY, z, entry.getValue(), entry.getKey(), isCrit);
                index++;
            }
        }
    }

    /**
     * 客戶端 Tick 事件
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.isPaused()) return;

        List<DamageNumber> toRemove = new ArrayList<>();

        for (DamageNumber number : activeNumbers) {
            number.tick();
            if (number.isDead()) {
                toRemove.add(number);
            }
        }

        if (!toRemove.isEmpty()) {
            activeNumbers.removeAll(toRemove);
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (activeNumbers.isEmpty()) return;

        Camera camera = event.getCamera();
        Vec3 cameraPos = camera.getPosition();

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        for (DamageNumber number : activeNumbers) {
            if (!number.isDead()) {
                renderDamageNumber(poseStack, bufferSource, camera, cameraPos, number, event.getPartialTick());
            }
        }

        bufferSource.endBatch();
    }

    private static void renderDamageNumber(PoseStack poseStack, MultiBufferSource bufferSource,
                                           Camera camera, Vec3 cameraPos, DamageNumber number,
                                           float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        DamageStyle style = number.style;

        // 計算位置
        double x = number.x - cameraPos.x;
        double y = (number.y + number.getYOffset(partialTick)) - cameraPos.y;
        double z = number.z - cameraPos.z;

        // 抖動效果
        if (style.shake && number.age < 10) {
            x += (RANDOM.nextDouble() - 0.5) * 0.1;
            y += (RANDOM.nextDouble() - 0.5) * 0.1;
        }

        // 距離檢查
        double distSq = x * x + y * y + z * z;
        if (distSq > 48 * 48) return;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(camera.rotation());

        // 計算縮放
        float scale = BASE_SCALE * style.scale;
        float lifeProgress = number.getLifeProgress();

        // 出現動畫
        if (lifeProgress < 0.1f) {
            float appearProgress = lifeProgress / 0.1f;
            scale *= 0.3f + 0.7f * appearProgress;
        }

        // 暴擊動畫
        if (number.isCrit) {
            float critAnimation = 1.0f;
            if (lifeProgress < 0.15f) {
                float bounce = (float) Math.sin(lifeProgress / 0.15f * Math.PI);
                critAnimation = 1.0f + bounce * 0.6f;
            }
            scale *= style.critScale * critAnimation;
        }

        // 淡出效果
        float alpha = 1.0f;
        if (lifeProgress > 0.6f) {
            alpha = 1.0f - (lifeProgress - 0.6f) / 0.4f;
        }
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));

        poseStack.scale(-scale, -scale, scale);

        // 格式化文字
        String text = formatDamage(number.damage);
        if (number.isCrit) {
            text = style.critPrefix + text + style.critSuffix;
        } else {
            text = style.prefix + text + style.suffix;
        }

        // 獲取顏色
        int color = getColorForNumber(number, alpha);

        float textWidth = font.width(text);
        Matrix4f matrix = poseStack.last().pose();

        // 細描邊（減少偏移量使字體更細）
        int shadowColor = ((int)(alpha * 120) << 24);
        float shadowOffset = 0.25f; // 更小的偏移量看起來更細
        // 簡化陰影層，只畫兩個偏移，並降低透明度
        font.drawInBatch(text, -textWidth / 2 - shadowOffset, 0, shadowColor, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        font.drawInBatch(text, -textWidth / 2 + shadowOffset, 0, shadowColor, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

         // 主文字
         font.drawInBatch(text, -textWidth / 2, 0, color, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);

        poseStack.popPose();
    }

    /**
     * 獲取傷害數字的顏色
     */
    private static int getColorForNumber(DamageNumber number, float alpha) {
        DamageStyle style = number.style;
        int baseColor;

        // 彩虹效果
        if (style.rainbow) {
            float hue = (number.age * 0.05f) % 1.0f;
            baseColor = java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f) & 0x00FFFFFF;
        } else {
            baseColor = number.isCrit ? style.critColor : style.color;
        }

        // 暴擊時顏色更亮
        if (number.isCrit && !style.rainbow) {
            int r = Math.min(255, ((baseColor >> 16) & 0xFF) + 30);
            int g = Math.min(255, ((baseColor >> 8) & 0xFF) + 30);
            int b = Math.min(255, (baseColor & 0xFF) + 30);
            baseColor = (r << 16) | (g << 8) | b;
        }

        int alphaInt = (int)(alpha * 255) << 24;
        return alphaInt | baseColor;
    }

    private static String formatDamage(double damage) {
        if (damage >= 1000000) {
            return String.format("%.1fM", damage / 1000000);
        } else if (damage >= 1000) {
            return String.format("%.1fK", damage / 1000);
        } else if (damage >= 100) {
            return String.format("%.0f", damage);
        } else if (damage >= 10) {
            return String.format("%.1f", damage);
        } else {
            return String.format("%.1f", damage);
        }
    }

    public static void clearAll() {
        activeNumbers.clear();
    }

    /**
     * 傷害數字數據類
     */
    private static class DamageNumber {
        final double x, z;
        double y;
        final double damage;
        final DamageTypeEx type;
        final boolean isCrit;
        final DamageStyle style;
        int age = 0;
        double yVelocity;

        DamageNumber(double x, double y, double z, double damage, DamageTypeEx type, boolean isCrit, DamageStyle style) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.damage = damage;
            this.type = type;
            this.isCrit = isCrit;
            this.style = style;
            this.yVelocity = (isCrit ? RISE_SPEED * 2.0 : RISE_SPEED * 1.2) * style.riseSpeed;
        }

        void tick() {
            age++;
            y += yVelocity;
            yVelocity *= 0.92;
            yVelocity = Math.max(yVelocity, RISE_SPEED * 0.3 * style.riseSpeed);
        }

        double getYOffset(float partialTick) {
            return yVelocity * partialTick;
        }

        boolean isDead() {
            return age >= LIFETIME;
        }

        float getLifeProgress() {
            return (float) age / LIFETIME;
        }
    }
}

