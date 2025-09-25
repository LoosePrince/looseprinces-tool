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
        tooltip.add(Text.empty());
        boolean showStory = isReadOnClient("divine_power") || isReadOnClient("complete_divinity");
        if (!showStory) {
            tooltip.add(Text.translatable("tooltip.looseprinces-tool.story.unread").formatted(Formatting.DARK_PURPLE, Formatting.ITALIC));
            return;
        }
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
    private static boolean isReadOnClient(String id) {
        try {
            Class<?> mcClazz = Class.forName("net.minecraft.client.MinecraftClient");
            Object mc = mcClazz.getMethod("getInstance").invoke(null);
            if (mc == null) return false;
            Object player = mcClazz.getField("player").get(mc);
            Object server = mcClazz.getMethod("getServer").invoke(mc);
            if (player == null || server == null) return false;
            java.util.UUID uuid = (java.util.UUID) player.getClass().getMethod("getUuid").invoke(player);
            Object spm = server.getClass().getMethod("getPlayerManager").invoke(server);
            Object sp = spm.getClass().getMethod("getPlayer", java.util.UUID.class).invoke(spm, uuid);
            if (sp == null) return false;
            com.tool.looseprince.state.CodexState st = com.tool.looseprince.state.CodexState.get((net.minecraft.server.network.ServerPlayerEntity) sp);
            return st.isRead(id);
        } catch (Throwable ignored) { return false; }
    }
}


