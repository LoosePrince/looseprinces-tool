package com.tool.looseprince.config;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能配置类
 * 存储单个功能的启用状态和特定配置选项
 */
public class FeatureConfig {
    // 功能是否启用
    @SerializedName("enabled")
    private boolean enabled;
    
    // 功能特定的配置选项
    @SerializedName("options")
    private Map<String, Object> options;
    
    public FeatureConfig() {
        this.enabled = false;
        this.options = new HashMap<>();
    }
    
    public FeatureConfig(boolean enabled) {
        this.enabled = enabled;
        this.options = new HashMap<>();
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Map<String, Object> getOptions() {
        return options;
    }
    
    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
    
    public Object getOption(String key) {
        return options.get(key);
    }
    
    public void setOption(String key, Object value) {
        options.put(key, value);
    }
    
    public boolean getBooleanOption(String key, boolean defaultValue) {
        Object value = options.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
    
    public String getStringOption(String key, String defaultValue) {
        Object value = options.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }
    
    /**
     * 获取整数类型的配置选项
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public int getIntOption(String key, int defaultValue) {
        Object value = options.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }
    
    /**
     * 获取双精度浮点数类型的配置选项
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值或默认值
     */
    public double getDoubleOption(String key, double defaultValue) {
        Object value = options.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
    
    /**
     * 检查是否包含指定的配置选项
     * @param key 配置键
     * @return 如果包含返回true，否则返回false
     */
    public boolean hasOption(String key) {
        return options.containsKey(key);
    }
    
    /**
     * 创建一个功能配置的副本
     * @return 新的FeatureConfig实例
     */
    public FeatureConfig copy() {
        FeatureConfig copy = new FeatureConfig(this.enabled);
        copy.options.putAll(this.options);
        return copy;
    }
}