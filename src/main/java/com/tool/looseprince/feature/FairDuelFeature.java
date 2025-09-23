package com.tool.looseprince.feature;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;
import com.tool.looseprince.item.FairDuelItem;
import com.tool.looseprince.event.FairDuelEventHandler;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

/**
 * 公平对决功能
 * 提供一个物品，放在背包中时根据玩家对目标造成的伤害比例调整玩家受到的伤害比例
 */
public class FairDuelFeature implements Feature {
    private static final String ID = "fair_duel";
    private static final String DISPLAY_NAME = "公平对决";
    private static final String DESCRIPTION = "根据与目标互相造成伤害的比例来平衡对决";

    // 物品实例
    private Item fairDuelItem;

    // 用于物品组展示
    private static Item staticFairDuelItem;
    private FairDuelEventHandler eventHandler;

    // 公平对决状态效果
    public static final RegistryKey<StatusEffect> FAIR_DUEL_EFFECT_KEY = RegistryKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "fair_duel"));
    private RegistryEntry<StatusEffect> fairDuelEffect;

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

        registerItem();

        // 注册事件
        eventHandler = new FairDuelEventHandler(this);
        eventHandler.registerEvents();

        // 注册状态效果
        registerStatusEffect();

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

    private void registerItem() {
        try {
            Item.Settings settings = new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC);

            fairDuelItem = new FairDuelItem(settings);
            staticFairDuelItem = fairDuelItem;

            Registry.register(
                    Registries.ITEM,
                    Identifier.of(LoosePrincesTool.MOD_ID, "fair_duel"),
                    fairDuelItem
            );

            LoosePrincesTool.LOGGER.debug("公平对决物品注册成功");
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("注册公平对决物品失败", e);
        }
    }

    public Item getItem() {
        return fairDuelItem;
    }

    public static Item getStaticItem() {
        return staticFairDuelItem;
    }

    public FairDuelEventHandler getEventHandler() {
        return eventHandler;
    }

    private void registerStatusEffect() {
        try {
            StatusEffect effect = new StatusEffect(StatusEffectCategory.BENEFICIAL, 0xF6C453) {};
            StatusEffect registered = Registry.register(Registries.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "fair_duel"), effect);
            this.fairDuelEffect = Registries.STATUS_EFFECT.getEntry(registered);
            LoosePrincesTool.LOGGER.debug("公平对决状态效果注册成功");
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("注册公平对决状态效果失败", e);
        }
    }

    public RegistryEntry<StatusEffect> getFairDuelEffect() {
        return fairDuelEffect;
    }
}


