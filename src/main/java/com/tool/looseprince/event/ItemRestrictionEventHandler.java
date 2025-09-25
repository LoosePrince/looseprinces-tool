package com.tool.looseprince.event;

import com.tool.looseprince.feature.DivinityFeature;
import com.tool.looseprince.feature.FeatureRegistry;
import com.tool.looseprince.register.DivinityRegistrar;
import com.tool.looseprince.register.FairDuelRegistrar;
import com.tool.looseprince.register.FlyingRuneRegistrar;
import com.tool.looseprince.util.SoulBindingUtils;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

/**
 * 物品禁用/冷却下拦截左/右键使用
 * - 冷却：拦截任意正在冷却的物品
 * - 禁用：神力静默期间拦截本模组关键物品；灵魂绑定非拥有者拦截
 */
public final class ItemRestrictionEventHandler {
    private ItemRestrictionEventHandler() {}

    public static void register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) ->
                isBlockedFor(player, player.getStackInHand(hand)) ? ActionResult.FAIL : ActionResult.PASS);

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
                isBlockedFor(player, player.getStackInHand(hand)) ? ActionResult.FAIL : ActionResult.PASS);

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (isBlockedFor(player, player.getStackInHand(hand))) {
                return net.minecraft.util.TypedActionResult.fail(player.getStackInHand(hand));
            }
            return net.minecraft.util.TypedActionResult.pass(player.getStackInHand(hand));
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) ->
                isBlockedFor(player, player.getStackInHand(hand)) ? ActionResult.FAIL : ActionResult.PASS);
    }

    public static boolean isBlockedFor(PlayerEntity player, ItemStack stack) {
        try {
            if (player == null) return false;
            if (stack == null || stack.isEmpty()) return false;

            // 冷却中的任意物品
            if (player.getItemCooldownManager().isCoolingDown(stack.getItem())) {
                return true;
            }

            // 灵魂绑定：非拥有者禁用
            try {
                if (SoulBindingUtils.hasSoulBinding(stack) && player instanceof net.minecraft.server.network.ServerPlayerEntity sp) {
                    if (!SoulBindingUtils.isOwner(sp, stack)) return true;
                }
            } catch (Throwable ignored) {}

            // 神力静默：禁用关键物品
            try {
                DivinityFeature div = (DivinityFeature) FeatureRegistry.getInstance().getFeature("divinity");
                if (div != null && div.getDivineSilenceEffect() != null && player.hasStatusEffect(div.getDivineSilenceEffect())) {
                    Item it = stack.getItem();
                    if (isOurRestrictedItem(it)) return true;
                }
            } catch (Throwable ignored) {}

        } catch (Exception ignored) {}
        return false;
    }

    private static boolean isOurRestrictedItem(Item it) {
        try {
            if (it == FlyingRuneRegistrar.get()) return true;
            if (it == FairDuelRegistrar.getItem()) return true;
            if (it == DivinityRegistrar.getImperfectItem()) return true;
            if (it == DivinityRegistrar.getCompleteItem()) return true;
            try { if (it == DivinityRegistrar.getCreatorItem()) return true; } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
        return false;
    }
}


