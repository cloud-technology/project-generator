package com.ct.builder.application.component;

import com.ct.builder.domain.valueobjects.ApiVO;

public interface ApiGenerator {
    public void generate(ApiVO apiVO) throws Exception;
}
