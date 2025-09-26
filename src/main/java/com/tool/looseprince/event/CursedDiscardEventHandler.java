package com.tool.looseprince.event;

import com.tool.looseprince.impl.KeepOnDeathService;
import com.tool.looseprince.logic.CursedDiscardLogic;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 复用死亡不掉落：在死亡与复活事件中挂接通用服务。
 */
public final class CursedDiscardEventHandler {
    private CursedDiscardEventHandler() {}

    public static void register() {
        // 复活后回填
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (!CursedDiscardLogic.isEnabled() || !CursedDiscardLogic.shouldKeepOnDeath()) return;
            KeepOnDeathService.restoreOnRespawn(newPlayer);
        });
    }
}


