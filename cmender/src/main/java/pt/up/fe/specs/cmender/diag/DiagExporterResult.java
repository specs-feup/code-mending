package pt.up.fe.specs.cmender.diag;

import java.util.List;

// TODO maybe also have separate stdout and stderr
public record DiagExporterResult(
        String processOutput,
        List<DiagExporterSourceResult> sourceResults
) { }
