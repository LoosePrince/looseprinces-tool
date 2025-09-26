package com.tool.looseprince.logic;

import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;
import com.tool.looseprince.feature.BindingEnchantmentFeature;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

/**
 * 逻辑层：绑定附魔判定与配置访问
 */
public final class BindingLogic {
    private BindingLogic() {}

    public static boolean isEnabled() {
        FeatureConfig cfg = ConfigManager.getInstance().getFeatureConfig("binding_enchantment");
        return cfg != null && cfg.isEnabled();
    }

    public static boolean shouldPreventDrop() {
        FeatureConfig cfg = ConfigManager.getInstance().getFeatureConfig("binding_enchantment");
        return cfg != null && cfg.getBooleanOption("preventDrop", true);
    }

    /**
     * 是否为带有本模组“绑定”附魔的非附魔书物品。
     */
    public static boolean matches(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() instanceof EnchantedBookItem) return false; // 附魔书不参与死亡保留
        try {
            var comp = EnchantmentHelper.getEnchantments(stack);
            java.util.Set<RegistryEntry<Enchantment>> set = comp.getEnchantments();
            for (RegistryEntry<Enchantment> e : set) {
                if (e.matchesKey(BindingEnchantmentFeature.BINDING)) {
                    return comp.getLevel(e) > 0;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }
}


