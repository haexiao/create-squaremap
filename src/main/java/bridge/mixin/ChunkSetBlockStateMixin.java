package bridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import bridge.BridgeConfig;
import bridge.BridgeQueue;

/**
 * 兜底：拦截 WorldChunk#setBlockState —— 如果 Create（或其他 mod）绕过 World 直接写 chunk，
 * 这里也能捕获。与 World 层的捕获按区块去重，不会重复触发。
 * 全程 try-catch：任何异常静默吞掉。
 */
@Mixin(WorldChunk.class)
public abstract class ChunkSetBlockStateMixin {
    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Lnet/minecraft/block/BlockState;",
            at = @At("HEAD"))
    private void bridge$onChunkSetBlockState(BlockPos pos, BlockState state, int flags, CallbackInfoReturnable<BlockState> cir) {
        try {
            WorldChunk chunk = (WorldChunk) (Object) this;
            World world = chunk.getWorld();
            if (!(world instanceof ServerWorld serverWorld)) {
                return;
            }
            if (!world.isChunkLoaded(pos)) {
                return;
            }
            BlockState old = chunk.getBlockState(pos);
            if (old == state) {
                return;
            }
            if (BridgeConfig.filterLiquids && !old.getFluidState().isEmpty() && !state.getFluidState().isEmpty()) {
                return;
            }
            BridgeQueue.mark(serverWorld, pos);
        } catch (Throwable ignored) {
            // 静默失败：桥接不影响游戏
        }
    }
}
