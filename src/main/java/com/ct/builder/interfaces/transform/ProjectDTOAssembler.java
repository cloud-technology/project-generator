package com.ct.builder.interfaces.transform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ct.builder.domain.commands.CreateProjectCommand;
import com.ct.builder.domain.commands.CreateProjectCommand.CreateProjectCommandBuilder;

@Component
public class ProjectDTOAssembler {
    public CreateProjectCommand toCommandFromDTO(String type, String groupId, String artifactId,
            String name, String description, String packageName, String jvmVersion,
            MultipartFile openAPIFile, String dbUrl, String dbUsername, String dbPassword,
            String runtime) throws IOException {

        CreateProjectCommandBuilder builder = CreateProjectCommand.builder();
        builder.type(type).groupId(groupId).artifactId(artifactId).name(name).description(description)
                .packageName(packageName).jvmVersion(jvmVersion);

        if (!ObjectUtils.isEmpty(openAPIFile)) {
            Path projectTempPath = Files.createTempDirectory("upload_");
            Path openAPIFIle = projectTempPath.resolve(openAPIFile.getOriginalFilename());
            Files.copy(openAPIFile.getInputStream(), openAPIFIle);
            builder.openAPIFIle(openAPIFIle);
        }

        if(StringUtils.hasText(dbUrl) && StringUtils.hasText(dbUsername) && StringUtils.hasText(dbPassword)){
            builder.dbUrl(dbUrl).dbUsername(dbUsername).dbPassword(dbPassword);
        }

        if(StringUtils.hasText(runtime) && runtime.equalsIgnoreCase("GKE")){
            builder.runtime(runtime);
            builder.isGKE(true);
        }else{
            builder.runtime("CloudRun");
            builder.isCloudRun(true);
        }

        return builder.build();
    }

}
