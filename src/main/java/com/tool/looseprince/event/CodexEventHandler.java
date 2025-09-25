package com.tool.looseprince.event;

import com.tool.looseprince.register.CodexRegistrar;
import com.tool.looseprince.state.CodexState;
import com.tool.looseprince.util.SoulBindingUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;


/**
 * 神秘典籍相关事件：
 * - 首次进入世界发放“神秘典籍/神祇之书”，并附加“灵魂绑定II”，记录归属
 */
public final class CodexEventHandler {
    private CodexEventHandler() {}

    public static void register() {
        // 使用服务器tick作为轻量时机，检测未发放玩家并发放
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                tryGiveTome(p);
            }
        });
    }

    private static void tryGiveTome(ServerPlayerEntity player) {
        try {
            CodexState st = CodexState.get(player);
            if (st.isGivenOnce()) return;
            ItemStack tome = new ItemStack(CodexRegistrar.getMysticTome());
            // 附加灵魂绑定 II
            try {
                // 基于当前附魔组件追加 灵魂绑定 II
                net.minecraft.component.type.ItemEnchantmentsComponent current = EnchantmentHelper.getEnchantments(tome);
                java.util.Map<RegistryEntry<Enchantment>, Integer> builder = new java.util.HashMap<>();
                if (current != null) {
                    for (RegistryEntry<Enchantment> e : current.getEnchantments()) {
                        builder.put(e, current.getLevel(e));
                    }
                }
                var reg = player.getServer().getRegistryManager().get(RegistryKeys.ENCHANTMENT);
                RegistryEntry<Enchantment> soul = reg.getEntry(com.tool.looseprince.feature.SoulBindingFeature.SOUL_BINDING).orElse(null);
                if (soul != null) {
                    com.tool.looseprince.util.EnchantUtils.add(tome, soul, 2);
                }
            } catch (Throwable ignored) {}

            // 标记拥有者
            try { SoulBindingUtils.ensureOwner(tome, player); } catch (Throwable ignored) {}

            // 尝试放入背包
            boolean inserted = player.getInventory().insertStack(tome);
            if (!inserted) {
                player.dropItem(tome, false);
            }
            st.setGivenOnce(true);
            st.save(player);
        } catch (Exception ignored) {}
    }
}


