package com.tool.looseprince.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 兼容性附魔工具：在不同 Yarn 版本下尽力为物品添加附魔
 */
public final class EnchantUtils {
    private EnchantUtils() {}

    public static void add(ItemStack stack, RegistryEntry<Enchantment> enchant, int level) {
        try {
            // 读取当前附魔映射
            Object comp = EnchantmentHelper.getEnchantments(stack);
            Map<RegistryEntry<Enchantment>, Integer> map = new HashMap<>();
            if (comp != null) {
                try {
                    // comp.getEnchantments(): Set<RegistryEntry<Enchantment>>
                    Method getEnchantments = comp.getClass().getMethod("getEnchantments");
                    @SuppressWarnings("unchecked")
                    java.util.Set<RegistryEntry<Enchantment>> set = (java.util.Set<RegistryEntry<Enchantment>>) getEnchantments.invoke(comp);
                    Method getLevel = comp.getClass().getMethod("getLevel", RegistryEntry.class);
                    for (RegistryEntry<Enchantment> e : set) {
                        int lv = (Integer) getLevel.invoke(comp, e);
                        map.put(e, lv);
                    }
                } catch (Throwable ignored) {}
            }
            map.put(enchant, Math.max(1, level));

            // 优先：EnchantmentHelper.set(stack, ItemEnchantmentsComponent)
            try {
                Class<?> iec = Class.forName("net.minecraft.component.type.ItemEnchantmentsComponent");
                // 先尝试 fromMap
                Object newComp = null;
                try {
                    Method fromMap = iec.getMethod("fromMap", Map.class);
                    newComp = fromMap.invoke(null, map);
                } catch (Throwable ignored) {}
                if (newComp == null) {
                    try {
                        Method create = iec.getMethod("create", Map.class);
                        newComp = create.invoke(null, map);
                    } catch (Throwable ignored) {}
                }
                if (newComp == null) {
                    try {
                        Constructor<?> ctor = iec.getConstructor(Map.class);
                        newComp = ctor.newInstance(map);
                    } catch (Throwable ignored) {}
                }
                if (newComp != null) {
                    try {
                        Method setMethod = EnchantmentHelper.class.getMethod("set", ItemStack.class, iec);
                        setMethod.invoke(null, stack, newComp);
                        return;
                    } catch (Throwable ignored) {}
                }
            } catch (Throwable ignored) {}

            // 退化：EnchantmentHelper.setEnchantments(map, stack)
            try {
                Method legacy = EnchantmentHelper.class.getMethod("setEnchantments", Map.class, ItemStack.class);
                legacy.invoke(null, map, stack);
                return;
            } catch (Throwable ignored) {}

            // 最后兜底：直接写入 DataComponent ENCHANTMENTS
            try {
                Class<?> iec = Class.forName("net.minecraft.component.type.ItemEnchantmentsComponent");
                Object newComp = null;
                try {
                    Method fromMap = iec.getMethod("fromMap", Map.class);
                    newComp = fromMap.invoke(null, map);
                } catch (Throwable ignored) {}
                if (newComp == null) return;
                Class<?> dct = Class.forName("net.minecraft.component.DataComponentTypes");
                Field f = dct.getField("ENCHANTMENTS");
                Object type = f.get(null);
                Method set = ItemStack.class.getMethod("set", Class.forName("net.minecraft.component.ComponentType"), Object.class);
                set.invoke(stack, type, newComp);
            } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }
}


