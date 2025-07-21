package com.tool.looseprince.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * 飞行符文物品类
 * 提供飞行能力的神奇符文
 */
public class FlyingRuneItem extends Item {
    
    public FlyingRuneItem(Settings settings) {
        super(settings);
    }
    
    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        
        // 添加物品描述
        tooltip.add(Text.translatable("item.looseprinces-tool.flying_rune.tooltip.line1")
                .formatted(Formatting.AQUA));
        tooltip.add(Text.translatable("item.looseprinces-tool.flying_rune.tooltip.line2")
                .formatted(Formatting.GRAY));
        tooltip.add(Text.empty());
        tooltip.add(Text.translatable("item.looseprinces-tool.flying_rune.tooltip.usage")
                .formatted(Formatting.YELLOW));
    }
    
    @Override
    public boolean hasGlint(ItemStack stack) {
        // 让飞行符文始终有附魔光效
        return true;
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        // 飞行符文不能被附魔
        return false;
    }
}