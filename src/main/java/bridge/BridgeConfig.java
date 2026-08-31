package bridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置文件读取：config/create-squaremap-bridge.properties
 * 首次启动自动生成默认配置；修改后重启服务器生效。
 */
public final class BridgeConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("create-squaremap-bridge");
    private static final String FILE_NAME = "create-squaremap-bridge.properties";

    /** 方块变化合并冲刷间隔（tick，20 tick = 1 秒）。默认 40 = 2 秒 */
    public static int flushIntervalTicks = 40;
    /** 是否过滤液体间流动（true = 水/岩浆互流不触发重绘） */
    public static boolean filterLiquids = true;
    /** 调试日志：打印每次冲刷触发的区块数 */
    public static boolean debugLog = false;

    static {
        load();
    }

    private BridgeConfig() {
    }

    private static void load() {
        Path dir = FabricLoader.getInstance().getConfigDir();
        Path file = dir.resolve(FILE_NAME);
        Properties props = new Properties();
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                LOGGER.warn("读取配置失败，使用默认值: {}", e.toString());
            }
        }
        flushIntervalTicks = getInt(props, "flush-interval-ticks", 40, 1, 6000);
        filterLiquids = getBool(props, "filter-liquids", true);
        debugLog = getBool(props, "debug-log", false);

        if (!Files.exists(file)) {
            props.setProperty("flush-interval-ticks", String.valueOf(flushIntervalTicks));
            props.setProperty("filter-liquids", String.valueOf(filterLiquids));
            props.setProperty("debug-log", String.valueOf(debugLog));
            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, "create-squaremap-bridge 配置（修改后重启生效）");
                LOGGER.info("已生成默认配置文件: {}", file);
            } catch (IOException e) {
                LOGGER.warn("写入默认配置失败: {}", e.toString());
            }
        }
    }

    private static int getInt(Properties props, String key, int def, int min, int max) {
        try {
            int v = Integer.parseInt(props.getProperty(key, String.valueOf(def)).trim());
            return Math.max(min, Math.min(max, v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static boolean getBool(Properties props, String key, boolean def) {
        return Boolean.parseBoolean(props.getProperty(key, String.valueOf(def)).trim());
    }
}
