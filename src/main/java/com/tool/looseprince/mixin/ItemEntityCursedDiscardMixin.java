package com.tool.looseprince.mixin;

import com.tool.looseprince.impl.CursedDiscardService;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 丢弃诅咒的掉落实体行为：瞬间回归与伤害保护。
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityCursedDiscardMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void lpt$returnInstantly(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        CursedDiscardService.onItemEntityTick(self);
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void lpt$preventDestroy(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity) (Object) this;
        boolean blocked = CursedDiscardService.preventDestroy(self, source);
        if (blocked) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}


