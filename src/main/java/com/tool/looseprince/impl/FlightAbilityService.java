package com.tool.looseprince.impl;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.logic.FlightDecision;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * 实现层：实际应用飞行能力与跌落处理
 */
public final class FlightAbilityService {
    private FlightAbilityService() {}

    public static void apply(ServerPlayerEntity player, FlightDecision decision) {
        boolean shouldAllowFlying = decision.shouldAllowFlight();

        if (player.getAbilities().allowFlying != shouldAllowFlying) {
            player.getAbilities().allowFlying = shouldAllowFlying;

            if (!shouldAllowFlying && player.getAbilities().flying) {
                player.getAbilities().flying = false;
                if (decision.shouldPreventFallDamage()) {
                    player.fallDistance = 0.0f;
                }
            }

            player.sendAbilitiesUpdate();

            if (shouldAllowFlying && com.tool.looseprince.LoosePrincesTool.isOurAdvancementsLoaded()) {
                grantAdvancement(player, "wings", "granted_by_code");
                try { var st = com.tool.looseprince.state.CodexState.get(player); st.unlock("flying_rune"); st.save(player);} catch (Throwable ignored) {}
            }
        }
    }

    private static void grantAdvancement(ServerPlayerEntity player, String path, String criterion) {
        try {
            Identifier id = Identifier.of(LoosePrincesTool.MOD_ID, path);
            AdvancementEntry adv = player.getServer().getAdvancementLoader().get(id);
            if (adv != null) {
                boolean granted = player.getAdvancementTracker().grantCriterion(adv, criterion);
                LoosePrincesTool.LOGGER.info("[Adv] grant {}:{} -> {} => {}", id.getNamespace(), id.getPath(), criterion, granted);
            } else {
                LoosePrincesTool.LOGGER.warn("[Adv] missing advancement {}", id);
            }
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[Adv] grant error", e);
        }
    }
}


