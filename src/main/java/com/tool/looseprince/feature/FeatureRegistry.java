package com.tool.looseprince.feature;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * 功能注册器
 * 管理所有可配置功能的注册和初始化
 */
public class FeatureRegistry {
    private static FeatureRegistry instance;
    private final Map<String, Feature> features;
    private final List<Feature> initializedFeatures;
    private final Map<String, Set<String>> dependencyGraph;
    private final Map<String, Set<String>> reverseDependencyGraph;
    
    private FeatureRegistry() {
        this.features = new HashMap<>();
        this.initializedFeatures = new ArrayList<>();
        this.dependencyGraph = new HashMap<>();
        this.reverseDependencyGraph = new HashMap<>();
    }
    
    public static FeatureRegistry getInstance() {
        if (instance == null) {
            instance = new FeatureRegistry();
        }
        return instance;
    }
    
    /**
     * 注册功能
     * @param feature 要注册的功能
     * @return 如果注册成功返回true，否则返回false
     */
    public boolean registerFeature(Feature feature) {
        if (feature == null) {
            LoosePrincesTool.LOGGER.warn("尝试注册空功能");
            return false;
        }
        
        String featureId = feature.getId();
        if (featureId == null || featureId.isEmpty()) {
            LoosePrincesTool.LOGGER.warn("功能ID不能为空");
            return false;
        }
        
        if (features.containsKey(featureId)) {
            LoosePrincesTool.LOGGER.warn("功能 {} 已经注册", featureId);
            return false;
        }
        
        features.put(featureId, feature);
        LoosePrincesTool.LOGGER.info("注册功能: {}", featureId);
        
        // 处理依赖关系
        List<String> dependencies = feature.getDependencies();
        if (dependencies != null && !dependencies.isEmpty()) {
            Set<String> dependencySet = new HashSet<>(dependencies);
            dependencyGraph.put(featureId, dependencySet);
            
            // 更新反向依赖图
            for (String dependency : dependencies) {
                reverseDependencyGraph.computeIfAbsent(dependency, k -> new HashSet<>()).add(featureId);
            }
            
            LoosePrincesTool.LOGGER.info("功能 {} 依赖于: {}", featureId, String.join(", ", dependencies));
        }
        
        // 确保配置管理器中有该功能的配置
        ConfigManager configManager = ConfigManager.getInstance();
        if (configManager.getFeatureConfig(featureId) == null) {
            configManager.setFeatureConfig(featureId, feature.getDefaultConfig());
            configManager.saveConfig();
        }
        
        return true;
    }
    
    /**
     * 初始化所有启用的功能
     */
    public void initializeEnabledFeatures() {
        LoosePrincesTool.LOGGER.info("开始初始化启用的功能");
        
        // 清空已初始化功能列表
        initializedFeatures.clear();
        
        // 检查依赖关系并按顺序初始化功能
        List<Feature> orderedFeatures = getInitializationOrder();
        
        for (Feature feature : orderedFeatures) {
            try {
                if (feature.isEnabled()) {
                    // 检查依赖是否都已启用
                    boolean dependenciesMet = checkDependencies(feature);
                    
                    if (dependenciesMet) {
                        LoosePrincesTool.LOGGER.info("启用功能: {}", feature.getId());
                        feature.onEnable();
                        
                        LoosePrincesTool.LOGGER.info("初始化功能: {}", feature.getId());
                        feature.initialize();
                        
                        initializedFeatures.add(feature);
                        LoosePrincesTool.LOGGER.info("功能 {} 初始化成功", feature.getId());
                    } else {
                        LoosePrincesTool.LOGGER.warn("功能 {} 的依赖未满足，跳过初始化", feature.getId());
                    }
                } else {
                    LoosePrincesTool.LOGGER.info("功能 {} 已禁用，跳过初始化", feature.getId());
                }
            } catch (Exception e) {
                LoosePrincesTool.LOGGER.error("初始化功能 {} 失败", feature.getId(), e);
            }
        }
        
        LoosePrincesTool.LOGGER.info("功能初始化完成，共初始化 {} 个功能", initializedFeatures.size());
    }
    
    /**
     * 禁用所有已初始化的功能
     */
    public void disableAllFeatures() {
        LoosePrincesTool.LOGGER.info("开始禁用所有功能");
        
        // 按照初始化的相反顺序禁用功能
        for (int i = initializedFeatures.size() - 1; i >= 0; i--) {
            Feature feature = initializedFeatures.get(i);
            try {
                LoosePrincesTool.LOGGER.info("禁用功能: {}", feature.getId());
                feature.onDisable();
            } catch (Exception e) {
                LoosePrincesTool.LOGGER.error("禁用功能 {} 失败", feature.getId(), e);
            }
        }
        
        initializedFeatures.clear();
        LoosePrincesTool.LOGGER.info("所有功能已禁用");
    }
    
    /**
     * 获取功能初始化顺序
     * 使用拓扑排序确保依赖项在被依赖项之前初始化
     * @return 排序后的功能列表
     */
    private List<Feature> getInitializationOrder() {
        List<Feature> result = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> inProgress = new HashSet<>();
        
        // 检测循环依赖并进行拓扑排序
        for (String featureId : features.keySet()) {
            if (!visited.contains(featureId)) {
                if (!topologicalSort(featureId, visited, inProgress, result)) {
                    LoosePrincesTool.LOGGER.error("检测到循环依赖，涉及功能: {}", featureId);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 拓扑排序辅助方法
     * @param featureId 当前功能ID
     * @param visited 已访问的功能ID集合
     * @param inProgress 正在处理的功能ID集合（用于检测循环）
     * @param result 排序结果
     * @return 如果没有循环依赖返回true，否则返回false
     */
    private boolean topologicalSort(String featureId, Set<String> visited, Set<String> inProgress, List<Feature> result) {
        if (inProgress.contains(featureId)) {
            return false; // 检测到循环依赖
        }
        
        if (visited.contains(featureId)) {
            return true; // 已经处理过
        }
        
        inProgress.add(featureId);
        
        // 处理依赖
        Set<String> dependencies = dependencyGraph.get(featureId);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                if (!topologicalSort(dependency, visited, inProgress, result)) {
                    return false;
                }
            }
        }
        
        inProgress.remove(featureId);
        visited.add(featureId);
        
        Feature feature = features.get(featureId);
        if (feature != null) {
            result.add(feature);
        }
        
        return true;
    }
    
    /**
     * 检查功能的依赖是否都已启用
     * @param feature 要检查的功能
     * @return 如果所有依赖都已启用返回true，否则返回false
     */
    private boolean checkDependencies(Feature feature) {
        List<String> dependencies = feature.getDependencies();
        if (dependencies == null || dependencies.isEmpty()) {
            return true;
        }
        
        for (String dependencyId : dependencies) {
            Feature dependency = features.get(dependencyId);
            if (dependency == null) {
                LoosePrincesTool.LOGGER.warn("功能 {} 依赖的功能 {} 未注册", feature.getId(), dependencyId);
                return false;
            }
            
            if (!dependency.isEnabled()) {
                LoosePrincesTool.LOGGER.warn("功能 {} 依赖的功能 {} 未启用", feature.getId(), dependencyId);
                return false;
            }
            
            if (!initializedFeatures.contains(dependency)) {
                LoosePrincesTool.LOGGER.warn("功能 {} 依赖的功能 {} 未初始化", feature.getId(), dependencyId);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 获取注册的功能
     * @param featureId 功能ID
     * @return 功能实例，如果不存在返回null
     */
    public Feature getFeature(String featureId) {
        return features.get(featureId);
    }
    
    /**
     * 获取所有注册的功能
     * @return 功能映射表
     */
    public Map<String, Feature> getAllFeatures() {
        return new HashMap<>(features);
    }
    
    /**
     * 获取所有已初始化的功能
     * @return 已初始化的功能列表
     */
    public List<Feature> getInitializedFeatures() {
        return new ArrayList<>(initializedFeatures);
    }
    
    /**
     * 检查功能是否已注册
     * @param featureId 功能ID
     * @return 如果已注册返回true，否则返回false
     */
    public boolean isFeatureRegistered(String featureId) {
        return features.containsKey(featureId);
    }
    
    /**
     * 获取注册的功能数量
     * @return 功能数量
     */
    public int getFeatureCount() {
        return features.size();
    }
    
    /**
     * 获取依赖于指定功能的所有功能
     * @param featureId 功能ID
     * @return 依赖于该功能的功能ID集合
     */
    public Set<String> getDependentFeatures(String featureId) {
        Set<String> dependents = reverseDependencyGraph.get(featureId);
        return dependents != null ? new HashSet<>(dependents) : new HashSet<>();
    }
    
    /**
     * 重新加载功能配置
     * 禁用所有功能，重新加载配置，然后重新初始化启用的功能
     */
    public void reloadFeatures() {
        LoosePrincesTool.LOGGER.info("重新加载功能配置");
        
        // 禁用所有功能
        disableAllFeatures();
        
        // 重新加载配置
        ConfigManager.getInstance().loadConfig();
        
        // 重新初始化启用的功能
        initializeEnabledFeatures();
    }
    
    /**
     * 通知所有已初始化的功能世界已加载
     */
    public void notifyWorldLoaded() {
        for (Feature feature : initializedFeatures) {
            try {
                feature.onWorldLoaded();
            } catch (Exception e) {
                LoosePrincesTool.LOGGER.error("功能 {} 处理世界加载事件失败", feature.getId(), e);
            }
        }
    }
    
    /**
     * 通知所有已初始化的功能世界已卸载
     */
    public void notifyWorldUnloaded() {
        for (Feature feature : initializedFeatures) {
            try {
                feature.onWorldUnloaded();
            } catch (Exception e) {
                LoosePrincesTool.LOGGER.error("功能 {} 处理世界卸载事件失败", feature.getId(), e);
            }
        }
    }
}