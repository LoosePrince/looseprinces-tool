package com.tool.looseprince.event;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.feature.FlyingRuneFeature;
import com.tool.looseprince.item.FlyingRuneItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * 飞行符文事件处理器
 * 处理玩家飞行能力的检测和应用
 */
public class FlyingRuneEventHandler {
    private final FlyingRuneFeature feature;
    
    public FlyingRuneEventHandler(FlyingRuneFeature feature) {
        this.feature = feature;
    }
    
    /**
     * 注册事件监听器
     */
    public void registerEvents() {
        // 注册服务器tick事件，每tick检查玩家的飞行状态
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // 遍历所有在线玩家
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                updatePlayerFlightAbility(player);
            }
        });
        
        LoosePrincesTool.LOGGER.info("Flying Rune event listeners registered");
    }
    
    /**
     * 更新玩家的飞行能力
     * @param player 玩家实体
     */
    private void updatePlayerFlightAbility(ServerPlayerEntity player) {
        try {
            // 检查功能是否启用
            if (!feature.isEnabled()) {
                return;
            }
            
            // 检查玩家是否在创造模式或观察者模式
            if (player.isCreative() || player.isSpectator()) {
                return; // 创造模式和观察者模式本身就有飞行能力，不需要处理
            }
            
            // 检查当前维度是否允许使用飞行符文
            String dimensionId = player.getWorld().getRegistryKey().getValue().toString();
            if (!feature.isAllowedInDimension(dimensionId)) {
                // 如果当前维度不允许使用，移除飞行能力
                if (player.getAbilities().allowFlying) {
                    player.getAbilities().allowFlying = false;
                    player.getAbilities().flying = false;
                    player.sendAbilitiesUpdate();
                }
                return;
            }
            
            // 检查背包中是否有飞行符文
            boolean hasFlyingRune = hasRuneInInventory(player);
            
            // 更新飞行能力
            boolean shouldAllowFlying = hasFlyingRune;
            
            if (player.getAbilities().allowFlying != shouldAllowFlying) {
                player.getAbilities().allowFlying = shouldAllowFlying;
                
                // 如果不再允许飞行且玩家正在飞行，停止飞行
                if (!shouldAllowFlying && player.getAbilities().flying) {
                    player.getAbilities().flying = false;
                    
                    // 如果配置了防止摔落伤害，给玩家一个缓慢下降的效果
                    if (feature.shouldPreventFallDamage()) {
                        // 重置摔落距离以防止摔落伤害
                        player.fallDistance = 0.0f;
                    }
                }
                
                // 同步能力到客户端
                player.sendAbilitiesUpdate();
            }
            
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("Error updating flight ability for player {}", player.getName().getString(), e);
        }
    }
    
    /**
     * 检查玩家背包中是否有飞行符文
     * @param player 玩家实体
     * @return 如果有飞行符文返回true，否则返回false
     */
    private boolean hasRuneInInventory(PlayerEntity player) {
        if (feature.getFlyingRune() == null) {
            return false;
        }
        
        // 检查背包中的所有物品
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof FlyingRuneItem) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 检查物品是否为飞行符文
     * @param stack 物品堆叠
     * @return 如果是飞行符文返回true，否则返回false
     */
    private boolean isFlyingRune(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == feature.getFlyingRune();
    }
}