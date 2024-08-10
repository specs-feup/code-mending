package pt.up.fe.specs.cmender.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import java.nio.file.Paths;

public class Logging {
    private static final String FILE_LOGGER_NAME = "pt.up.fe.specs.cmender.FileLogger";

    private static final String FILE_APPENDER_NAME = "pt.up.fe.specs.cmender.FileAppender";

    private static final String FILE_APPENDER_PATTERN = "%d{yyyy-MM-dd HH:mm:ss} (%t) %-5level - %msg%n%ex";

    private static final String LOG_FILE_NAME = "cmender.log";

    private static final Level DEFAULT_LOG_LEVEL = Level.INFO;

    private static final Level DEFAULT_STATUS_LEVEL = Level.WARN;

    public static final Logger FILE_LOGGER = LogManager.getLogger(FILE_LOGGER_NAME);

    static {
        // TODO check here if user provided folder exists and has permissions

        var logLevel = getCMenderLogLevel();

        if (logLevel == null) {
            System.err.println("error: invalid log level: " + System.getProperty("log.level"));
            System.exit(1);
        }

        ConfigurationBuilder<BuiltConfiguration> configBuilder = ConfigurationBuilderFactory.newConfigurationBuilder();

        var fileAppenderComponentBuilder = configBuilder
                .newAppender(FILE_APPENDER_NAME, "File")
                .addAttribute("fileName", Paths.get(getCMenderLogsDirpath(), LOG_FILE_NAME))
                .add(configBuilder.newLayout("PatternLayout")
                        .addAttribute("pattern", FILE_APPENDER_PATTERN));

        var rootLoggerComponentBuilder = configBuilder
                .newRootLogger(logLevel)
                .add(configBuilder.newAppenderRef(FILE_APPENDER_NAME));

        var fileLoggerComponentBuilder = configBuilder
                .newLogger(FILE_LOGGER_NAME, logLevel)
                .add(configBuilder.newAppenderRef(FILE_APPENDER_NAME))
                .addAttribute("additivity", false);

        configBuilder
                .setStatusLevel(DEFAULT_STATUS_LEVEL)
                .add(fileAppenderComponentBuilder)
                .add(rootLoggerComponentBuilder)
                .add(fileLoggerComponentBuilder);

        Configurator.initialize(configBuilder.build());
    }

    public static Level getCMenderLogLevel() {
        var logLevel = System.getProperty("log.level");

        if (logLevel == null) {
            return DEFAULT_LOG_LEVEL;
        }

        try {
            return Level.valueOf(logLevel.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String getCMenderLogsDirpath() {
        var os = System.getProperty("os.name").toLowerCase();
        var home = System.getProperty("user.home");

        String defaultLogfilesPath;

        // TODO is it worth to add Apache Commons Lang to simplify this?
        if (os.contains("win")) {
            defaultLogfilesPath = Paths.get(home, "AppData", "Local", "CMender", "logs").toString();
        } else if (os.contains("mac")) {
            defaultLogfilesPath = Paths.get(home, "Library", "Logs", "CMender").toString();
        } else {
            defaultLogfilesPath = Paths.get(home, ".CMender", "logs").toString();
        }

        return System.getProperty("log.dir", defaultLogfilesPath);
    }
}
