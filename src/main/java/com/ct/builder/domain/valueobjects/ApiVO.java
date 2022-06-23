package com.ct.builder.domain.valueobjects;

import java.nio.file.Path;

public record ApiVO(Path projectTempPath, Path specSource, String packageName) {
}
