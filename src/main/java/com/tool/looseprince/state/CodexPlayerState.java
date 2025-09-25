package com.tool.looseprince.state;

import net.minecraft.nbt.NbtCompound;

import java.util.HashSet;
import java.util.Set;

public class CodexPlayerState {
    public boolean givenOnce;
    public final Set<String> unlocked = new HashSet<>();

    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean("given_once", givenOnce);
        if (!unlocked.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String s : unlocked) { if (sb.length() > 0) sb.append(','); sb.append(s); }
            nbt.putString("entries", sb.toString());
        }
        return nbt;
    }

    public static CodexPlayerState fromNbt(NbtCompound nbt) {
        CodexPlayerState st = new CodexPlayerState();
        st.givenOnce = nbt.getBoolean("given_once");
        if (nbt.contains("entries")) {
            String csv = nbt.getString("entries");
            if (csv != null && !csv.isEmpty()) {
                for (String s : csv.split(",")) if (!s.isEmpty()) st.unlocked.add(s);
            }
        }
        return st;
    }
}


