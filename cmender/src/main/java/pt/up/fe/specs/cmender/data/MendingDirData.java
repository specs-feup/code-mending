package pt.up.fe.specs.cmender.data;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record MendingDirData(
        UUID id,
        String dirPath,
        String sourceFilePath,
        String sourceFileCopyPath,
        String mendfilePath,
        String diagsFilePath,
        List<String> mendfileCopyPaths,
        String includePath,
        String diagsDirPath,
        String mendfileCopiesDirPath,
        String sourceReportPath
) { }
