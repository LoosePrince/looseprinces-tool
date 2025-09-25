package com.tool.looseprince.logic;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;
import com.tool.looseprince.feature.DivinityFeature;
import com.tool.looseprince.feature.FeatureRegistry;
import com.tool.looseprince.item.FlyingRuneItem;
import com.tool.looseprince.impl.CooldownService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 逻辑层：集中进行是否应允许飞行的判定
 */
public final class FlightLogic {
    private FlightLogic() {}

    public static FlightDecision evaluate(ServerPlayerEntity player) {
        try {
            // 创造/旁观不干预
            if (player.isCreative() || player.isSpectator()) {
                return new FlightDecision(true, false);
            }

            FeatureConfig config = ConfigManager.getInstance().getFeatureConfig("flying_rune");
            boolean preventFallDamage = config != null && config.getBooleanOption("preventFallDamage", true);

            // 维度限制
            String dimensionId = player.getWorld().getRegistryKey().getValue().toString();
            if (!isAllowedInDimension(config, dimensionId)) {
                return new FlightDecision(false, preventFallDamage);
            }

            // 造物主冷却期间禁飞
            long nowTick = player.getServerWorld().getTime();
            boolean creatorCooling = false;
            try {
                creatorCooling = com.tool.looseprince.util.CreatorCooldownManager.getInstance()
                    .isCoolingDown(player.getUuid(), nowTick);
            } catch (Exception ignored) {}

            if (creatorCooling) {
                return new FlightDecision(false, preventFallDamage);
            }

            // 神格效果可直接启用飞行
            boolean hasGodLikePower = hasDivineFlight(player);

            // 物品要求：在背包或手持
            boolean requireInInventory = config == null || config.getBooleanOption("requireInInventory", true);
            boolean hasRune = requireInInventory ? hasRuneInInventory(player) : hasRuneInHand(player);

            // 玩家级冷却（神力静默等）：视为不持有
            boolean playerCooling = CooldownService.isPlayerCooling(player, com.tool.looseprince.logic.CooldownKeys.FLYING_RUNE);
            boolean allow = (hasRune || hasGodLikePower) && !playerCooling;
            return new FlightDecision(allow, preventFallDamage);
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[logic] 评估飞行判定失败", e);
            return new FlightDecision(false, true);
        }
    }

    private static boolean isAllowedInDimension(FeatureConfig config, String dimensionId) {
        if (config == null) {
            return true;
        }
        switch (dimensionId) {
            case "minecraft:the_nether":
                return config.getBooleanOption("allowInNether", true);
            case "minecraft:the_end":
                return config.getBooleanOption("allowInEnd", true);
            default:
                return true;
        }
    }

    private static boolean hasDivineFlight(ServerPlayerEntity player) {
        try {
            DivinityFeature div = (DivinityFeature) FeatureRegistry.getInstance().getFeature("divinity");
            if (div == null) {
                return false;
            }
            boolean god = div.getDivinePowerEffect() != null && player.hasStatusEffect(div.getDivinePowerEffect());
            boolean creator = div.getCreatorEffect() != null && player.hasStatusEffect(div.getCreatorEffect());
            return god || creator;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean hasRuneInInventory(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof FlyingRuneItem) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasRuneInHand(PlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        ItemStack off = player.getOffHandStack();
        return (!main.isEmpty() && main.getItem() instanceof FlyingRuneItem)
            || (!off.isEmpty() && off.getItem() instanceof FlyingRuneItem);
    }
}


