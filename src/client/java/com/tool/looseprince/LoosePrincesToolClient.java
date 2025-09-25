package com.tool.looseprince;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.text.Text;
import com.tool.looseprince.util.SoulBindingUtils;
import com.tool.looseprince.feature.FeatureRegistry;
import com.tool.looseprince.feature.SoulBindingFeature;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import com.tool.looseprince.feature.DivinityFeature;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
 
import com.tool.looseprince.network.payload.CreatorRequestPayload;

public class LoosePrincesToolClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// 追加"已绑定至"Tooltip
		ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
			try {
				SoulBindingFeature feature = (SoulBindingFeature) FeatureRegistry.getInstance()
					.getFeature("soul_binding");
				if (feature != null && feature.isEnabled() && feature.shouldShowOwnerTooltip()) {
					Text extra = SoulBindingUtils.getBoundTooltip(stack);
					if (extra != null) {
						lines.add(extra);
					}
				}
			} catch (Exception e) {
				// 忽略错误，可能是功能尚未注册
			}
		});

		// 按键绑定：默认Z（可扩展到配置）
		KeyBinding creatorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.looseprinces-tool.creator.open",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_Z,
				"category.looseprinces-tool"
		));

		// 追加“造物主神格”动态Tooltip
		ItemTooltipCallback.EVENT.register((stack, ctx, type2, lines) -> {
			try {
				if (!(stack.getItem() instanceof com.tool.looseprince.item.CreatorDivinityItem)) return;
				// 读取配置
				com.tool.looseprince.config.FeatureConfig cfg = com.tool.looseprince.config.ConfigManager.getInstance().getFeatureConfig("divinity");
				int seconds = cfg != null ? cfg.getIntOption("creatorCooldownSeconds", 900) : 900;
				int amount = cfg != null ? cfg.getIntOption("creatorGiveAmount", 1) : 1;
				String modeKey = amount >= 0 ? "item.looseprinces-tool.creator_divinity.mode.give" : "item.looseprinces-tool.creator_divinity.mode.take";
				// 判断是否处于冷却（基于神力静默）
				boolean cooling = false;
				com.tool.looseprince.feature.DivinityFeature div = (com.tool.looseprince.feature.DivinityFeature) com.tool.looseprince.feature.FeatureRegistry.getInstance().getFeature("divinity");
				if (div != null && net.minecraft.client.MinecraftClient.getInstance().player != null && div.getDivineSilenceEffect() != null) {
					cooling = net.minecraft.client.MinecraftClient.getInstance().player.hasStatusEffect(div.getDivineSilenceEffect());
				}
				// 插入动态内容（放在标题后）
				int insertIndex = 1;
				if (!cooling) {
					lines.add(insertIndex++, Text.translatable("item.looseprinces-tool.creator_divinity.normal.title").formatted(net.minecraft.util.Formatting.GOLD));
					lines.add(insertIndex++, Text.translatable("item.looseprinces-tool.creator_divinity.normal.detail",
						Text.translatable("item.looseprinces-tool.creator_divinity.key"),
						Text.translatable(modeKey),
						Text.literal(String.valueOf(seconds))
					).formatted(net.minecraft.util.Formatting.GRAY));
					lines.add(insertIndex++, Text.empty());
					for (String ln : net.minecraft.text.Text.translatable("item.looseprinces-tool.creator_divinity.story.normal").getString().split("\n")) {
						lines.add(insertIndex++, Text.literal(ln).formatted(net.minecraft.util.Formatting.DARK_PURPLE, net.minecraft.util.Formatting.ITALIC));
					}
				} else {
					lines.add(insertIndex++, Text.translatable("item.looseprinces-tool.creator_divinity.cooldown.title").formatted(net.minecraft.util.Formatting.AQUA));
					lines.add(insertIndex++, Text.translatable("item.looseprinces-tool.creator_divinity.cooldown.detail",
						Text.translatable("item.looseprinces-tool.creator_divinity.key"),
						Text.translatable(modeKey),
						Text.literal(String.valueOf(seconds))
					).formatted(net.minecraft.util.Formatting.GRAY));
					lines.add(insertIndex++, Text.empty());
					String lineKey = amount >= 0 ? "item.looseprinces-tool.creator_divinity.story.cooldown_line.give" : "item.looseprinces-tool.creator_divinity.story.cooldown_line.take";
					String full = net.minecraft.text.Text.translatable("item.looseprinces-tool.creator_divinity.story.cooldown",
						Text.translatable(lineKey)
					).getString();
					for (String ln : full.split("\n")) {
						lines.add(insertIndex++, Text.literal(ln).formatted(net.minecraft.util.Formatting.DARK_PURPLE, net.minecraft.util.Formatting.ITALIC));
					}
				}
			} catch (Exception ignored) {}
		});

		// 追加 冷却/禁用 提示（放在最后一行）
		ItemTooltipCallback.EVENT.register((stack, ctx, type2, lines) -> {
			try {
				MinecraftClient mc = MinecraftClient.getInstance();
				if (mc == null || mc.player == null) return;
				boolean showDisabled = false;
				Text disabledReason = null;
				Text disabledSolution = null;

				// 灵魂绑定：非拥有者 → 禁用
				try {
					if (com.tool.looseprince.util.SoulBindingUtils.hasSoulBinding(stack)
							&& com.tool.looseprince.util.SoulBindingUtils.hasOwner(stack)) {
						java.util.UUID owner = com.tool.looseprince.util.SoulBindingUtils.getOwnerUuid(stack);
						if (owner != null && !owner.equals(mc.player.getUuid())) {
							showDisabled = true;
							disabledReason = Text.translatable("tooltip.looseprinces-tool.reason.not_owner");
							disabledSolution = Text.translatable("tooltip.looseprinces-tool.solution.return_or_drop");
						}
					}
				} catch (Exception ignored) {}

				if (showDisabled) {
					lines.add(Text.translatable("tooltip.looseprinces-tool.disabled.reason", disabledReason, disabledSolution)
							.formatted(net.minecraft.util.Formatting.RED));
					return;
				}

				// 冷却遮罩提示：仅对本模组相关物品在“神力静默”时提示；任意物品自身冷却时提示
				boolean itemCooling = mc.player.getItemCooldownManager().isCoolingDown(stack.getItem());
				boolean silence = false;
				try {
					DivinityFeature div = (DivinityFeature) FeatureRegistry.getInstance().getFeature("divinity");
					if (div != null && div.getDivineSilenceEffect() != null) {
						silence = mc.player.hasStatusEffect(div.getDivineSilenceEffect());
					}
				} catch (Exception ignored) {}
				boolean isTargetItem = false;
				try {
					isTargetItem =
							stack.getItem() == com.tool.looseprince.register.FlyingRuneRegistrar.get() ||
							stack.getItem() == com.tool.looseprince.register.FairDuelRegistrar.getItem() ||
							stack.getItem() == com.tool.looseprince.register.DivinityRegistrar.getImperfectItem() ||
							stack.getItem() == com.tool.looseprince.register.DivinityRegistrar.getCompleteItem() ||
							stack.getItem() == com.tool.looseprince.register.DivinityRegistrar.getCreatorItem();
				} catch (Exception ignored) {}

				boolean useSilence = silence && isTargetItem;
				if (itemCooling || useSilence) {
					net.minecraft.text.Text reasonText = useSilence
							? net.minecraft.text.Text.translatable("tooltip.looseprinces-tool.reason.silence")
							: net.minecraft.text.Text.translatable("tooltip.looseprinces-tool.reason.cooldown");
					lines.add(net.minecraft.text.Text.translatable(
							"tooltip.looseprinces-tool.cooldown.reason",
							reasonText
					).formatted(net.minecraft.util.Formatting.GRAY));
				}
			} catch (Exception ignored) {}
		});

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (creatorKey.wasPressed()) {
				if (client.player == null) return;
				// 仅生存/冒险下打开
				if (client.player.isCreative() || client.player.isSpectator()) return;
				// 必须拥有造物主效果
				DivinityFeature div = (DivinityFeature) FeatureRegistry.getInstance().getFeature("divinity");
				if (div == null || div.getCreatorEffect() == null || !client.player.hasStatusEffect(div.getCreatorEffect())) {
					client.player.sendMessage(Text.translatable("screen.looseprinces-tool.creator.need_effect"), true);
					return;
				}
				client.setScreen(new CreatorScreen());
			}
		});
	}

	static class CreatorScreen extends Screen {
		private TextFieldWidget input;
		protected CreatorScreen() {
			super(Text.translatable("screen.looseprinces-tool.creator.title"));
		}
		@Override
		protected void init() {
			int w = 200;
			int x = (this.width - w) / 2;
			int y = this.height / 2 - 10;
			input = new TextFieldWidget(this.textRenderer, x, y, w, 20, Text.literal("item id"));
			addDrawableChild(input);
			addDrawableChild(ButtonWidget.builder(Text.translatable("screen.looseprinces-tool.creator.submit"), btn -> submit())
					.dimensions(x, y + 30, 200, 20).build());
		}

		private void submit() {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client == null || client.player == null) return;
			String id = input.getText();
			CreatorRequestPayload.registerTypeC2S();
			ClientPlayNetworking.send(new CreatorRequestPayload(id));
			client.setScreen(null);
		}
	}
}