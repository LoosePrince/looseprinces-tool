package com.tool.looseprince.mixin;

import com.tool.looseprince.impl.CursedDiscardService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 禁止丢弃诅咒物品被放入容器（包括 shift+点击移动到容器）。
 */
@Mixin(Slot.class)
public abstract class SlotCursedDiscardInsertMixin {

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    private void lpt$preventCursedInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        try {
            Slot self = (Slot) (Object) this;
            boolean isPlayerInv = self.inventory instanceof PlayerInventory;
            PlayerEntity player = null;
            if (self.inventory instanceof PlayerInventory inv) {
                player = inv.player;
            }
            if (CursedDiscardService.shouldPreventContainerInsert(player, stack, isPlayerInv)) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        } catch (Throwable ignored) {}
    }
}


