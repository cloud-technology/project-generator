package com.ct.builder.application.component;

import com.ct.builder.domain.valueobjects.RepositoryVO;

public interface RepositoryGenerator {
    public void generate(RepositoryVO repositoryVO) throws Exception;
}
