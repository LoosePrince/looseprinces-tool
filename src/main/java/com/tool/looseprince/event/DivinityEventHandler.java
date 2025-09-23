package com.tool.looseprince.event;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.feature.DivinityFeature;
import com.tool.looseprince.feature.FairDuelFeature;
import com.tool.looseprince.feature.FeatureRegistry;
import com.tool.looseprince.item.CompleteDivinityItem;
import com.tool.looseprince.item.ImperfectDivinityItem;
import com.tool.looseprince.item.CreatorDivinityItem;
import com.tool.looseprince.util.CreatorCooldownManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.util.Identifier;

/**
 * 神格事件处理：
 * - 每0.5秒检测玩家背包，赋予相应状态效果
 * - 完整神格授予飞行能力；失去时移除
 */
public class DivinityEventHandler {
    private final DivinityFeature feature;

    public DivinityEventHandler(DivinityFeature feature) {
        this.feature = feature;
    }

    public void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long time = server.getOverworld().getTime();
            if ((time % 10) != 0) { // 每0.5s
                return;
            }
            try {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    boolean hasImperfect = hasImperfect(player);
                    boolean hasComplete = hasComplete(player);
                    boolean hasCreator = hasCreator(player);

                    boolean cooling = CreatorCooldownManager.getInstance().isCoolingDown(player.getUuid(), time);

                    // 取公平对决效果引用
                    FairDuelFeature fair = (FairDuelFeature) FeatureRegistry.getInstance().getFeature("fair_duel");

                    // 冷却期：剥夺相关效果并禁止飞行，同时维持神力静默计时
                    if (cooling) {
                        // 移除所有相关效果
                        if (feature.getCreatorEffect() != null && player.hasStatusEffect(feature.getCreatorEffect())) {
                            player.removeStatusEffect(feature.getCreatorEffect());
                        }
                        if (feature.getDivinePowerEffect() != null && player.hasStatusEffect(feature.getDivinePowerEffect())) {
                            player.removeStatusEffect(feature.getDivinePowerEffect());
                        }
                        if (feature.getImperfectDivinityEffect() != null && player.hasStatusEffect(feature.getImperfectDivinityEffect())) {
                            player.removeStatusEffect(feature.getImperfectDivinityEffect());
                        }
                        if (fair != null && fair.getFairDuelEffect() != null && player.hasStatusEffect(fair.getFairDuelEffect())) {
                            player.removeStatusEffect(fair.getFairDuelEffect());
                        }
                        // 禁止飞行
                        if (!player.isCreative() && !player.isSpectator()) {
                            if (player.getAbilities().allowFlying) {
                                player.getAbilities().allowFlying = false;
                                player.getAbilities().flying = false;
                                player.sendAbilitiesUpdate();
                            }
                        }
                        // 刷新神力静默效果时长（与剩余冷却同步）
                        if (feature.getDivineSilenceEffect() != null) {
                            long remain = CreatorCooldownManager.getInstance().getRemainingTicks(player.getUuid(), time);
                            int dur = (int) Math.min(Integer.MAX_VALUE, remain);
                            if (dur > 0) {
                                player.addStatusEffect(new StatusEffectInstance(feature.getDivineSilenceEffect(), dur, 0, true, true, true));
                            }
                        }
                        continue;
                    }

                    // 非冷却：优先造物主 > 完整神格 > 残缺神格
                    if (hasCreator && feature.getCreatorEffect() != null) {
                        // 获得造物主效果（包含神的力量含义），并清理其他冲突
                        player.addStatusEffect(new StatusEffectInstance(feature.getCreatorEffect(), 30, 0, true, true, true));
                        if (com.tool.looseprince.LoosePrincesTool.isOurAdvancementsLoaded()) {
                            grantAdvancementCriterion(player, "above_sky", "granted_by_code");
                        }
                        if (feature.getDivinePowerEffect() != null && player.hasStatusEffect(feature.getDivinePowerEffect())) {
                            player.removeStatusEffect(feature.getDivinePowerEffect());
                        }
                        if (feature.getImperfectDivinityEffect() != null && player.hasStatusEffect(feature.getImperfectDivinityEffect())) {
                            player.removeStatusEffect(feature.getImperfectDivinityEffect());
                        }
                        if (fair != null && fair.getFairDuelEffect() != null && player.hasStatusEffect(fair.getFairDuelEffect())) {
                            player.removeStatusEffect(fair.getFairDuelEffect());
                        }
                        // 飞行允许
                        if (!player.isCreative() && !player.isSpectator()) {
                            if (!player.getAbilities().allowFlying) {
                                player.getAbilities().allowFlying = true;
                                player.sendAbilitiesUpdate();
                            }
                        }
                    } else if (hasComplete && feature.getDivinePowerEffect() != null) {
                        // 神的力量（无敌），并清理公平对决效果
                        player.addStatusEffect(new StatusEffectInstance(feature.getDivinePowerEffect(), 30, 0, true, true, true));
                        // 进度：王座承认了你
                        if (com.tool.looseprince.LoosePrincesTool.isOurAdvancementsLoaded()) {
                            grantAdvancementCriterion(player, "throne", "granted_by_code");
                        }
                        if (fair != null && fair.getFairDuelEffect() != null && player.hasStatusEffect(fair.getFairDuelEffect())) {
                            player.removeStatusEffect(fair.getFairDuelEffect());
                        }
                        // 飞行：仅在拥有完整神格时强制开启
                        if (!player.isCreative() && !player.isSpectator()) {
                            if (!player.getAbilities().allowFlying) {
                                player.getAbilities().allowFlying = true;
                                player.sendAbilitiesUpdate();
                            }
                        }
                    } else if (hasImperfect) {
                        // 残缺的神格：仅赋予自定义状态效果，该效果内部包含抗性提升V和公平对决功能
                        if (feature.getImperfectDivinityEffect() != null) {
                            player.addStatusEffect(new StatusEffectInstance(feature.getImperfectDivinityEffect(), 30, 0, true, true, true));
                            // 进度：窃火者的荆棘冠
                            if (com.tool.looseprince.LoosePrincesTool.isOurAdvancementsLoaded()) {
                                grantAdvancementCriterion(player, "thorn_crown", "granted_by_code");
                            }
                        }
                        // 不覆盖飞行状态（交由飞行符文或其他来源控制）
                    } else {
                        // 既没有完整神格也没有残缺神格：不要覆盖飞行状态，交由其他功能处理
                    }
                }
            } catch (Exception e) {
                LoosePrincesTool.LOGGER.error("DivinityEventHandler error", e);
            }
        });

        // 第二个tick事件：基于"残缺的神格"状态效果赋予抗性提升V和公平对决
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long time = server.getOverworld().getTime();
            if ((time % 10) != 0) { // 每0.5s
                return;
            }
            try {
                FairDuelFeature fair = (FairDuelFeature) FeatureRegistry.getInstance().getFeature("fair_duel");
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    // 检查是否有"残缺的神格"状态效果
                    if (feature.getImperfectDivinityEffect() != null && player.hasStatusEffect(feature.getImperfectDivinityEffect())) {
                        // 抗性提升V（amplifier 4）
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 30, 4, true, true, true));
                        // 公平对决效果
                        if (fair != null && fair.getFairDuelEffect() != null) {
                            player.addStatusEffect(new StatusEffectInstance(fair.getFairDuelEffect(), 30, 0, true, true, true));
                        }
                    }
                }
            } catch (Exception e) {
                LoosePrincesTool.LOGGER.error("DivinityEventHandler secondary tick error", e);
            }
        });
    }

    private void grantAdvancementCriterion(ServerPlayerEntity player, String path, String criterion) {
        try {
            Identifier id = Identifier.of(LoosePrincesTool.MOD_ID, path);
            AdvancementEntry adv = player.getServer().getAdvancementLoader().get(id);
            if (adv != null) {
                boolean granted = player.getAdvancementTracker().grantCriterion(adv, criterion);
                LoosePrincesTool.LOGGER.info("[Adv] grant {}:{} -> {} => {}", id.getNamespace(), id.getPath(), criterion, granted);
            } else {
                LoosePrincesTool.LOGGER.warn("[Adv] missing advancement {}", id);
            }
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[Adv] grant error", e);
        }
    }

    private boolean hasImperfect(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ImperfectDivinityItem) {
                return true;
            }
        }
        if (!player.getOffHandStack().isEmpty() && player.getOffHandStack().getItem() instanceof ImperfectDivinityItem) {
            return true;
        }
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack armorStack = player.getInventory().armor.get(i);
            if (!armorStack.isEmpty() && armorStack.getItem() instanceof ImperfectDivinityItem) {
                return true;
            }
        }
        return false;
    }

    private boolean hasComplete(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof CompleteDivinityItem) {
                return true;
            }
        }
        if (!player.getOffHandStack().isEmpty() && player.getOffHandStack().getItem() instanceof CompleteDivinityItem) {
            return true;
        }
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack armorStack = player.getInventory().armor.get(i);
            if (!armorStack.isEmpty() && armorStack.getItem() instanceof CompleteDivinityItem) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCreator(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof CreatorDivinityItem) {
                return true;
            }
        }
        if (!player.getOffHandStack().isEmpty() && player.getOffHandStack().getItem() instanceof CreatorDivinityItem) {
            return true;
        }
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack armorStack = player.getInventory().armor.get(i);
            if (!armorStack.isEmpty() && armorStack.getItem() instanceof CreatorDivinityItem) {
                return true;
            }
        }
        return false;
    }
}


