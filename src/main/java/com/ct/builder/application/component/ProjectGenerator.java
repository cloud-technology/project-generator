package com.ct.builder.application.component;

import com.ct.builder.domain.valueobjects.ProjectVO;

public interface ProjectGenerator {
    public void generate(ProjectVO projectVO) throws Exception;
}
