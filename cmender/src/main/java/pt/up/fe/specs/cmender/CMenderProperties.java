package pt.up.fe.specs.cmender;

import pt.up.fe.specs.cmender.cli.CliReporting;
import pt.up.fe.specs.cmender.logging.Logging;

import java.io.IOException;
import java.util.jar.JarFile;

public record CMenderProperties(
        String name,
        String vendor,
        String description,
        String version,
        String date
) {
    public static CMenderProperties get() {
        try {
            var jarFilepath = CMenderProperties.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            var jarFile = new JarFile(jarFilepath);
            var manifest = jarFile.getManifest();

            if (manifest == null) {
                CliReporting.error("could not read CMender properties: manifest is missing");
                Logging.FILE_LOGGER.fatal("could not read CMender properties: manifest is missing");
                return null;
            }

            var attributes = manifest.getMainAttributes();

            jarFile.close();

            return new CMenderProperties(
                    attributes.getValue("Implementation-Title"),
                    attributes.getValue("Implementation-Vendor"),
                    attributes.getValue("X-Implementation-Description"),
                    attributes.getValue("Implementation-Version"),
                    attributes.getValue("X-Release-Date"));
        } catch (IOException e) {
            CliReporting.error("could not read CMender properties: %s", e.getMessage());
            Logging.FILE_LOGGER.fatal("could not read CMender properties: {}", e.getMessage(), e);
        }

        return null;
    }
}
