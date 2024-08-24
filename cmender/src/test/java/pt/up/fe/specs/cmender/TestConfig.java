package pt.up.fe.specs.cmender;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

import pt.up.fe.specs.cmender.diag.DiagExporter;

@Builder
@Getter
@Accessors(fluent = true)
public class TestConfig {
    public static final String DIAG_EXPORTER_PATH_PROPERTY = "diagExporter.path";

    @Builder.Default
    private final String diagExporterPath = System.getProperty(DIAG_EXPORTER_PATH_PROPERTY);

    public void verifyTestDiagExporter() {
        if (diagExporterPath() == null) {
            throw new IllegalStateException(String.format("No '%s' property configured", TestConfig.DIAG_EXPORTER_PATH_PROPERTY));
        }

        var diagExporter = new DiagExporter(diagExporterPath);

        if (!diagExporter.verify()) {
            throw new IllegalStateException("provided diag-exporter failed the verification");
        }

        System.out.printf("Using diag-exporter in '%s' for testing%n", diagExporterPath());
    }
}
