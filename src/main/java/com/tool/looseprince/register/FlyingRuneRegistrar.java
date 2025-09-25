package com.tool.looseprince.register;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.item.FlyingRuneItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

/**
 * 注册层：负责飞行符文物品的注册与对外暴露
 */
public final class FlyingRuneRegistrar {
    private static Item FLYING_RUNE;

    private FlyingRuneRegistrar() {}

    public static void register() {
        if (FLYING_RUNE != null) {
            return;
        }
        try {
            Item.Settings settings = new Item.Settings()
                .maxCount(1)
                .rarity(Rarity.EPIC);

            FLYING_RUNE = new FlyingRuneItem(settings);

            Registry.register(
                Registries.ITEM,
                Identifier.of(LoosePrincesTool.MOD_ID, "flying_rune"),
                FLYING_RUNE
            );

            LoosePrincesTool.LOGGER.info("飞行符文物品注册成功[register层]");
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[register] 注册飞行符文失败", e);
        }
    }

    public static Item get() {
        return FLYING_RUNE;
    }
}


