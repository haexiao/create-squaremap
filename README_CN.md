# create-squaremap-bridge

一个轻量的 Fabric 服务端模组，解决 [Create Fly 机械动力飞越版](https://github.com/ZurrTum/Create-Fly) 改动的方块不更新 [squaremap 网页地图](https://github.com/jpenilla/squaremap) 的问题。

Create 的 Contraption（机械结构）部署/拆解时直接写入区块底层数据，不走标准方块更新事件，导致 squaremap 监听不到、地图不重绘。本模组桥接两者：监听方块写入 → 批量触发 squaremap 的 `CHUNK_CHANGED` 事件。

## 功能

- Create 机械挖/放/搬运的方块自动更新到 squaremap 地图（默认 15 秒内，与 squaremap 后台渲染周期一致）
- 覆盖 Contraption 装配/拆解、轨道铺设、运送机搬运等所有 `setBlockState` 路径
- 液体流动自动过滤（防止海洋/岩浆持续触发重绘）
- 多世界安全（主世界/下界/末地分队列，互不串扰）
- 稳定性兜底：全链路异常静默、`defaultRequire: 0`（未来 MC 版本改方法签名时仅降级不崩服）

## 环境要求

| 项目 | 版本 |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.18.x |
| squaremap | 1.3.12+ |
| create-fly | 6.0.9-5 |

## 构建

需要 JDK 21+。

```bash
./gradlew build
# 产物：build/libs/create-squaremap-bridge-1.0.0.jar
```

## 安装

1. 将 `build/libs/create-squaremap-bridge-1.0.0.jar` 放入服务器的 `mods/` 目录
2. 重启服务器
3. 启动日志中无 mixin 报错即加载成功

## 配置

配置文件：`config/create-squaremap-bridge.properties`（首次启动自动生成）

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `flush-interval-ticks` | `40` | 方块变化合并冲刷间隔（tick，20 tick = 1 秒）。调大 → 重绘更稀疏（省性能/网络），地图更新更慢；调小 → 更频繁 |
| `filter-liquids` | `true` | 过滤液体间流动（水/岩浆互流不触发重绘，防性能浪费）。`false` = 液体流动也重绘 |
| `debug-log` | `false` | 调试日志：`true` 时每次冲刷打印触发区块数到服务器日志 |

> 修改后**重启服务器**生效。
>
> 提示：squaremap 本身的 `background-render.interval-seconds`（默认 15）是最终渲染节奏，`flush-interval-ticks` 再小也不会快过它。

## 工作原理

```
Create 机械写方块 → mixin 捕获 World/WorldChunk.setBlockState
→ 方块实际变化？→ 按区块去重入队（每 tick 检查，2 秒合并一批）
→ 调用 squaremap CHUNK_CHANGED 事件 → 区块重新渲染
```

## 兼容性

- 已与服务器上全部 15 个 mod 静态验证无 mixin 冲突（Lithium / create-fly / fabric-api / journeymap 等）
- **注意**：依赖 squaremap 内部类 `xyz.jpenilla.squaremap.fabric.event.MapUpdateEvents`（非公开 API）。升级 squaremap **大版本**时可能需要适配；1.3.x 系列内已验证稳定
- 无 fabric-api 运行时依赖（tick 钩子用 mixin 实现）

## License

[MIT](LICENSE)
