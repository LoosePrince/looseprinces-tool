package com.tool.looseprince.event;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.feature.FairDuelFeature;
import com.tool.looseprince.feature.DivinityFeature;
import com.tool.looseprince.feature.FeatureRegistry;
import com.tool.looseprince.item.FairDuelItem;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.util.Identifier;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 公平对决事件处理器
 * 记录玩家对某目标造成的上一次伤害占比，并按该占比调整玩家后续从该目标处受到的伤害
 */
public class FairDuelEventHandler {
    private final FairDuelFeature feature; // 保留指针用于未来配置读取

    // 使用嵌套Map避免Pair键可能导致的hash/equals问题
    // outerKey: playerUUID (攻击者/受击者中的玩家)
    // innerKey: otherUUID (对手生物)
    private final Map<UUID, Map<UUID, Double>> lastDamagePercent = new HashMap<>();
    private final Map<UUID, Map<UUID, Integer>> lastUpdateTick = new HashMap<>();
    // 最近一次成功赋予效果的tick
    private final Map<UUID, Integer> lastEffectAppliedTick = new HashMap<>();

    public FairDuelEventHandler(FairDuelFeature feature) {
        this.feature = feature;
    }

    public void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int tick = (int) server.getOverworld().getTime();
            // 清理超过 10 秒未更新的记录 (200 tick)
            Iterator<Map.Entry<UUID, Map<UUID, Integer>>> outer = lastUpdateTick.entrySet().iterator();
            while (outer.hasNext()) {
                Map.Entry<UUID, Map<UUID, Integer>> e = outer.next();
                UUID playerId = e.getKey();
                Map<UUID, Integer> inner = e.getValue();
                if (inner == null || inner.isEmpty()) {
                    outer.remove();
                    continue;
                }
                Iterator<Map.Entry<UUID, Integer>> innerIt = inner.entrySet().iterator();
                while (innerIt.hasNext()) {
                    Map.Entry<UUID, Integer> ie = innerIt.next();
                    if (tick - ie.getValue() > 200) {
                        innerIt.remove();
                        Map<UUID, Double> percentInner = lastDamagePercent.get(playerId);
                        if (percentInner != null) {
                            percentInner.remove(ie.getKey());
                            if (percentInner.isEmpty()) {
                                lastDamagePercent.remove(playerId);
                            }
                        }
                    }
                }
                if (inner.isEmpty()) {
                    outer.remove();
                }
            }
        });

        // 每0.5秒给满足来源(物品或残缺神格效果)的玩家赋予1.5秒公平对决效果
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long time = server.getOverworld().getTime();
            if ((time % 10) != 0) { // 20t/s → 每10tick=0.5s
                return;
            }
            try {
                for (PlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    if (hasFairDuel(player) && feature.getFairDuelEffect() != null) {
                        // 1.5s(30t) 时长，0.5s 赋予一次，容错抖动
                        player.addStatusEffect(new StatusEffectInstance(feature.getFairDuelEffect(), 30, 0, true, true, true));
                        lastEffectAppliedTick.put(player.getUuid(), (int) time);
                        if (player instanceof ServerPlayerEntity sp) {
                            if (com.tool.looseprince.LoosePrincesTool.isOurAdvancementsLoaded()) {
                                grantAdvancementCriterion(sp, "god_scale", "granted_by_code");
                            }
                        }
                    }
                }
            } catch (Exception ignored) {}
        });
    }

    public void recordPlayerDealtDamage(PlayerEntity attacker, LivingEntity target, float amount) {
        if (attacker == null || target == null || amount <= 0) {
            return;
        }
        if (!hasFairDuel(attacker)) {
            return;
        }
        float max = target.getMaxHealth();
        if (max <= 0.0001f) {
            return;
        }
        double percent = Math.max(0.0, Math.min(1.0, amount / max));
        UUID playerId = attacker.getUuid();
        UUID otherId = target.getUuid();
        lastDamagePercent.computeIfAbsent(playerId, k -> new HashMap<>()).put(otherId, percent);
        lastUpdateTick.computeIfAbsent(playerId, k -> new HashMap<>()).put(otherId, (int) attacker.getWorld().getTime());
        LoosePrincesTool.LOGGER.info("[FairDuel] record: attacker={}({}) target={}({}) dealt={} max={} percent={}",
                attacker.getName().getString(), attacker.getUuid(), target.getName().getString(), target.getUuid(), amount, max, percent);
    }

    public float modifyIncomingDamage(PlayerEntity victim, Entity attacker, float originalDamage) {
        if (victim == null || attacker == null || originalDamage <= 0) {
            LoosePrincesTool.LOGGER.info("[FairDuel] skip: victimOrAttackerNull={} originalDamage={}", attacker == null, originalDamage);
            return originalDamage;
        }
        // 现在改为基于药水效果：只有有公平对决状态时才应用
        if (feature.getFairDuelEffect() == null) {
            return originalDamage;
        }
        // 冷却期间禁止公平对决
        try {
            long now = victim.getWorld().getTime();
            if (com.tool.looseprince.util.CreatorCooldownManager.getInstance().isCoolingDown(victim.getUuid(), now)) {
                return originalDamage;
            }
        } catch (Exception ignored) {}

        boolean hasEffect = victim.hasStatusEffect(feature.getFairDuelEffect());
        // 容错：效果可能在赋予间隔的边界瞬间缺失，允许5tick宽限
        int nowTick = (int) victim.getWorld().getTime();
        int lastTick = lastEffectAppliedTick.getOrDefault(victim.getUuid(), -999999);
        boolean withinGrace = nowTick - lastTick <= 5;
        if (!hasEffect && !withinGrace) {
            LoosePrincesTool.LOGGER.info("[FairDuel] victimNoEffect victim={}", victim.getName().getString());
            return originalDamage;
        }
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            LoosePrincesTool.LOGGER.info("[FairDuel] attackerNotLiving attacker={} class={}", attacker.getName().getString(), attacker.getClass().getName());
            return originalDamage;
        }
        UUID playerId = victim.getUuid();
        UUID otherId = livingAttacker.getUuid();
        Double percent = null;
        Map<UUID, Double> inner = lastDamagePercent.get(playerId);
        if (inner != null) {
            percent = inner.get(otherId);
        }
        if (percent == null || percent <= 0) {
            LoosePrincesTool.LOGGER.info("[FairDuel] percentMissing victim={}({}) attacker={}({})", victim.getName().getString(), victim.getUuid(), livingAttacker.getName().getString(), livingAttacker.getUuid());
            return originalDamage;
        }
        float victimMax = victim.getMaxHealth();
        double damageRatio = feature.getDamageRatio(); // 获取配置的伤害比例
        float adjusted = (float) Math.max(0.0, victimMax * percent * damageRatio);
        LoosePrincesTool.LOGGER.info("[FairDuel] apply: victim={} attacker={} origDamage={} victimMax={} percent={} ratio={} adjusted={}",
                victim.getName().getString(), livingAttacker.getName().getString(), originalDamage, victimMax, percent, damageRatio, adjusted);
        return adjusted;
    }

    private boolean hasFairDuel(PlayerEntity player) {
        try {
            long now = player.getWorld().getTime();
            if (com.tool.looseprince.util.CreatorCooldownManager.getInstance().isCoolingDown(player.getUuid(), now)) {
                return false;
            }
        } catch (Exception ignored) {}
        // 来源1：残缺的神格状态效果（由神格物品授予），允许刷新公平对决
        try {
            DivinityFeature div = (DivinityFeature) FeatureRegistry.getInstance().getFeature("divinity");
            if (div != null && div.getImperfectDivinityEffect() != null && player.hasStatusEffect(div.getImperfectDivinityEffect())) {
                return true;
            }
        } catch (Exception ignored) {}

        // 来源2：持有公平对决物品
        // 主背包
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof FairDuelItem) {
                return true;
            }
        }
        // 副手
        ItemStack offhand = player.getOffHandStack();
        if (!offhand.isEmpty() && offhand.getItem() instanceof FairDuelItem) {
            return true;
        }
        // 盔甲槽
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack armorStack = player.getInventory().armor.get(i);
            if (!armorStack.isEmpty() && armorStack.getItem() instanceof FairDuelItem) {
                return true;
            }
        }
        return false;
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
}


