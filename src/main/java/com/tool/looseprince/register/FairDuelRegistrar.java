package com.tool.looseprince.register;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.item.FairDuelItem;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

/**
 * 注册层：公平对决的物品与状态效果注册
 */
public final class FairDuelRegistrar {
    private static Item FAIR_DUEL_ITEM;
    private static RegistryEntry<StatusEffect> FAIR_DUEL_EFFECT;

    private FairDuelRegistrar() {}

    public static void register() {
        registerItem();
        registerEffect();
    }

    private static void registerItem() {
        if (FAIR_DUEL_ITEM != null) {
            return;
        }
        try {
            Item.Settings settings = new Item.Settings()
                .maxCount(1)
                .rarity(Rarity.EPIC);
            FAIR_DUEL_ITEM = new FairDuelItem(settings);
            Registry.register(Registries.ITEM, Identifier.of(LoosePrincesTool.MOD_ID, "fair_duel"), FAIR_DUEL_ITEM);
            LoosePrincesTool.LOGGER.debug("[register] 公平对决物品注册成功");
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[register] 公平对决物品注册失败", e);
        }
    }

    private static void registerEffect() {
        if (FAIR_DUEL_EFFECT != null) {
            return;
        }
        try {
            StatusEffect effect = new StatusEffect(StatusEffectCategory.BENEFICIAL, 0xF6C453) {};
            StatusEffect registered = Registry.register(Registries.STATUS_EFFECT, Identifier.of(LoosePrincesTool.MOD_ID, "fair_duel"), effect);
            FAIR_DUEL_EFFECT = Registries.STATUS_EFFECT.getEntry(registered);
            LoosePrincesTool.LOGGER.debug("[register] 公平对决状态效果注册成功");
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[register] 公平对决状态效果注册失败", e);
        }
    }

    public static Item getItem() {
        return FAIR_DUEL_ITEM;
    }

    public static RegistryEntry<StatusEffect> getEffect() {
        return FAIR_DUEL_EFFECT;
    }
}


