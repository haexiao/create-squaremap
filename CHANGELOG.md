# Changelog

## 1.1.0 — 2026-10-04

**更名版本 / Rename release** — 功能零变化，纯命名与版本调整。

### 变更 / Changed

- **项目更名**：`create-squaremap-bridge` → `create-squaremap`，同步更新 mod id、jar 文件名、配置文件名、`assets/<id>/` 资源目录与文档
  / Project renamed from `create-squaremap-bridge` to `create-squaremap`; mod id, jar name, config file name, asset directory and docs updated accordingly
- **配置文件更名**：`config/create-squaremap.properties`（配置项与默认值完全不变）
- 显示名：`Create Squaremap`

### ⚠️ 破坏性变更 / Breaking

- **mod id 已变更** → 升级前必须删除旧 jar `create-squaremap-bridge-fabric-mc1.21.11-1.0.0.jar`。新旧两个 mod id 不同，若同时存在会双双加载并对同一批方块变更**重复触发重绘**（不崩服，但白费 CPU/带宽）
- 旧配置文件 `config/create-squaremap-bridge.properties` 不再被读取
  / Old config file is no longer read

### 升级步骤 / Upgrade

1. 停服 / Stop the server
2. 删除 `mods/create-squaremap-bridge-fabric-mc1.21.11-1.0.0.jar`
3. 放入 `mods/create-squaremap-fabric-mc1.21.11-1.1.0.jar`
4. （可选）删除 `config/create-squaremap-bridge.properties`，启动后会自动生成新名文件

### 未变 / Unchanged

- mixin 注入点（`World#setBlockState` HEAD + `WorldChunk#setBlockState` 兜底）、按世界分队列、40 tick 批量冲刷、液体过滤、`defaultRequire: 0` 降级策略、4 个配置项及其默认值

---

## 1.0.0 — 2026-08-31

- 首个版本：捕获 Create（create-fly）机械结构直接写入区块的方块变更，批量触发 squaremap `CHUNK_CHANGED` 事件，让机械改动实时上图
- First release: captures Create contraption block writes that bypass standard block update events and forwards them to squaremap's `CHUNK_CHANGED` event
