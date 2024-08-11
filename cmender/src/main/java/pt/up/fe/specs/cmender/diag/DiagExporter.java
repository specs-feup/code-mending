package pt.up.fe.specs.cmender.diag;

import pt.up.fe.specs.cmender.logging.Logging;

import java.io.IOException;

public class DiagExporter {
    private final String executablePath;

    public DiagExporter(String executablePath) {
        this.executablePath = executablePath;
    }

    public DiagExporterResult run(DiagExporterInvocation invocation) {
        var args = invocation.asInvocationArgs();
        args.addFirst(executablePath);

        Logging.FILE_LOGGER.debug(invocation);
        Logging.FILE_LOGGER.debug("diag-exporter invocation: {} {}", executablePath, invocation.asInvocationArgsString());

        var processBuilder = new ProcessBuilder(args)
                .redirectErrorStream(false);

        // TODO save stdout and stderr in logs

        int exitCode = 0;

        try {
            var process = processBuilder.start();

            exitCode = process.waitFor();

            if (exitCode != 0) {
                System.err.println("error: diag-exporter process exited with code " + exitCode);
                Logging.FILE_LOGGER.error("diag-exporter process exited with code {}", exitCode);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("error: diag-exporter process exception: " + e.getMessage());
            Logging.FILE_LOGGER.error("diag-exporter process exception: {}", e.getMessage(), e);
        }

        return new DiagExporterResult(exitCode, invocation.getOutputFilepath());
    }
}
