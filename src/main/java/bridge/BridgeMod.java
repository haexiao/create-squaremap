package bridge;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/**
 * Create-Squaremap 桥接 mod 入口。
 * 作用：Create 机械写方块不走标准方块更新事件，squaremap 收不到信号导致地图不更新。
 * 本 mod 通过 mixin 捕获所有方块写入，转成 squaremap 的 CHUNK_CHANGED 事件。
 */
public final class BridgeMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(BridgeQueue::onServerTickEnd);
    }
}
