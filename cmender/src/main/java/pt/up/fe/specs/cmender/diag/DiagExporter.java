package pt.up.fe.specs.cmender.diag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import pt.up.fe.specs.cmender.logging.Logging;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Pattern;

public class DiagExporter {
    private final String executablePath;

    public DiagExporter(String executablePath) {
        this.executablePath = executablePath;
    }

    public DiagExporterResult run(DiagExporterInvocation invocation) throws DiagExporterException {
        var args = invocation.asInvocationArgs();
        args.addFirst(executablePath);

        Logging.FILE_LOGGER.debug(invocation);
        Logging.FILE_LOGGER.debug("diag-exporter invocation: {} {}", executablePath, invocation.asInvocationArgsString());

        var processBuilder = new ProcessBuilder(args)
                .redirectErrorStream(true);

        // TODO save stdout and stderr in logs

        int exitCode = 0;
        var processOutputBuilder = new StringBuilder();

        try {
            var process = processBuilder.start();

            try (var bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    processOutputBuilder.append(line).append(System.lineSeparator());
                }
            }

            exitCode = process.waitFor();

            if (exitCode != 0) {
                throw DiagExporterException.ofBadProcessExitCode(exitCode, processOutputBuilder.toString());
            }
        } catch (IOException | InterruptedException e) {
            throw DiagExporterException.ofProcessException(e);
        }

        var results = invocation.isHelp() || invocation.isVersion()?
                null : readResults(invocation.outputFilepath());

        return new DiagExporterResult(processOutputBuilder.toString(), results);
    }

    // TODO maybe a method to extract the version
    public boolean verify() {
        try {
            var versionResult = run(DiagExporterInvocation.builder().isVersion(true).build());

            var versionResultOutput = versionResult.processOutput();

            var pattern = Pattern.compile("\\bversion\\s\\d+(\\.\\d+)", Pattern.CASE_INSENSITIVE);
            var matcher = pattern.matcher(versionResultOutput);

            return matcher.find();
        } catch (DiagExporterException e) {
            e.printStackTrace();
            return false;
        }
    }

    // TODO think if this should throw a IllegalStateException or a custom DiagExporterException
    private static List<DiagExporterSingleSourceResult> readResults(String outputFilepath) {
        var mapper = new ObjectMapper();
        // mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        try (var bufferedReader = new BufferedReader(new FileReader(outputFilepath))) {
            return mapper.readValue(bufferedReader, new TypeReference<>() {});
        } catch (IOException e) {
            Logging.FILE_LOGGER.error("cannot read output file: {}", outputFilepath, e);
            throw new IllegalStateException(
                    String.format("cannot read output file: %s", outputFilepath), e);
        }
    }
}
