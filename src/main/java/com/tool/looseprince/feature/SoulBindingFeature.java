package com.tool.looseprince.feature;

import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;
import com.tool.looseprince.LoosePrincesTool;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * 灵魂绑定附魔功能
 * 提供灵魂绑定附魔，附魔的物品会绑定到特定玩家，其他玩家无法拾取或从容器中取出
 */
public class SoulBindingFeature implements Feature {
    private static final String ID = "soul_binding";
    private static final String DISPLAY_NAME = "灵魂绑定附魔";
    private static final String DESCRIPTION = "使物品绑定到特定玩家，其他玩家无法拾取或移动";
    
    // 灵魂绑定附魔注册键
    public static final RegistryKey<Enchantment> SOUL_BINDING = RegistryKey.of(
        RegistryKeys.ENCHANTMENT, 
        Identifier.of(LoosePrincesTool.MOD_ID, "soul_binding")
    );
    
    @Override
    public String getId() {
        return ID;
    }
    
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
    
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
    
    @Override
    public void initialize() {
        if (!isEnabled()) {
            LoosePrincesTool.LOGGER.info("灵魂绑定附魔功能已禁用，跳过初始化");
            return;
        }
        
        LoosePrincesTool.LOGGER.info("初始化灵魂绑定附魔功能");
        
        // 灵魂绑定附魔功能通过数据包定义，无需额外注册代码
        // 相关逻辑已通过 Mixin 实现
        
        LoosePrincesTool.LOGGER.info("灵魂绑定附魔功能初始化完成");
    }
    
    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().isFeatureEnabled(ID);
    }
    
    @Override
    public FeatureConfig getDefaultConfig() {
        FeatureConfig config = new FeatureConfig(true);
        
        // 添加默认配置选项
        config.setOption("preventPickup", true);  // 是否阻止其他玩家拾取
        config.setOption("preventContainerTake", true);  // 是否阻止其他玩家从容器中取出
        config.setOption("showOwnerTooltip", true);  // 是否显示拥有者信息
        
        return config;
    }
    
    /**
     * 检查是否应该阻止其他玩家拾取
     */
    public boolean shouldPreventPickup() {
        FeatureConfig config = getConfig();
        return config != null && config.getBooleanOption("preventPickup", true);
    }
    
    /**
     * 检查是否应该阻止其他玩家从容器中取出
     */
    public boolean shouldPreventContainerTake() {
        FeatureConfig config = getConfig();
        return config != null && config.getBooleanOption("preventContainerTake", true);
    }
    
    /**
     * 检查是否应该显示拥有者信息
     */
    public boolean shouldShowOwnerTooltip() {
        FeatureConfig config = getConfig();
        return config != null && config.getBooleanOption("showOwnerTooltip", true);
    }
    
    @Override
    public void onEnable() {
        LoosePrincesTool.LOGGER.info("灵魂绑定附魔功能已启用");
    }
    
    @Override
    public void onDisable() {
        LoosePrincesTool.LOGGER.info("灵魂绑定附魔功能已禁用");
    }
}
