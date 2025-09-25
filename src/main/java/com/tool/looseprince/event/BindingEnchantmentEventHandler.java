package com.tool.looseprince.event;

import com.tool.looseprince.feature.BindingEnchantmentFeature;
import com.tool.looseprince.LoosePrincesTool;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import com.tool.looseprince.impl.BindingService;
import com.tool.looseprince.logic.BindingLogic;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

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
        if (!BindingLogic.isEnabled() || !BindingLogic.shouldPreventDrop()) {
            return;
        }
        BindingService.restoreOnRespawn(newPlayer);
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
        return BindingLogic.matches(stack);
    }
    
    /**
     * 获取绑定附魔功能实例
     * @return 绑定附魔功能实例
     */
    public BindingEnchantmentFeature getFeature() {
        return feature;
    }
} 