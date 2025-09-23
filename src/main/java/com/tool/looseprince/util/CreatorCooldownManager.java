package com.tool.looseprince.util;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 造物主冷却管理：基于服务器世界tick时间记录每名玩家的冷却结束tick
 */
public class CreatorCooldownManager {
    private static final CreatorCooldownManager INSTANCE = new CreatorCooldownManager();

    private final Map<UUID, Long> playerCooldownEndTick = new HashMap<>();
    private final Map<UUID, Long> playerLastCooldownEndTick = new HashMap<>();

    public static CreatorCooldownManager getInstance() {
        return INSTANCE;
    }

    public boolean isCoolingDown(UUID playerId, long currentTick) {
        Long end = playerCooldownEndTick.get(playerId);
        return end != null && currentTick < end;
    }

    public long getRemainingTicks(UUID playerId, long currentTick) {
        Long end = playerCooldownEndTick.get(playerId);
        if (end == null) return 0L;
        return Math.max(0L, end - currentTick);
    }

    public void startCooldown(UUID playerId, long currentTick, long durationTicks) {
        long end = currentTick + Math.max(0, durationTicks);
        playerCooldownEndTick.put(playerId, end);
    }

    public void endCooldown(UUID playerId, long currentTick) {
        playerCooldownEndTick.remove(playerId);
        playerLastCooldownEndTick.put(playerId, currentTick);
    }

    /** 最近一次冷却结束至今是否超过指定tick（例如5秒=100tick） */
    public boolean hasBeenOutOfCooldownLongerThan(UUID playerId, long currentTick, long thresholdTicks) {
        Long lastEnd = playerLastCooldownEndTick.get(playerId);
        if (lastEnd == null) return true; // 从未冷却，视为超过
        return (currentTick - lastEnd) > thresholdTicks;
    }
}


