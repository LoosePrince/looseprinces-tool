package com.tool.looseprince.event;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.feature.FairDuelFeature;
import com.tool.looseprince.impl.FairDuelService;
import com.tool.looseprince.logic.FairDuelLogic;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
 
 
/**
 * 公平对决事件处理器
 * 记录玩家对某目标造成的上一次伤害占比，并按该占比调整玩家后续从该目标处受到的伤害
 */
public class FairDuelEventHandler {
    @SuppressWarnings("unused")
    private final FairDuelFeature feature; // 保留用于读取配置等

    // 使用嵌套Map避免Pair键可能导致的hash/equals问题
    // outerKey: playerUUID (攻击者/受击者中的玩家)
    // innerKey: otherUUID (对手生物)
    // 数据转由实现层持有

    public FairDuelEventHandler(FairDuelFeature feature) {
        this.feature = feature;
    }

    public void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            FairDuelService.tickCleanup(server);
        });

        // 每0.5秒给满足来源(物品或残缺神格效果)的玩家赋予1.5秒公平对决效果
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            FairDuelService.tickGrantEffect(server);
            try {
                for (net.minecraft.server.network.ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                    if (com.tool.looseprince.logic.FairDuelLogic.hasAnyFairDuelSource(p)) {
                        var st = com.tool.looseprince.state.CodexState.get(p);
                        st.unlock("fair_duel");
                        st.unlock("fair_duel_effect");
                        st.save(p);
                    }
                }
            } catch (Throwable ignored) {}
        });
    }

    public void recordPlayerDealtDamage(PlayerEntity attacker, LivingEntity target, float amount) {
        if (attacker == null || target == null || amount <= 0) {
            return;
        }
        if (!hasFairDuel(attacker)) {
            return;
        }
        float max = target.getMaxHealth();
        if (max <= 0.0001f) {
            return;
        }
        FairDuelService.recordPlayerDealtDamage(attacker, target, amount);
    }

    public float modifyIncomingDamage(PlayerEntity victim, Entity attacker, float originalDamage) {
        if (victim == null || attacker == null || originalDamage <= 0) {
            LoosePrincesTool.LOGGER.info("[FairDuel] skip: victimOrAttackerNull={} originalDamage={}", attacker == null, originalDamage);
            return originalDamage;
        }
        return FairDuelService.maybeAdjustIncomingDamage(victim, attacker, originalDamage);
    }

    private boolean hasFairDuel(PlayerEntity player) {
        return FairDuelLogic.hasAnyFairDuelSource(player) && !FairDuelLogic.isBlockedByDivinity(player);
    }
}


