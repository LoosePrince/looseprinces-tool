package com.tool.looseprince.logic;

import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.feature.DivinityFeature;
import com.tool.looseprince.feature.FeatureRegistry;
import com.tool.looseprince.register.FairDuelRegistrar;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 逻辑层：公平对决逻辑编排（来源激活、冲突屏蔽、周期赋予效果、伤害换算）
 */
public final class FairDuelLogic {
    private FairDuelLogic() {}

    public static boolean isBlockedByDivinity(PlayerEntity player) {
        try {
            DivinityFeature div = (DivinityFeature) FeatureRegistry.getInstance().getFeature("divinity");
            if (div == null) return false;
            boolean creator = div.getCreatorEffect() != null && player.hasStatusEffect(div.getCreatorEffect());
            boolean god = div.getDivinePowerEffect() != null && player.hasStatusEffect(div.getDivinePowerEffect());
            return creator || god;
        } catch (Exception ignored) { return false; }
    }

    public static boolean isCooling(PlayerEntity player, long nowTick) {
        try {
            return com.tool.looseprince.util.CreatorCooldownManager.getInstance().isCoolingDown(player.getUuid(), nowTick);
        } catch (Exception ignored) {
            return false;
        }
    }

    public static double readDamageRatio() {
        Object v = ConfigManager.getInstance().getFeatureConfig("fair_duel").getOption("damageRatio");
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        return 1.0;
    }

    public static void tickGrantEffect(MinecraftServer server) {
        long time = server.getOverworld().getTime();
        if ((time % 10) != 0) {
            return;
        }
        var effectEntry = FairDuelRegistrar.getEffect();
        if (effectEntry == null) {
            return;
        }
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (isCooling(player, time) || isBlockedByDivinity(player)) {
                continue;
            }
            if (hasAnyFairDuelSource(player)) {
                player.addStatusEffect(new StatusEffectInstance(effectEntry, 30, 0, true, true, true));
            }
        }
    }

    public static boolean hasAnyFairDuelSource(PlayerEntity player) {
        // 来源1：残缺神格效果
        try {
            DivinityFeature div = (DivinityFeature) FeatureRegistry.getInstance().getFeature("divinity");
            if (div != null && div.getImperfectDivinityEffect() != null && player.hasStatusEffect(div.getImperfectDivinityEffect())) {
                return true;
            }
        } catch (Exception ignored) {}
        // 来源2：公平对决物品 —— 简化为背包/副手/护甲任意存在
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).getItem() == FairDuelRegistrar.getItem()) {
                return true;
            }
        }
        if (player.getOffHandStack().getItem() == FairDuelRegistrar.getItem()) {
            return true;
        }
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            if (player.getInventory().armor.get(i).getItem() == FairDuelRegistrar.getItem()) {
                return true;
            }
        }
        return false;
    }

    public static float computeAdjustedDamage(PlayerEntity victim, Entity attacker, float originalDamage, double percent) {
        if (percent <= 0) return originalDamage;
        double ratio = readDamageRatio();
        float victimMax = victim.getMaxHealth();
        return (float) Math.max(0.0, victimMax * percent * ratio);
    }
}


