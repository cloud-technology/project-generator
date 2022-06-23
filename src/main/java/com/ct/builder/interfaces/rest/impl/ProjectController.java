package com.ct.builder.interfaces.rest.impl;

import java.nio.file.Path;

import javax.validation.Valid;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import com.ct.builder.application.internal.commandgateways.ProjectService;
import com.ct.builder.domain.commands.CreateProjectCommand;
import com.ct.builder.interfaces.rest.ProjectApi;
import com.ct.builder.interfaces.transform.ProjectDTOAssembler;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
@Tag(name = "Project", description = "the Project API")
public class ProjectController implements ProjectApi {

    private final ProjectDTOAssembler projectDTOAssembler;
    private final ProjectService projectService;

    @Override
    public ResponseEntity<Object> createProject(@Valid String type, @Valid String groupId, @Valid String artifactId,
            @Valid String name, @Valid String description, @Valid String packageName, @Valid String jvmVersion,
            MultipartFile openAPIFile, @Valid String dbUrl, @Valid String dbUsername, @Valid String dbPassword,
            @Valid String runtime) throws Exception {

        try {
            CreateProjectCommand createProjectCommand = projectDTOAssembler.toCommandFromDTO(type, groupId, artifactId,
                    name, description, packageName, jvmVersion, openAPIFile, dbUrl, dbUsername, dbPassword, runtime);
            Path zipFile = projectService.create(createProjectCommand);
            // Resource resource = new ClassPathResource("static/gradle.zip");

            Resource resource = new FileSystemResource(zipFile);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + ".zip\"");

            return new ResponseEntity<>(resource, headers, HttpStatus.CREATED);
        } catch (Exception e) {
            throw e;
        }

    }

}
