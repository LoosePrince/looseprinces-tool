package com.tool.looseprince.mixin;

import com.tool.looseprince.logic.SoulBindingLogic;
import com.tool.looseprince.impl.CooldownService;
import com.tool.looseprince.logic.CooldownKeys;
import com.tool.looseprince.register.FlyingRuneRegistrar;
import com.tool.looseprince.register.FairDuelRegistrar;
import com.tool.looseprince.register.DivinityRegistrar;
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
        if (!SoulBindingLogic.hasSoulBinding(stack)) return;
        SoulBindingLogic.ensureOwner(stack, serverPlayer);

        // 非拥有者持有 → 对玩家独立禁用（冷却极长），并应用可视化遮罩
        boolean hasOwner = SoulBindingLogic.hasOwner(stack);
        boolean isOwner = hasOwner && SoulBindingLogic.isOwner(serverPlayer, stack);
        if (hasOwner && !isOwner) {
            int veryLong = Integer.MAX_VALUE / 4; // 近似永久禁用，后续可通过事件清理
            var item = stack.getItem();
            if (item == FlyingRuneRegistrar.get()) {
                CooldownService.setPlayerCooldown(serverPlayer, CooldownKeys.FLYING_RUNE, veryLong);
                serverPlayer.getItemCooldownManager().set(item, veryLong);
            } else if (item == FairDuelRegistrar.getItem()) {
                CooldownService.setPlayerCooldown(serverPlayer, CooldownKeys.FAIR_DUEL, veryLong);
                serverPlayer.getItemCooldownManager().set(item, veryLong);
            } else if (item == DivinityRegistrar.getImperfectItem()) {
                CooldownService.setPlayerCooldown(serverPlayer, CooldownKeys.DIVINITY_IMPERFECT, veryLong);
                serverPlayer.getItemCooldownManager().set(item, veryLong);
            } else if (item == DivinityRegistrar.getCompleteItem()) {
                CooldownService.setPlayerCooldown(serverPlayer, CooldownKeys.DIVINITY_COMPLETE, veryLong);
                serverPlayer.getItemCooldownManager().set(item, veryLong);
            } else if (item == DivinityRegistrar.getCreatorItem()) {
                CooldownService.setPlayerCooldown(serverPlayer, CooldownKeys.DIVINITY_CREATOR, veryLong);
                serverPlayer.getItemCooldownManager().set(item, veryLong);
            }
        } else if (isOwner) {
            // 拥有者持有 → 清理禁用（设为0）
            var item = stack.getItem();
            if (item == FlyingRuneRegistrar.get()) {
                CooldownService.setPlayerCooldown(serverPlayer, CooldownKeys.FLYING_RUNE, 0);
            } else if (item == FairDuelRegistrar.getItem()) {
                CooldownService.setPlayerCooldown(serverPlayer, CooldownKeys.FAIR_DUEL, 0);
            } else if (item == DivinityRegistrar.getImperfectItem()) {
                CooldownService.setPlayerCooldown(serverPlayer, CooldownKeys.DIVINITY_IMPERFECT, 0);
            } else if (item == DivinityRegistrar.getCompleteItem()) {
                CooldownService.setPlayerCooldown(serverPlayer, CooldownKeys.DIVINITY_COMPLETE, 0);
            } else if (item == DivinityRegistrar.getCreatorItem()) {
                CooldownService.setPlayerCooldown(serverPlayer, CooldownKeys.DIVINITY_CREATOR, 0);
            }
        }
    }
}


