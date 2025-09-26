package com.tool.looseprince.state;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 通过 DataComponent CUSTOM_DATA 持久化玩家 Codex 状态
 */
public final class CodexState {

    public boolean isGivenOnce() { return getInternal().givenOnce; }
    public void setGivenOnce(boolean v) { getInternal().givenOnce = v; }
    public boolean isAncientCityFirstLootGiven() { return getInternal().ancientCityFirstLootGiven; }
    public void setAncientCityFirstLootGiven(boolean v) { getInternal().ancientCityFirstLootGiven = v; }

    public java.util.Set<String> getUnlockedEntries() { return getInternal().unlocked; }
    public void unlock(String id) { if (id != null && !id.isEmpty()) getInternal().unlocked.add(id); }
    public boolean isUnlocked(String id) { return id != null && getInternal().unlocked.contains(id); }
    public boolean isRead(String id) { return id != null && getInternal().read.contains(id); }
    public void markRead(String id) { if (id != null && !id.isEmpty()) getInternal().read.add(id); }

    private CodexPlayerState delegate;
    private CodexPlayerState getInternal() {
        if (delegate == null) delegate = new CodexPlayerState();
        return delegate;
    }

    public static CodexState get(ServerPlayerEntity player) {
        CodexState st = new CodexState();
        try {
            CodexPersistent ps = CodexPersistent.get(player.getServer());
            st.delegate = ps.get(player.getUuid());
        } catch (Throwable ignored) { st.delegate = new CodexPlayerState(); }
        return st;
    }

    public void save(ServerPlayerEntity player) {
        try {
            CodexPersistent ps = CodexPersistent.get(player.getServer());
            ps.put(player.getUuid(), getInternal());
        } catch (Throwable ignored) {}
    }
}


