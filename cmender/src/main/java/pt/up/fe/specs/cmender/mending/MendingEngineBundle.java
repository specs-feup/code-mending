package pt.up.fe.specs.cmender.mending;

import pt.up.fe.specs.cmender.data.MendingDirData;

import java.util.List;

public record MendingEngineBundle(
        CMenderReport cmenderReport,
        List<MendingDirData> mendingDirDatas
) { }
