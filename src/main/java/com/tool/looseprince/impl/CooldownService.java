package com.tool.looseprince.impl;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用冷却/禁用服务
 * 支持两种模式：
 * 1) 玩家独立：按 (playerUUID, key) 记录，某玩家对某功能处于冷却
 * 2) 物品独立：按 NBT 写入/读取剩余tick，物品自身处于冷却
 */
public final class CooldownService {
    private static final Map<UUID, Map<String, Integer>> playerCooldownTicks = new HashMap<>();

    private CooldownService() {}

    // 玩家独立冷却
    public static void setPlayerCooldown(ServerPlayerEntity player, String key, int ticks) {
        playerCooldownTicks.computeIfAbsent(player.getUuid(), k -> new HashMap<>()).put(key, ticks);
    }

    public static boolean isPlayerCooling(ServerPlayerEntity player, String key) {
        Map<String, Integer> map = playerCooldownTicks.get(player.getUuid());
        if (map == null) return false;
        Integer left = map.get(key);
        return left != null && left > 0;
    }

    public static int getPlayerRemaining(ServerPlayerEntity player, String key) {
        Map<String, Integer> map = playerCooldownTicks.get(player.getUuid());
        if (map == null) return 0;
        Integer left = map.get(key);
        return left == null ? 0 : Math.max(0, left);
    }

    // 物品独立冷却（写入物品 NBT: lptCooldown.{key}=ticks）
    public static void setItemCooldown(ItemStack stack, String key, int ticks) {
        if (stack == null || stack.isEmpty()) return;
        NbtCompound root = getCustomData(stack);
        NbtCompound cd = root.getCompound("lptCooldown");
        cd.putInt(key, Math.max(0, ticks));
        root.put("lptCooldown", cd);
        setCustomData(stack, root);
    }

    public static boolean isItemCooling(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty()) return false;
        NbtCompound root = getCustomData(stack);
        NbtCompound cd = root.getCompound("lptCooldown");
        return cd.getInt(key) > 0;
    }

    public static int getItemRemaining(ItemStack stack, String key) {
        if (stack == null || stack.isEmpty()) return 0;
        NbtCompound root = getCustomData(stack);
        NbtCompound cd = root.getCompound("lptCooldown");
        return Math.max(0, cd.getInt(key));
    }

    private static NbtCompound getCustomData(ItemStack stack) {
        try {
            NbtComponent comp = stack.get(DataComponentTypes.CUSTOM_DATA);
            return comp != null ? comp.copyNbt() : new NbtCompound();
        } catch (Exception e) {
            return new NbtCompound();
        }
    }

    private static void setCustomData(ItemStack stack, NbtCompound nbt) {
        try {
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        } catch (Exception ignored) {}
    }

    // 每 tick 递减
    public static void tick(ServerPlayerEntity player) {
        Map<String, Integer> map = playerCooldownTicks.get(player.getUuid());
        if (map == null || map.isEmpty()) return;
        map.replaceAll((k, v) -> Math.max(0, v - 1));
    }
}


