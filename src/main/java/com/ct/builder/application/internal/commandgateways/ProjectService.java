package com.ct.builder.application.internal.commandgateways;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.ct.builder.application.component.ApiGenerator;
import com.ct.builder.application.component.ProjectGenerator;
import com.ct.builder.application.component.RepositoryGenerator;
import com.ct.builder.application.component.SchemaVersioning;
import com.ct.builder.application.property.ProjectBuilderProperty;
import com.ct.builder.application.utils.ZipUtil;
import com.ct.builder.domain.commands.CreateProjectCommand;
import com.ct.builder.domain.valueobjects.ApiVO;
import com.ct.builder.domain.valueobjects.ProjectVO;
import com.ct.builder.domain.valueobjects.RepositoryVO;
import com.ct.builder.domain.valueobjects.SchemaVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {
        private final ApplicationContext appContext;
        private final ProjectBuilderProperty projectBuilderProperty;

        public Path create(CreateProjectCommand createProjectCommand) throws Exception {

                ProjectGenerator projectGenerator = appContext.getBean("gradleProjectGenerator",
                                ProjectGenerator.class);

                Path projectTempPath = Files.createTempDirectory("project_");
                log.info("projectTempPath={}", projectTempPath.toAbsolutePath());
                ProjectVO projectVO = new ProjectVO(projectTempPath, createProjectCommand.getType(),
                                createProjectCommand.getGroupId(), createProjectCommand.getArtifactId(),
                                createProjectCommand.getName(),
                                createProjectCommand.getDescription(), createProjectCommand.getPackageName(),
                                createProjectCommand.getJvmVersion(), createProjectCommand.getRuntime(), createProjectCommand.getIsCloudRun(),createProjectCommand.getIsGKE());
                projectGenerator.generate(projectVO);
                //
                if (!ObjectUtils.isEmpty(createProjectCommand.getOpenAPIFIle())) {
                        ApiGenerator apiGenerator = appContext.getBean(projectBuilderProperty.getApi(),
                                        ApiGenerator.class);
                        ApiVO apiVO = new ApiVO(projectTempPath, createProjectCommand.getOpenAPIFIle(),
                                        createProjectCommand.getPackageName());
                        apiGenerator.generate(apiVO);
                }
                if (StringUtils.hasText(createProjectCommand.getDbUrl())
                                && StringUtils.hasText(createProjectCommand.getDbUsername())
                                && StringUtils.hasText(createProjectCommand.getDbPassword())) {
                        RepositoryGenerator repositoryGenerator = appContext.getBean(
                                        projectBuilderProperty.getRepository(),
                                        RepositoryGenerator.class);
                        RepositoryVO repositoryVO = new RepositoryVO(projectTempPath,
                                        createProjectCommand.getPackageName(),
                                        createProjectCommand.getDbUrl(), createProjectCommand.getDbUsername(),
                                        createProjectCommand.getDbPassword());
                        repositoryGenerator.generate(repositoryVO);
                        //
                        SchemaVersioning schemaVersioning = appContext.getBean(
                                        projectBuilderProperty.getSchemaVersioning(),
                                        SchemaVersioning.class);
                        SchemaVO schemaVO = new SchemaVO(projectTempPath,
                                        createProjectCommand.getDbUrl(), createProjectCommand.getDbUsername(),
                                        createProjectCommand.getDbPassword());
                        schemaVersioning.generate(schemaVO);
                }
                // 壓縮
                Path zipFile = Files.createTempFile(createProjectCommand.getName(), ".zip");
                ZipUtil.compressDirectory(projectTempPath, zipFile);
                return zipFile;
        }
}
