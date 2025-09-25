package com.tool.looseprince.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * 神秘典籍：右键打开客户端界面
 */
public class MysticTomeItem extends Item {
    public MysticTomeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            // 使用反射避免在主源集直接引用客户端类
            try {
                Class<?> c = Class.forName("com.tool.looseprince.client.screens.CodexScreens");
                java.lang.reflect.Method m = c.getMethod("openMysticTome");
                m.invoke(null);
            } catch (Throwable ignored) {}
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.looseprinces-tool.mystic_tome.desc"));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true; // 自带灵魂绑定效果的可视化
    }

    // 不直接写入附魔组件，逻辑层恒视为灵魂绑定II，且强制发光

    @Override
    @Environment(EnvType.CLIENT)
    public Text getName(ItemStack stack) {
        try {
            // 反射获取客户端实例与玩家，避免直接 import 客户端类
            Class<?> mcClazz = Class.forName("net.minecraft.client.MinecraftClient");
            java.lang.reflect.Method getInstance = mcClazz.getMethod("getInstance");
            Object mc = getInstance.invoke(null);
            if (mc == null) return super.getName(stack);
            java.lang.reflect.Field playerField = mcClazz.getField("player");
            Object player = playerField.get(mc);
            if (player == null) return super.getName(stack);

            com.tool.looseprince.feature.DivinityFeature div = (com.tool.looseprince.feature.DivinityFeature) com.tool.looseprince.feature.FeatureRegistry.getInstance().getFeature("divinity");
            if (div == null) return super.getName(stack);

            java.lang.reflect.Method hasStatusEffect = player.getClass().getMethod("hasStatusEffect", net.minecraft.registry.entry.RegistryEntry.class);
            boolean hasDivine = false;
            if (div.getDivinePowerEffect() != null) {
                hasDivine = (boolean) hasStatusEffect.invoke(player, div.getDivinePowerEffect());
            }
            if (!hasDivine && div.getCreatorEffect() != null) {
                hasDivine = (boolean) hasStatusEffect.invoke(player, div.getCreatorEffect());
            }
            if (hasDivine) {
                return Text.translatable("item.looseprinces-tool.mystic_tome.divine_name");
            }
        } catch (Throwable ignored) {}
        return super.getName(stack);
    }
}


