package com.yichenxbohan.mcnb.skill.network;

import com.yichenxbohan.mcnb.skill.SkillService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * C2S 技能操作封包，由 API 統一處理升級/分支/施放。
 */
public class SkillActionPacket {

    private final SkillActionType action;
    private final String skillId;
    private final String branchId;

    public SkillActionPacket(SkillActionType action, String skillId, String branchId) {
        this.action = action;
        this.skillId = skillId == null ? "" : skillId;
        this.branchId = branchId == null ? "" : branchId;
    }

    public SkillActionPacket(FriendlyByteBuf buf) {
        this.action = buf.readEnum(SkillActionType.class);
        this.skillId = buf.readUtf(128);
        this.branchId = buf.readUtf(128);
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeUtf(skillId);
        buf.writeUtf(branchId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }

            boolean changed = switch (action) {
                case UPGRADE -> SkillService.upgradeSkill(player, skillId);
                case SELECT_BRANCH -> SkillService.selectBranch(player, skillId, branchId);
                case CAST -> SkillService.castSkill(player, skillId);
                case RESET -> SkillService.resetAll(player);
            };

            if (changed || action == SkillActionType.CAST) {
                SkillService.syncToClient(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

