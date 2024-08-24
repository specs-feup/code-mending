package pt.up.fe.specs.cmender.diag;

import pt.up.fe.specs.cmender.diag.args.DiagnosticArg;

import java.util.List;

public record DiagnosticMessage(
        String text,
        String format,
        List<DiagnosticArg> args
) { }
