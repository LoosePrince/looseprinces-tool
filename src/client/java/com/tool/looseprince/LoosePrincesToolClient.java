package com.tool.looseprince;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.text.Text;
import com.tool.looseprince.util.SoulBindingUtils;
import com.tool.looseprince.feature.FeatureRegistry;
import com.tool.looseprince.feature.SoulBindingFeature;

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
	}
}