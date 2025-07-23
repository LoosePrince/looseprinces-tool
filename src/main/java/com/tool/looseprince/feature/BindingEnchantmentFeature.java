package com.tool.looseprince.feature;

import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;
import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.event.BindingEnchantmentEventHandler;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * 绑定附魔功能
 * 提供绑定附魔，使物品在死亡时不会掉落
 */
public class BindingEnchantmentFeature implements Feature {
    private static final String ID = "binding_enchantment";
    private static final String DISPLAY_NAME = "绑定附魔";
    private static final String DESCRIPTION = "使物品在死亡时不会掉落的附魔";
    
    // 绑定附魔注册键
    public static final RegistryKey<Enchantment> BINDING = RegistryKey.of(
        RegistryKeys.ENCHANTMENT, 
        Identifier.of("looseprinces-tool", "binding")
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
            LoosePrincesTool.LOGGER.info("绑定附魔功能已禁用，跳过初始化");
            return;
        }
        
        LoosePrincesTool.LOGGER.info("初始化绑定附魔功能");
        
        // 注册事件监听器
        registerEventListeners();
        
        LoosePrincesTool.LOGGER.info("绑定附魔功能初始化完成");
    }
    
    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().isFeatureEnabled(ID);
    }
    
    @Override
    public FeatureConfig getDefaultConfig() {
        FeatureConfig config = new FeatureConfig(true);
        
        // 添加默认配置选项
        config.setOption("preventDrop", true);  // 是否阻止物品掉落
        config.setOption("affectAllItems", true);  // 是否影响所有物品类型
        config.setOption("maxLevel", 1);  // 最大附魔等级
        
        return config;
    }
    
    /**
     * 注册事件监听器
     */
    private void registerEventListeners() {
        try {
            BindingEnchantmentEventHandler eventHandler = new BindingEnchantmentEventHandler(this);
            eventHandler.registerEvents();
            
            LoosePrincesTool.LOGGER.info("绑定附魔事件监听器注册成功");
            
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("注册绑定附魔事件监听器失败", e);
        }
    }
    
    /**
     * 检查是否应该阻止物品掉落
     * @return 如果应该阻止返回true，否则返回false
     */
    public boolean shouldPreventDrop() {
        FeatureConfig config = getConfig();
        return config != null && config.getBooleanOption("preventDrop", true);
    }
    
    /**
     * 检查是否影响所有物品类型
     * @return 如果影响所有物品返回true，否则返回false
     */
    public boolean affectAllItems() {
        FeatureConfig config = getConfig();
        return config == null || config.getBooleanOption("affectAllItems", true);
    }
    
    /**
     * 获取最大附魔等级
     * @return 最大附魔等级
     */
    public int getMaxLevel() {
        FeatureConfig config = getConfig();
        return config != null ? config.getIntOption("maxLevel", 1) : 1;
    }
    
    @Override
    public void onEnable() {
        LoosePrincesTool.LOGGER.info("绑定附魔功能已启用");
    }
    
    @Override
    public void onDisable() {
        LoosePrincesTool.LOGGER.info("绑定附魔功能已禁用");
    }
} 