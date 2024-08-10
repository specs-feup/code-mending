package pt.up.fe.specs.cmender;

import pt.up.fe.specs.cmender.logging.Logging;

import java.io.IOException;
import java.util.jar.JarFile;

public record CMenderProperties (
        String name,
        String vendor,
        String description,
        String version,
        String date
) {
    public static CMenderProperties get() {
        var logger = Logging.FILE_LOGGER;

        try {
            var jarFilepath = CMenderProperties.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            var jarFile = new JarFile(jarFilepath);
            var manifest = jarFile.getManifest();

            if (manifest == null) {
                System.err.println("error: could not read CMender properties: manifest is missing");
                logger.fatal("could not read CMender properties: manifest is missing");
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
            System.err.println("error: could not read CMender properties: " + e.getMessage());
            logger.fatal("could not read CMender properties: {}", e.getMessage(), e);
        }

        return null;
    }
}
