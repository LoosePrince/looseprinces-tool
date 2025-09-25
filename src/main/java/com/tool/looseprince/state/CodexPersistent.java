package com.tool.looseprince.state;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.registry.RegistryWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 全局持久化：按玩家UUID存储 CodexPlayerState 的 NBT
 */
public class CodexPersistent extends PersistentState {
    public static final String NAME = "looseprinces_tool_codex";

    private final Map<String, NbtCompound> data = new HashMap<>();

    public static CodexPersistent get(MinecraftServer server) {
        PersistentStateManager mgr = server.getOverworld().getPersistentStateManager();
        return mgr.getOrCreate(new PersistentState.Type<>(CodexPersistent::new, CodexPersistent::fromNbt, null), NAME);
    }

    public CodexPersistent() {}

    public static CodexPersistent fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        CodexPersistent ps = new CodexPersistent();
        if (nbt != null && nbt.contains("players")) {
            NbtCompound players = nbt.getCompound("players");
            for (String k : players.getKeys()) {
                ps.data.put(k, players.getCompound(k));
            }
        }
        return ps;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound players = new NbtCompound();
        for (Map.Entry<String, NbtCompound> e : data.entrySet()) {
            players.put(e.getKey(), e.getValue());
        }
        nbt.put("players", players);
        return nbt;
    }

    public CodexPlayerState get(UUID uuid) {
        String key = uuid.toString();
        NbtCompound tag = data.getOrDefault(key, new NbtCompound());
        return CodexPlayerState.fromNbt(tag);
    }

    public void put(UUID uuid, CodexPlayerState st) {
        data.put(uuid.toString(), st.toNbt());
        markDirty();
    }
}


