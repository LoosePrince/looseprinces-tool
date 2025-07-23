package com.tool.looseprince.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tool.looseprince.LoosePrincesTool;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * 配置管理器
 * 负责加载、解析和保存配置文件，提供配置访问API
 */
public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "looseprinces-tool.json";
    private static ConfigManager instance;
    
    private Config config;
    private File configFile;
    
    private ConfigManager() {
        // 获取配置文件路径
        File configDir = FabricLoader.getInstance().getConfigDir().toFile();
        this.configFile = new File(configDir, CONFIG_FILE_NAME);
        loadConfig();
    }
    
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    
    /**
     * 加载配置文件
     */
    public void loadConfig() {
        if (!configFile.exists()) {
            LoosePrincesTool.LOGGER.info("配置文件不存在，创建默认配置");
            config = createDefaultConfig();
            saveConfig();
            return;
        }
        
        try (Reader reader = new FileReader(configFile)) {
            config = GSON.fromJson(reader, Config.class);
            if (config == null) {
                LoosePrincesTool.LOGGER.warn("配置文件为空，使用默认配置");
                config = createDefaultConfig();
                saveConfig();
            } else {
                LoosePrincesTool.LOGGER.info("成功加载配置文件");
                validateAndUpdateConfig();
            }
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("加载配置文件失败，使用默认配置", e);
            config = createDefaultConfig();
            saveConfig();
        }
    }
    
    /**
     * 保存配置到文件
     */
    public void saveConfig() {
        try {
            // 确保配置目录存在
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            
            try (Writer writer = new FileWriter(configFile)) {
                GSON.toJson(config, writer);
                LoosePrincesTool.LOGGER.info("配置文件已保存");
            }
        } catch (IOException e) {
            LoosePrincesTool.LOGGER.error("保存配置文件失败", e);
        }
    }
    
    /**
     * 检查功能是否启用
     */
    public boolean isFeatureEnabled(String featureId) {
        FeatureConfig featureConfig = config.getFeatureConfig(featureId);
        return featureConfig != null && featureConfig.isEnabled();
    }
    
    /**
     * 获取功能的配置
     */
    public FeatureConfig getFeatureConfig(String featureId) {
        return config.getFeatureConfig(featureId);
    }
    
    /**
     * 设置功能配置
     */
    public void setFeatureConfig(String featureId, FeatureConfig featureConfig) {
        config.setFeatureConfig(featureId, featureConfig);
    }
    
    /**
     * 获取完整配置对象
     */
    public Config getConfig() {
        return config;
    }
    
    /**
     * 创建默认配置
     */
    private Config createDefaultConfig() {
        Config defaultConfig = new Config("1.0.0");
        
        // 添加飞行符文的默认配置
        FeatureConfig flyingRuneConfig = new FeatureConfig(true);
        flyingRuneConfig.setOption("allowInNether", true);
        flyingRuneConfig.setOption("allowInEnd", true);
        defaultConfig.setFeatureConfig("flying_rune", flyingRuneConfig);
        
        // 添加绑定附魔的默认配置
        FeatureConfig bindingEnchantmentConfig = new FeatureConfig(true);
        bindingEnchantmentConfig.setOption("preventDrop", true);
        bindingEnchantmentConfig.setOption("affectAllItems", true);
        bindingEnchantmentConfig.setOption("maxLevel", 1);
        defaultConfig.setFeatureConfig("binding_enchantment", bindingEnchantmentConfig);
        
        return defaultConfig;
    }
    
    /**
     * 验证和更新配置
     */
    private void validateAndUpdateConfig() {
        boolean needsSave = false;
        
        // 检查版本
        if (config.getVersion() == null) {
            config.setVersion("1.0.0");
            needsSave = true;
        }
        
        // 确保飞行符文配置存在
        if (config.getFeatureConfig("flying_rune") == null) {
            FeatureConfig flyingRuneConfig = new FeatureConfig(true);
            flyingRuneConfig.setOption("allowInNether", true);
            flyingRuneConfig.setOption("allowInEnd", true);
            config.setFeatureConfig("flying_rune", flyingRuneConfig);
            needsSave = true;
        }
        
        // 确保绑定附魔配置存在
        if (config.getFeatureConfig("binding_enchantment") == null) {
            FeatureConfig bindingEnchantmentConfig = new FeatureConfig(true);
            bindingEnchantmentConfig.setOption("preventDrop", true);
            bindingEnchantmentConfig.setOption("affectAllItems", true);
            bindingEnchantmentConfig.setOption("maxLevel", 1);
            config.setFeatureConfig("binding_enchantment", bindingEnchantmentConfig);
            needsSave = true;
        }
        
        if (needsSave) {
            saveConfig();
        }
    }
}