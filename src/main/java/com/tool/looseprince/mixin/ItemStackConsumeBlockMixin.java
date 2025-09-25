package com.tool.looseprince.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 某些抛投/消耗类物品会在 use/onItemRightClick 之外消耗，这里在 ItemStack.useOnBlock 前拦截，避免消耗
 */
@Mixin(ItemStack.class)
public abstract class ItemStackConsumeBlockMixin {
    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void lpt$preventConsumeOnBlock(net.minecraft.item.ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        try {
            PlayerEntity player = context.getPlayer();
            if (player == null) return;
            if (com.tool.looseprince.event.ItemRestrictionEventHandler.isBlockedFor(player, context.getStack())) {
                cir.setReturnValue(ActionResult.FAIL);
            }
        } catch (Throwable ignored) {}
    }
}


