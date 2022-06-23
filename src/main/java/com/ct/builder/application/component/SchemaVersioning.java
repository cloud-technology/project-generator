package com.ct.builder.application.component;

import com.ct.builder.domain.valueobjects.SchemaVO;

public interface SchemaVersioning {
    public void generate(SchemaVO schemaVO) throws Exception;
}
