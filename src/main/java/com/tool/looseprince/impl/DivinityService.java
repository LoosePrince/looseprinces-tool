package com.tool.looseprince.impl;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.logic.DivinityLogic;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 实现层：神格系统具体效果应用（赋予/移除效果、飞行能力、成就授予）
 */
public final class DivinityService {
    private DivinityService() {}

    public static void applyCooling(ServerPlayerEntity player, long nowTick) {
        try {
            // 清理相关效果
            if (DivinityLogic.creatorEffect() != null && player.hasStatusEffect(DivinityLogic.creatorEffect())) player.removeStatusEffect(DivinityLogic.creatorEffect());
            if (DivinityLogic.divinePowerEffect() != null && player.hasStatusEffect(DivinityLogic.divinePowerEffect())) player.removeStatusEffect(DivinityLogic.divinePowerEffect());
            if (DivinityLogic.imperfectEffect() != null && player.hasStatusEffect(DivinityLogic.imperfectEffect())) player.removeStatusEffect(DivinityLogic.imperfectEffect());
            if (DivinityLogic.fairDuelEffect() != null && player.hasStatusEffect(DivinityLogic.fairDuelEffect())) player.removeStatusEffect(DivinityLogic.fairDuelEffect());
            // 禁止飞行
            if (!player.isCreative() && !player.isSpectator()) {
                if (player.getAbilities().allowFlying) {
                    player.getAbilities().allowFlying = false;
                    player.getAbilities().flying = false;
                    player.sendAbilitiesUpdate();
                }
            }
            // 刷新神力静默
            if (DivinityLogic.silenceEffect() != null) {
                long remain = com.tool.looseprince.util.CreatorCooldownManager.getInstance().getRemainingTicks(player.getUuid(), nowTick);
                int dur = (int) Math.min(Integer.MAX_VALUE, remain);
                if (dur > 0) {
                    player.addStatusEffect(new StatusEffectInstance(DivinityLogic.silenceEffect(), dur, 0, true, true, true));
                }
            }
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[DivinityService] applyCooling error", e);
        }
    }

    public static void applyCreator(ServerPlayerEntity player) {
        try {
            if (DivinityLogic.creatorEffect() != null) {
                player.addStatusEffect(new StatusEffectInstance(DivinityLogic.creatorEffect(), 30, 0, true, true, true));
            }
            // 移除其他冲突
            if (DivinityLogic.divinePowerEffect() != null && player.hasStatusEffect(DivinityLogic.divinePowerEffect())) player.removeStatusEffect(DivinityLogic.divinePowerEffect());
            if (DivinityLogic.imperfectEffect() != null && player.hasStatusEffect(DivinityLogic.imperfectEffect())) player.removeStatusEffect(DivinityLogic.imperfectEffect());
            if (DivinityLogic.fairDuelEffect() != null && player.hasStatusEffect(DivinityLogic.fairDuelEffect())) player.removeStatusEffect(DivinityLogic.fairDuelEffect());
            // 飞行
            if (!player.isCreative() && !player.isSpectator()) {
                if (!player.getAbilities().allowFlying) {
                    player.getAbilities().allowFlying = true;
                    player.sendAbilitiesUpdate();
                }
            }
            // 清理静默
            if (DivinityLogic.silenceEffect() != null && player.hasStatusEffect(DivinityLogic.silenceEffect())) player.removeStatusEffect(DivinityLogic.silenceEffect());
            grant(player, "above_sky");
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[DivinityService] applyCreator error", e);
        }
    }

    public static void applyDivinePower(ServerPlayerEntity player) {
        try {
            if (DivinityLogic.divinePowerEffect() != null) {
                player.addStatusEffect(new StatusEffectInstance(DivinityLogic.divinePowerEffect(), 30, 0, true, true, true));
            }
            if (DivinityLogic.fairDuelEffect() != null && player.hasStatusEffect(DivinityLogic.fairDuelEffect())) player.removeStatusEffect(DivinityLogic.fairDuelEffect());
            if (!player.isCreative() && !player.isSpectator()) {
                if (!player.getAbilities().allowFlying) {
                    player.getAbilities().allowFlying = true;
                    player.sendAbilitiesUpdate();
                }
            }
            if (DivinityLogic.silenceEffect() != null && player.hasStatusEffect(DivinityLogic.silenceEffect())) player.removeStatusEffect(DivinityLogic.silenceEffect());
            grant(player, "throne");
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[DivinityService] applyDivinePower error", e);
        }
    }

    public static void applyImperfect(ServerPlayerEntity player) {
        try {
            if (DivinityLogic.imperfectEffect() != null) {
                player.addStatusEffect(new StatusEffectInstance(DivinityLogic.imperfectEffect(), 30, 0, true, true, true));
                // 附带抗性提升V
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 30, 4, true, true, true));
                // 公平对决（如未被阻止）
                if (!DivinityLogic.shouldBlockFairDuel(player) && DivinityLogic.fairDuelEffect() != null) {
                    player.addStatusEffect(new StatusEffectInstance(DivinityLogic.fairDuelEffect(), 30, 0, true, true, true));
                }
                grant(player, "thorn_crown");
            }
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[DivinityService] applyImperfect error", e);
        }
    }

    private static void grant(ServerPlayerEntity player, String path) {
        try {
            if (!com.tool.looseprince.LoosePrincesTool.isOurAdvancementsLoaded()) return;
            var id = net.minecraft.util.Identifier.of(LoosePrincesTool.MOD_ID, path);
            var adv = player.getServer().getAdvancementLoader().get(id);
            if (adv != null) {
                player.getAdvancementTracker().grantCriterion(adv, "granted_by_code");
            }
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[Adv] grant error", e);
        }
    }
}


