package bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 调试日志工具：仅 BridgeConfig.debugLog=true 时输出 */
public final class BridgeLog {
    private static final Logger LOGGER = LoggerFactory.getLogger("create-squaremap-bridge");

    private BridgeLog() {
    }

    public static void debug(String msg, Object... args) {
        if (BridgeConfig.debugLog) {
            LOGGER.info("[bridge] " + msg, args);
        }
    }
}
