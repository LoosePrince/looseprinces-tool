package com.tool.looseprince.mixin;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.feature.FairDuelFeature;
import com.tool.looseprince.feature.FeatureRegistry;
import com.tool.looseprince.event.FairDuelEventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 公平决斗功能：记录玩家对目标的伤害百分比，并在玩家受到伤害时直接设置生命值，
 * 完全绕过所有减伤效果（护甲、抗性提升等），实现真正的"无视减伤"等比例伤害。
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFairDuelMixin {
    @Shadow public abstract float getHealth();
    @Shadow public abstract void setHealth(float health);
    @Shadow public abstract float getMaxHealth();

    private float lp_fairduel_preHealth;
    private DamageSource lp_fairduel_lastSource;

    @Inject(method = "damage", at = @At("HEAD"))
    private void lp_fairduel_capturePreHealth(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        lp_fairduel_preHealth = self.getHealth();
        lp_fairduel_lastSource = source;
    }

    @Inject(method = "damage", at = @At("TAIL"))
    private void lp_fairduel_onDamageTail(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.getWorld().isClient()) {
            return; // 仅在服务端执行
        }

        FairDuelFeature feature = (FairDuelFeature) FeatureRegistry.getInstance().getFeature("fair_duel");
        if (feature == null || !feature.isEnabled()) {
            return;
        }
        FairDuelEventHandler handler = feature.getEventHandler();
        if (handler == null) {
            return;
        }

        // 1) 记录玩家对目标造成的实际伤害
        Entity attacker = source.getAttacker() != null ? source.getAttacker() : source.getSource();
        if (attacker instanceof PlayerEntity playerAttacker && self != playerAttacker) {
            float dealt = Math.max(0.0f, lp_fairduel_preHealth - self.getHealth());
            if (dealt > 0.0f) {
                handler.recordPlayerDealtDamage(playerAttacker, self, dealt);
                LoosePrincesTool.LOGGER.debug("[FairDuel][record] attacker={} target={} dealt={}",
                        playerAttacker.getName().getString(), self.getName().getString(), dealt);
            }
        }

        // 2) 如果是玩家受到伤害，直接设置生命值（绕过所有减伤）
        if (self instanceof PlayerEntity playerVictim) {
            float adjustedDamage = handler.modifyIncomingDamage(playerVictim, attacker, amount);
            
            // 如果调整后的伤害与原伤害不同，直接设置生命值
            if (adjustedDamage != amount) {
                float newHealth = Math.max(0.0f, lp_fairduel_preHealth - adjustedDamage);
                setHealth(newHealth);
                
                LoosePrincesTool.LOGGER.info("[FairDuel][apply] victim={} attacker={} original={} adjusted={} finalHealth={}",
                        playerVictim.getName().getString(), 
                        attacker != null ? attacker.getName().getString() : "null",
                        amount, adjustedDamage, newHealth);
            }
        }
    }
}


