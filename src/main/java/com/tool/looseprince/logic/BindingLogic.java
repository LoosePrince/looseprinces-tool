package com.tool.looseprince.logic;

import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

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

    public static boolean matches(ItemStack stack) {
        try {
            String s = EnchantmentHelper.getEnchantments(stack).toString();
            return s.contains("looseprinces-tool:binding") || (s.contains("looseprinces-tool") && s.contains("binding"));
        } catch (Exception ignored) { return false; }
    }
}


