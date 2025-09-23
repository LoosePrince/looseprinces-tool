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

- **公平对决**：
  - 新增物品"公平对决"。放在背包中时，每 0.5 秒自动赋予 1 秒的同名药水效果。
  - 持有效果时：根据玩家此前对同一生物造成的伤害占该生物生命上限的百分比，调整玩家从该生物处受到的伤害为"玩家生命上限 × 相同百分比 × 伤害转换比例"。该调整无视护甲、减伤（如抗性提升）与额外增伤。
  - 例：玩家对 100 血的铁傀儡造成 1 点伤害（1%），默认转换比例100%，则铁傀儡对玩家造成的伤害被调整为"玩家生命上限的 1%"。

## 安装方法

1. 安装 [Fabric Loader](https://fabricmc.net/use/) 和 [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)。
2. 下载本模组的 jar 文件，放入 `mods` 文件夹。
3. 启动游戏，模组会自动加载。

## 配置说明

- 配置文件路径：`config/looseprinces-tool.json`
- 可通过编辑 JSON 文件启用/禁用各功能及其详细选项。

### 完整配置示例

```json
{
  "version": "1.0.1",
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
    "fair_duel": {
      "enabled": true,
      "damageRatio": 1.0
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

#### 公平对决 (fair_duel)
- `enabled`: 是否启用公平对决功能
- `damageRatio`: 伤害转换比例（默认：1.0，即100%）
  - `1.0` = 100% 转换（完全等比例）
  - `0.5` = 50% 转换（减半伤害）
  - `2.0` = 200% 转换（双倍伤害）

## 开发与贡献

欢迎提交 Issue 或 PR 参与开发！
- 代码结构清晰，功能注册与配置均有注释说明。
- 新功能请实现 `Feature` 接口并注册到 `FeatureRegistry`。
- 详细开发文档与 API 说明请见源码注释。

### 调试提示

- 使用 `./gradlew runClient` 启动开发客户端。
- 已为 `runClient` 配置 UTF-8 编码参数，终端可正确显示中文日志。

## 致谢与协议

- 本项目由 LoosePrince 开发，遵循 MIT 协议。
- 感谢 Fabric 社区及所有开源贡献者。

---

如有建议或问题，欢迎在 Issue 区留言反馈！
