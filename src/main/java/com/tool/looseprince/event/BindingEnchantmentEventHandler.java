package com.tool.looseprince.event;

import com.tool.looseprince.feature.BindingEnchantmentFeature;
import com.tool.looseprince.LoosePrincesTool;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

import java.util.HashMap;
import java.util.Map;

/**
 * 绑定附魔事件处理器
 * 处理玩家死亡时的物品掉落逻辑
 */
public class BindingEnchantmentEventHandler {
    private final BindingEnchantmentFeature feature;
    // 存储玩家死亡前的绑定附魔物品及其位置信息
    private static final Map<String, Map<Integer, ItemStack>> savedBindingItems = new HashMap<>();
    
    public BindingEnchantmentEventHandler(BindingEnchantmentFeature feature) {
        this.feature = feature;
    }
    
    /**
     * 注册事件监听器
     */
    public void registerEvents() {
        // 注册玩家复活事件
        ServerPlayerEvents.AFTER_RESPAWN.register(this::onPlayerRespawn);
        
        LoosePrincesTool.LOGGER.info("绑定附魔事件监听器已注册");
    }
    
    /**
     * 玩家复活后恢复绑定附魔物品到原始位置
     */
    private void onPlayerRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        if (!feature.isEnabled() || !feature.shouldPreventDrop()) {
            return;
        }
        
        String playerName = newPlayer.getName().getString();
        Map<Integer, ItemStack> savedItems = savedBindingItems.remove(playerName);
        
        if (savedItems != null && !savedItems.isEmpty()) {
            LoosePrincesTool.LOGGER.info("恢复玩家 {} 的绑定附魔物品", playerName);
            
            for (Map.Entry<Integer, ItemStack> entry : savedItems.entrySet()) {
                int originalSlot = entry.getKey();
                ItemStack stack = entry.getValue();
                
                if (!stack.isEmpty()) {
                    // 尝试将物品放回原始位置
                    try {
                        if (originalSlot >= 0 && originalSlot < newPlayer.getInventory().size()) {
                            ItemStack currentItem = newPlayer.getInventory().getStack(originalSlot);
                            if (currentItem.isEmpty()) {
                                // 原始位置为空，直接放回
                                newPlayer.getInventory().setStack(originalSlot, stack);
                                LoosePrincesTool.LOGGER.debug("恢复物品到原始位置 {}: {}", originalSlot, stack.getName().getString());
                            } else {
                                // 原始位置被占用，尝试放入背包其他位置
                                if (!newPlayer.getInventory().insertStack(stack)) {
                                    // 背包满了，掉落在玩家位置
                                    newPlayer.dropItem(stack, false);
                                    LoosePrincesTool.LOGGER.debug("背包满，掉落物品: {}", stack.getName().getString());
                                } else {
                                    LoosePrincesTool.LOGGER.debug("恢复物品到其他位置: {}", stack.getName().getString());
                                }
                            }
                        } else {
                            // 位置无效，直接尝试插入背包
                            if (!newPlayer.getInventory().insertStack(stack)) {
                                newPlayer.dropItem(stack, false);
                            }
                        }
                    } catch (Exception e) {
                        // 发生错误时，直接掉落物品
                        newPlayer.dropItem(stack, false);
                        LoosePrincesTool.LOGGER.warn("恢复物品时发生错误，掉落物品: {}", e.getMessage());
                    }
                }
            }
        }
    }
    
    /**
     * 保存具有绑定附魔的物品及其位置信息
     * 这个方法会被Mixin调用
     */
    public static void saveBindingItems(ServerPlayerEntity player, Map<Integer, ItemStack> itemsWithSlots) {
        String playerName = player.getName().getString();
        savedBindingItems.put(playerName, itemsWithSlots);
        LoosePrincesTool.LOGGER.info("已保存玩家 {} 的绑定附魔物品: {} 个", playerName, itemsWithSlots.size());
    }
    
    /**
     * 检查物品是否具有绑定附魔
     */
    public static boolean hasBindingEnchantment(ItemStack stack) {
        try {
            String enchantmentsString = EnchantmentHelper.getEnchantments(stack).toString();
            return enchantmentsString.contains("binding") || enchantmentsString.contains("looseprinces-tool");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取绑定附魔功能实例
     * @return 绑定附魔功能实例
     */
    public BindingEnchantmentFeature getFeature() {
        return feature;
    }
} 