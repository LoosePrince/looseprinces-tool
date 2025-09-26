package com.tool.looseprince.logic;

import com.tool.looseprince.feature.CursedDiscardFeature;
import com.tool.looseprince.feature.FeatureRegistry;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 丢弃诅咒判定与配置访问。尽量复用 SoulBindingUtils 的 owner 读写与 Tooltip。
 */
public final class CursedDiscardLogic {
    private CursedDiscardLogic() {}

    private static CursedDiscardFeature feature() {
        return (CursedDiscardFeature) FeatureRegistry.getInstance().getFeature("cursed_discard");
    }

    public static boolean isEnabled() {
        CursedDiscardFeature f = feature();
        return f != null && f.isEnabled();
    }

    public static boolean matches(ItemStack stack) {
        try {
            if (stack == null || stack.isEmpty()) return false;
            if (stack.getItem() instanceof net.minecraft.item.EnchantedBookItem) return false; // 附魔书不参与绑定/保留
            // 优先用官方 API 判断是否具有此附魔
            var comp = EnchantmentHelper.getEnchantments(stack);
            for (RegistryEntry<Enchantment> e : comp.getEnchantments()) {
                if (e.matchesKey(CursedDiscardFeature.CURSED_DISCARD)) {
                    return comp.getLevel(e) > 0;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static boolean shouldPreventDrop() { CursedDiscardFeature f = feature(); return f != null && f.shouldPreventDrop(); }
    public static boolean shouldPreventContainerInsert() { CursedDiscardFeature f = feature(); return f != null && f.shouldPreventContainerInsert(); }
    public static boolean shouldKeepOnDeath() { CursedDiscardFeature f = feature(); return f != null && f.shouldKeepOnDeath(); }
    public static boolean shouldInstantReturn() { CursedDiscardFeature f = feature(); return f != null && f.shouldInstantReturn(); }
    public static boolean lavaImmune() { CursedDiscardFeature f = feature(); return f == null || f.isLavaImmune(); }
    public static boolean voidDestroyable() { CursedDiscardFeature f = feature(); return f != null && f.isVoidDestroyable(); }
    public static boolean cactusImmune() { CursedDiscardFeature f = feature(); return f == null || f.isCactusImmune(); }
    public static boolean showOwnerTooltip() { CursedDiscardFeature f = feature(); return f == null || f.shouldShowOwnerTooltip(); }

    // 复用 SoulBinding 的 owner 工具
    public static boolean hasOwner(ItemStack stack) { return com.tool.looseprince.util.SoulBindingUtils.hasOwner(stack); }
    public static void ensureOwner(ItemStack stack, ServerPlayerEntity player) { com.tool.looseprince.util.SoulBindingUtils.ensureOwner(stack, player); }
    public static boolean isOwner(ServerPlayerEntity player, ItemStack stack) { return com.tool.looseprince.util.SoulBindingUtils.isOwner(player, stack); }
}


