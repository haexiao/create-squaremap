package bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 调试日志工具：仅 BridgeConfig.debugLog=true 时输出，消息按 language 中/英切换 */
public final class BridgeLog {
    private static final Logger LOGGER = LoggerFactory.getLogger("create-squaremap");

    private BridgeLog() {
    }

    public static void debug(String zh, String en, Object... args) {
        if (BridgeConfig.debugLog) {
            LOGGER.info("[bridge] " + BridgeConfig.tr(zh, en), args);
        }
    }
}
