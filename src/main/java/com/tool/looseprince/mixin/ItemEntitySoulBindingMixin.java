package com.tool.looseprince.mixin;

import com.tool.looseprince.util.SoulBindingUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getStack();
        if (stack.isEmpty()) return;
        if (!SoulBindingUtils.hasSoulBinding(stack)) return;

        // 如果未记录拥有者，首次被任意玩家接触时写入拥有者
        if (!SoulBindingUtils.hasOwner(stack)) {
            SoulBindingUtils.ensureOwner(stack, serverPlayer);
            return; // 允许拥有者本次继续拾取
        }

        // 已有拥有者且不是该玩家，则阻止拾取
        if (!SoulBindingUtils.isOwner(serverPlayer, stack)) {
            ci.cancel();
        }
    }
}


