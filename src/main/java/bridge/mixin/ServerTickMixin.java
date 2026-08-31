package bridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.MinecraftServer;

import bridge.BridgeQueue;

/**
 * 每 tick 末尾冲刷一次脏区块队列（替代 fabric-api 的 ServerTickEvents.END_SERVER_TICK，
 * 使本 mod 不依赖 fabric-api，与 create-fly 的独立风格一致）。
 */
@Mixin(MinecraftServer.class)
public abstract class ServerTickMixin {
    @Inject(method = "tick", at = @At("RETURN"))
    private void bridge$onServerTickEnd(CallbackInfo ci) {
        try {
            BridgeQueue.onServerTickEnd((MinecraftServer) (Object) this);
        } catch (Throwable ignored) {
            // 静默：桥接失败绝不影响服务器 tick
        }
    }
}
