package pt.up.fe.specs.cmender.data;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record MendingDirData(
        UUID id,
        String sourceFilePath,
        String sourceFileCopyPath,
        String mendfilePath,
        List<String> mendfileCopyPaths,
        String includePath
) { }
