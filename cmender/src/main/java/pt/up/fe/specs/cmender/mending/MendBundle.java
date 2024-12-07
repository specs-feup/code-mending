package pt.up.fe.specs.cmender.mending;

import pt.up.fe.specs.cmender.data.MendingDirData;

public record MendBundle(
        SourceResult sourceResult,
        MendingDirData mendingDirData
) { }
