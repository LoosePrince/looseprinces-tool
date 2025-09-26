package com.tool.looseprince.feature;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * 丢弃诅咒附魔：
 * - 绑定到玩家（显示“已绑定至”），拥有者无法丢弃、无法放入容器、死亡不掉落；
 * - 被丢弃瞬间回归原主（背包满则替换主手并把原主手物品丢出）；
 * - 不会被虚空/熔岩/仙人掌等销毁；
 * - 非拥有者持有时可以丢弃（仍不能放入容器），丢弃瞬间回归原主。
 */
public class CursedDiscardFeature implements Feature {
    private static final String ID = "cursed_discard";
    private static final String DISPLAY_NAME = "丢弃诅咒";
    private static final String DESCRIPTION = "无法丢弃、无法存箱、死亡不掉落并会回归原主的诅咒";

    public static final RegistryKey<Enchantment> CURSED_DISCARD = RegistryKey.of(
            RegistryKeys.ENCHANTMENT,
            Identifier.of(LoosePrincesTool.MOD_ID, "cursed_discard")
    );

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void initialize() {
        if (!isEnabled()) {
            LoosePrincesTool.LOGGER.info("丢弃诅咒已禁用，跳过初始化");
            return;
        }
        LoosePrincesTool.LOGGER.info("初始化丢弃诅咒功能");
        // 逻辑由 mixin 与服务类实现，附魔通过数据包定义
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().isFeatureEnabled(ID);
    }

    @Override
    public FeatureConfig getDefaultConfig() {
        FeatureConfig cfg = new FeatureConfig(true);
        // 行为配置
        cfg.setOption("preventDrop", true); // 拥有者无法丢弃
        cfg.setOption("preventContainerInsert", true); // 无法放入容器
        cfg.setOption("keepOnDeath", true); // 死亡不掉落
        cfg.setOption("instantReturn", true); // 丢弃瞬间回归
        cfg.setOption("lavaImmune", true); // 岩浆免疫
        cfg.setOption("voidDestroyable", false); // 禁止被虚空销毁
        cfg.setOption("cactusImmune", true); // 仙人掌免疫
        // 文本显示
        cfg.setOption("showOwnerTooltip", true);
        return cfg;
    }

    public boolean shouldPreventDrop() { FeatureConfig c = getConfig(); return c != null && c.getBooleanOption("preventDrop", true); }
    public boolean shouldPreventContainerInsert() { FeatureConfig c = getConfig(); return c != null && c.getBooleanOption("preventContainerInsert", true); }
    public boolean shouldKeepOnDeath() { FeatureConfig c = getConfig(); return c != null && c.getBooleanOption("keepOnDeath", true); }
    public boolean shouldInstantReturn() { FeatureConfig c = getConfig(); return c != null && c.getBooleanOption("instantReturn", true); }
    public boolean isLavaImmune() { FeatureConfig c = getConfig(); return c == null || c.getBooleanOption("lavaImmune", true); }
    public boolean isVoidDestroyable() { FeatureConfig c = getConfig(); return c != null && c.getBooleanOption("voidDestroyable", false); }
    public boolean isCactusImmune() { FeatureConfig c = getConfig(); return c == null || c.getBooleanOption("cactusImmune", true); }
    public boolean shouldShowOwnerTooltip() { FeatureConfig c = getConfig(); return c == null || c.getBooleanOption("showOwnerTooltip", true); }
}


