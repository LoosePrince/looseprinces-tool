package com.tool.looseprince.registry;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.feature.FlyingRuneFeature;
import com.tool.looseprince.feature.FairDuelFeature;
import com.tool.looseprince.feature.DivinityFeature;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
                            entries.add(FlyingRuneFeature.getStaticFlyingRune());
                            entries.add(FairDuelFeature.getStaticItem());
                            entries.add(DivinityFeature.getStaticImperfectDivinityItem());
                            entries.add(DivinityFeature.getStaticCompleteDivinityItem());
                            
                            // 添加绑定附魔书
                            try {
                                if (context.hasPermissions()) {
                                    entries.add(Items.ENCHANTED_BOOK);
                                }
                            } catch (Exception e) {
                                // 忽略错误
                            }
                            
                            // 添加灵魂绑定附魔书 - 暂时只添加普通附魔书
                            // 在运行时，玩家可以通过指令获得具体的附魔书
                            // 例如：/give @p enchanted_book{StoredEnchantments:[{id:"looseprinces-tool:soul_binding",lvl:1}]}
                        })
                        .build()
        );
    }
} 