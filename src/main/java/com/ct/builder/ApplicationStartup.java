package com.ct.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.ct.builder.application.internal.commandgateways.ProjectService;
import com.ct.builder.application.utils.ZipUtil;
import com.ct.builder.domain.valueobjects.ProjectVO;
import com.samskivert.mustache.Mustache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationStartup {

	private final ApplicationContext appContext;

	private final ProjectService projectService;

	private final Mustache.Compiler compiler;

	@EventListener(ApplicationReadyEvent.class)
	public void afterStartup() throws Exception {

		// ProjectVO projectVO = new ProjectVO(null, "com.demo",
		// 		"com.demo", "com.demo",
		// 		"com.demo",
		// 		"com.demo", "com.demo",
		// 		"com.demo");

		// Resource resource = new
		// ClassPathResource("templates/project/settings.gradle.mustache");
		// String templateContent = Files.readString(Path.of(resource.getURI()),
		// StandardCharsets.UTF_8);
		// log.info("templateContent={}", templateContent);
		// String outputContent = compiler.compile(templateContent).execute(projectVO);
		// log.info("outputContent={}", outputContent);
		// Path outputFile = Paths.get("/Users/samzhu/workspace/test/test-zip",
		// "settings.gradle");
		// Files.writeString(outputFile, outputContent, StandardCharsets.UTF_8,
		// StandardOpenOption.TRUNCATE_EXISTING);

		// FileOutputStream fos = new
		// FileOutputStream(Path.of("/Users/samzhu/workspace/test/test.zip").toFile());
		// ZipOutputStream zos = new ZipOutputStream(fos, StandardCharsets.UTF_8);

		// projectService.zipWholeDirectory(Path.of("/Users/samzhu/workspace/test/test-zip"),
		// Path.of("/Users/samzhu/workspace/test/test-zip").toFile(), zos);
		// zos.flush();
		// fos.flush();
		// zos.close();

		// fos.close();

		// ZipUtil.compressDirectory("/Users/samzhu/workspace/test/springboot-cloud-run-lab-2022-0221", "/Users/samzhu/workspace/test/test.zip");

		// this.compressSingleFile("/Users/samzhu/workspace/test/test-zip/settings.gradle",
		// 		"/Users/samzhu/workspace/test");

		// CreateProjectCommand createProjectCommand = new CreateProjectCommand();
		// createProjectCommand.setType("gradle-project");
		// createProjectCommand.setGroupId("com.ct");
		// createProjectCommand.setArtifactId("demo");//setting
		// createProjectCommand.setName("projectBuilder");
		// createProjectCommand.setDescription("Demo project for Spring Boot");
		// createProjectCommand.setPackageName("com.ct");
		// createProjectCommand.setJvmVersion("17");
		// createProjectCommand.setOpenAPIFIle(Paths.get("/Users/samzhu/workspace/test/projectBuilder/dev-resources/reference/openapi.yaml"));
		// createProjectCommand.setDbUrl("jdbc:postgresql://127.0.0.1/devdb");
		// createProjectCommand.setDbUsername("user1");
		// createProjectCommand.setDbPassword("pw123456");

		// projectService.create(createProjectCommand);

		// String[] beans = appContext.getBeanDefinitionNames();
		// Arrays.sort(beans);
		// for (String bean : beans) {
		// System.out.println(bean);
		// }

		// ApiGenerator apiGenerator =
		// appContext.getBean(projectBuilderProperty.getApi(), ApiGenerator.class);

		// System.out.println("apiGenerator=" + apiGenerator.toString());
	}

	public void compressSingleFile(String filePath, String outPut) {
		try {
			File file = new File(filePath);
			String zipFileName = file.getName().concat(".zip");
			System.out.println("zipFileName:" + zipFileName);

			// if you want change the menu of output ,just fix here
			// FileOutputStream fos = new FileOutputStream(zipFileName);
			FileOutputStream fos = new FileOutputStream(outPut + File.separator + zipFileName);

			ZipOutputStream zos = new ZipOutputStream(fos);

			zos.putNextEntry(new ZipEntry(file.getName()));

			byte[] bytes = Files.readAllBytes(Paths.get(filePath));
			zos.write(bytes, 0, bytes.length);
			zos.closeEntry();
			zos.close();

		} catch (FileNotFoundException ex) {
			System.err.format("The file %s does not exist", filePath);
		} catch (IOException ex) {
			System.err.println("I/O error: " + ex);
		}
	}

}