package com.tool.looseprince.event;

import com.tool.looseprince.register.CodexRegistrar;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * 将未知来源的手稿注入古城战利品箱：
 * - 第一次开启古城战利品箱必出（基于玩家状态标记）
 * - 之后为 1.5% 概率
 */
public final class LootInjectionHandler {
    private LootInjectionHandler() {}

    private static final RegistryKey<LootTable> ANCIENT_CITY_LOOT = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of("minecraft", "chests/ancient_city"));

    public static void register() {
        // 1.5% 常驻掉落注入
        LootTableEvents.MODIFY.register((key, tableBuilder, source, lookup) -> {
            if (!ANCIENT_CITY_LOOT.equals(key)) return;
            try {
                LootPool.Builder pool = LootPool.builder()
                        .rolls(ConstantLootNumberProvider.create(1))
                        .with(ItemEntry.builder(CodexRegistrar.getUnknownManuscript()))
                        .conditionally(RandomChanceLootCondition.builder(0.015f));
                tableBuilder.pool(pool);
            } catch (Throwable ignored) {}
        });

        // 首次开启：在交互时补发
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            try {
                if (world.isClient) return net.minecraft.util.ActionResult.PASS;
                var pos = hitResult.getBlockPos();
                if (pos == null) return net.minecraft.util.ActionResult.PASS;
                var be = world.getBlockEntity(pos);
                if (!(be instanceof net.minecraft.block.entity.LootableContainerBlockEntity lb)) return net.minecraft.util.ActionResult.PASS;
                RegistryKey<LootTable> lootKey = lb.getLootTable();
                if (lootKey == null || !ANCIENT_CITY_LOOT.equals(lootKey)) return net.minecraft.util.ActionResult.PASS;
                if (!(player instanceof net.minecraft.server.network.ServerPlayerEntity sp)) return net.minecraft.util.ActionResult.PASS;
                var st = com.tool.looseprince.state.CodexState.get(sp);
                if (!st.isAncientCityFirstLootGiven()) {
                    net.minecraft.item.ItemStack stack = new net.minecraft.item.ItemStack(CodexRegistrar.getUnknownManuscript());
                    // 尝试放入容器：优先叠加到相同物品，其次放入空槽
                    if (be instanceof net.minecraft.inventory.Inventory inv) {
                        int size = inv.size();
                        // 先叠加
                        for (int i = 0; i < size && stack.getCount() > 0; i++) {
                            net.minecraft.item.ItemStack cur = inv.getStack(i);
                            if (!cur.isEmpty() && cur.getItem() == stack.getItem() && cur.getCount() < cur.getMaxCount()) {
                                int can = Math.min(cur.getMaxCount() - cur.getCount(), stack.getCount());
                                if (can > 0) {
                                    cur.increment(can);
                                    inv.setStack(i, cur);
                                    stack.decrement(can);
                                }
                            }
                        }
                        // 再找空位
                        for (int i = 0; i < size && stack.getCount() > 0; i++) {
                            if (inv.getStack(i).isEmpty()) {
                                inv.setStack(i, stack.copy());
                                stack.decrement(stack.getCount());
                            }
                        }
                    }
                    st.setAncientCityFirstLootGiven(true);
                    st.save(sp);
                }
            } catch (Throwable ignored) {}
            return net.minecraft.util.ActionResult.PASS;
        });
    }
}


