package com.tool.looseprince.impl;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.logic.FairDuelLogic;
import com.tool.looseprince.register.FairDuelRegistrar;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 实现层：公平对决数据记录、效果授予与伤害替换应用
 */
public final class FairDuelService {
    private static final Map<UUID, Map<UUID, Double>> lastDamagePercent = new HashMap<>();
    private static final Map<UUID, Map<UUID, Integer>> lastUpdateTick = new HashMap<>();
    private static final Map<UUID, Integer> lastEffectAppliedTick = new HashMap<>();

    private FairDuelService() {}

    public static void tickCleanup(MinecraftServer server) {
        int tick = (int) server.getOverworld().getTime();
        lastUpdateTick.entrySet().removeIf(e -> {
            UUID playerId = e.getKey();
            Map<UUID, Integer> inner = e.getValue();
            if (inner == null) return true;
            inner.entrySet().removeIf(ie -> {
                boolean expired = tick - ie.getValue() > 200;
                if (expired) {
                    Map<UUID, Double> percentInner = lastDamagePercent.get(playerId);
                    if (percentInner != null) {
                        percentInner.remove(ie.getKey());
                        if (percentInner.isEmpty()) {
                            lastDamagePercent.remove(playerId);
                        }
                    }
                }
                return expired;
            });
            return inner.isEmpty();
        });
    }

    public static void tickGrantEffect(MinecraftServer server) {
        long time = server.getOverworld().getTime();
        var effectEntry = FairDuelRegistrar.getEffect();
        if (effectEntry == null) return;
        if ((time % 10) != 0) return;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (FairDuelLogic.isCooling(player, time) || FairDuelLogic.isBlockedByDivinity(player)) continue;
            if (FairDuelLogic.hasAnyFairDuelSource(player)) {
                player.addStatusEffect(new StatusEffectInstance(effectEntry, 30, 0, true, true, true));
                lastEffectAppliedTick.put(player.getUuid(), (int) time);
                if (com.tool.looseprince.LoosePrincesTool.isOurAdvancementsLoaded()) {
                    grantAdvancement(player, "god_scale", "granted_by_code");
                }
            }
        }
    }

    public static void recordPlayerDealtDamage(PlayerEntity attacker, LivingEntity target, float amount) {
        if (attacker == null || target == null || amount <= 0) return;
        float max = target.getMaxHealth();
        if (max <= 0.0001f) return;
        double percent = Math.max(0.0, Math.min(1.0, amount / max));
        UUID playerId = attacker.getUuid();
        UUID otherId = target.getUuid();
        lastDamagePercent.computeIfAbsent(playerId, k -> new HashMap<>()).put(otherId, percent);
        lastUpdateTick.computeIfAbsent(playerId, k -> new HashMap<>()).put(otherId, (int) attacker.getWorld().getTime());
        LoosePrincesTool.LOGGER.debug("[FairDuel] record attacker={} target={} dealt={} max={} percent={}",
            attacker.getName().getString(), target.getName().getString(), amount, max, percent);
    }

    public static float maybeAdjustIncomingDamage(PlayerEntity victim, Entity attacker, float originalDamage) {
        var effectEntry = FairDuelRegistrar.getEffect();
        if (effectEntry == null) return originalDamage;
        long now = victim.getWorld().getTime();
        if (FairDuelLogic.isCooling(victim, now) || FairDuelLogic.isBlockedByDivinity(victim)) return originalDamage;
        boolean hasEffect = victim.hasStatusEffect(effectEntry);
        int lastTick = lastEffectAppliedTick.getOrDefault(victim.getUuid(), -999999);
        boolean withinGrace = ((int) now) - lastTick <= 5;
        if (!hasEffect && !withinGrace) return originalDamage;
        if (!(attacker instanceof LivingEntity livingAttacker)) return originalDamage;
        UUID playerId = victim.getUuid();
        UUID otherId = livingAttacker.getUuid();
        Map<UUID, Double> inner = lastDamagePercent.get(playerId);
        Double percent = inner != null ? inner.get(otherId) : null;
        if (percent == null || percent <= 0) return originalDamage;
        return FairDuelLogic.computeAdjustedDamage(victim, attacker, originalDamage, percent);
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


