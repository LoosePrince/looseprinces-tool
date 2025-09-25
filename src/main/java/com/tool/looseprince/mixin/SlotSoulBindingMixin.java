package com.tool.looseprince.mixin;

import com.tool.looseprince.impl.SoulBindingService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 限制容器内的灵魂绑定物品被非拥有者拿起/移动
 */
@Mixin(Slot.class)
public abstract class SlotSoulBindingMixin {

    @Inject(method = "canTakeItems", at = @At("HEAD"), cancellable = true)
    private void lpt$preventTakeByOthers(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (!(player instanceof ServerPlayerEntity)) return;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        if (serverPlayer.getAbilities() != null && serverPlayer.getAbilities().creativeMode) return; // 创造模式不拦截
        Slot self = (Slot) (Object) this;
        // 允许玩家从自己的物品栏丢弃（不拦截 PlayerInventory）
        if (self.inventory instanceof PlayerInventory) {
            return;
        }
        ItemStack stack = self.getStack();
        if (SoulBindingService.shouldPreventContainerTake(serverPlayer, stack)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}


