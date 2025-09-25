package com.tool.looseprince.codex;

import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Supplier;

public class CodexEntry {
    private final String id;
    private final Supplier<ItemStack> iconSupplier;
    private final Supplier<Text> titleSupplier;
    private final Supplier<List<String>> contentSupplier;

    public CodexEntry(String id, Supplier<ItemStack> iconSupplier, Supplier<Text> titleSupplier, Supplier<List<String>> contentSupplier) {
        this.id = id;
        this.iconSupplier = iconSupplier;
        this.titleSupplier = titleSupplier;
        this.contentSupplier = contentSupplier;
    }

    public String getId() { return id; }
    public ItemStack getIcon() { return iconSupplier != null ? iconSupplier.get() : ItemStack.EMPTY; }
    public Text getTitle() { return titleSupplier != null ? titleSupplier.get() : Text.literal(id); }
    public List<String> getContentLines() { return contentSupplier != null ? contentSupplier.get() : java.util.Collections.emptyList(); }
}


