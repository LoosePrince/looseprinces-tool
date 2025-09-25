package com.tool.looseprince.event;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.logic.FlightDecision;
import com.tool.looseprince.logic.FlightLogic;
import com.tool.looseprince.impl.FlightAbilityService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 飞行符文事件处理器
 * 处理玩家飞行能力的检测和应用
 */
public class FlyingRuneEventHandler {
    public FlyingRuneEventHandler() {}
    
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
            // 使用逻辑层进行判定
            FlightDecision decision = FlightLogic.evaluate(player);
            // 使用实现层应用效果
            FlightAbilityService.apply(player, decision);
            
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("Error updating flight ability for player {}", player.getName().getString(), e);
        }
    }
    
    
}