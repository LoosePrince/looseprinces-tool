package com.tool.looseprince.config;

import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

/**
 * 模组配置类
 * 存储模组版本和各功能的配置信息
 */
public class Config {
    // 模组版本，用于配置兼容性检查
    @SerializedName("version")
    private String version;
    
    // 功能配置映射表
    @SerializedName("features")
    private Map<String, FeatureConfig> features;
    
    public Config() {
        this.version = "1.0.3";
        this.features = new HashMap<>();
    }
    
    public Config(String version) {
        this.version = version;
        this.features = new HashMap<>();
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Map<String, FeatureConfig> getFeatures() {
        return features;
    }
    
    public void setFeatures(Map<String, FeatureConfig> features) {
        this.features = features;
    }
    
    public FeatureConfig getFeatureConfig(String featureId) {
        return features.get(featureId);
    }
    
    public void setFeatureConfig(String featureId, FeatureConfig config) {
        features.put(featureId, config);
    }
    
    /**
     * 检查功能是否启用
     * @param featureId 功能ID
     * @return 如果功能存在且启用返回true，否则返回false
     */
    public boolean isFeatureEnabled(String featureId) {
        FeatureConfig config = features.get(featureId);
        return config != null && config.isEnabled();
    }
    
    /**
     * 确保指定功能的配置存在，如果不存在则创建默认配置
     * @param featureId 功能ID
     * @param defaultConfig 默认配置
     */
    public void ensureFeatureConfig(String featureId, FeatureConfig defaultConfig) {
        if (!features.containsKey(featureId)) {
            features.put(featureId, defaultConfig);
        }
    }
    
    /**
     * 验证配置的完整性
     * @return 如果配置有效返回true，否则返回false
     */
    public boolean isValid() {
        return version != null && !version.isEmpty() && features != null;
    }
}