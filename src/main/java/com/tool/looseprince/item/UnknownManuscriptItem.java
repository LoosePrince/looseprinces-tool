package com.tool.looseprince.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

/**
 * 未知来源的手稿：仅承载文本/故事，后续可在书内引用
 */
public class UnknownManuscriptItem extends Item {
    public UnknownManuscriptItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, net.minecraft.entity.player.PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient) {
            try {
                // 每次至少解锁手稿主词条，并随机解锁一条谣传
                net.minecraft.util.math.random.Random r = world.getRandom();
                String[] rumors = new String[] { "rumor_1", "rumor_2", "rumor_3", "rumor_4", "rumor_5", "rumor_6", "rumor_7", "rumor_8", "rumor_9", "rumor_10", "rumor_11", "rumor_12", "rumor_13", "rumor_14", "rumor_15", "rumor_16" };
                String rumorId = rumors[r.nextInt(rumors.length)];
                net.minecraft.server.network.ServerPlayerEntity sp = (net.minecraft.server.network.ServerPlayerEntity) user;
                com.tool.looseprince.state.CodexState st = com.tool.looseprince.state.CodexState.get(sp);
                st.unlock("unknown_manuscript");
                st.unlock(rumorId);
                st.save(sp);
                // 消耗1个
                stack.decrement(1);
                user.getItemCooldownManager().set(this, 10);
                user.sendMessage(Text.translatable("message.looseprinces-tool.codex.unlocked", Text.translatable(resolveTitleKey(rumorId))), true);
            } catch (Throwable ignored) {}
        }
        return TypedActionResult.success(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable("item.looseprinces-tool.unknown_manuscript.desc"));
    }

    private String resolveTitleKey(String id) {
        return switch (id) {
            case "rumor_1" -> "codex.looseprinces-tool.rumor.1.title";
            case "rumor_2" -> "codex.looseprinces-tool.rumor.2.title";
            case "rumor_3" -> "codex.looseprinces-tool.rumor.3.title";
            case "rumor_4" -> "codex.looseprinces-tool.rumor.4.title";
            case "rumor_5" -> "codex.looseprinces-tool.rumor.5.title";
            case "rumor_6" -> "codex.looseprinces-tool.rumor.6.title";
            case "rumor_7" -> "codex.looseprinces-tool.rumor.7.title";
            case "rumor_8" -> "codex.looseprinces-tool.rumor.8.title";
            case "rumor_9" -> "codex.looseprinces-tool.rumor.9.title";
            case "rumor_10" -> "codex.looseprinces-tool.rumor.10.title";
            case "rumor_11" -> "codex.looseprinces-tool.rumor.11.title";
            case "rumor_12" -> "codex.looseprinces-tool.rumor.12.title";
            case "rumor_13" -> "codex.looseprinces-tool.rumor.13.title";
            case "rumor_14" -> "codex.looseprinces-tool.rumor.14.title";
            case "rumor_15" -> "codex.looseprinces-tool.rumor.15.title";
            case "rumor_16" -> "codex.looseprinces-tool.rumor.16.title";
            case "unknown_manuscript" -> "item.looseprinces-tool.unknown_manuscript";
            default -> id;
        };
    }
}


