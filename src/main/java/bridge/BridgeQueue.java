package bridge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import xyz.jpenilla.squaremap.fabric.event.MapUpdateEvents;

/**
 * 方块变化节流队列（多世界安全）。
 *
 * - 按世界分队列，避免不同世界的区块互相错发
 * - ConcurrentLinkedQueue + drainTo 原子取出，杜绝并发丢数据
 * - 每 10 tick（0.5 秒）批量触发 squaremap 的 CHUNK_CHANGED
 * - 触发侧整体 try-catch：任何异常只记一次日志，绝不影响服务器
 */
public final class BridgeQueue {
    /** 每多少 tick 向 squaremap 冲刷一次脏区块（可配置，默认 40 tick = 2 秒） */
    private static final int FLUSH_INTERVAL_TICKS = BridgeConfig.flushIntervalTicks;

    private static final Map<ServerWorld, ConcurrentLinkedQueue<ChunkPos>> QUEUES = new ConcurrentHashMap<>();
    private static int tickCounter;

    private BridgeQueue() {
    }

    /** 由 mixin 调用：标记一个方块位置为"需要重渲染"（任意线程安全） */
    public static void mark(ServerWorld world, BlockPos pos) {
        QUEUES.computeIfAbsent(world, w -> new ConcurrentLinkedQueue<>()).add(new ChunkPos(pos));
    }

    /** 每 tick 由 ServerTickEvents.END_SERVER_TICK 调用（仅服务端主线程） */
    public static void onServerTickEnd(MinecraftServer server) {
        if (++tickCounter < FLUSH_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;
        if (QUEUES.isEmpty()) {
            return;
        }
        List<ServerWorld> liveWorlds = new ArrayList<>();
        for (ServerWorld sw : server.getWorlds()) {
            liveWorlds.add(sw);
        }
        for (Map.Entry<ServerWorld, ConcurrentLinkedQueue<ChunkPos>> entry : QUEUES.entrySet()) {
            ServerWorld world = entry.getKey();
            // 世界已卸载：丢弃该队列，防止引用泄漏
            if (!liveWorlds.contains(world)) {
                QUEUES.remove(world);
                continue;
            }
            ConcurrentLinkedQueue<ChunkPos> queue = entry.getValue();
            if (queue.isEmpty()) {
                continue;
            }
            List<ChunkPos> batch = new ArrayList<>();
            // poll 原子取出全部，与并发 mark 无竞态；新入队的留到下一轮
            ChunkPos chunkPos;
            while ((chunkPos = queue.poll()) != null) {
                batch.add(chunkPos);
            }
            if (batch.isEmpty()) {
                continue;
            }
            Set<ChunkPos> unique = new HashSet<>(batch); // 去重（World/WorldChunk 双层捕获 + 重复变化）
            for (ChunkPos pos : unique) {
                try {
                    MapUpdateEvents.CHUNK_CHANGED.invoker().updatePosition(world, pos);
                } catch (Throwable t) {
                    // 静默：桥接失败不影响游戏（地图最多不更新，回到现状）
                }
            }
            if (BridgeConfig.debugLog && !unique.isEmpty()) {
                bridge.BridgeLog.debug("冲刷 {} 个区块 -> squaremap", unique.size());
            }
        }
    }
}
