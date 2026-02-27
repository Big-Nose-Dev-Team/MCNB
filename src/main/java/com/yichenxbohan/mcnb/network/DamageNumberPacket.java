package com.yichenxbohan.mcnb.network;

import com.yichenxbohan.mcnb.client.damage.DamageNumberRenderer;
import com.yichenxbohan.mcnb.combat.damage.DamageTypeEx;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 傷害數字網路包 - 從服務端發送到客戶端
 * 用於在客戶端顯示傷害數字
 */
public class DamageNumberPacket {

    private final double x;
    private final double y;
    private final double z;
    private final EnumMap<DamageTypeEx, Double> damages;
    private final boolean isCrit;

    /**
     * 單一傷害類型構造器
     */
    public DamageNumberPacket(double x, double y, double z, double damage, DamageTypeEx type, boolean isCrit) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.damages = new EnumMap<>(DamageTypeEx.class);
        this.damages.put(type, damage);
        this.isCrit = isCrit;
    }

    /**
     * 多種傷害類型構造器
     */
    public DamageNumberPacket(double x, double y, double z, EnumMap<DamageTypeEx, Double> damages, boolean isCrit) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.damages = damages != null ? damages : new EnumMap<>(DamageTypeEx.class);
        this.isCrit = isCrit;
    }

    /**
     * 從網路緩衝區解碼
     */
    public DamageNumberPacket(FriendlyByteBuf buf) {
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.isCrit = buf.readBoolean();

        this.damages = new EnumMap<>(DamageTypeEx.class);
        int count = buf.readVarInt();
        for (int i = 0; i < count; i++) {
            DamageTypeEx type = buf.readEnum(DamageTypeEx.class);
            double damage = buf.readDouble();
            this.damages.put(type, damage);
        }
    }

    /**
     * 編碼到網路緩衝區
     */
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeBoolean(isCrit);

        // 只寫入有傷害的類型
        int count = 0;
        for (Double damage : damages.values()) {
            if (damage > 0) count++;
        }

        buf.writeVarInt(count);
        for (Map.Entry<DamageTypeEx, Double> entry : damages.entrySet()) {
            if (entry.getValue() > 0) {
                buf.writeEnum(entry.getKey());
                buf.writeDouble(entry.getValue());
            }
        }
    }

    /**
     * 處理接收到的數據包（客戶端）
     */
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 使用 DistExecutor 安全地調用客戶端代碼
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                ClientHandler.handle(this);
            });
        });
        context.setPacketHandled(true);
    }

    /**
     * 客戶端處理器 - 分離到獨立類避免服務端加載問題
     */
    @OnlyIn(Dist.CLIENT)
    public static class ClientHandler {
        public static void handle(DamageNumberPacket packet) {
            // 確保在客戶端且世界存在
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;

            // 為每種傷害類型添加數字
            int index = 0;
            for (Map.Entry<DamageTypeEx, Double> entry : packet.damages.entrySet()) {
                if (entry.getValue() > 0) {
                    double offsetY = index * 0.35; // 錯開顯示
                    DamageNumberRenderer.addDamageNumber(
                        packet.x, packet.y + offsetY, packet.z,
                        entry.getValue(),
                        entry.getKey(),
                        packet.isCrit
                    );
                    index++;
                }
            }
        }
    }
}
