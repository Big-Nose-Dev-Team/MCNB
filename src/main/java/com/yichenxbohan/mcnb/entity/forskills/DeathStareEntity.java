package com.yichenxbohan.mcnb.entity.forskills;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class DeathStareEntity extends Projectile {

    public DeathStareEntity(EntityType<? extends DeathStareEntity> type, Level level) {
        super(type, level);
        // 刪除原本報錯的那行，這裡保持乾淨即可
    }

    /**
     * 🎯 真正移除基礎碰撞阻擋的方法
     * 回傳 false 代表這個實體完全沒有實體碰撞箱，玩家或其他生物可以直接穿過去，
     * 同時也絕對不會阻擋玩家在眼睛的位置放置方塊（蓋建築）。
     */
    @Override
    public boolean canCollideWith(net.minecraft.world.entity.Entity entity) {
        return false;
    }

    /**
     * 確保實體本身不會被方塊的物理邊界卡住或推擠
     */
    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        // 初始化同步數據
    }

    @Override
    public void tick() {
        super.tick();

        // 確保實體在剛被召喚或移動時，絕對不會在資料層被設定為著火狀態
        if (this.isOnFire()) {
            this.clearFire();
        }

        // 基礎的移動與碰撞檢測邏輯
        Vec3 vec3 = this.getDeltaMovement();
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hitresult.getType() != HitResult.Type.MISS) {
            this.onHit(hitresult);
        }
        this.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
    }

    /**
     * 🎯 需求 1：全面熄滅火焰外觀
     * 覆寫此方法並恆常回傳 false，這會強制客戶端渲染器（Renderer）不要在眼睛周圍繪製原版的火焰特效。
     */
    @Override
    public boolean isOnFire() {
        return false;
    }

    /**
     * 🎯 需求 2：防止被玩家左鍵打飞
     * 當玩家左鍵點擊（攻擊）這個實體時，系統會呼叫 hurt 方法。
     * 我們直接攔截並回傳 false，代表此實體不吃任何物理打擊與擊退，玩家的手會直接穿過去，無法將其打飛。
     */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 如果你希望它完全不被任何東西（包括玩家左鍵、箭矢等）干擾，直接回傳 false
        return false;
    }

    /**
     * 額外保險：確保該實體不會與玩家發生推擠物理碰撞
     */
    @Override
    public boolean isPickable() {
        // 如果回傳 false，玩家的滑鼠準心甚至無法選中它（完全無法左鍵）；
        // 如果希望維持可以被準心指著（例如顯示血條或名字），請維持回傳 true，交由上方的 hurt() 去免疫左鍵。
        return true;
    }
}