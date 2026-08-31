package com.yichenxbohan.mcnb.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.yichenxbohan.mcnb.Mcnb;
import com.yichenxbohan.mcnb.entity.forskills.DeathStareEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class CustomBillboardRenderer extends EntityRenderer<DeathStareEntity> {

    // 🎯 指向你的自訂眼睛貼圖路徑
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(Mcnb.MODID, "textures/entity/death_stare.png");

    public CustomBillboardRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DeathStareEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 1. 縮放：控制這顆眼睛在畫面的大小（預設 1.0F 是一格寬高，可改大或改小）
        poseStack.scale(15.0F, 15.0F, 15.0F);

        // 2. 廣告看板核心：鎖定玩家鏡頭視角，讓貼圖永遠正對螢幕
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f matrix4f = lastPose.pose();
        Matrix3f matrix3f = lastPose.normal();

        // 使用 entityCutoutNoCull 確保眼睛去背（透明部分）能正確顯示
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));

        // 3. 【已修正】手動繪製 2D 正中心四邊形 (X 軸從 -0.5 到 0.5，Y 軸從 -0.5 到 0.5)
        // 參數順序: consumer, matrix, normal, light, 空間X, 空間Y, 貼圖U, 貼圖V
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, -0.5F, -0.5F, 0.0F, 1.0F); // 左下
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight,  0.5F, -0.5F, 1.0F, 1.0F); // 右下
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight,  0.5F,  0.5F, 1.0F, 0.0F); // 右上
        vertex(vertexConsumer, matrix4f, matrix3f, packedLight, -0.5F,  0.5F, 0.0F, 0.0F); // 左上

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, int light, float x, float y, float u, float v) {
        // 【已修正】移除原本混亂的 y - 0.25F 偏置，直接採用傳入的精確漂浮坐標
        consumer.vertex(matrix, x, y, 0.0F)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(DeathStareEntity entity) {
        return TEXTURE;
    }

    // 建議將這段取消註解：這樣眼睛在黑夜或暗處才不會變成黑麻麻的一團，能維持原本的高亮眼神
    @Override
    protected int getBlockLightLevel(DeathStareEntity entity, net.minecraft.core.BlockPos pos) {
        return 15;
    }
}