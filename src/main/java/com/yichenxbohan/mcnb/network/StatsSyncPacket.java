package com.yichenxbohan.mcnb.network;

import com.yichenxbohan.mcnb.ModCapabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 伺服器 -> 客戶端：同步玩家戰鬥屬性
 */
public class StatsSyncPacket {

    // ── 攻擊 ──
    private final double physicalAttack;
    private final double magicAttack;
    private final double energyAttack;
    private final double soulAttack;
    private final double chaosAttack;
    private final double spatialAttack;
    private final double temporalAttack;
    private final double trueAttack;
    private final double weaponMultiplier;
    private final double penetration;
    private final double critChance;
    private final double critDamage;
    private final double damageBonus;
    private final double finalDamageMultiplier;
    // ── 防禦（統一） ──
    private final float  defense;
    private final double damageReduction;
    private final double evasion;
    // ── 特殊 ──
    private final float  energyShield;
    private final float  maxEnergyShield;
    private final float  soulIntegrity;
    // ── 恢復 ──
    private final double healingBonus;
    private final double regen;

    public StatsSyncPacket(
            double physicalAttack, double magicAttack,
            double energyAttack, double soulAttack, double chaosAttack,
            double spatialAttack, double temporalAttack, double trueAttack,
            double weaponMultiplier, double penetration,
            double critChance, double critDamage,
            double damageBonus, double finalDamageMultiplier,
            float defense,
            double damageReduction, double evasion,
            float energyShield, float maxEnergyShield, float soulIntegrity,
            double healingBonus, double regen) {

        this.physicalAttack       = physicalAttack;
        this.magicAttack          = magicAttack;
        this.energyAttack         = energyAttack;
        this.soulAttack           = soulAttack;
        this.chaosAttack          = chaosAttack;
        this.spatialAttack        = spatialAttack;
        this.temporalAttack       = temporalAttack;
        this.trueAttack           = trueAttack;
        this.weaponMultiplier     = weaponMultiplier;
        this.penetration          = penetration;
        this.critChance           = critChance;
        this.critDamage           = critDamage;
        this.damageBonus          = damageBonus;
        this.finalDamageMultiplier = finalDamageMultiplier;
        this.defense             = defense;
        this.damageReduction      = damageReduction;
        this.evasion              = evasion;
        this.energyShield         = energyShield;
        this.maxEnergyShield      = maxEnergyShield;
        this.soulIntegrity        = soulIntegrity;
        this.healingBonus         = healingBonus;
        this.regen                = regen;
    }

    public StatsSyncPacket(FriendlyByteBuf buf) {
        physicalAttack       = buf.readDouble();
        magicAttack          = buf.readDouble();
        energyAttack         = buf.readDouble();
        soulAttack           = buf.readDouble();
        chaosAttack          = buf.readDouble();
        spatialAttack        = buf.readDouble();
        temporalAttack       = buf.readDouble();
        trueAttack           = buf.readDouble();
        weaponMultiplier     = buf.readDouble();
        penetration          = buf.readDouble();
        critChance           = buf.readDouble();
        critDamage           = buf.readDouble();
        damageBonus          = buf.readDouble();
        finalDamageMultiplier = buf.readDouble();
        defense              = buf.readFloat();
        damageReduction      = buf.readDouble();
        evasion              = buf.readDouble();
        energyShield         = buf.readFloat();
        maxEnergyShield      = buf.readFloat();
        soulIntegrity        = buf.readFloat();
        healingBonus         = buf.readDouble();
        regen                = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(physicalAttack);
        buf.writeDouble(magicAttack);
        buf.writeDouble(energyAttack);
        buf.writeDouble(soulAttack);
        buf.writeDouble(chaosAttack);
        buf.writeDouble(spatialAttack);
        buf.writeDouble(temporalAttack);
        buf.writeDouble(trueAttack);
        buf.writeDouble(weaponMultiplier);
        buf.writeDouble(penetration);
        buf.writeDouble(critChance);
        buf.writeDouble(critDamage);
        buf.writeDouble(damageBonus);
        buf.writeDouble(finalDamageMultiplier);
        buf.writeFloat(defense);
        buf.writeDouble(damageReduction);
        buf.writeDouble(evasion);
        buf.writeFloat(energyShield);
        buf.writeFloat(maxEnergyShield);
        buf.writeFloat(soulIntegrity);
        buf.writeDouble(healingBonus);
        buf.writeDouble(regen);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::handleClient)
        );
        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private void handleClient() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        player.getCapability(ModCapabilities.COMBAT_DATA).ifPresent(cap -> {
            cap.setPhysicalAttack(physicalAttack);
            cap.setMagicAttack(magicAttack);
            cap.setEnergyAttack(energyAttack);
            cap.setSoulAttack(soulAttack);
            cap.setChaosAttack(chaosAttack);
            cap.setSpatialAttack(spatialAttack);
            cap.setTemporalAttack(temporalAttack);
            cap.setTrueAttack(trueAttack);
            cap.setWeaponMultiplier(weaponMultiplier);
            cap.setPenetration(penetration);
            cap.setCritChance(critChance);
            cap.setCritDamage(critDamage);
            cap.setDamageBonus(damageBonus);
            cap.setFinalDamageMultiplier(finalDamageMultiplier);
            cap.setDefense(defense);
            cap.setDamageReduction(damageReduction);
            cap.setEvasion(evasion);
            cap.setEnergyShield(energyShield);
            cap.setMaxEnergyShield(maxEnergyShield);
            cap.setSoulIntegrity(soulIntegrity);
            cap.setHealingBonus(healingBonus);
            cap.setRegen(regen);
        });
    }

    /** 從玩家的 Capability 建構封包 */
    public static StatsSyncPacket from(net.minecraft.world.entity.player.Player player) {
        return player.getCapability(ModCapabilities.COMBAT_DATA).map(cap ->
            new StatsSyncPacket(
                cap.getPhysicalAttack(), cap.getMagicAttack(),
                cap.getEnergyAttack(),   cap.getSoulAttack(),
                cap.getChaosAttack(),    cap.getSpatialAttack(),
                cap.getTemporalAttack(), cap.getTrueAttack(),
                cap.getWeaponMultiplier(), cap.getPenetration(),
                cap.getCritChance(),     cap.getCritDamage(),
                cap.getDamageBonus(),    cap.getFinalDamageMultiplier(),
                cap.getDefense(),
                cap.getDamageReduction(), cap.getEvasion(),
                cap.getEnergyShield(),   cap.getMaxEnergyShield(),
                cap.getSoulIntegrity(),
                cap.getHealingBonus(),   cap.getRegen()
            )
        ).orElse(null);
    }
}
