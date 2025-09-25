package com.tool.looseprince.event;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.feature.DivinityFeature;
import com.tool.looseprince.impl.DivinityService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
 
import net.minecraft.server.network.ServerPlayerEntity;
 

/**
 * 神格事件处理：
 * - 每0.5秒检测玩家背包，赋予相应状态效果
 * - 完整神格授予飞行能力；失去时移除
 */
public class DivinityEventHandler {
    private final DivinityFeature feature;

    public DivinityEventHandler(DivinityFeature feature) {
        this.feature = feature;
    }

    public void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long time = server.getOverworld().getTime();
            if ((time % 10) != 0) { // 每0.5s
                return;
            }
            try {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    boolean hasImperfect = com.tool.looseprince.logic.DivinityLogic.hasImperfectItem(player);
                    boolean hasComplete = com.tool.looseprince.logic.DivinityLogic.hasCompleteItem(player);
                    boolean hasCreator = com.tool.looseprince.logic.DivinityLogic.hasCreatorItem(player);

                    boolean cooling = com.tool.looseprince.logic.DivinityLogic.isCooling(player, time);

                    // 冷却期：剥夺相关效果并禁止飞行，同时维持神力静默计时
                    if (cooling) {
                        DivinityService.applyCooling(player, time);
                        continue;
                    }

                    // 非冷却：优先造物主 > 完整神格 > 残缺神格
                    if (hasCreator && feature.getCreatorEffect() != null) {
                        DivinityService.applyCreator(player);
                        try { var st = com.tool.looseprince.state.CodexState.get(player); st.unlock("divine_power"); st.unlock("complete_divinity"); st.save(player);} catch (Throwable ignored) {}
                    } else if (hasComplete && feature.getDivinePowerEffect() != null) {
                        DivinityService.applyDivinePower(player);
                        try { var st = com.tool.looseprince.state.CodexState.get(player); st.unlock("divine_power"); st.unlock("complete_divinity"); st.save(player);} catch (Throwable ignored) {}
                    } else if (hasImperfect) {
                        DivinityService.applyImperfect(player);
                        try { var st = com.tool.looseprince.state.CodexState.get(player); st.unlock("imperfect_divinity"); st.save(player);} catch (Throwable ignored) {}
                        // 不覆盖飞行状态（交由飞行符文或其他来源控制）
                    } else {
                        // 既没有完整神格也没有残缺神格：不要覆盖飞行状态，交由其他功能处理
                    }
                }
            } catch (Exception e) {
                LoosePrincesTool.LOGGER.error("DivinityEventHandler error", e);
            }
        });

        // 第二个tick事件：基于"残缺的神格"状态效果赋予抗性提升V和公平对决
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long time = server.getOverworld().getTime();
            if ((time % 10) != 0) { // 每0.5s
                return;
            }
            try {
                var fairEffect = com.tool.looseprince.logic.DivinityLogic.fairDuelEffect();
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    // 检查是否有"残缺的神格"状态效果
                    if (com.tool.looseprince.logic.DivinityLogic.imperfectEffect() != null && player.hasStatusEffect(com.tool.looseprince.logic.DivinityLogic.imperfectEffect())) {
                        // 抗性提升V（amplifier 4）
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 30, 4, true, true, true));
                        // 公平对决效果（若拥有造物主或神的力量，则不赋予）
                        if (fairEffect != null) {
                            boolean blockFair = false;
                            try {
                                blockFair = com.tool.looseprince.logic.DivinityLogic.shouldBlockFairDuel(player);
                            } catch (Exception ignored) {}
                            if (!blockFair) {
                                player.addStatusEffect(new StatusEffectInstance(fairEffect, 30, 0, true, true, true));
                            } else {
                                // 若已有则移除，确保“不会因为公平对决或残缺神格获得”
                                if (player.hasStatusEffect(fairEffect)) {
                                    player.removeStatusEffect(fairEffect);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LoosePrincesTool.LOGGER.error("DivinityEventHandler secondary tick error", e);
            }
        });
    }

    // 成就授予由实现层处理

    // 物品判定挪至 DivinityLogic
}


