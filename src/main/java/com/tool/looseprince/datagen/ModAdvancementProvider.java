package com.tool.looseprince.datagen;

import com.tool.looseprince.LoosePrincesTool;
import com.tool.looseprince.registry.ModItemGroups;
import com.tool.looseprince.feature.FlyingRuneFeature;
import com.tool.looseprince.feature.FairDuelFeature;
import com.tool.looseprince.feature.DivinityFeature;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.ImpossibleCriterion;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModAdvancementProvider extends FabricAdvancementProvider {
    public ModAdvancementProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(RegistryWrapper.WrapperLookup registryLookup, Consumer<AdvancementEntry> consumer) {
        // Root advancement - 分页入口
        AdvancementEntry rootAdvancement = Advancement.Builder.create()
                .display(
                        ModItemGroups.MOD_ICON_ITEM, // 使用模组创造分组图标
                        Text.translatable("advancements.looseprinces-tool.root.title"),
                        Text.translatable("advancements.looseprinces-tool.root.description"),
                        Identifier.of("minecraft", "textures/gui/advancements/backgrounds/adventure.png"),
                        AdvancementFrame.TASK,
                        false, // 不显示toast
                        false, // 不在聊天中公告
                        false  // 不隐藏
                )
                .criterion("impossible", Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, LoosePrincesTool.MOD_ID + ":root");

        // 亵渎者的羽翼 - 持有飞行符文/在生存飞行，由代码授予
        Advancement.Builder.create()
                .parent(rootAdvancement)
                .display(
                        FlyingRuneFeature.getStaticFlyingRune(), // 飞行符文物品图标
                        Text.translatable("advancements.looseprinces-tool.wings.title"),
                        Text.translatable("advancements.looseprinces-tool.wings.description"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("granted_by_code", Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, LoosePrincesTool.MOD_ID + ":wings");

        // 神之尺度 - 获得公平对决效果，由代码授予
        Advancement.Builder.create()
                .parent(rootAdvancement)
                .display(
                        FairDuelFeature.getStaticItem(), // 公平对决物品图标
                        Text.translatable("advancements.looseprinces-tool.god_scale.title"),
                        Text.translatable("advancements.looseprinces-tool.god_scale.description"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("granted_by_code", Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, LoosePrincesTool.MOD_ID + ":god_scale");

        // 窃火者的荆棘冠 - 获得残缺的神格效果，由代码授予
        Advancement.Builder.create()
                .parent(rootAdvancement)
                .display(
                        DivinityFeature.getStaticImperfectDivinityItem(), // 残缺神格物品图标
                        Text.translatable("advancements.looseprinces-tool.thorn_crown.title"),
                        Text.translatable("advancements.looseprinces-tool.thorn_crown.description"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion("granted_by_code", Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, LoosePrincesTool.MOD_ID + ":thorn_crown");

        // 王座承认了你 - 获得神的力量效果，由代码授予
        Advancement.Builder.create()
                .parent(rootAdvancement)
                .display(
                        DivinityFeature.getStaticCompleteDivinityItem(), // 完整神格物品图标
                        Text.translatable("advancements.looseprinces-tool.throne.title"),
                        Text.translatable("advancements.looseprinces-tool.throne.description"),
                        null,
                        AdvancementFrame.GOAL, // 设为目标类型
                        true,
                        true,
                        false
                )
                .criterion("granted_by_code", Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, LoosePrincesTool.MOD_ID + ":throne");

        // 高天之上 - 获得造物主效果，由代码授予
        Advancement.Builder.create()
                .parent(rootAdvancement)
                .display(
                        DivinityFeature.getStaticCreatorDivinityItem(),
                        Text.translatable("advancements.looseprinces-tool.above_sky.title"),
                        Text.translatable("advancements.looseprinces-tool.above_sky.description"),
                        null,
                        AdvancementFrame.CHALLENGE,
                        true,
                        true,
                        false
                )
                .criterion("granted_by_code", Criteria.IMPOSSIBLE.create(new ImpossibleCriterion.Conditions()))
                .build(consumer, LoosePrincesTool.MOD_ID + ":above_sky");
    }
}