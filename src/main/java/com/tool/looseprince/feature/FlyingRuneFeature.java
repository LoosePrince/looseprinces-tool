package com.tool.looseprince.feature;

import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;
import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.event.FlyingRuneEventHandler;
import com.tool.looseprince.register.FlyingRuneRegistrar;
import net.minecraft.item.Item;

/**
 * 飞行符文功能
 * 提供一个物品，持有时可以在生存模式下飞行
 */
public class FlyingRuneFeature implements Feature {
    private static final String ID = "flying_rune";
    private static final String DISPLAY_NAME = "飞行符文";
    private static final String DESCRIPTION = "持有时可以在生存模式下飞行的神奇符文";
    
    // 仅用于兼容旧引用：不再在此类中持有物品实例
    // 事件处理器
    private FlyingRuneEventHandler eventHandler;
    
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
            LoosePrincesTool.LOGGER.info("飞行符文功能已禁用，跳过初始化");
            return;
        }
        
        LoosePrincesTool.LOGGER.info("初始化飞行符文功能");
        
        // 注册飞行符文物品（注册层）
        FlyingRuneRegistrar.register();
        
        // 注册事件监听器
        registerEventListeners();
        
        LoosePrincesTool.LOGGER.info("飞行符文功能初始化完成");
    }
    
    @Override
    public boolean isEnabled() {
        return ConfigManager.getInstance().isFeatureEnabled(ID);
    }
    
    @Override
    public FeatureConfig getDefaultConfig() {
        FeatureConfig config = new FeatureConfig(true);
        
        // 添加默认配置选项
        config.setOption("allowInNether", true);  // 是否允许在下界使用
        config.setOption("allowInEnd", true);     // 是否允许在末地使用
        config.setOption("preventFallDamage", true); // 是否防止摔落伤害
        config.setOption("requireInInventory", true); // 是否需要在背包中（而不是手持）
        
        return config;
    }
    
    /**
     * 注册事件监听器
     */
    private void registerEventListeners() {
        eventHandler = new FlyingRuneEventHandler();
        eventHandler.registerEvents();
    }
    
    /**
     * 静态方法用于ItemGroup注册
     * @return 已注册的飞行符文物品实例
     */
    public static Item getStaticFlyingRune() {
        return com.tool.looseprince.register.FlyingRuneRegistrar.get();
    }
    
    /**
     * 检查是否允许在指定维度使用飞行符文
     * @param dimensionId 维度ID
     * @return 如果允许返回true，否则返回false
     */
    public boolean isAllowedInDimension(String dimensionId) {
        FeatureConfig config = getConfig();
        if (config == null) {
            return true; // 如果没有配置，默认允许
        }
        
        switch (dimensionId) {
            case "minecraft:the_nether":
                return config.getBooleanOption("allowInNether", true);
            case "minecraft:the_end":
                return config.getBooleanOption("allowInEnd", true);
            default:
                return true; // 主世界和其他维度默认允许
        }
    }
    
    /**
     * 检查是否应该防止摔落伤害
     * @return 如果应该防止返回true，否则返回false
     */
    public boolean shouldPreventFallDamage() {
        FeatureConfig config = getConfig();
        return config != null && config.getBooleanOption("preventFallDamage", true);
    }
    
    /**
     * 检查是否需要符文在背包中（而不是手持）
     * @return 如果需要在背包中返回true，否则返回false
     */
    public boolean requireInInventory() {
        FeatureConfig config = getConfig();
        return config == null || config.getBooleanOption("requireInInventory", true);
    }
    
    @Override
    public void onEnable() {
        LoosePrincesTool.LOGGER.info("飞行符文功能已启用");
    }
    
    @Override
    public void onDisable() {
        LoosePrincesTool.LOGGER.info("飞行符文功能已禁用");
    }
    
    @Override
    public String getVersion() {
        return "1.0.0";
    }
}