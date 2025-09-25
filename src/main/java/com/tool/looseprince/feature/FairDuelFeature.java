package com.tool.looseprince.feature;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;
import com.tool.looseprince.register.FairDuelRegistrar;
import com.tool.looseprince.event.FairDuelEventHandler;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

/**
 * 公平对决功能
 * 提供一个物品，放在背包中时根据玩家对目标造成的伤害比例调整玩家受到的伤害比例
 */
public class FairDuelFeature implements Feature {
    private static final String ID = "fair_duel";
    private static final String DISPLAY_NAME = "公平对决";
    private static final String DESCRIPTION = "根据与目标互相造成伤害的比例来平衡对决";

    // 用于物品组展示
    private FairDuelEventHandler eventHandler;

    // 公平对决状态效果
    public static final RegistryKey<StatusEffect> FAIR_DUEL_EFFECT_KEY = RegistryKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "fair_duel"));

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public void initialize() {
        if (!isEnabled()) {
            LoosePrincesTool.LOGGER.debug("公平对决功能已禁用，跳过初始化");
            return;
        }

        LoosePrincesTool.LOGGER.debug("初始化公平对决功能");

        FairDuelRegistrar.register();

        // 注册事件
        eventHandler = new FairDuelEventHandler(this);
        eventHandler.registerEvents();

        LoosePrincesTool.LOGGER.debug("公平对决功能初始化完成");
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().isFeatureEnabled(ID);
    }

    @Override
    public FeatureConfig getDefaultConfig() {
        FeatureConfig config = new FeatureConfig(true);
        config.setOption("damageRatio", 1.0); // 默认100%转换比例
        return config;
    }

    /**
     * 获取伤害转换比例
     */
    public double getDamageRatio() {
        FeatureConfig config = ConfigManager.getInstance().getFeatureConfig(ID);
        if (config != null && config.hasOption("damageRatio")) {
            Object ratio = config.getOption("damageRatio");
            if (ratio instanceof Number) {
                return ((Number) ratio).doubleValue();
            }
        }
        return 1.0; // 默认100%
    }

    public static Item getStaticItem() {
        return com.tool.looseprince.register.FairDuelRegistrar.getItem();
    }

    public FairDuelEventHandler getEventHandler() {
        return eventHandler;
    }

    public RegistryEntry<StatusEffect> getFairDuelEffect() {
        return com.tool.looseprince.register.FairDuelRegistrar.getEffect();
    }
}


