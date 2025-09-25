package com.tool.looseprince.registry;

import com.tool.looseprince.LoosePrincesTool;
 
import com.tool.looseprince.register.FlyingRuneRegistrar;
import com.tool.looseprince.register.FairDuelRegistrar;
import com.tool.looseprince.register.DivinityRegistrar;
import com.tool.looseprince.register.CodexRegistrar;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItemGroups {
    public static ItemGroup LOOSEPRINCES_GROUP;
    
    // 模组图标物品
    public static final Item MOD_ICON_ITEM = Registry.register(
        Registries.ITEM,
        Identifier.of(LoosePrincesTool.MOD_ID, "mod_icon"),
        new Item(new Item.Settings().rarity(Rarity.EPIC).maxCount(1))
    );

    public static void register() {
        LOOSEPRINCES_GROUP = Registry.register(
                Registries.ITEM_GROUP,
                Identifier.of(LoosePrincesTool.MOD_ID, "main"),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(MOD_ICON_ITEM))
                        .displayName(Text.translatable("itemGroup.looseprinces-tool.main"))
                        .entries((context, entries) -> {
                            entries.add(FlyingRuneRegistrar.get());
                            entries.add(FairDuelRegistrar.getItem());
                            entries.add(DivinityRegistrar.getImperfectItem());
                            entries.add(DivinityRegistrar.getCompleteItem());
                            try { entries.add(DivinityRegistrar.getCreatorItem()); } catch (Exception ignored) {}
                            try { entries.add(CodexRegistrar.getMysticTome()); } catch (Exception ignored) {}
                            try { entries.add(CodexRegistrar.getUnknownManuscript()); } catch (Exception ignored) {}
                        })
                        .build()
        );
    }
} 