package com.tool.looseprince.registry;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.feature.FlyingRuneFeature;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static ItemGroup LOOSEPRINCES_GROUP;

    public static void register() {
        LOOSEPRINCES_GROUP = Registry.register(
                Registries.ITEM_GROUP,
                Identifier.of(LoosePrincesTool.MOD_ID, "main"),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(FlyingRuneFeature.getStaticFlyingRune()))
                        .displayName(Text.translatable("itemGroup.looseprinces-tool.main"))
                        .entries((context, entries) -> {
                            entries.add(FlyingRuneFeature.getStaticFlyingRune());
                        })
                        .build()
        );
    }
} 