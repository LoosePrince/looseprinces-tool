package com.tool.looseprince.impl;

import com.tool.looseprince.logic.SoulBindingLogic;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 实现层：灵魂绑定的具体动作（拦截拾取/容器取走、虚空/岩浆保护、回归传送）
 */
public final class SoulBindingService {
    private SoulBindingService() {}

    public static boolean shouldPreventPickup(ServerPlayerEntity player, ItemEntity itemEntity) {
        if (!SoulBindingLogic.isEnabled() || !SoulBindingLogic.preventPickup()) return false;
        ItemStack stack = itemEntity.getStack();
        if (stack.isEmpty() || !SoulBindingLogic.hasSoulBinding(stack)) return false;
        if (!SoulBindingLogic.hasOwner(stack)) {
            SoulBindingLogic.ensureOwner(stack, player);
            return false; // 首次接触写入拥有者后允许拾取
        }
        return !SoulBindingLogic.isOwner(player, stack);
    }

    public static boolean shouldPreventContainerTake(ServerPlayerEntity player, ItemStack stack) {
        if (!SoulBindingLogic.isEnabled() || !SoulBindingLogic.preventContainerTake()) return false;
        if (stack == null || stack.isEmpty() || !SoulBindingLogic.hasSoulBinding(stack)) return false;
        if (!SoulBindingLogic.hasOwner(stack)) {
            SoulBindingLogic.ensureOwner(stack, player);
            return false;
        }
        return !SoulBindingLogic.isOwner(player, stack);
    }

    public static void applyLevel2Tick(ItemEntity self) {
        ItemStack stack = self.getStack();
        if (stack.isEmpty() || !SoulBindingLogic.hasSoulBinding(stack)) return;
        int level = SoulBindingLogic.getSoulBindingLevel(stack);
        if (level < 2) return;
        // 虚空上浮保护
        if (!SoulBindingLogic.voidDestroyable()) {
            int bottomY = self.getWorld().getBottomY();
            if (self.getY() < bottomY + 1) {
                self.setPosition(self.getX(), bottomY + 1, self.getZ());
                self.setVelocity(self.getVelocity().x, Math.max(self.getVelocity().y, 0.4), self.getVelocity().z);
            }
        }
        // 岩浆中轻微上浮
        if (SoulBindingLogic.lavaImmune() && self.isInLava()) {
            self.setVelocity(self.getVelocity().x, Math.max(self.getVelocity().y, 0.08), self.getVelocity().z);
        }
        // 回归
        int recorded = SoulBindingLogic.getDropTick(stack);
        int age = self.getItemAge();
        if (recorded < 0) {
            SoulBindingLogic.markDropTick(stack, age);
            return;
        }
        int waitTicks = SoulBindingLogic.level2TeleportSeconds() * 20;
        if (age - recorded >= waitTicks) {
            var server = self.getWorld().getServer();
            if (server != null) {
                var pm = server.getPlayerManager();
                if (pm != null) {
                    java.util.UUID owner = SoulBindingLogic.getOwnerUuid(stack);
                    if (owner != null) {
                        ServerPlayerEntity ownerPlayer = pm.getPlayer(owner);
                        if (ownerPlayer != null) {
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

    public static boolean preventDestroy(ItemEntity self, net.minecraft.entity.damage.DamageSource source) {
        ItemStack stack = self.getStack();
        if (stack.isEmpty() || !SoulBindingLogic.hasSoulBinding(stack)) return false;
        int level = SoulBindingLogic.getSoulBindingLevel(stack);
        if (level < 2) return false;
        // 岩浆免疫
        if (SoulBindingLogic.lavaImmune() && (source.isOf(net.minecraft.entity.damage.DamageTypes.LAVA) || source.isOf(net.minecraft.entity.damage.DamageTypes.IN_FIRE) || source.isOf(net.minecraft.entity.damage.DamageTypes.ON_FIRE))) {
            return true;
        }
        // 虚空保护：不销毁，改为上浮
        if (!SoulBindingLogic.voidDestroyable() && source.isOf(net.minecraft.entity.damage.DamageTypes.OUT_OF_WORLD)) {
            double x = self.getX();
            double z = self.getZ();
            double safeY = Math.max(self.getWorld().getBottomY() + 1, 1);
            self.setPosition(x, safeY, z);
            self.setVelocity(0, 0.4, 0);
            return true;
        }
        return false;
    }
}


