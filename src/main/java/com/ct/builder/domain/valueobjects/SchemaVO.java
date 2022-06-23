package com.ct.builder.domain.valueobjects;

import java.nio.file.Path;

public record SchemaVO(Path projectTempPath, String dbUrl, String dbUsername,
        String dbPassword) {

}
