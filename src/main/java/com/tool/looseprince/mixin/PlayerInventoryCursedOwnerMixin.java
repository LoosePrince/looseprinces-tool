package com.tool.looseprince.mixin;

import com.tool.looseprince.logic.CursedDiscardLogic;
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
 * 当丢弃诅咒物品进入玩家背包时，补写拥有者信息。
 */
@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryCursedOwnerMixin {

    @Shadow public PlayerEntity player;

    @Inject(method = "setStack", at = @At("HEAD"))
    private void lpt$writeOwnerOnSetStack(int slot, ItemStack stack, CallbackInfo ci) {
        if (!(player instanceof ServerPlayerEntity sp)) return;
        if (stack == null || stack.isEmpty()) return;
        if (!CursedDiscardLogic.isEnabled() || !CursedDiscardLogic.matches(stack)) return;
        CursedDiscardLogic.ensureOwner(stack, sp);
    }
}


