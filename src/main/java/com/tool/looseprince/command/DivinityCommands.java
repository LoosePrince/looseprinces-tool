package com.tool.looseprince.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.tool.looseprince.feature.DivinityFeature;
import com.tool.looseprince.feature.FeatureRegistry;
import com.tool.looseprince.util.CreatorCooldownManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DivinityCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("lpt")
                    .then(CommandManager.literal("divinity")
                            .then(CommandManager.literal("clear_silence").executes(ctx -> clearSilence(ctx.getSource()))));
            dispatcher.register(root);
        });
    }

    private static int clearSilence(ServerCommandSource source) {
        ServerPlayerEntity player = source.getPlayer();
        if (player == null) return 0;
        try {
            DivinityFeature div = (DivinityFeature) FeatureRegistry.getInstance().getFeature("divinity");
            if (div != null && div.getDivineSilenceEffect() != null) {
                player.removeStatusEffect(div.getDivineSilenceEffect());
            }
            long now = player.getWorld().getTime();
            CreatorCooldownManager.getInstance().endCooldown(player.getUuid(), now);
            source.sendFeedback(() -> Text.literal("Divine silence removed."), true);
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendError(Text.literal("Failed to clear divine silence."));
            return 0;
        }
    }
}


