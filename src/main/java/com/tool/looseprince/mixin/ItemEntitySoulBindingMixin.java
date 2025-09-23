package com.tool.looseprince.mixin;

import com.tool.looseprince.feature.FeatureRegistry;
import com.tool.looseprince.feature.SoulBindingFeature;
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
        if (serverPlayer.getAbilities() != null && serverPlayer.getAbilities().creativeMode) return; // 创造模式不拦截
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

    @Inject(method = "tick", at = @At("HEAD"))
    private void lpt$level2Tick(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getStack();
        if (stack.isEmpty() || !SoulBindingUtils.hasSoulBinding(stack)) return;

        // 获取功能配置
        SoulBindingFeature feature = (SoulBindingFeature) FeatureRegistry.getInstance().getFeature("soul_binding");
        if (feature == null || !feature.isEnabled()) return;

        // 仅在等级为2时生效：通过字符串匹配不易准确获取等级，这里使用附魔字符串中统计 soul_binding 出现的 level
        int level = SoulBindingUtils.getSoulBindingLevel(stack);
        if (level < 2) return;

        // 虚空上浮保护
        if (!feature.isVoidDestroyable()) {
            int bottomY = self.getWorld().getBottomY();
            if (self.getY() < bottomY + 1) {
                self.setPosition(self.getX(), bottomY + 1, self.getZ());
                self.setVelocity(self.getVelocity().x, Math.max(self.getVelocity().y, 0.4), self.getVelocity().z);
            }
        }

        // 岩浆中轻微上浮（与伤害拦截配合，避免沉没焚毁）
        if (feature.isLavaImmune() && self.isInLava()) {
            self.setVelocity(self.getVelocity().x, Math.max(self.getVelocity().y, 0.08), self.getVelocity().z);
        }

        // 记录首次掉落 tick
        int recorded = SoulBindingUtils.getDropTick(stack);
        int age = self.getItemAge();
        if (recorded < 0) {
            SoulBindingUtils.markDropTick(stack, age);
            return;
        }

        // 到达传送时间则传送到拥有者
        int waitTicks = feature.getLevel2TeleportSeconds() * 20;
        if (age - recorded >= waitTicks) {
            // 找拥有者并传送到其身边
            if (self.getWorld().getServer() != null && self.getWorld().getServer().getPlayerManager() != null) {
                java.util.UUID owner = SoulBindingUtils.getOwnerUuid(stack);
                if (owner != null) {
                    net.minecraft.server.network.ServerPlayerEntity ownerPlayer = self.getWorld().getServer().getPlayerManager().getPlayer(owner);
                    if (ownerPlayer != null) {
                        // 尝试直接放入背包，否则掉落在玩家脚下
                        if (!ownerPlayer.getInventory().insertStack(stack.copy())) {
                            self.setPosition(ownerPlayer.getX(), ownerPlayer.getY() + 0.5, ownerPlayer.getZ());
                        } else {
                            self.discard();
                        }
                    }
                }
            }
        }
    }
}


