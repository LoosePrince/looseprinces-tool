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
                Item.Settings manu = new Item.Settings().maxCount(16).rarity(Rarity.COMMON);
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
                    "item.looseprinces-tool.fair_duel.usage",
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
            // 未知来源的手稿（源条目）
            CodexRegistry.register("unknown_manuscript", CodexRegistry.simple(
                    "unknown_manuscript",
                    com.tool.looseprince.codex.CodexEntryType.DOCUMENT,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "item.looseprinces-tool.unknown_manuscript",
                    "item.looseprinces-tool.unknown_manuscript.story"
            ));
            // 谣传词条
            CodexRegistry.register("rumor_1", CodexRegistry.simple(
                    "rumor_1",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.1.title",
                    "codex.looseprinces-tool.rumor.1"
            ));
            CodexRegistry.register("rumor_2", CodexRegistry.simple(
                    "rumor_2",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.2.title",
                    "codex.looseprinces-tool.rumor.2"
            ));
            // 更多谣传词条
            CodexRegistry.register("rumor_3", CodexRegistry.simple(
                    "rumor_3",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.3.title",
                    "codex.looseprinces-tool.rumor.3"
            ));
            CodexRegistry.register("rumor_4", CodexRegistry.simple(
                    "rumor_4",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.4.title",
                    "codex.looseprinces-tool.rumor.4"
            ));
            CodexRegistry.register("rumor_5", CodexRegistry.simple(
                    "rumor_5",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.5.title",
                    "codex.looseprinces-tool.rumor.5"
            ));
            CodexRegistry.register("rumor_6", CodexRegistry.simple(
                    "rumor_6",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.6.title",
                    "codex.looseprinces-tool.rumor.6"
            ));
            CodexRegistry.register("rumor_7", CodexRegistry.simple(
                    "rumor_7",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.7.title",
                    "codex.looseprinces-tool.rumor.7"
            ));
            CodexRegistry.register("rumor_8", CodexRegistry.simple(
                    "rumor_8",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.8.title",
                    "codex.looseprinces-tool.rumor.8"
            ));
            CodexRegistry.register("rumor_9", CodexRegistry.simple(
                    "rumor_9",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.9.title",
                    "codex.looseprinces-tool.rumor.9"
            ));
            CodexRegistry.register("rumor_10", CodexRegistry.simple(
                    "rumor_10",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.10.title",
                    "codex.looseprinces-tool.rumor.10"
            ));
            CodexRegistry.register("rumor_11", CodexRegistry.simple(
                    "rumor_11",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.11.title",
                    "codex.looseprinces-tool.rumor.11"
            ));
            CodexRegistry.register("rumor_12", CodexRegistry.simple(
                    "rumor_12",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.12.title",
                    "codex.looseprinces-tool.rumor.12"
            ));
            CodexRegistry.register("rumor_13", CodexRegistry.simple(
                    "rumor_13",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.13.title",
                    "codex.looseprinces-tool.rumor.13"
            ));
            CodexRegistry.register("rumor_14", CodexRegistry.simple(
                    "rumor_14",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.14.title",
                    "codex.looseprinces-tool.rumor.14"
            ));
            CodexRegistry.register("rumor_15", CodexRegistry.simple(
                    "rumor_15",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.15.title",
                    "codex.looseprinces-tool.rumor.15"
            ));
            CodexRegistry.register("rumor_16", CodexRegistry.simple(
                    "rumor_16",
                    com.tool.looseprince.codex.CodexEntryType.RUMOR,
                    () -> new net.minecraft.item.ItemStack(UNKNOWN_MANUSCRIPT),
                    "codex.looseprinces-tool.rumor.16.title",
                    "codex.looseprinces-tool.rumor.16"
            ));
            // 造物主的神格
            CodexRegistry.register("creator_divinity", CodexRegistry.simple(
                    "creator_divinity",
                    com.tool.looseprince.codex.CodexEntryType.ITEM,
                    () -> new net.minecraft.item.ItemStack(com.tool.looseprince.register.DivinityRegistrar.getCreatorItem()),
                    "item.looseprinces-tool.creator_divinity",
                    "item.looseprinces-tool.creator_divinity.normal.title",
                    "item.looseprinces-tool.creator_divinity.normal.detail",
                    "item.looseprinces-tool.creator_divinity.usage.extra",
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


