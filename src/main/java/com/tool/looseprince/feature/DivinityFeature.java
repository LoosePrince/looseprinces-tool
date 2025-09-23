package com.tool.looseprince.feature;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;
import com.tool.looseprince.event.DivinityEventHandler;
import com.tool.looseprince.item.CompleteDivinityItem;
import com.tool.looseprince.item.ImperfectDivinityItem;
import com.tool.looseprince.item.CreatorDivinityItem;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

/**
 * 神格功能：提供两件物品及对应状态效果
 * - 残缺的神格：背包中持续获得“残缺的神格”效果（等效 抗性提升V + 公平对决）
 * - 完整的神格：背包中持续获得“神的力量”（免疫伤害，禁用公平对决），并获得飞行
 */
public class DivinityFeature implements Feature {
    private static final String ID = "divinity";
    private static final String DISPLAY_NAME = "神格";
    private static final String DESCRIPTION = "授予残缺/完整神格效果";

    // 物品
    private Item imperfectDivinityItem;
    private Item completeDivinityItem;
    private Item creatorDivinityItem;
    private static Item staticImperfectDivinityItem;
    private static Item staticCompleteDivinityItem;
    private static Item staticCreatorDivinityItem;

    // 状态效果键
    public static final RegistryKey<StatusEffect> IMPERFECT_DIVINITY_EFFECT_KEY = RegistryKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "imperfect_divinity"));
    public static final RegistryKey<StatusEffect> DIVINE_POWER_EFFECT_KEY = RegistryKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "divine_power"));
    public static final RegistryKey<StatusEffect> CREATOR_EFFECT_KEY = RegistryKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "creator"));
    public static final RegistryKey<StatusEffect> DIVINE_SILENCE_EFFECT_KEY = RegistryKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "divine_silence"));

    // 注册后引用
    private RegistryEntry<StatusEffect> imperfectDivinityEffect;
    private RegistryEntry<StatusEffect> divinePowerEffect;
    private RegistryEntry<StatusEffect> creatorEffect;
    private RegistryEntry<StatusEffect> divineSilenceEffect;

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

        registerItems();
        registerEffects();

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
        // 默认启用
        return new FeatureConfig(true);
    }

    private void registerItems() {
        try {
            Item.Settings settingsEpic = new Item.Settings().maxCount(1).rarity(Rarity.EPIC);
            Item.Settings settingsMythic = new Item.Settings().maxCount(1).rarity(Rarity.RARE);

            imperfectDivinityItem = new ImperfectDivinityItem(settingsEpic);
            completeDivinityItem = new CompleteDivinityItem(settingsEpic);
            creatorDivinityItem = new CreatorDivinityItem(settingsMythic);

            staticImperfectDivinityItem = imperfectDivinityItem;
            staticCompleteDivinityItem = completeDivinityItem;
            staticCreatorDivinityItem = creatorDivinityItem;

            Registry.register(Registries.ITEM, Identifier.of(LoosePrincesTool.MOD_ID, "imperfect_divinity"), imperfectDivinityItem);
            Registry.register(Registries.ITEM, Identifier.of(LoosePrincesTool.MOD_ID, "complete_divinity"), completeDivinityItem);
            Registry.register(Registries.ITEM, Identifier.of(LoosePrincesTool.MOD_ID, "creator_divinity"), creatorDivinityItem);

            LoosePrincesTool.LOGGER.info("神格物品注册成功");
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("注册神格物品失败", e);
        }
    }

    private void registerEffects() {
        try {
            StatusEffect imperfect = new StatusEffect(StatusEffectCategory.BENEFICIAL, 0x7E57C2) {};
            StatusEffect divine = new StatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700) {};
            StatusEffect creator = new StatusEffect(StatusEffectCategory.BENEFICIAL, 0x00E5FF) {};
            StatusEffect silence = new StatusEffect(StatusEffectCategory.NEUTRAL, 0x777777) {};

            StatusEffect impReg = Registry.register(Registries.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "imperfect_divinity"), imperfect);
            StatusEffect divReg = Registry.register(Registries.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "divine_power"), divine);
            StatusEffect creatorReg = Registry.register(Registries.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "creator"), creator);
            StatusEffect silenceReg = Registry.register(Registries.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "divine_silence"), silence);

            this.imperfectDivinityEffect = Registries.STATUS_EFFECT.getEntry(impReg);
            this.divinePowerEffect = Registries.STATUS_EFFECT.getEntry(divReg);
            this.creatorEffect = Registries.STATUS_EFFECT.getEntry(creatorReg);
            this.divineSilenceEffect = Registries.STATUS_EFFECT.getEntry(silenceReg);

            LoosePrincesTool.LOGGER.info("神格状态效果注册成功");
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("注册神格状态效果失败", e);
        }
    }

    public Item getImperfectDivinityItem() {
        return imperfectDivinityItem;
    }

    public Item getCompleteDivinityItem() {
        return completeDivinityItem;
    }

    public Item getCreatorDivinityItem() {
        return creatorDivinityItem;
    }

    public static Item getStaticImperfectDivinityItem() {
        return staticImperfectDivinityItem;
    }

    public static Item getStaticCompleteDivinityItem() {
        return staticCompleteDivinityItem;
    }

    public static Item getStaticCreatorDivinityItem() {
        return staticCreatorDivinityItem;
    }

    public RegistryEntry<StatusEffect> getImperfectDivinityEffect() {
        return imperfectDivinityEffect;
    }

    public RegistryEntry<StatusEffect> getDivinePowerEffect() {
        return divinePowerEffect;
    }

    public RegistryEntry<StatusEffect> getCreatorEffect() {
        return creatorEffect;
    }

    public RegistryEntry<StatusEffect> getDivineSilenceEffect() {
        return divineSilenceEffect;
    }
}


