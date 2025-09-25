package com.tool.looseprince.feature;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;
import com.tool.looseprince.event.DivinityEventHandler;
import com.tool.looseprince.register.DivinityRegistrar;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

/**
 * 神格功能：提供两件物品及对应状态效果
 * - 残缺的神格：背包中持续获得“残缺的神格”效果（等效 抗性提升V + 公平对决）
 * - 完整的神格：背包中持续获得“神的力量”（免疫伤害，禁用公平对决），并获得飞行
 */
public class DivinityFeature implements Feature {
    private static final String ID = "divinity";
    private static final String DISPLAY_NAME = "神格";
    private static final String DESCRIPTION = "授予残缺/完整神格效果";

    // 状态效果键
    public static final RegistryKey<StatusEffect> IMPERFECT_DIVINITY_EFFECT_KEY = RegistryKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "imperfect_divinity"));
    public static final RegistryKey<StatusEffect> DIVINE_POWER_EFFECT_KEY = RegistryKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "divine_power"));
    public static final RegistryKey<StatusEffect> CREATOR_EFFECT_KEY = RegistryKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "creator"));
    public static final RegistryKey<StatusEffect> DIVINE_SILENCE_EFFECT_KEY = RegistryKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "divine_silence"));

    // 事件
    private DivinityEventHandler eventHandler;

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
            LoosePrincesTool.LOGGER.info("神格功能已禁用，跳过初始化");
            return;
        }

        LoosePrincesTool.LOGGER.info("初始化神格功能");

        DivinityRegistrar.register();

        eventHandler = new DivinityEventHandler(this);
        eventHandler.registerEvents();

        LoosePrincesTool.LOGGER.info("神格功能初始化完成");
    }

    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().isFeatureEnabled(ID);
    }

    @Override
    public FeatureConfig getDefaultConfig() {
        return new FeatureConfig(true);
    }

    public Item getImperfectDivinityItem() {
        return com.tool.looseprince.register.DivinityRegistrar.getImperfectItem();
    }

    public Item getCompleteDivinityItem() {
        return com.tool.looseprince.register.DivinityRegistrar.getCompleteItem();
    }

    public Item getCreatorDivinityItem() {
        return com.tool.looseprince.register.DivinityRegistrar.getCreatorItem();
    }

    public static Item getStaticImperfectDivinityItem() {
        return com.tool.looseprince.register.DivinityRegistrar.getImperfectItem();
    }

    public static Item getStaticCompleteDivinityItem() {
        return com.tool.looseprince.register.DivinityRegistrar.getCompleteItem();
    }

    public static Item getStaticCreatorDivinityItem() {
        return com.tool.looseprince.register.DivinityRegistrar.getCreatorItem();
    }

    public RegistryEntry<StatusEffect> getImperfectDivinityEffect() {
        return com.tool.looseprince.register.DivinityRegistrar.getImperfectEffect();
    }

    public RegistryEntry<StatusEffect> getDivinePowerEffect() {
        return com.tool.looseprince.register.DivinityRegistrar.getDivinePowerEffect();
    }

    public RegistryEntry<StatusEffect> getCreatorEffect() {
        return com.tool.looseprince.register.DivinityRegistrar.getCreatorEffect();
    }

    public RegistryEntry<StatusEffect> getDivineSilenceEffect() {
        return com.tool.looseprince.register.DivinityRegistrar.getDivineSilenceEffect();
    }
}


