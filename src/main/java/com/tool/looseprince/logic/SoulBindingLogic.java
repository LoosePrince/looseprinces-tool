package com.tool.looseprince.logic;

import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;
import com.tool.looseprince.util.SoulBindingUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 逻辑层：灵魂绑定的配置读取与通用判定，具体状态读写委托给 SoulBindingUtils
 */
public final class SoulBindingLogic {
    private SoulBindingLogic() {}

    private static FeatureConfig cfg() {
        return ConfigManager.getInstance().getFeatureConfig("soul_binding");
    }

    public static boolean isEnabled() {
        FeatureConfig c = cfg();
        return c != null && c.isEnabled();
    }

    public static boolean preventPickup() {
        FeatureConfig c = cfg();
        return c == null || c.getBooleanOption("preventPickup", true);
    }

    public static boolean preventContainerTake() {
        FeatureConfig c = cfg();
        return c == null || c.getBooleanOption("preventContainerTake", true);
    }

    public static int level2TeleportSeconds() {
        FeatureConfig c = cfg();
        Object v = c != null ? c.getOption("level2TeleportSeconds") : null;
        if (v instanceof Number n) return n.intValue();
        return 30;
    }

    public static boolean lavaImmune() {
        FeatureConfig c = cfg();
        return c == null || c.getBooleanOption("lavaImmune", true);
    }

    public static boolean voidDestroyable() {
        FeatureConfig c = cfg();
        return c != null && c.getBooleanOption("voidDestroyable", false);
    }

    // 封装 Utils
    public static boolean hasSoulBinding(ItemStack stack) { return SoulBindingUtils.hasSoulBinding(stack); }
    public static int getSoulBindingLevel(ItemStack stack) { return SoulBindingUtils.getSoulBindingLevel(stack); }
    public static boolean hasOwner(ItemStack stack) { return SoulBindingUtils.hasOwner(stack); }
    public static void ensureOwner(ItemStack stack, ServerPlayerEntity player) { SoulBindingUtils.ensureOwner(stack, player); }
    public static boolean isOwner(ServerPlayerEntity player, ItemStack stack) { return SoulBindingUtils.isOwner(player, stack); }
    public static int getDropTick(ItemStack stack) { return SoulBindingUtils.getDropTick(stack); }
    public static void markDropTick(ItemStack stack, int tick) { SoulBindingUtils.markDropTick(stack, tick); }
    public static java.util.UUID getOwnerUuid(ItemStack stack) { return SoulBindingUtils.getOwnerUuid(stack); }
}


