package com.tool.looseprince.register;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.item.MysticTomeItem;
import com.tool.looseprince.item.UnknownManuscriptItem;
import com.tool.looseprince.codex.CodexRegistry;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

/**
 * 注册层：神秘典籍与未知来源的手稿
 */
public final class CodexRegistrar {
    private static Item MYSTIC_TOME;
    private static Item UNKNOWN_MANUSCRIPT;

    private CodexRegistrar() {}

    public static void register() {
        if (MYSTIC_TOME == null) {
            try {
                Item.Settings tome = new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON);
                Item.Settings manu = new Item.Settings().maxCount(1).rarity(Rarity.COMMON);
                MYSTIC_TOME = new MysticTomeItem(tome);
                UNKNOWN_MANUSCRIPT = new UnknownManuscriptItem(manu);
                Registry.register(Registries.ITEM, Identifier.of(LoosePrincesTool.MOD_ID, "mystic_tome"), MYSTIC_TOME);
                Registry.register(Registries.ITEM, Identifier.of(LoosePrincesTool.MOD_ID, "unknown_manuscript"), UNKNOWN_MANUSCRIPT);
                LoosePrincesTool.LOGGER.info("[register] 神秘典籍/未知来源的手稿 注册完成");

                // 注册词条
                registerEntries();
            } catch (Exception e) {
                LoosePrincesTool.LOGGER.error("[register] 注册 神秘典籍/未知来源的手稿 失败", e);
            }
        }
    }

    public static Item getMysticTome() { return MYSTIC_TOME; }
    public static Item getUnknownManuscript() { return UNKNOWN_MANUSCRIPT; }

    private static void registerEntries() {
        try {
            CodexRegistry.register("self", CodexRegistry.simple(
                    "self",
                    () -> new net.minecraft.item.ItemStack(MYSTIC_TOME),
                    "screen.looseprinces-tool.codex.title",
                    "screen.looseprinces-tool.codex.story"
            ));
            CodexRegistry.register("flying_rune", CodexRegistry.simple(
                    "flying_rune",
                    () -> new net.minecraft.item.ItemStack(com.tool.looseprince.register.FlyingRuneRegistrar.get()),
                    "item.looseprinces-tool.flying_rune",
                    "item.looseprinces-tool.flying_rune.tooltip.line1",
                    "item.looseprinces-tool.flying_rune.story"
            ));
            CodexRegistry.register("complete_divinity", CodexRegistry.simple(
                    "complete_divinity",
                    () -> new net.minecraft.item.ItemStack(com.tool.looseprince.register.DivinityRegistrar.getCompleteItem()),
                    "item.looseprinces-tool.complete_divinity",
                    "item.looseprinces-tool.complete_divinity.detail",
                    "item.looseprinces-tool.complete_divinity.story"
            ));
            CodexRegistry.register("divine_power", CodexRegistry.simple(
                    "divine_power",
                    () -> new net.minecraft.item.ItemStack(com.tool.looseprince.register.DivinityRegistrar.getCompleteItem()),
                    "effect.looseprinces-tool.divine_power",
                    "effect.looseprinces-tool.divine_power.desc",
                    "item.looseprinces-tool.complete_divinity.story"
            ));
        } catch (Throwable ignored) {}
    }
}


