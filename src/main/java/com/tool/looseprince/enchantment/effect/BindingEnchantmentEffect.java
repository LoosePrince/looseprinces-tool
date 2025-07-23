package com.tool.looseprince.enchantment.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;

/**
 * 绑定附魔效果
 * 防止物品在死亡时掉落的附魔效果
 * 实际的逻辑通过事件监听器处理
 */
public record BindingEnchantmentEffect(EnchantmentLevelBasedValue amount) {
    
    public static final MapCodec<BindingEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            EnchantmentLevelBasedValue.CODEC.fieldOf("amount").forGetter(BindingEnchantmentEffect::amount)
        ).apply(instance, BindingEnchantmentEffect::new)
    );
} 