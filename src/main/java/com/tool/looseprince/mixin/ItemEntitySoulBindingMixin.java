package com.tool.looseprince.mixin;

import com.tool.looseprince.impl.SoulBindingService;
import com.tool.looseprince.logic.SoulBindingLogic;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 限制掉落实体被非拥有者拾取
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntitySoulBindingMixin {

    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    private void lpt$preventPickupByOthers(PlayerEntity player, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        if (serverPlayer.getAbilities() != null && serverPlayer.getAbilities().creativeMode) return; // 创造模式不拦截
        ItemEntity self = (ItemEntity) (Object) this;
        if (SoulBindingService.shouldPreventPickup(serverPlayer, self)) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void lpt$level2Tick(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        if (!SoulBindingLogic.isEnabled()) return;
        SoulBindingService.applyLevel2Tick(self);
    }
}


