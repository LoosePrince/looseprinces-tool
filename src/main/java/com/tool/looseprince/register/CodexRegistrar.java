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
            // 神秘典籍
            CodexRegistry.register("self", CodexRegistry.simple(
                    "self",
                    com.tool.looseprince.codex.CodexEntryType.ITEM,
                    () -> new net.minecraft.item.ItemStack(MYSTIC_TOME),
                    "screen.looseprinces-tool.codex.title",
                    "screen.looseprinces-tool.codex.story"
            ));
            // 飞行符文
            CodexRegistry.register("flying_rune", CodexRegistry.simple(
                    "flying_rune",
                    com.tool.looseprince.codex.CodexEntryType.ITEM,
                    () -> new net.minecraft.item.ItemStack(com.tool.looseprince.register.FlyingRuneRegistrar.get()),
                    "item.looseprinces-tool.flying_rune",
                    "item.looseprinces-tool.flying_rune.tooltip.line1",
                    "item.looseprinces-tool.flying_rune.story"
            ));
            // 公平对决（物品）
            CodexRegistry.register("fair_duel", CodexRegistry.simple(
                    "fair_duel",
                    com.tool.looseprince.codex.CodexEntryType.ITEM,
                    () -> new net.minecraft.item.ItemStack(com.tool.looseprince.register.FairDuelRegistrar.getItem()),
                    "item.looseprinces-tool.fair_duel",
                    "item.looseprinces-tool.fair_duel.tooltip.line1",
                    "item.looseprinces-tool.fair_duel.tooltip.line2",
                    "item.looseprinces-tool.fair_duel.story"
            ));
            // 公平对决（效果）
            CodexRegistry.register("fair_duel_effect", CodexRegistry.simple(
                    "fair_duel_effect",
                    com.tool.looseprince.codex.CodexEntryType.POTION,
                    () -> net.minecraft.item.ItemStack.EMPTY,
                    "effect.looseprinces-tool.fair_duel",
                    "effect.looseprinces-tool.fair_duel.desc"
            ));
            // 完整的神格
            CodexRegistry.register("complete_divinity", CodexRegistry.simple(
                    "complete_divinity",
                    com.tool.looseprince.codex.CodexEntryType.ITEM,
                    () -> new net.minecraft.item.ItemStack(com.tool.looseprince.register.DivinityRegistrar.getCompleteItem()),
                    "item.looseprinces-tool.complete_divinity",
                    "item.looseprinces-tool.complete_divinity.detail",
                    "item.looseprinces-tool.complete_divinity.story"
            ));
            // 神的力量
            CodexRegistry.register("divine_power", CodexRegistry.simple(
                    "divine_power",
                    com.tool.looseprince.codex.CodexEntryType.EFFECT,
                    () -> net.minecraft.item.ItemStack.EMPTY,
                    "effect.looseprinces-tool.divine_power",
                    "effect.looseprinces-tool.divine_power.desc",
                    "item.looseprinces-tool.complete_divinity.story"
            ));
            // 残缺的神格
            CodexRegistry.register("imperfect_divinity", CodexRegistry.simple(
                    "imperfect_divinity",
                    com.tool.looseprince.codex.CodexEntryType.ITEM,
                    () -> new net.minecraft.item.ItemStack(com.tool.looseprince.register.DivinityRegistrar.getImperfectItem()),
                    "item.looseprinces-tool.imperfect_divinity",
                    "item.looseprinces-tool.imperfect_divinity.detail",
                    "item.looseprinces-tool.imperfect_divinity.story"
            ));
            // 造物主的神格
            CodexRegistry.register("creator_divinity", CodexRegistry.simple(
                    "creator_divinity",
                    com.tool.looseprince.codex.CodexEntryType.ITEM,
                    () -> new net.minecraft.item.ItemStack(com.tool.looseprince.register.DivinityRegistrar.getCreatorItem()),
                    "item.looseprinces-tool.creator_divinity",
                    "item.looseprinces-tool.creator_divinity.normal.title",
                    "item.looseprinces-tool.creator_divinity.normal.detail",
                    "item.looseprinces-tool.creator_divinity.story.normal"
            ));
            // 造物主
            CodexRegistry.register("creator", CodexRegistry.simple(
                    "creator",
                    com.tool.looseprince.codex.CodexEntryType.EFFECT,
                    () -> net.minecraft.item.ItemStack.EMPTY,
                    "effect.looseprinces-tool.creator",
                    "effect.looseprinces-tool.creator.desc"
            ));
            // 附魔：绑定
            CodexRegistry.register("binding_enchantment", CodexRegistry.simple(
                    "binding_enchantment",
                    com.tool.looseprince.codex.CodexEntryType.ITEM,
                    () -> new net.minecraft.item.ItemStack(MYSTIC_TOME),
                    "enchantment.looseprinces-tool.binding"
            ));
            // 附魔：灵魂绑定
            CodexRegistry.register("soul_binding", CodexRegistry.simple(
                    "soul_binding",
                    com.tool.looseprince.codex.CodexEntryType.ITEM,
                    () -> new net.minecraft.item.ItemStack(MYSTIC_TOME),
                    "enchantment.looseprinces-tool.soul_binding"
            ));
        } catch (Throwable ignored) {}
    }
}


