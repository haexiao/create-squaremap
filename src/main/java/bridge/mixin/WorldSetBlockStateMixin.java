package bridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import bridge.BridgeConfig;
import bridge.BridgeQueue;

/**
 * 拦截所有经过 World#setBlockState 的方块写入（玩家、机械、命令、结构生成都走这里）。
 * 只在服务端、区块已加载、方块实际变化时标记；液体→液体（流动）跳过。
 * 全程 try-catch：任何异常静默吞掉，绝不影响 setBlockState 调用链。
 */
@Mixin(World.class)
public abstract class WorldSetBlockStateMixin {
    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z",
            at = @At("HEAD"))
    private void bridge$onSetBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<Boolean> cir) {
        try {
            World self = (World) (Object) this;
            if (!(self instanceof ServerWorld world)) {
                return; // 客户端忽略
            }
            if (!world.isChunkLoaded(pos)) {
                return; // 与 squaremap 自身行为一致：只标记已加载区块
            }
            BlockState old = self.getBlockState(pos);
            if (old == state) {
                return;
            }
            if (BridgeConfig.filterLiquids && !old.getFluidState().isEmpty() && !state.getFluidState().isEmpty()) {
                return; // 液体流动，跳过（可配置 filter-liquids=false 关闭）
            }
            BridgeQueue.mark(world, pos);
        } catch (Throwable ignored) {
            // 静默失败：桥接不影响游戏
        }
    }
}
