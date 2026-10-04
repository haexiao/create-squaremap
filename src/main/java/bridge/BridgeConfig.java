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
 * 配置文件读取：config/create-squaremap.properties
 * 首次启动自动生成默认配置；修改后重启服务器生效。
 */
public final class BridgeConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("create-squaremap");
    private static final String FILE_NAME = "create-squaremap.properties";

    /** 方块变化合并冲刷间隔（tick，20 tick = 1 秒）。默认 40 = 2 秒 */
    public static int flushIntervalTicks = 40;
    /** 是否过滤液体间流动（true = 水/岩浆互流不触发重绘） */
    public static boolean filterLiquids = true;
    /** 调试日志：打印每次冲刷触发的区块数 */
    public static boolean debugLog = false;
    /** 控制台日志语言：zh / en（默认 en，国际化） */
    public static String language = "en";

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
                LOGGER.warn(tr("读取配置失败，使用默认值: {}", "Failed to read config, using defaults: {}"), e.toString());
            }
        }
        flushIntervalTicks = getInt(props, "flush-interval-ticks", 40, 1, 6000);
        filterLiquids = getBool(props, "filter-liquids", true);
        debugLog = getBool(props, "debug-log", false);
        language = getStr(props, "language", "en");
        if (!"zh".equals(language) && !"en".equals(language)) {
            language = "en";
        }

        if (!Files.exists(file)) {
            props.setProperty("flush-interval-ticks", String.valueOf(flushIntervalTicks));
            props.setProperty("filter-liquids", String.valueOf(filterLiquids));
            props.setProperty("debug-log", String.valueOf(debugLog));
            props.setProperty("language", language);
            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, tr("create-squaremap 配置（修改后重启生效）",
                        "create-squaremap config (restart to apply)"));
                LOGGER.info(tr("已生成默认配置文件: {}", "Generated default config file: {}"), file);
            } catch (IOException e) {
                LOGGER.warn(tr("写入默认配置失败: {}", "Failed to write default config: {}"), e.toString());
            }
        }
    }

    /** 按 language 选择中/英文消息 */
    public static String tr(String zh, String en) {
        return "en".equals(language) ? en : zh;
    }

    private static String getStr(Properties props, String key, String def) {
        String v = props.getProperty(key, def);
        return v == null ? def : v.trim();
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
