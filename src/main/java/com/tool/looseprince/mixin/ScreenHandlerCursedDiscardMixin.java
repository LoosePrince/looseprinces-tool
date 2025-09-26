package com.tool.looseprince.mixin;

import com.tool.looseprince.logic.CursedDiscardLogic;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 兜底：阻止通过 ScreenHandler 的 shift-点击/快速移动将“丢弃诅咒”物品移入容器槽位。
 */
@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerCursedDiscardMixin {

    @Inject(method = "insertItem", at = @At("HEAD"), cancellable = true)
    private void lpt$preventInsert(ItemStack stack, int startIndex, int endIndex, boolean fromLast, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (stack == null || stack.isEmpty()) return;
            if (CursedDiscardLogic.isEnabled() && CursedDiscardLogic.shouldPreventContainerInsert() && CursedDiscardLogic.matches(stack)) {
                // insertItem 是向一段 Slot 写入（通常为容器槽位区间），直接禁止
                cir.setReturnValue(false);
                cir.cancel();
            }
        } catch (Throwable ignored) {}
    }
}


