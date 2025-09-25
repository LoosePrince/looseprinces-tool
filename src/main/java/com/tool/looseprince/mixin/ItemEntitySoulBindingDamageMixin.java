package com.tool.looseprince.mixin;

import com.tool.looseprince.impl.SoulBindingService;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
 
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 岩浆/虚空销毁保护（等级2启用，可配置）
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntitySoulBindingDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void lpt$preventDestroy(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity) (Object) this;
        boolean blocked = SoulBindingService.preventDestroy(self, source);
        if (blocked) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}


