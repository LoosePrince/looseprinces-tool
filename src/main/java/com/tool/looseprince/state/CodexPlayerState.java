package com.tool.looseprince.state;

import net.minecraft.nbt.NbtCompound;

import java.util.HashSet;
import java.util.Set;

public class CodexPlayerState {
    public boolean givenOnce;
    public boolean ancientCityFirstLootGiven;
    public final Set<String> unlocked = new HashSet<>();
    public final Set<String> read = new HashSet<>();

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("given_once", givenOnce);
        nbt.putBoolean("ancient_city_first_loot_given", ancientCityFirstLootGiven);
        if (!unlocked.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String s : unlocked) { if (sb.length() > 0) sb.append(','); sb.append(s); }
            nbt.putString("entries", sb.toString());
        }
        if (!read.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String s : read) { if (sb.length() > 0) sb.append(','); sb.append(s); }
            nbt.putString("read", sb.toString());
        }
        return nbt;
    }

    public static CodexPlayerState fromNbt(NbtCompound nbt) {
        CodexPlayerState st = new CodexPlayerState();
        st.givenOnce = nbt.getBoolean("given_once");
        st.ancientCityFirstLootGiven = nbt.getBoolean("ancient_city_first_loot_given");
        if (nbt.contains("entries")) {
            String csv = nbt.getString("entries");
            if (csv != null && !csv.isEmpty()) {
                for (String s : csv.split(",")) if (!s.isEmpty()) st.unlocked.add(s);
            }
        }
        if (nbt.contains("read")) {
            String csv = nbt.getString("read");
            if (csv != null && !csv.isEmpty()) {
                for (String s : csv.split(",")) if (!s.isEmpty()) st.read.add(s);
            }
        }
        return st;
    }
}


