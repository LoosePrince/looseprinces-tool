package com.tool.looseprince.logic;

/**
 * 逻辑层：飞行判定结果
 */
public final class FlightDecision {
    private final boolean allowFlight;
    private final boolean preventFallDamage;

    public FlightDecision(boolean allowFlight, boolean preventFallDamage) {
        this.allowFlight = allowFlight;
        this.preventFallDamage = preventFallDamage;
    }

    public boolean shouldAllowFlight() {
        return allowFlight;
    }

    public boolean shouldPreventFallDamage() {
        return preventFallDamage;
    }
}


