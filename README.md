# LoosePrince's Tool

## 项目简介

**LoosePrince's Tool** 是一个基于 [Fabric](https://fabricmc.net/) 的 Minecraft 模组，添加一些物品或功能。

## 主要特性

- **模块化设计**：所有功能均为独立模块，互不干扰，易于扩展。
- **完善的配置系统**：所有功能均可通过 JSON 配置文件灵活调整。
- **日志与热重载**：支持日志输出与功能热重载，方便调试与维护。

## 功能列表

- **飞行符文**：
  - 持有该物品即可在生存模式下飞行。

- **绑定附魔**：
  - 拥有该附魔的物品在死亡时不会掉落。

- **灵魂绑定**：
  - 附魔给物品后会记录玩家的 UUID，物品描述中显示"已绑定至:玩家名(uuid)"。
  - 附魔灵魂绑定后该物品丢出后无法被其它玩家拾取。
  - 在容器中（如箱子、漏斗等）无法被其它玩家拿起或移动（包括快捷键移动）。
  - 创造模式玩家不受上述拾取/移动限制影响。
  - 提供 II 级：
    - 掉落后 N 秒（默认 30s）自动回到绑定者（优先放入背包，否则传送至脚下）。
    - 岩浆中不会被烧毁，并会缓慢上浮。
    - 虚空中不会被销毁，会被抬升至世界底部线上方并上浮脱离。

- **公平对决**：
  - 放在背包中时，每 0.5 秒自动赋予 1 秒的同名药水效果。
  - 持有效果时：根据玩家此前对同一生物造成的伤害占该生物生命上限的百分比，调整玩家从该生物处受到的伤害为"玩家生命上限 × 相同百分比 × 伤害转换比例"。该调整无视护甲、减伤（如抗性提升）与额外增伤。
  - 例：玩家对 100 血的铁傀儡造成 1 点伤害（1%），默认转换比例100%，则铁傀儡对玩家造成的伤害被调整为"玩家生命上限的 1%"。

- **神格系统**：
  - **残缺的神格**：史诗级物品，背包中持续获得"残缺的神格"效果，等同于抗性提升V + 公平对决效果。
  - **完整的神格**：神话级物品，背包中持续获得"神的力量"效果，提供完全无敌状态、禁用公平对决效果，并自动获得飞行能力。
  - **造物主的神格**：神话级物品，背包中持续获得"造物主"效果（包含"神的力量"，赋予无敌与生存飞行），并移除"残缺的神格"与"神的力量"效果。按Z（可配置）打开输入界面，输入物品 ID 以获取/扣除物品（成功后进入冷却）。冷却期间玩家将失去并无法获得"造物主"、"神的力量"、"残缺的神格"、"公平对决"效果且无法在生存模式下飞行，并获得用于显示剩余时间的"神力静默"效果，冷却结束时自动清除。
  - 两种神格物品均无法附魔，具有独特的故事背景和视觉效果。

## 系统要求

- **Minecraft 版本**：1.21
- **Java 版本**：Java 21 或更高
- **Fabric Loader**：0.16.14 或更高
- **Fabric API**：0.102.0+1.21 或兼容版本
- **操作系统**：Windows、macOS、Linux

## 安装方法

1. 安装 [Fabric Loader](https://fabricmc.net/use/) 和 [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)。
2. 下载本模组的 jar 文件，放入 `mods` 文件夹。
3. 启动游戏，模组会自动加载。
4. 首次运行后，可在 `config/looseprinces-tool.json` 中调整配置。

## 配置说明

- 配置文件路径：`config/looseprinces-tool.json`
- 可通过编辑 JSON 文件启用/禁用各功能及其详细选项。

### 完整配置示例

```json
{
  "version": "1.0.5",
  "features": {
    "flying_rune": {
      "enabled": true,
      "allowInNether": true,
      "allowInEnd": true,
      "preventFallDamage": true,
      "requireInInventory": true
    },
    "binding_enchantment": {
      "enabled": true,
      "preventDrop": true,
      "affectAllItems": true,
      "maxLevel": 1
    },
    "soul_binding": {
      "enabled": true,
      "preventPickup": true,
      "preventContainerTake": true,
      "showOwnerTooltip": true,
      "level2TeleportSeconds": 30,
      "lavaImmune": true,
      "voidDestroyable": false
    },
    "fair_duel": {
      "enabled": true,
      "damageRatio": 1.0
    },
    "divinity": {
      "enabled": true
    }
  }
}
```

### 参数说明

#### 飞行符文 (flying_rune)
- `enabled`: 是否启用飞行符文功能
- `allowInNether`: 是否允许在下界使用（默认：true）
- `allowInEnd`: 是否允许在末地使用（默认：true）
- `preventFallDamage`: 是否防止摔落伤害（默认：true）
- `requireInInventory`: 是否在背包中时即可飞行，false时需手持物品才能飞行（默认：true）

#### 绑定附魔 (binding_enchantment)
- `enabled`: 是否启用绑定附魔功能
- `preventDrop`: 是否防止物品掉落（默认：true）
- `affectAllItems`: 是否影响所有物品（默认：true）
- `maxLevel`: 附魔最大等级（默认：1）

#### 灵魂绑定 (soul_binding)
- `enabled`: 是否启用灵魂绑定附魔功能
- `preventPickup`: 是否阻止其他玩家拾取（默认：true）
- `preventContainerTake`: 是否阻止其他玩家从容器中取出（默认：true）
- `showOwnerTooltip`: 是否显示拥有者信息提示（默认：true）
- `level2TeleportSeconds`: 掉落后自动回到拥有者的时间（默认：30s）
- `lavaImmune`: 是否对岩浆免疫（默认：true）
- `voidDestroyable`: 是否允许被虚空销毁（默认：false）

#### 公平对决 (fair_duel)
- `enabled`: 是否启用公平对决功能
- `damageRatio`: 伤害转换比例（默认：1.0，即100%）
  - `1.0` = 100% 转换（完全等比例）
  - `0.5` = 50% 转换（减半伤害）
  - `2.0` = 200% 转换（双倍伤害）

#### 神格系统 (divinity)
- `enabled`: 是否启用神格功能
- `creatorCooldownSeconds`: 造物主获取/扣除后进入的冷却秒数（默认：900）
- `creatorGiveAmount`: 获取/扣除的数量（范围 -64 到 640，默认：1；负数为扣除）

### 命令

- `/lpt divinity clear_silence`：移除自身的"神力静默"效果并结束冷却

## 进度系统

本模组包含完整的进度系统，记录玩家的冒险历程：

### 进度列表

- **LoosePrince的工具箱** - 根进度，模组的入口
- **亵渎者的羽翼** - 获得飞行符文或在生存模式下飞行
- **神之尺度** - 激活公平对决效果
- **窃火者的荆棘冠** - 获得残缺的神格效果
- **王座承认了你** - 获得完整的神格效果
- **高天之上** - 获得“造物主”效果

## 开发与贡献

欢迎提交 Issue 或 PR 参与开发！

### 项目结构

```
src/main/java/com/tool/looseprince/
├── LoosePrincesTool.java          # 模组主类
├── config/                        # 配置管理
│   ├── Config.java                # 配置数据类
│   ├── ConfigManager.java         # 配置管理器
│   └── FeatureConfig.java         # 功能配置基类
├── feature/                       # 功能模块
│   ├── Feature.java               # 功能接口
│   ├── FeatureRegistry.java       # 功能注册器
│   ├── FlyingRuneFeature.java     # 飞行符文
│   ├── BindingEnchantmentFeature.java # 绑定附魔
│   ├── SoulBindingFeature.java    # 灵魂绑定
│   ├── FairDuelFeature.java       # 公平对决
│   └── DivinityFeature.java       # 神格系统
├── event/                         # 事件处理
├── item/                          # 自定义物品
├── registry/                      # 注册管理
└── datagen/                       # 数据生成
```

### 开发指南

- **代码结构**：采用模块化设计，所有功能均为独立模块，互不干扰
- **新功能开发**：实现 `Feature` 接口并注册到 `FeatureRegistry`
- **配置系统**：每个功能都有独立的配置类，支持 JSON 序列化
- **事件系统**：使用 Fabric 事件系统进行功能交互
- **本地化**：支持中文和英文，所有文本均在 `lang` 文件中定义

### 调试提示

- 使用 `./gradlew runClient` 启动开发客户端
- 使用 `./gradlew runDatagen` 生成数据文件
- 配置文件位于 `run/config/looseprinces-tool.json`
- 日志文件位于 `run/logs/` 目录

## 致谢与协议

- 本项目由 LoosePrince 开发，遵循 MIT 协议。
- 感谢 Fabric 社区及所有开源贡献者。

---

如有建议或问题，欢迎在 Issue 区留言反馈！
