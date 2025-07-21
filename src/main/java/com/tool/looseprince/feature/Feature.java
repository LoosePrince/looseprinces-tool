package com.tool.looseprince.feature;

import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.config.FeatureConfig;

import java.util.Collections;
import java.util.List;

/**
 * 功能接口
 * 所有可配置功能必须实现的接口，定义功能的生命周期方法
 */
public interface Feature {
    /**
     * 获取功能的唯一标识符
     * @return 功能ID
     */
    String getId();
    
    /**
     * 初始化功能
     * 在功能启用时调用，用于注册物品、事件监听器等
     */
    void initialize();
    
    /**
     * 检查功能是否启用
     * @return 如果功能启用返回true，否则返回false
     */
    boolean isEnabled();
    
    /**
     * 获取功能的默认配置
     * @return 默认的功能配置对象
     */
    FeatureConfig getDefaultConfig();
    
    /**
     * 获取功能的显示名称
     * @return 功能的显示名称
     */
    default String getDisplayName() {
        return getId();
    }
    
    /**
     * 获取功能的描述
     * @return 功能描述
     */
    default String getDescription() {
        return "无描述";
    }
    
    /**
     * 获取功能的当前配置
     * @return 当前的功能配置对象
     */
    default FeatureConfig getConfig() {
        return ConfigManager.getInstance().getFeatureConfig(getId());
    }
    
    /**
     * 获取功能依赖的其他功能ID列表
     * @return 依赖的功能ID列表
     */
    default List<String> getDependencies() {
        return Collections.emptyList();
    }
    
    /**
     * 功能启用时调用
     * 在初始化之前调用，可用于准备资源
     */
    default void onEnable() {
        // 默认实现为空
    }
    
    /**
     * 功能禁用时调用
     * 可用于清理资源
     */
    default void onDisable() {
        // 默认实现为空
    }
    
    /**
     * 游戏世界加载完成时调用
     * 可用于执行需要在世界加载后进行的操作
     */
    default void onWorldLoaded() {
        // 默认实现为空
    }
    
    /**
     * 游戏世界卸载时调用
     * 可用于保存数据或清理资源
     */
    default void onWorldUnloaded() {
        // 默认实现为空
    }
    
    /**
     * 检查功能是否可用
     * 除了配置启用外，可能还有其他条件（如依赖项）
     * @return 如果功能可用返回true，否则返回false
     */
    default boolean isAvailable() {
        return isEnabled();
    }
    
    /**
     * 获取功能的版本
     * @return 功能版本
     */
    default String getVersion() {
        return "1.0.0";
    }
    
    /**
     * 获取功能的作者
     * @return 功能作者
     */
    default String getAuthor() {
        return "LoosePrince";
    }
}