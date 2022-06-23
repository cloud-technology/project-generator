package com.ct.builder.application.component.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.ct.builder.application.component.ProjectGenerator;
import com.ct.builder.domain.valueobjects.ProjectVO;
import com.samskivert.mustache.Mustache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GradleProjectGenerator implements ProjectGenerator {

    private final Mustache.Compiler compiler;

    @Override
    public void generate(ProjectVO projectVO) throws Exception {
        Path javaPath = Path.of("src", "main", "java");
        Path javaMainPath = javaPath.resolve(projectVO.packageName().replaceAll("\\.", File.separator));
        Path configurationPath = javaPath.resolve(projectVO.packageName().replaceAll("\\.", File.separator) + File.separator + "configuration");

        this.writeTemplate(projectVO, "templates/project/application-gcp.yml.mustache", Path.of("config"));
        this.writeTemplate(projectVO, "templates/project/application-dev.yml.mustache", Path.of("config"));
        
        this.writeTemplate(projectVO, "templates/project/ApplicationStarter.java.mustache", javaMainPath);

        this.writeTemplate(projectVO, "templates/project/RedisConfig.java.mustache", configurationPath);
        this.writeTemplate(projectVO, "templates/project/RepositoryConfig.java.mustache", configurationPath);
        this.writeTemplate(projectVO, "templates/project/OpenAPIConfig.java.mustache", configurationPath);
        
        this.copyClasspathFile(projectVO, "static/application-loadtests.yml", Path.of("config"));
        this.copyClasspathFile(projectVO, "static/application-unittest.yml", Path.of("config"));
        this.copyClasspathFile(projectVO, "static/application-chaos-monkey.yml", Path.of("config"));

        this.writeTemplate(projectVO, "templates/project/settings.gradle.mustache", Path.of("."));
        this.writeTemplate(projectVO, "templates/project/build.gradle.mustache", Path.of("."));
        this.writeTemplate(projectVO, "templates/project/application.yml.mustache",
                Path.of("src", "main", "resources"));
        //
        this.copyClasspathFile(projectVO, "static/docker-compose.yml", Path.of("dev-resources"));
        this.copyClasspathFile(projectVO, "static/db.changelog-master.yaml",
                Path.of("src", "main", "resources", "db", "changelog"));
        this.copyClasspathFile(projectVO, "static/bootstrap.properties", Path.of("src", "main", "resources"));
        this.copyClasspathFile(projectVO, "static/logback-spring.xml", Path.of("src", "main", "resources"));
        //
        this.unzipDirectory(Path.of(new ClassPathResource("static/devcontainer.zip").getURI()), projectVO.projectTempPath());
        this.unzipDirectory(Path.of(new ClassPathResource("static/gradle.zip").getURI()), projectVO.projectTempPath());
    }

    private void writeTemplate(ProjectVO projectVO, String templatePath, Path outputPath)
            throws IOException {
        Path projectTempPath = projectVO.projectTempPath();
        Resource resource = new ClassPathResource(templatePath);
        String templateContent = this.readResourceToString(resource);
        String outputContent = compiler.compile(templateContent).execute(projectVO);
        Files.createDirectories(projectTempPath.resolve(outputPath));
        Files.writeString(
                projectTempPath.resolve(outputPath).resolve(resource.getFilename().replaceAll("\\.mustache", "")),
                outputContent, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);
    }

    private void copyClasspathFile(ProjectVO projectVO, String classpathFilePath, Path outputPath)
            throws IOException {
        Path projectTempPath = projectVO.projectTempPath();
        Path resourcePath = Path.of(new ClassPathResource(classpathFilePath).getURI());
        Files.createDirectories(projectTempPath.resolve(outputPath));
        Files.copy(resourcePath, projectTempPath.resolve(outputPath).resolve(resourcePath.getFileName()),
                StandardCopyOption.REPLACE_EXISTING);
    }

    private String readResourceToString(Resource resource) throws IOException {
        return Files.readString(Path.of(resource.getURI()), StandardCharsets.UTF_8);
    }

    private String readResourceToString(String path) throws IOException {
        Resource resource = new ClassPathResource(path);
        return Files.readString(Path.of(resource.getURI()), StandardCharsets.UTF_8);
        // "classpath:templates/project/settings.gradle.mustache
        // return FileUtils.readFileToString(ResourceUtils.getFile(path),
        // StandardCharsets.UTF_8);
    }

    private void unzipDirectory(Path zipFile, Path targetDirectory) throws IOException {
        if (!Files.exists(zipFile)) {
            return;
        }
        if (!Files.exists(targetDirectory)) {
            Files.createDirectory(targetDirectory);
        }
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                final Path toPath = targetDirectory.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectory(toPath);
                } else {
                    if (!Files.exists(toPath.getParent())) {
                        Files.createDirectories(toPath.getParent());
                    }
                    Files.copy(zipInputStream, toPath);
                }
            }
        }
    }

}
