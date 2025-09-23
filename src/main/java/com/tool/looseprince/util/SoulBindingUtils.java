package com.tool.looseprince.util;

import com.tool.looseprince.LoosePrincesTool;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.registry.Registries;

import java.util.UUID;

/**
 * 灵魂绑定工具
 * 负责判定物品是否具有灵魂绑定、写入/读取拥有者 NBT、以及生成展示文本
 */
public final class SoulBindingUtils {
    private SoulBindingUtils() {}

    // NBT key 常量
    public static final String NBT_OWNER_UUID = "lpt_soul_owner_uuid";
    public static final String NBT_OWNER_NAME = "lpt_soul_owner_name";
    public static final String NBT_DROP_TICK = "lpt_soul_drop_tick";

    // 本模组的灵魂绑定附魔 id（用于字符串匹配兜底）
    public static final String SOUL_BINDING_ID = Identifier.of(LoosePrincesTool.MOD_ID, "soul_binding").toString();

    /**
     * 物品是否具有"灵魂绑定"附魔
     * 说明：为了兼容不同 Yarn/Fabric 版本的 EnchantmentHelper 返回结构，这里使用字符串匹配兜底。
     */
    public static boolean hasSoulBinding(ItemStack stack) {
        return getSoulBindingLevel(stack) > 0;
    }

    /**
     * 获取灵魂绑定附魔等级（优先使用官方API）
     */
    public static int getSoulBindingLevel(ItemStack stack) {
        try {
            ItemEnchantmentsComponent comp = EnchantmentHelper.getEnchantments(stack);
            java.util.Set<RegistryEntry<Enchantment>> set = comp.getEnchantments();
            for (RegistryEntry<Enchantment> e : set) {
                if (e.matchesKey(com.tool.looseprince.feature.SoulBindingFeature.SOUL_BINDING)) {
                    int v = comp.getLevel(e);
                    return v > 0 ? v : 1;
                }
            }
        } catch (Throwable ignored) {}
        // 兜底：未能解析到则视为未附魔
        return 0;
    }

    /**
     * 如果物品具有灵魂绑定且尚未记录所有者，则写入玩家 UUID/Name
     */
    public static void ensureOwner(ItemStack stack, ServerPlayerEntity player) {
        if (stack == null || stack.isEmpty() || player == null) return;
        if (!hasSoulBinding(stack)) return;
        NbtCompound nbt = getCustomData(stack);
        if (!nbt.containsUuid(NBT_OWNER_UUID)) {
            nbt.putUuid(NBT_OWNER_UUID, player.getUuid());
            nbt.putString(NBT_OWNER_NAME, player.getName().getString());
            setCustomData(stack, nbt);
        }
    }

    /**
     * 判断是否为拥有者
     */
    public static boolean isOwner(ServerPlayerEntity player, ItemStack stack) {
        if (player == null || stack == null || stack.isEmpty()) return false;
        if (!hasSoulBinding(stack)) return false;
        NbtCompound nbt = getCustomData(stack);
        if (nbt == null || !nbt.containsUuid(NBT_OWNER_UUID)) return false;
        try {
            UUID owner = nbt.getUuid(NBT_OWNER_UUID);
            return owner.equals(player.getUuid());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasOwner(ItemStack stack) {
        NbtCompound nbt = getCustomData(stack);
        return nbt != null && nbt.containsUuid(NBT_OWNER_UUID);
    }

    public static UUID getOwnerUuid(ItemStack stack) {
        NbtCompound nbt = getCustomData(stack);
        if (nbt != null && nbt.containsUuid(NBT_OWNER_UUID)) {
            return nbt.getUuid(NBT_OWNER_UUID);
        }
        return null;
    }

    public static String getOwnerName(ItemStack stack) {
        NbtCompound nbt = getCustomData(stack);
        if (nbt != null && nbt.contains(NBT_OWNER_NAME)) {
            return nbt.getString(NBT_OWNER_NAME);
        }
        return null;
    }

    /**
     * 生成 Tooltip 文本，如：已绑定至: 玩家名(uuid)
     */
    public static Text getBoundTooltip(ItemStack stack) {
        if (!hasSoulBinding(stack) || !hasOwner(stack)) return null;
        String name = getOwnerName(stack);
        UUID uuid = getOwnerUuid(stack);
        String shown = name != null ? name : (uuid != null ? uuid.toString() : "?");
        String uuidPart = uuid != null ? "(" + uuid + ")" : "";
        return Text.translatable("tooltip.looseprinces-tool.bound_to", shown, uuidPart).formatted(Formatting.GRAY);
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
        } catch (Exception ignored) {
        }
    }

    public static void markDropTick(ItemStack stack, int tick) {
        NbtCompound nbt = getCustomData(stack);
        nbt.putInt(NBT_DROP_TICK, tick);
        setCustomData(stack, nbt);
    }

    public static int getDropTick(ItemStack stack) {
        NbtCompound nbt = getCustomData(stack);
        return nbt.contains(NBT_DROP_TICK) ? nbt.getInt(NBT_DROP_TICK) : -1;
    }
}


