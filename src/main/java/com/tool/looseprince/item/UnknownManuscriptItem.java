package com.tool.looseprince.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

/**
 * 未知来源的手稿：仅承载文本/故事，后续可在书内引用
 */
public class UnknownManuscriptItem extends Item {
    public UnknownManuscriptItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.looseprinces-tool.unknown_manuscript.desc"));
    }
}


