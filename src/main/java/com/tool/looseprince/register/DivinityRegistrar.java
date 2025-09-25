package com.tool.looseprince.register;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.item.CompleteDivinityItem;
import com.tool.looseprince.item.CreatorDivinityItem;
import com.tool.looseprince.item.ImperfectDivinityItem;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

/**
 * 注册层：神格系统的物品与状态效果注册
 */
public final class DivinityRegistrar {
    private static Item IMPERFECT_ITEM;
    private static Item COMPLETE_ITEM;
    private static Item CREATOR_ITEM;

    private static RegistryEntry<StatusEffect> IMPERFECT_EFFECT;
    private static RegistryEntry<StatusEffect> DIVINE_POWER_EFFECT;
    private static RegistryEntry<StatusEffect> CREATOR_EFFECT;
    private static RegistryEntry<StatusEffect> DIVINE_SILENCE_EFFECT;

    private DivinityRegistrar() {}

    public static void register() {
        registerItems();
        registerEffects();
    }

    private static void registerItems() {
        if (IMPERFECT_ITEM != null) return;
        try {
            Item.Settings epic = new Item.Settings().maxCount(1).rarity(Rarity.EPIC);
            Item.Settings myth = new Item.Settings().maxCount(1).rarity(Rarity.RARE);
            IMPERFECT_ITEM = new ImperfectDivinityItem(epic);
            COMPLETE_ITEM = new CompleteDivinityItem(epic);
            CREATOR_ITEM = new CreatorDivinityItem(myth);

            Registry.register(Registries.ITEM, Identifier.of(LoosePrincesTool.MOD_ID, "imperfect_divinity"), IMPERFECT_ITEM);
            Registry.register(Registries.ITEM, Identifier.of(LoosePrincesTool.MOD_ID, "complete_divinity"), COMPLETE_ITEM);
            Registry.register(Registries.ITEM, Identifier.of(LoosePrincesTool.MOD_ID, "creator_divinity"), CREATOR_ITEM);

            LoosePrincesTool.LOGGER.info("[register] 神格物品注册完成");
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[register] 神格物品注册失败", e);
        }
    }

    private static void registerEffects() {
        if (IMPERFECT_EFFECT != null) return;
        try {
            StatusEffect imperfect = new StatusEffect(StatusEffectCategory.BENEFICIAL, 0x7E57C2) {};
            StatusEffect divine = new StatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700) {};
            StatusEffect creator = new StatusEffect(StatusEffectCategory.BENEFICIAL, 0x00E5FF) {};
            StatusEffect silence = new StatusEffect(StatusEffectCategory.NEUTRAL, 0x777777) {};

            StatusEffect impReg = Registry.register(Registries.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "imperfect_divinity"), imperfect);
            StatusEffect divReg = Registry.register(Registries.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "divine_power"), divine);
            StatusEffect creatorReg = Registry.register(Registries.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "creator"), creator);
            StatusEffect silenceReg = Registry.register(Registries.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "divine_silence"), silence);

            IMPERFECT_EFFECT = Registries.STATUS_EFFECT.getEntry(impReg);
            DIVINE_POWER_EFFECT = Registries.STATUS_EFFECT.getEntry(divReg);
            CREATOR_EFFECT = Registries.STATUS_EFFECT.getEntry(creatorReg);
            DIVINE_SILENCE_EFFECT = Registries.STATUS_EFFECT.getEntry(silenceReg);

            LoosePrincesTool.LOGGER.info("[register] 神格状态效果注册完成");
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[register] 神格状态效果注册失败", e);
        }
    }

    public static Item getImperfectItem() { return IMPERFECT_ITEM; }
    public static Item getCompleteItem() { return COMPLETE_ITEM; }
    public static Item getCreatorItem() { return CREATOR_ITEM; }

    public static RegistryEntry<StatusEffect> getImperfectEffect() { return IMPERFECT_EFFECT; }
    public static RegistryEntry<StatusEffect> getDivinePowerEffect() { return DIVINE_POWER_EFFECT; }
    public static RegistryEntry<StatusEffect> getCreatorEffect() { return CREATOR_EFFECT; }
    public static RegistryEntry<StatusEffect> getDivineSilenceEffect() { return DIVINE_SILENCE_EFFECT; }
}


