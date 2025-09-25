package com.tool.looseprince.codex;

import com.tool.looseprince.LoosePrincesTool;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CodexRegistry {
    private static final Map<String, CodexEntry> ENTRIES = new LinkedHashMap<>();

    private CodexRegistry() {}

    public static void register(String id, CodexEntry entry) {
        if (id == null || id.isEmpty() || entry == null) return;
        ENTRIES.put(id, entry);
        LoosePrincesTool.LOGGER.debug("[codex] register entry {}", id);
    }

    public static CodexEntry get(String id) { return ENTRIES.get(id); }
    public static Map<String, CodexEntry> all() { return Collections.unmodifiableMap(ENTRIES); }

    public static CodexEntry simple(String id, CodexEntryType type, java.util.function.Supplier<ItemStack> icon, String titleKey, String... contentKeys) {
        return new CodexEntry(
                id,
                type,
                icon,
                () -> Text.translatable(titleKey),
                () -> {
                    java.util.ArrayList<String> list = new java.util.ArrayList<>();
                    for (String k : contentKeys) {
                        String text = Text.translatable(k).getString();
                        for (String ln : text.split("\n")) list.add(ln);
                    }
                    return list;
                }
        );
    }
}


