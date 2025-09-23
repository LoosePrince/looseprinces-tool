package com.tool.looseprince.mixin;

import com.tool.looseprince.feature.FeatureRegistry;
import com.tool.looseprince.feature.SoulBindingFeature;
import com.tool.looseprince.util.SoulBindingUtils;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 岩浆/虚空销毁保护（等级2启用，可配置）
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntitySoulBindingDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void lpt$preventDestroy(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getStack();
        if (stack.isEmpty() || !SoulBindingUtils.hasSoulBinding(stack)) return;

        SoulBindingFeature feature = (SoulBindingFeature) FeatureRegistry.getInstance().getFeature("soul_binding");
        if (feature == null || !feature.isEnabled()) return;

        int level = SoulBindingUtils.getSoulBindingLevel(stack);
        if (level < 2) return;

        // 岩浆免疫
        if (feature.isLavaImmune() && (source.isOf(DamageTypes.LAVA) || source.isOf(DamageTypes.IN_FIRE) || source.isOf(DamageTypes.ON_FIRE))) {
            cir.setReturnValue(false);
            cir.cancel();
            return;
        }

        // 虚空处理：不销毁，改为上浮脱离虚空
        if (!feature.isVoidDestroyable() && source.isOf(DamageTypes.OUT_OF_WORLD)) {
            // 取消伤害
            cir.setReturnValue(false);
            cir.cancel();
            // 将物品实体上移到世界高度 0.5 并清除动量，使其脱离虚空
            double x = self.getX();
            double z = self.getZ();
            double safeY = Math.max(self.getWorld().getBottomY() + 1, 1);
            ((ItemEntity)(Object)this).setPosition(x, safeY, z);
            ((ItemEntity)(Object)this).setVelocity(0, 0.4, 0);
        }
    }
}


