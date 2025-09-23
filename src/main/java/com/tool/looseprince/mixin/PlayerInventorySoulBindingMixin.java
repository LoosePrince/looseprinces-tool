package com.tool.looseprince.mixin;

import com.tool.looseprince.util.SoulBindingUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 在物品进入玩家背包时写入灵魂绑定拥有者
 */
@Mixin(PlayerInventory.class)
public abstract class PlayerInventorySoulBindingMixin {

    @Shadow public PlayerEntity player;

    @Inject(method = "setStack", at = @At("HEAD"))
    private void lpt$writeOwnerOnSetStack(int slot, ItemStack stack, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
        if (stack == null || stack.isEmpty()) return;
        if (!SoulBindingUtils.hasSoulBinding(stack)) return;
        SoulBindingUtils.ensureOwner(stack, serverPlayer);
    }
}


