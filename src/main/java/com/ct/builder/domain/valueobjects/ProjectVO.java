package com.ct.builder.domain.valueobjects;

import java.nio.file.Path;

public record ProjectVO(Path projectTempPath, String type, String groupId, String artifactId, String name,
        String description, String packageName, String jvmVersion, String runTime, Boolean isCloudRun, Boolean isGKE) {

}
