package com.tool.looseprince.mixin;

import com.tool.looseprince.impl.BindingService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * 玩家实体Mixin
 * 用于处理绑定附魔的物品掉落逻辑
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    
    @Shadow public abstract PlayerInventory getInventory();
    
    /**
     * 在玩家死亡掉落物品之前，保存具有绑定附魔的物品
     */
    @Inject(method = "dropInventory", at = @At("HEAD"))
    private void onDropInventory(CallbackInfo ci) {
        // 确保这是服务端玩家
        if (!(((Object) this) instanceof ServerPlayerEntity player)) {
            return;
        }
        
        try {
            BindingService.saveOnDeath(player, getInventory());
            
        } catch (Exception e) {
            // 如果处理过程中出现错误，记录日志但不影响正常游戏流程
            System.err.println("处理绑定附魔物品时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 