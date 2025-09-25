package com.tool.looseprince.config;

import com.tool.looseprince.LoosePrincesTool;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置管理器
 * 负责加载、解析和保存配置文件，提供配置访问API
 */
public class ConfigManager {
    private static final String CONFIG_FILE_NAME = "looseprinces-tool.toml";
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
     * 加载配置文件（TOML）
     */
    public void loadConfig() {
        if (!configFile.exists()) {
            LoosePrincesTool.LOGGER.info("配置文件不存在，创建默认配置 (TOML)");
            config = createDefaultConfig();
            saveConfig();
            return;
        }

        try {
            this.config = readConfigFromToml();
            if (this.config == null) {
                LoosePrincesTool.LOGGER.warn("配置文件为空或无效，使用默认配置");
                this.config = createDefaultConfig();
            }
            validateAndUpdateConfig();
            LoosePrincesTool.LOGGER.info("成功加载 TOML 配置文件");
        } catch (Exception e) {
            LoosePrincesTool.LOGGER.error("加载 TOML 配置失败，使用默认配置", e);
            this.config = createDefaultConfig();
            saveConfig();
        }
    }

    /**
     * 保存配置到 TOML 文件（带注释）
     */
    public void saveConfig() {
        try {
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            writeConfigToToml();
            LoosePrincesTool.LOGGER.info("配置文件已保存 (TOML)");
        } catch (Exception e) {
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
        Config defaultConfig = new Config("1.0.6");

        // 添加飞行符文的默认配置
        FeatureConfig flyingRuneConfig = new FeatureConfig(true);
        flyingRuneConfig.setOption("allowInNether", true);
        flyingRuneConfig.setOption("allowInEnd", true);
        flyingRuneConfig.setOption("preventFallDamage", true);
        flyingRuneConfig.setOption("requireInInventory", true);
        defaultConfig.setFeatureConfig("flying_rune", flyingRuneConfig);

        // 绑定附魔默认配置
        FeatureConfig bindingEnchantmentConfig = new FeatureConfig(true);
        bindingEnchantmentConfig.setOption("preventDrop", true);
        bindingEnchantmentConfig.setOption("affectAllItems", true);
        bindingEnchantmentConfig.setOption("maxLevel", 1);
        defaultConfig.setFeatureConfig("binding_enchantment", bindingEnchantmentConfig);

        // 公平对决默认配置
        FeatureConfig fairDuelConfig = new FeatureConfig(true);
        fairDuelConfig.setOption("damageRatio", 1.0);
        defaultConfig.setFeatureConfig("fair_duel", fairDuelConfig);

        // 灵魂绑定默认配置
        FeatureConfig soulBindingConfig = new FeatureConfig(true);
        soulBindingConfig.setOption("preventPickup", true);
        soulBindingConfig.setOption("preventContainerTake", true);
        soulBindingConfig.setOption("showOwnerTooltip", true);
        defaultConfig.setFeatureConfig("soul_binding", soulBindingConfig);

        // 神格系统默认配置
        FeatureConfig divinityConfig = new FeatureConfig(true);
        divinityConfig.setOption("creatorCooldownSeconds", 900);
        divinityConfig.setOption("creatorGiveAmount", 1);
        defaultConfig.setFeatureConfig("divinity", divinityConfig);

        return defaultConfig;
    }

    /**
     * 验证和更新配置
     */
    private void validateAndUpdateConfig() {
        boolean needsSave = false;

        if (config.getVersion() == null) {
            config.setVersion("1.0.6");
            needsSave = true;
        }

        if (config.getFeatureConfig("flying_rune") == null) {
            FeatureConfig flyingRuneConfig = new FeatureConfig(true);
            flyingRuneConfig.setOption("allowInNether", true);
            flyingRuneConfig.setOption("allowInEnd", true);
            flyingRuneConfig.setOption("preventFallDamage", true);
            flyingRuneConfig.setOption("requireInInventory", true);
            config.setFeatureConfig("flying_rune", flyingRuneConfig);
            needsSave = true;
        }

        if (config.getFeatureConfig("binding_enchantment") == null) {
            FeatureConfig bindingEnchantmentConfig = new FeatureConfig(true);
            bindingEnchantmentConfig.setOption("preventDrop", true);
            bindingEnchantmentConfig.setOption("affectAllItems", true);
            bindingEnchantmentConfig.setOption("maxLevel", 1);
            config.setFeatureConfig("binding_enchantment", bindingEnchantmentConfig);
            needsSave = true;
        }

        if (config.getFeatureConfig("soul_binding") == null) {
            FeatureConfig soulBindingConfig = new FeatureConfig(true);
            soulBindingConfig.setOption("preventPickup", true);
            soulBindingConfig.setOption("preventContainerTake", true);
            soulBindingConfig.setOption("showOwnerTooltip", true);
            config.setFeatureConfig("soul_binding", soulBindingConfig);
            needsSave = true;
        }

        FeatureConfig divinity = config.getFeatureConfig("divinity");
        if (divityNullOrMissing(divinity)) {
            FeatureConfig divinityConfig = new FeatureConfig(true);
            divinityConfig.setOption("creatorCooldownSeconds", 900);
            divinityConfig.setOption("creatorGiveAmount", 1);
            config.setFeatureConfig("divinity", divinityConfig);
            needsSave = true;
        } else {
            boolean changed = false;
            if (!divinity.hasOption("creatorCooldownSeconds")) { divinity.setOption("creatorCooldownSeconds", 900); changed = true; }
            if (!divinity.hasOption("creatorGiveAmount")) { divinity.setOption("creatorGiveAmount", 1); changed = true; }
            if (changed) { needsSave = true; }
        }

        if (needsSave) {
            saveConfig();
        }
    }

    private boolean divityNullOrMissing(FeatureConfig cfg) {
        return cfg == null || cfg.getOptions() == null;
    }

    private Config readConfigFromToml() throws IOException {
        Object toml = buildTomlConfig(configFile, true);
        invokeNoArgs(toml, "load");

        String version = (String) invoke(toml, "getOrElse", new Class[]{Object.class, Object.class}, new Object[]{"version", "1.0.6"});
        Config loaded = new Config(version);

        Object featuresCfg = invoke(toml, "get", new Class[]{Object.class}, new Object[]{"features"});
        if (featuresCfg != null) {
            Map<String, Object> featureMap = valueMapOrNull(featuresCfg);
            if (featureMap != null) {
                for (Map.Entry<String, Object> entry : featureMap.entrySet()) {
                    String featureId = entry.getKey();
                    Object fcfg = entry.getValue();

                    boolean enabled = Boolean.TRUE.equals(invoke(fcfg, "get", new Class[]{Object.class}, new Object[]{"enabled"}));
                    FeatureConfig fc = new FeatureConfig(enabled);

                    Object optionsCfg = invoke(fcfg, "get", new Class[]{Object.class}, new Object[]{"options"});
                    Map<String, Object> opts = valueMapOrNull(optionsCfg);
                    if (opts != null) {
                        fc.setOptions(new HashMap<>(opts));
                    }

                    loaded.setFeatureConfig(featureId, fc);
                }
            }
        }

        return loaded;
    }

    private void writeConfigToToml() {
        Object toml = buildTomlConfig(configFile, true);

        // 顶部注释
        invoke(toml, "setComment", new Class[]{Object.class, String.class}, new Object[]{"version",
                "LoosePrince's Tool 配置文件\n" +
                        "格式: TOML，可添加注释。\n" +
                        "修改后可热重载（若功能支持），或重启游戏生效。"});
        invoke(toml, "set", new Class[]{Object.class, Object.class}, new Object[]{"version", config.getVersion()});

        // features 段注释
        invoke(toml, "setComment", new Class[]{Object.class, String.class}, new Object[]{"features", "各功能模块的启用开关与选项"});

        // 逐功能写入
        for (Map.Entry<String, FeatureConfig> entry : config.getFeatures().entrySet()) {
            String id = entry.getKey();
            FeatureConfig fc = entry.getValue();
            String base = "features." + id;

            invoke(toml, "set", new Class[]{Object.class, Object.class}, new Object[]{base + ".enabled", fc.isEnabled()});
            invoke(toml, "setComment", new Class[]{Object.class, String.class}, new Object[]{base + ".enabled", featureEnabledComment(id)});

            if (fc.getOptions() != null) {
                for (Map.Entry<String, Object> opt : fc.getOptions().entrySet()) {
                    String optPath = base + ".options." + opt.getKey();
                    invoke(toml, "set", new Class[]{Object.class, Object.class}, new Object[]{optPath, opt.getValue()});
                    String c = optionComment(id, opt.getKey());
                    if (c != null && !c.isEmpty()) {
                        invoke(toml, "setComment", new Class[]{Object.class, String.class}, new Object[]{optPath, c});
                    }
                }
            }
        }

        invokeNoArgs(toml, "save");
        invokeNoArgs(toml, "close");
    }

    private Object buildTomlConfig(File file, boolean sync) {
        try {
            Class<?> cfc = Class.forName("com.electronwill.nightconfig.core.file.CommentedFileConfig");
            Object builder = cfc.getMethod("builder", File.class).invoke(null, file);
            // preserveInsertionOrder
            builder = builder.getClass().getMethod("preserveInsertionOrder").invoke(builder);
            if (sync) {
                try { builder = builder.getClass().getMethod("sync").invoke(builder); } catch (NoSuchMethodException ignored) {}
            }
            return builder.getClass().getMethod("build").invoke(builder);
        } catch (Exception e) {
            throw new RuntimeException("创建 TOML 配置失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> valueMapOrNull(Object cfg) {
        if (cfg == null) return null;
        try {
            Object map = cfg.getClass().getMethod("valueMap").invoke(cfg);
            return (Map<String, Object>) map;
        } catch (Exception e) {
            return null;
        }
    }

    private Object invoke(Object target, String method, Class<?>[] types, Object[] args) {
        try {
            return target.getClass().getMethod(method, types).invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("调用方法失败: " + method, e);
        }
    }

    private void invokeNoArgs(Object target, String method) {
        try {
            target.getClass().getMethod(method).invoke(target);
        } catch (Exception e) {
            throw new RuntimeException("调用方法失败: " + method, e);
        }
    }

    private String featureEnabledComment(String featureId) {
        switch (featureId) {
            case "flying_rune":
                return "飞行符文：启用后持有可在生存飞行";
            case "binding_enchantment":
                return "绑定附魔：启用后提供死亡不掉落等功能";
            case "soul_binding":
                return "灵魂绑定：启用后记录拥有者并限制拾取/容器移动";
            case "fair_duel":
                return "公平对决：启用后按比例换算所受伤害";
            case "divinity":
                return "神格系统：启用后可获得各类神格效果";
            default:
                return "功能启用开关";
        }
    }

    private String optionComment(String featureId, String optionKey) {
        switch (featureId) {
            case "flying_rune":
                switch (optionKey) {
                    case "allowInNether": return "是否允许在下界使用 (默认: true)";
                    case "allowInEnd": return "是否允许在末地使用 (默认: true)";
                    case "preventFallDamage": return "是否防止摔落伤害 (默认: true)";
                    case "requireInInventory": return "true=在背包即可飞，false=需手持 (默认: true)";
                }
                break;
            case "binding_enchantment":
                switch (optionKey) {
                    case "preventDrop": return "是否防止掉落 (默认: true)";
                    case "affectAllItems": return "是否影响所有物品 (默认: true)";
                    case "maxLevel": return "附魔最大等级 (默认: 1)";
                }
                break;
            case "soul_binding":
                switch (optionKey) {
                    case "preventPickup": return "是否阻止其他玩家拾取 (默认: true)";
                    case "preventContainerTake": return "是否阻止从容器取出 (默认: true)";
                    case "showOwnerTooltip": return "是否显示拥有者信息 (默认: true)";
                    case "level2TeleportSeconds": return "II级: 掉落后自动回到拥有者秒数 (默认: 30)";
                    case "lavaImmune": return "II级: 岩浆免疫 (默认: true)";
                    case "voidDestroyable": return "II级: 是否允许被虚空销毁 (默认: false)";
                }
                break;
            case "fair_duel":
                if ("damageRatio".equals(optionKey)) {
                    return "伤害转换比例 1.0=100% 0.5=50% 2.0=200% (默认:1.0)";
                }
                break;
            case "divinity":
                switch (optionKey) {
                    case "creatorCooldownSeconds": return "造物主冷却秒数 (默认: 900)";
                    case "creatorGiveAmount": return "获取/扣除数量 (-64..640, 默认:1; 负数为扣除)";
                }
                break;
        }
        return "";
    }
}