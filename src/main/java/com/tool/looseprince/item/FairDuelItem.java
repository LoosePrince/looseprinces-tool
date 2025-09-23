package com.tool.looseprince.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * 公平对决物品
 */
public class FairDuelItem extends Item {
    public FairDuelItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable("item.looseprinces-tool.fair_duel.tooltip.line1").formatted(Formatting.AQUA));
        tooltip.add(Text.translatable("item.looseprinces-tool.fair_duel.tooltip.line2").formatted(Formatting.GRAY));
        
        // 添加故事文本
        tooltip.add(Text.empty());
        String story = Text.translatable("item.looseprinces-tool.fair_duel.story").getString();
        String[] lines = story.split("\n");
        for (String line : lines) {
            tooltip.add(Text.literal(line).formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
        }
    }
}


