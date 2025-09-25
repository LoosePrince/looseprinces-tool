package com.tool.looseprince.event;

import com.tool.looseprince.impl.CooldownService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 每tick递减玩家独立冷却
 */
public final class CooldownTickHandler {
    private CooldownTickHandler() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                CooldownService.tick(p);
            }
        });
    }
}


