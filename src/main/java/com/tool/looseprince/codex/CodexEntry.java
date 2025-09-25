package com.tool.looseprince.codex;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Supplier;

public class CodexEntry {
    private final String id;
    private final CodexEntryType type; // 可选类型，用于目录标识
    private final Supplier<ItemStack> iconSupplier; // 可选图标
    private final Supplier<Text> titleSupplier;
    private final Supplier<List<String>> contentSupplier;

    public CodexEntry(String id, CodexEntryType type, Supplier<ItemStack> iconSupplier, Supplier<Text> titleSupplier, Supplier<List<String>> contentSupplier) {
        this.id = id;
        this.type = type;
        this.iconSupplier = iconSupplier;
        this.titleSupplier = titleSupplier;
        this.contentSupplier = contentSupplier;
    }

    public String getId() { return id; }
    public CodexEntryType getType() { return type; }
    public ItemStack getIcon() { return iconSupplier != null ? iconSupplier.get() : ItemStack.EMPTY; }
    public Text getTitle() { return titleSupplier != null ? titleSupplier.get() : Text.literal(id); }
    public List<String> getContentLines() { return contentSupplier != null ? contentSupplier.get() : java.util.Collections.emptyList(); }
    public List<String> getContentLinesWithContext(java.util.function.Function<String, String> formatter) {
        List<String> base = getContentLines();
        if (formatter == null || base.isEmpty()) return base;
        java.util.ArrayList<String> out = new java.util.ArrayList<>(base.size());
        for (String s : base) out.add(formatter.apply(s));
        return out;
    }
}


