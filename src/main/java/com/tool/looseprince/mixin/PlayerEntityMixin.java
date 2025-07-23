package com.tool.looseprince.mixin;

import com.tool.looseprince.event.BindingEnchantmentEventHandler;
import com.tool.looseprince.feature.BindingEnchantmentFeature;
import com.tool.looseprince.feature.FeatureRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * 玩家实体Mixin
 * 用于处理绑定附魔的物品掉落逻辑
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    
    @Shadow public abstract PlayerInventory getInventory();
    
    /**
     * 在玩家死亡掉落物品之前，保存具有绑定附魔的物品
     */
    @Inject(method = "dropInventory", at = @At("HEAD"))
    private void onDropInventory(CallbackInfo ci) {
        // 确保这是服务端玩家
        if (!(((Object) this) instanceof ServerPlayerEntity player)) {
            return;
        }
        
        try {
            // 获取绑定附魔功能实例
            BindingEnchantmentFeature feature = (BindingEnchantmentFeature) FeatureRegistry.getInstance()
                .getFeature("binding_enchantment");
            
            if (feature == null || !feature.isEnabled() || !feature.shouldPreventDrop()) {
                return;
            }
            
            PlayerInventory inventory = getInventory();
            Map<Integer, ItemStack> bindingItemsWithSlots = new HashMap<>();
            
            // 遍历所有物品，找出具有绑定附魔的物品
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                
                if (!stack.isEmpty() && BindingEnchantmentEventHandler.hasBindingEnchantment(stack)) {
                    // 保存物品及其位置
                    bindingItemsWithSlots.put(i, stack.copy());
                    // 从库存中移除，防止掉落
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }
            
            // 如果找到绑定附魔物品，保存它们及其位置
            if (!bindingItemsWithSlots.isEmpty()) {
                BindingEnchantmentEventHandler.saveBindingItems(player, bindingItemsWithSlots);
            }
            
        } catch (Exception e) {
            // 如果处理过程中出现错误，记录日志但不影响正常游戏流程
            System.err.println("处理绑定附魔物品时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 