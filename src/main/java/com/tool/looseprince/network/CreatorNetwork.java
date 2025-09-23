package com.tool.looseprince.network;

import com.tool.looseprince.feature.DivinityFeature;
import com.tool.looseprince.feature.FeatureRegistry;
import com.tool.looseprince.network.payload.CreatorRequestPayload;
import com.tool.looseprince.util.CreatorCooldownManager;
import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class CreatorNetwork {
    public static void registerServerReceiver() {
        try {
            CreatorRequestPayload.registerTypeC2S();
            ServerPlayNetworking.registerGlobalReceiver(CreatorRequestPayload.ID, (payload, context) -> {
                context.player().getServer().execute(() -> handle(context.player().getServer(), context.player(), payload.itemId()));
            });
        } catch (Exception ignored) {}
    }

    private static void handle(MinecraftServer server, ServerPlayerEntity player, String itemId) {
        try {
            if (player.isCreative() || player.isSpectator()) {
                return; // 仅生存/冒险
            }
            long now = server.getOverworld().getTime();
            if (CreatorCooldownManager.getInstance().isCoolingDown(player.getUuid(), now)) {
                return;
            }
            DivinityFeature div = (DivinityFeature) FeatureRegistry.getInstance().getFeature("divinity");
            if (div == null || !div.isEnabled()) return;

            // 要求拥有造物主效果
            boolean hasCreatorEffect = div.getCreatorEffect() != null && player.hasStatusEffect(div.getCreatorEffect());
            if (!hasCreatorEffect) {
                return;
            }

            net.minecraft.util.Identifier id = net.minecraft.util.Identifier.tryParse(itemId);
            if (id == null) return;
            java.util.Optional<Item> itemOpt = Registries.ITEM.getOrEmpty(id);
            if (itemOpt.isEmpty()) return;
            Item item = itemOpt.get();

            // 从配置读取数量与冷却
            FeatureConfig cfg = ConfigManager.getInstance().getFeatureConfig("divinity");
            int amount = cfg != null ? cfg.getIntOption("creatorGiveAmount", 1) : 1;
            amount = Math.max(-64, Math.min(640, amount));

            boolean success = false;
            if (amount == 0) {
                success = false; // 无操作
            } else if (amount > 0) {
                ItemStack stack = new ItemStack(item, amount);
                boolean inserted = player.getInventory().insertStack(stack);
                if (!inserted) {
                    player.dropItem(stack, false);
                }
                success = true; // 给出物品即视为成功
            } else {
                // 负数为扣除
                int needRemove = -amount;
                int removed = 0;
                for (int i = 0; i < player.getInventory().size() && needRemove > 0; i++) {
                    ItemStack st = player.getInventory().getStack(i);
                    if (!st.isEmpty() && st.getItem() == item) {
                        int remove = Math.min(needRemove, st.getCount());
                        st.decrement(remove);
                        needRemove -= remove;
                        removed += remove;
                    }
                }
                success = removed > 0;
            }

            if (success) {
                // 启动冷却并赋予神力静默
                int seconds = cfg != null ? cfg.getIntOption("creatorCooldownSeconds", 900) : 900;
                long duration = 20L * Math.max(0, seconds);
                CreatorCooldownManager.getInstance().startCooldown(player.getUuid(), now, duration);
                if (div.getDivineSilenceEffect() != null) {
                    player.addStatusEffect(new StatusEffectInstance(div.getDivineSilenceEffect(), (int) duration, 0, true, true, true));
                }
            }
        } catch (Exception ignored) {}
    }
}


