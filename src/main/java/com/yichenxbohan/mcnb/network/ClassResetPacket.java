package com.yichenxbohan.mcnb.network;

import com.yichenxbohan.mcnb.ModCapabilities;
import com.yichenxbohan.mcnb.playerclass.IPlayerClass;
import com.yichenxbohan.mcnb.playerclass.PlayerClass;
import com.yichenxbohan.mcnb.skill.SkillService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客戶端 -> 服務端：請求重置職業
 */
public class ClassResetPacket {

    // 因為只是發送「重置」指令，不需要傳遞額外的職業參數
    public ClassResetPacket() {
    }

    // 雖然沒有資料要讀取，但 Forge 註冊仍需要這個構造函數
    public ClassResetPacket(FriendlyByteBuf buf) {
    }

    // 雖然沒有資料要寫入，但 Forge 註冊仍需要這個方法
    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            IPlayerClass cap = ModCapabilities.getPlayerClass(player);
            if (cap != null) {
                // 1. 將職業重置為無職業 (假設你的 PlayerClass 裡面有 NONE 或 DEFAULT)
                // 如果是 null，請依據你的 IPlayerClass 實作修改
                PlayerClass defaultClass = PlayerClass.NONE;
                cap.setPlayerClass(defaultClass);

                // 2. 這裡可以加入重置技能點數或重置技能樹的邏輯
                // 例如：SkillService.resetSkills(player);

                cap.clearDirty();

                // 3. 同步回客戶端，讓客戶端的 UI 或資料更新
                ModNetworking.sendToPlayer(new ClassSyncPacket(defaultClass), player);
                SkillService.syncToClient(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}