package com.tool.looseprince.impl;

import com.tool.looseprince.LoosePrincesTool;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.BiPredicate;

/**
 * 通用“死亡不掉落并复原”服务。
 * 不直接依赖具体附魔，通过注册匹配器以决定哪些物品应被保留。
 */
public final class KeepOnDeathService {
    private KeepOnDeathService() {}

    private static final List<Predicate<ItemStack>> matchers = new ArrayList<>();
    private static final Map<String, Map<Integer, ItemStack>> saved = new HashMap<>();

    public static void registerMatcher(Predicate<ItemStack> matcher) {
        if (matcher != null) matchers.add(matcher);
    }

    private static boolean matchesAny(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        for (Predicate<ItemStack> m : matchers) {
            try {
                if (m.test(stack)) return true;
            } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * 在掉落前保存并清空应保留的物品。
     */
    public static void saveOnDeath(ServerPlayerEntity player, PlayerInventory inventory) {
        if (player == null || inventory == null) return;
        String name = player.getName().getString();
        Map<Integer, ItemStack> keep = new HashMap<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack != null && !stack.isEmpty() && matchesAny(stack)) {
                keep.put(i, stack.copy());
                inventory.setStack(i, ItemStack.EMPTY);
            }
        }
        if (!keep.isEmpty()) {
            saved.put(name, keep);
            LoosePrincesTool.LOGGER.info("[KeepOnDeath] saved {} stacks for {}", keep.size(), name);
        }
    }

    /**
     * 在掉落前保存并清空应保留的物品（带玩家上下文过滤）。
     */
    public static void saveOnDeath(ServerPlayerEntity player, PlayerInventory inventory, BiPredicate<ServerPlayerEntity, ItemStack> filter) {
        if (player == null || inventory == null) return;
        String name = player.getName().getString();
        Map<Integer, ItemStack> keep = new HashMap<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            boolean keepThis = false;
            try {
                keepThis = stack != null && !stack.isEmpty() && filter != null && filter.test(player, stack);
            } catch (Throwable ignored) {}
            if (keepThis) {
                keep.put(i, stack.copy());
                inventory.setStack(i, ItemStack.EMPTY);
            }
        }
        if (!keep.isEmpty()) {
            saved.put(name, keep);
            LoosePrincesTool.LOGGER.info("[KeepOnDeath] saved {} stacks for {} (filtered)", keep.size(), name);
        }
    }

    /**
     * 复活后尝试放回。
     */
    public static void restoreOnRespawn(ServerPlayerEntity player) {
        if (player == null) return;
        String name = player.getName().getString();
        Map<Integer, ItemStack> keep = saved.remove(name);
        if (keep == null || keep.isEmpty()) return;
        for (Map.Entry<Integer, ItemStack> e : keep.entrySet()) {
            int slot = e.getKey();
            ItemStack stack = e.getValue();
            if (stack == null || stack.isEmpty()) continue;
            try {
                if (slot >= 0 && slot < player.getInventory().size()) {
                    ItemStack cur = player.getInventory().getStack(slot);
                    if (cur.isEmpty()) {
                        player.getInventory().setStack(slot, stack);
                    } else if (!player.getInventory().insertStack(stack)) {
                        player.dropItem(stack, false);
                    }
                } else if (!player.getInventory().insertStack(stack)) {
                    player.dropItem(stack, false);
                }
            } catch (Exception ex) {
                player.dropItem(stack, false);
            }
        }
    }
}


