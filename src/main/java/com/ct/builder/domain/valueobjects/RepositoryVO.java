package com.ct.builder.domain.valueobjects;

import java.nio.file.Path;

public record RepositoryVO(Path projectTempPath, String packageName, String dbUrl, String dbUsername,
        String dbPassword) {

}
