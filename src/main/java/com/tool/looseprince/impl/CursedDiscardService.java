package com.tool.looseprince.impl;

import com.tool.looseprince.logic.CursedDiscardLogic;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 丢弃诅咒的具体动作：拦截丢弃/容器放入、丢弃瞬间回归、伤害保护。
 */
public final class CursedDiscardService {
    private CursedDiscardService() {}

    public static boolean shouldPreventOwnerDrop(ServerPlayerEntity player, ItemStack stack) {
        if (!CursedDiscardLogic.isEnabled() || !CursedDiscardLogic.shouldPreventDrop()) return false;
        if (stack == null || stack.isEmpty() || !CursedDiscardLogic.matches(stack)) return false;
        if (!CursedDiscardLogic.hasOwner(stack)) CursedDiscardLogic.ensureOwner(stack, player);
        return CursedDiscardLogic.isOwner(player, stack);
    }

    public static boolean shouldPreventContainerInsert(PlayerEntity player, ItemStack stack, boolean isPlayerInventory) {
        if (!CursedDiscardLogic.isEnabled() || !CursedDiscardLogic.shouldPreventContainerInsert()) return false;
        if (stack == null || stack.isEmpty() || !CursedDiscardLogic.matches(stack)) return false;
        // 玩家自己物品栏不拦截；非玩家容器一律禁止放入
        return !isPlayerInventory;
    }

    public static void onItemEntityTick(ItemEntity self) {
        if (!CursedDiscardLogic.isEnabled() || !CursedDiscardLogic.shouldInstantReturn()) return;
        ItemStack stack = self.getStack();
        if (stack.isEmpty() || !CursedDiscardLogic.matches(stack)) return;

        var server = self.getWorld().getServer();
        if (server == null) return;
        var pm = server.getPlayerManager();
        if (pm == null) return;

        java.util.UUID owner = com.tool.looseprince.util.SoulBindingUtils.getOwnerUuid(stack);
        if (owner == null) return;
        ServerPlayerEntity op = pm.getPlayer(owner);
        if (op == null) return;

        // 丢弃瞬间：尽快回归
        ItemStack copy = stack.copy();
        boolean inserted = op.getInventory().insertStack(copy);
        if (!inserted) {
            // 背包满：替换主手并将原主手丢出
            int sel = op.getInventory().selectedSlot;
            ItemStack main = op.getMainHandStack().copy();
            op.getInventory().setStack(sel, copy);
            if (!main.isEmpty()) {
                op.dropItem(main, false);
            }
        }
        self.discard();
    }

    public static boolean preventDestroy(ItemEntity self, net.minecraft.entity.damage.DamageSource source) {
        if (!CursedDiscardLogic.isEnabled()) return false;
        ItemStack stack = self.getStack();
        if (stack.isEmpty() || !CursedDiscardLogic.matches(stack)) return false;
        // 岩浆/火
        if (CursedDiscardLogic.lavaImmune() && (source.isOf(net.minecraft.entity.damage.DamageTypes.LAVA) || source.isOf(net.minecraft.entity.damage.DamageTypes.IN_FIRE) || source.isOf(net.minecraft.entity.damage.DamageTypes.ON_FIRE))) {
            return true;
        }
        // 虚空：不销毁，抬升
        if (!CursedDiscardLogic.voidDestroyable() && source.isOf(net.minecraft.entity.damage.DamageTypes.OUT_OF_WORLD)) {
            double x = self.getX();
            double z = self.getZ();
            double safeY = Math.max(self.getWorld().getBottomY() + 1, 1);
            self.setPosition(x, safeY, z);
            self.setVelocity(0, 0.4, 0);
            return true;
        }
        // 仙人掌/甜浆果灌木等接触伤害
        if (CursedDiscardLogic.cactusImmune() && (source.isOf(net.minecraft.entity.damage.DamageTypes.CACTUS) || source.isOf(net.minecraft.entity.damage.DamageTypes.SWEET_BERRY_BUSH))) {
            return true;
        }
        return false;
    }
}


