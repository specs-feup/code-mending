package pt.up.fe.specs.cmender.diag;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.io.TempDir;
import pt.up.fe.specs.cmender.SourceFiles;
import pt.up.fe.specs.cmender.TestConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class DiagExporterTest {

    private static final TestConfig config = TestConfig.builder().build();

    private static final String MISSING_DIAG_EXPORTER_PATH = Paths.get("src/test/resources/diag-exporter-doesnt-exist").toAbsolutePath().toString();

    private static final String INVALID_DIAG_EXPORTER_PATH = Paths.get("src/test/resources/invalid-diag-exporter").toAbsolutePath().toString();

    @TempDir
    private Path testTempDir;

    @BeforeAll
    public static void verifyTestConfig() {
        config.verifyTestDiagExporter();
    }

    @Test
    public void testVerifyWhenDiagExporterNotFound() {
        var diagExporter = new DiagExporter(MISSING_DIAG_EXPORTER_PATH);

        assertFalse(diagExporter.verify());
    }

    @Test
    public void testRunWhenDiagExporterNotFound() {
        var diagExporter = new DiagExporter(MISSING_DIAG_EXPORTER_PATH);

        var versionInvocation = DiagExporterInvocation.builder()
                                    .isVersion(true)
                                    .build();

        var exception =
                assertThrows(DiagExporterException.class,
                        () -> diagExporter.run(versionInvocation));

        assertThat(exception.getMessage(), is("a diag-exporter process exception occurred"));

        var exportInvocation = DiagExporterInvocation.builder()
                                    .files(SourceFiles.getSourceFilepaths(SourceFiles.EMPTY_MAIN_C))
                                    .build();

        exception =
                assertThrows(DiagExporterException.class,
                        () -> diagExporter.run(exportInvocation));

        assertThat(exception.getMessage(), is("a diag-exporter process exception occurred"));
    }

    @Test
    public void testVerifyWhenDiagExporterIsInvalid() {
        var diagExporter = new DiagExporter(INVALID_DIAG_EXPORTER_PATH);

        assertFalse(diagExporter.verify());
    }

    @Test
    public void testRunWhenDiagExporterIsInvalid() {
        var diagExporter = new DiagExporter(INVALID_DIAG_EXPORTER_PATH);

        var versionInvocation = DiagExporterInvocation.builder()
                                    .isVersion(true)
                                    .build();

        var exception =
                assertThrows(DiagExporterException.class,
                        () -> diagExporter.run(versionInvocation));

        assertThat(exception.getMessage(), is("a diag-exporter process exception occurred"));

        var exportInvocation = DiagExporterInvocation.builder()
                                    .files(SourceFiles.getSourceFilepaths(SourceFiles.EMPTY_MAIN_C))
                                    .build();

        exception =
                assertThrows(DiagExporterException.class,
                        () -> diagExporter.run(exportInvocation));

        assertThat(exception.getMessage(), is("a diag-exporter process exception occurred"));
    }

    @Test
    public void testRunWhenDiagExporterIsValid() {
        var diagExporter = new DiagExporter(config.diagExporterPath());

        var exportInvocation = DiagExporterInvocation.builder()
                                    .files(SourceFiles.getSourceFilepaths(SourceFiles.EMPTY_MAIN_C))
                                    .outputFilepath(
                                            Path.of(testTempDir.toString(), "test-run-when-dex-valid-",
                                                    UUID.randomUUID().toString()).toString())
                                    .build();

        var exportResult = assertDoesNotThrow(() -> diagExporter.run(exportInvocation));

        System.out.println(exportResult);
        assertNotNull(exportResult.processOutput());
        assertNotNull(exportResult.sourceResults());
        assertThat(exportResult.sourceResults(), hasSize(1));
    }

    @Test
    public void testRunWhenNoFilesProvided() {
        var diagExporter = new DiagExporter(config.diagExporterPath());

        var exportInvocation = DiagExporterInvocation.builder()
                                    .outputFilepath(Path.of(testTempDir.toString(), "test-run-when-no-files-provided-",
                                            UUID.randomUUID().toString()).toString())
                                    .build();

        var exception = assertThrows(DiagExporterException.class, () -> diagExporter.run(exportInvocation));

        assertThat(exception.getMessage(), is("diag-exporter process exited with code 1"));
    }
}
