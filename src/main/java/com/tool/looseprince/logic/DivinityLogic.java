package com.tool.looseprince.logic;

import com.tool.looseprince.register.DivinityRegistrar;
import com.tool.looseprince.register.FairDuelRegistrar;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * 逻辑层：神格系统的判定与冲突规则
 */
public final class DivinityLogic {
    private DivinityLogic() {}

    public static boolean hasImperfectItem(PlayerEntity player) {
        return hasItem(player, DivinityRegistrar.getImperfectItem());
    }

    public static boolean hasCompleteItem(PlayerEntity player) {
        return hasItem(player, DivinityRegistrar.getCompleteItem());
    }

    public static boolean hasCreatorItem(PlayerEntity player) {
        return hasItem(player, DivinityRegistrar.getCreatorItem());
    }

    private static boolean hasItem(PlayerEntity player, Item item) {
        if (item == null) return false;
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (player.getInventory().getStack(i).getItem() == item) return true;
        }
        ItemStack off = player.getOffHandStack();
        if (!off.isEmpty() && off.getItem() == item) return true;
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            if (player.getInventory().armor.get(i).getItem() == item) return true;
        }
        return false;
    }

    public static boolean isCooling(ServerPlayerEntity player, long nowTick) {
        try {
            return com.tool.looseprince.util.CreatorCooldownManager.getInstance().isCoolingDown(player.getUuid(), nowTick);
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean isGodLikeActive(PlayerEntity player) {
        RegistryEntry<StatusEffect> divine = DivinityRegistrar.getDivinePowerEffect();
        RegistryEntry<StatusEffect> creator = DivinityRegistrar.getCreatorEffect();
        return (divine != null && player.hasStatusEffect(divine))
            || (creator != null && player.hasStatusEffect(creator));
    }

    public static boolean shouldBlockFairDuel(PlayerEntity player) {
        return isGodLikeActive(player);
    }

    public static RegistryEntry<StatusEffect> fairDuelEffect() {
        return FairDuelRegistrar.getEffect();
    }

    public static RegistryEntry<StatusEffect> imperfectEffect() {
        return DivinityRegistrar.getImperfectEffect();
    }

    public static RegistryEntry<StatusEffect> divinePowerEffect() {
        return DivinityRegistrar.getDivinePowerEffect();
    }

    public static RegistryEntry<StatusEffect> creatorEffect() {
        return DivinityRegistrar.getCreatorEffect();
    }

    public static RegistryEntry<StatusEffect> silenceEffect() {
        return DivinityRegistrar.getDivineSilenceEffect();
    }
}


