package com.tool.looseprince.impl;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.logic.DivinityLogic;
import com.tool.looseprince.logic.CooldownKeys;
import com.tool.looseprince.register.FlyingRuneRegistrar;
import com.tool.looseprince.register.FairDuelRegistrar;
import com.tool.looseprince.register.DivinityRegistrar;
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
            // 将飞行符文、公平对决、残缺/完整/造物主 设为玩家级冷却（与静默一致）
            int remain = (int) Math.min(Integer.MAX_VALUE, com.tool.looseprince.util.CreatorCooldownManager.getInstance().getRemainingTicks(player.getUuid(), nowTick));
            CooldownService.setPlayerCooldown(player, CooldownKeys.FLYING_RUNE, remain);
            CooldownService.setPlayerCooldown(player, CooldownKeys.FAIR_DUEL, remain);
            CooldownService.setPlayerCooldown(player, CooldownKeys.DIVINITY_IMPERFECT, remain);
            CooldownService.setPlayerCooldown(player, CooldownKeys.DIVINITY_COMPLETE, remain);
            CooldownService.setPlayerCooldown(player, CooldownKeys.DIVINITY_CREATOR, remain);
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
                int dur = (int) Math.min(Integer.MAX_VALUE, com.tool.looseprince.util.CreatorCooldownManager.getInstance().getRemainingTicks(player.getUuid(), nowTick));
                if (dur > 0) {
                    player.addStatusEffect(new StatusEffectInstance(DivinityLogic.silenceEffect(), dur, 0, true, true, true));
                }
            }

            // 可视化遮罩：对相关物品设置物品冷却圈
            try {
                var mgr = player.getItemCooldownManager();
                int maskTicks = getMaskTicks(nowTick, player);
                if (maskTicks > 0) {
                    if (FlyingRuneRegistrar.get() != null) mgr.set(FlyingRuneRegistrar.get(), maskTicks);
                    if (FairDuelRegistrar.getItem() != null) mgr.set(FairDuelRegistrar.getItem(), maskTicks);
                    if (DivinityRegistrar.getImperfectItem() != null) mgr.set(DivinityRegistrar.getImperfectItem(), maskTicks);
                    if (DivinityRegistrar.getCompleteItem() != null) mgr.set(DivinityRegistrar.getCompleteItem(), maskTicks);
                    if (DivinityRegistrar.getCreatorItem() != null) mgr.set(DivinityRegistrar.getCreatorItem(), maskTicks);
                }
            } catch (Exception ignored) {}
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("[DivinityService] applyCooling error", e);
        }
    }

    private static int getMaskTicks(long nowTick, ServerPlayerEntity player) {
        try {
            long remain = com.tool.looseprince.util.CreatorCooldownManager.getInstance().getRemainingTicks(player.getUuid(), nowTick);
            return (int) Math.min(Integer.MAX_VALUE, Math.max(0, remain));
        } catch (Exception e) {
            return 0;
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
            // 清理静默，并移除所有冷却圈/禁用遮罩
            if (DivinityLogic.silenceEffect() != null && player.hasStatusEffect(DivinityLogic.silenceEffect())) player.removeStatusEffect(DivinityLogic.silenceEffect());
            try {
                var mgr = player.getItemCooldownManager();
                if (FlyingRuneRegistrar.get() != null) mgr.remove(FlyingRuneRegistrar.get());
                if (FairDuelRegistrar.getItem() != null) mgr.remove(FairDuelRegistrar.getItem());
                if (DivinityRegistrar.getImperfectItem() != null) mgr.remove(DivinityRegistrar.getImperfectItem());
                if (DivinityRegistrar.getCompleteItem() != null) mgr.remove(DivinityRegistrar.getCompleteItem());
                if (DivinityRegistrar.getCreatorItem() != null) mgr.remove(DivinityRegistrar.getCreatorItem());
            } catch (Throwable ignored) {}
            grant(player, "above_sky");
            try { var st = com.tool.looseprince.state.CodexState.get(player); st.unlock("creator"); st.unlock("creator_divinity"); st.save(player);} catch (Throwable ignored) {}
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


