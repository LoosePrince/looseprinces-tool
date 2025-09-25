package com.tool.looseprince.impl;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.logic.BindingLogic;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.collection.DefaultedList;

import java.util.HashMap;
import java.util.Map;

/**
 * 实现层：绑定附魔的死亡掉落保护与复原
 */
public final class BindingService {
    private static final Map<String, Map<Integer, ItemStack>> saved = new HashMap<>();

    private BindingService() {}

    public static void saveOnDeath(ServerPlayerEntity player, DefaultedList<ItemStack> inventorySnapshot) {
        if (!BindingLogic.isEnabled() || !BindingLogic.shouldPreventDrop()) return;
        String name = player.getName().getString();
        Map<Integer, ItemStack> keep = new HashMap<>();
        for (int i = 0; i < inventorySnapshot.size(); i++) {
            ItemStack s = inventorySnapshot.get(i);
            if (s != null && !s.isEmpty() && BindingLogic.matches(s)) {
                keep.put(i, s.copy());
            }
        }
        if (!keep.isEmpty()) {
            saved.put(name, keep);
            LoosePrincesTool.LOGGER.info("[Binding] saved {} stacks for {}", keep.size(), name);
        }
    }

    public static void restoreOnRespawn(ServerPlayerEntity player) {
        if (!BindingLogic.isEnabled() || !BindingLogic.shouldPreventDrop()) return;
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

    public static void saveOnDeath(ServerPlayerEntity player, PlayerInventory inventory) {
        if (!BindingLogic.isEnabled() || !BindingLogic.shouldPreventDrop()) return;
        String name = player.getName().getString();
        Map<Integer, ItemStack> keep = new HashMap<>();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack != null && !stack.isEmpty() && BindingLogic.matches(stack)) {
                keep.put(i, stack.copy());
                inventory.setStack(i, ItemStack.EMPTY);
            }
        }
        if (!keep.isEmpty()) {
            saved.put(name, keep);
            LoosePrincesTool.LOGGER.info("[Binding] saved {} stacks for {}", keep.size(), name);
        }
    }
}


