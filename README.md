# create-squaremap-bridge

一个轻量的 Fabric 服务端模组，解决 **Create（机械动力）改动的方块不更新 squaremap 网页地图** 的问题。

Create 的 Contraption（机械结构）部署/拆解时直接写入区块底层数据，不走标准方块更新事件，导致 squaremap 监听不到、地图不重绘。本模组桥接两者：监听方块写入 → 批量触发 squaremap 的 `CHUNK_CHANGED` 事件。

## 功能

- Create 机械挖/放/搬运的方块自动更新到 squaremap 地图（默认 15 秒内，与 squaremap 后台渲染周期一致）
- 覆盖 Contraption 装配/拆解、轨道铺设、运送机搬运等所有 `setBlockState` 路径
- 液体流动自动过滤（防止海洋/岩浆持续触发重绘）
- 多世界安全（主世界/下界/末地分队列，互不串扰）

## 兼容性

- 已与服务器上全部 15 个 mod 静态验证无 mixin 冲突（Lithium / Create / fabric-api / journeymap 等）
- **注意**：依赖 squaremap 内部类 `xyz.jpenilla.squaremap.fabric.event.MapUpdateEvents`（非公开 API）。升级 squaremap **大版本**时可能需要适配；1.3.x 系列内已验证稳定

## 环境要求

| 项目 | 版本 |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.18.x |
| squaremap | 1.3.12+ |
| create | 6.0.9-5（1.21.11 版） |

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

## 工作原理

```
Create 机械写方块 → mixin 捕获 World/WorldChunk.setBlockState
→ 方块实际变化？→ 按区块去重入队（每 tick 检查，2 秒合并一批）
→ 调用 squaremap CHUNK_CHANGED 事件 → 区块重新渲染
```

## 设计细节

- **双 mixin 注入点**：`ServerWorld#setBlockState`（主）+ `WorldChunk#setBlockState`（兜底 Create 底层写入）
- **零 fabric-api 依赖**：tick 钩子用 `MinecraftServer#tick` mixin 实现（编译期仅因 squaremap 事件签名需要 fabric-api 类型，compileOnly）
- **稳定性兜底**：全链路 try-catch（异常静默，绝不影响游戏）；`defaultRequire: 0`（未来 MC 版本改方法签名时仅降级不崩服）；按世界分队列（多世界不错乱）；原子 poll 取队（并发不丢数据）
- **零配置**：无配置文件，装完即用

## License

MIT
