package com.tool.looseprince;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tool.looseprince.config.ConfigManager;
import com.tool.looseprince.feature.FeatureRegistry;
import com.tool.looseprince.feature.FlyingRuneFeature;
import com.tool.looseprince.feature.BindingEnchantmentFeature;
import com.tool.looseprince.feature.SoulBindingFeature;
import com.tool.looseprince.feature.FairDuelFeature;
import com.tool.looseprince.feature.DivinityFeature;
import com.tool.looseprince.registry.ModItemGroups;
import com.tool.looseprince.register.CodexRegistrar;
import com.tool.looseprince.network.CreatorNetwork;
import com.tool.looseprince.command.DivinityCommands;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.List;

/**
 * LoosePrince's Tool 模组主类
 * 负责模组的初始化和功能注册
 */
public class LoosePrincesTool implements ModInitializer {
	public static final String MOD_ID = "looseprinces-tool";

	// 日志记录器，用于向控制台和日志文件写入文本
	// 使用模组ID作为日志记录器的名称是最佳实践
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// 缓存已加载的本模组进度ID（path），用于事件授予前校验
	private static List<String> LOADED_ADVANCEMENT_IDS = new ArrayList<>();

	@Override
	public void onInitialize() {
		// 当Minecraft处于模组加载就绪状态时运行此代码
		// 但是，某些内容（如资源）可能仍未初始化
		// 请谨慎进行

		LOGGER.info("LoosePrince's Tool 模组开始初始化");

		// 初始化配置管理器
		initializeConfig();

		// 注册创造模式分页
		ModItemGroups.register();

		// 注册神秘典籍与手稿
		CodexRegistrar.register();

		// 注册所有功能
		registerFeatures();

		// 初始化启用的功能
		initializeFeatures();

		// 冷却tick
		com.tool.looseprince.event.CooldownTickHandler.register();

		// 典籍发放/持久化事件
		com.tool.looseprince.event.CodexEventHandler.register();

		// 物品禁用/冷却拦截
		com.tool.looseprince.event.ItemRestrictionEventHandler.register();

		// 注册服务器端网络处理
		CreatorNetwork.registerServerReceiver();

		// 注册命令
		DivinityCommands.register();

		LOGGER.info("LoosePrince's Tool 模组初始化完成");

		// 在服务器启动后，确认我们的成就入口(root)已注册，避免“missing advancement”
		ServerLifecycleEvents.SERVER_STARTED.register(server -> ensureAdvancementsLoaded(server));
	}

	private void ensureAdvancementsLoaded(MinecraftServer server) {
		try {
			Identifier root = Identifier.of(MOD_ID, "root");
			AdvancementEntry entry = server.getAdvancementLoader().get(root);
			if (entry == null) {
				LOGGER.warn("[Adv] root advancement not found at server start: {}", root);
			} else {
				LOGGER.info("[Adv] root advancement loaded: {}", root);
			}
			// 枚举本模组命名空间下的所有进度，便于排查
			List<String> ours = new ArrayList<>();
			for (AdvancementEntry e : server.getAdvancementLoader().getAdvancements()) {
				Identifier id = e.id();
				if (MOD_ID.equals(id.getNamespace())) {
					ours.add(id.getPath());
				}
			}
			LOGGER.info("[Adv] loaded entries in {}: {}", MOD_ID, ours);
			LOADED_ADVANCEMENT_IDS = ours;
		} catch (Exception e) {
			LOGGER.error("[Adv] ensureAdvancementsLoaded error", e);
		}
	}

	public static boolean isOurAdvancementsLoaded() {
		return LOADED_ADVANCEMENT_IDS != null && !LOADED_ADVANCEMENT_IDS.isEmpty();
	}

	/**
	 * 初始化配置管理器
	 */
	private void initializeConfig() {
		LOGGER.info("初始化配置管理器");
		ConfigManager.getInstance(); // 这会触发配置加载
	}

	/**
	 * 注册所有功能
	 */
	private void registerFeatures() {
		LOGGER.info("开始注册功能");
		FeatureRegistry registry = FeatureRegistry.getInstance();
		
		// 注册飞行符文功能
		registry.registerFeature(new FlyingRuneFeature());
		
		// 注册绑定附魔功能
		registry.registerFeature(new BindingEnchantmentFeature());
		
		// 注册灵魂绑定附魔功能
		registry.registerFeature(new SoulBindingFeature());

		// 注册公平对决功能
		registry.registerFeature(new FairDuelFeature());

		// 注册神格功能
		registry.registerFeature(new DivinityFeature());
		
		LOGGER.info("功能注册完成，共注册 {} 个功能", registry.getFeatureCount());
	}

	/**
	 * 初始化启用的功能
	 */
	private void initializeFeatures() {
		LOGGER.info("初始化启用的功能");
		FeatureRegistry.getInstance().initializeEnabledFeatures();
	}
}