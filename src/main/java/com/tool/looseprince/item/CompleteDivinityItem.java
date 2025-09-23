package com.tool.looseprince.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class CompleteDivinityItem extends Item {
    public CompleteDivinityItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable("item.looseprinces-tool.complete_divinity.tooltip").formatted(Formatting.GOLD));
        tooltip.add(Text.translatable("item.looseprinces-tool.complete_divinity.detail").formatted(Formatting.GRAY));
        
        // 添加故事文本
        tooltip.add(Text.empty());
        String story = Text.translatable("item.looseprinces-tool.complete_divinity.story").getString();
        String[] lines = story.split("\n");
        for (String line : lines) {
            tooltip.add(Text.literal(line).formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}


