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
  - 支持自定义是否允许在下界/末地使用。
  - 可配置是否防止摔落伤害、是否需在背包中。

## 安装方法

1. 安装 [Fabric Loader](https://fabricmc.net/use/) 和 [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)。
2. 下载本模组的 jar 文件，放入 `mods` 文件夹。
3. 启动游戏，模组会自动加载。

## 配置说明

- 配置文件路径：`config/looseprinces-tool.json`
- 可通过编辑 JSON 文件启用/禁用各功能及其详细选项。
- 以“飞行符文”为例：
  ```json
  {
    "features": {
      "flying_rune": {
        "enabled": true,
        "options": {
          "allowInNether": true,
          "allowInEnd": true,
          "preventFallDamage": true,
          "requireInInventory": true
        }
      }
    }
  }
  ```
- 修改配置后可在游戏内通过重载命令或重启生效。

## 开发与贡献

欢迎提交 Issue 或 PR 参与开发！
- 代码结构清晰，功能注册与配置均有注释说明。
- 新功能请实现 `Feature` 接口并注册到 `FeatureRegistry`。
- 详细开发文档与 API 说明请见源码注释。

## 致谢与协议

- 本项目由 LoosePrince 开发，遵循 MIT 协议。
- 感谢 Fabric 社区及所有开源贡献者。

---

如有建议或问题，欢迎在 Issue 区留言反馈！
