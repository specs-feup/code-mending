package pt.up.fe.specs.cmender.diag.args;

public record AddrSpaceArg(
        String addrSpace

) implements DiagnosticArg {

    @Override
    public DiagnosticArgKind kind() {
        return DiagnosticArgKind.ADDR_SPACE;
    }
}
