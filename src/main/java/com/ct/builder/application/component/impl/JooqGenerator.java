package com.ct.builder.application.component.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.Configuration;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;
import org.springframework.stereotype.Component;

import com.ct.builder.application.component.RepositoryGenerator;
import com.ct.builder.domain.valueobjects.RepositoryVO;

import lombok.extern.slf4j.Slf4j;

/**
 * https://www.jooq.org/doc/latest/manual/code-generation/codegen-programmatic/
 * https://www.jooq.org/doc/latest/manual/code-generation/codegen-configuration/
 * https://www.jooq.org/doc/latest/manual/code-generation/codegen-custom-code/
 */
@Slf4j
@Component
public class JooqGenerator implements RepositoryGenerator {

	@Override
	public void generate(RepositoryVO repositoryVO) throws Exception {
		log.debug("repositoryVO={}", repositoryVO);
		Generate generate = new Generate();
		generate.setRecords(Boolean.FALSE);
		generate.setPojos(Boolean.TRUE);
		generate.setPojosToString(Boolean.FALSE);
		generate.setJavaTimeTypes(Boolean.TRUE);
		generate.setJpaAnnotations(Boolean.TRUE);
		generate.setJpaVersion("2");
		generate.withValidationAnnotations(Boolean.TRUE);
		generate.setSpringAnnotations(Boolean.TRUE);
		generate.setGlobalObjectReferences(Boolean.FALSE);
		Configuration configuration = new org.jooq.meta.jaxb.Configuration()

				// Configure the database connection here
				.withJdbc(new Jdbc()
						.withDriver("org.postgresql.Driver")
						.withUrl(repositoryVO.dbUrl())
						.withUser(repositoryVO.dbUsername())
						.withPassword(repositoryVO.dbPassword()))
				.withGenerator(new Generator()
						.withName("com.ct.builder.application.component.jooq.JooqJavaGenerator")
						.withDatabase(new Database()
								.withName("org.jooq.meta.postgres.PostgresDatabase")
								.withIncludes(".*")
								.withExcludes("flyway_schema_history | databasechangelog | databasechangeloglock")
								.withInputSchema("public"))
						.withGenerate(generate)
						.withTarget(new Target()
								.withPackageName(repositoryVO.packageName() + ".infrastructure.repositories")
								.withDirectory(
										repositoryVO.projectTempPath() + "/src/main/java")));

		GenerationTool.generate(configuration);

		Path repositoriePath = Paths.get(repositoryVO.projectTempPath() + "/src/main/java/"
				+ repositoryVO.packageName().replaceAll("\\.", File.separator) + File.separator + "infrastructure" + File.separator +"repositories");
		log.debug("repositoriePath={}", repositoriePath);
		Paths.get(repositoriePath + "/DefaultCatalog.java").toFile().delete();
		Paths.get(repositoriePath + "/Public.java").toFile().delete();

		Path tablesPath = Paths.get(repositoriePath + "/tables");
		if (null != tablesPath && null != tablesPath.toFile().listFiles()) {
			Arrays.stream(tablesPath.toFile().listFiles())
					.filter(File::isFile)
					.forEach(File::delete);
		}

		Path pojosPath = Path.of(repositoriePath.toString(), "tables", "pojos");

		this.convertJakartaToJavax(pojosPath);

		Path repositoryTempPath = Path.of(repositoryVO.projectTempPath().toString(), "RepositoryTemp");

		File[] files = repositoryTempPath.toFile().listFiles();
		if (null != files) {
			for (File file : files) {
				if (file.isFile()) {
					log.debug("move to {}", Path.of(repositoriePath.toString(), file.getName()));
					Files.move(file.toPath(), Path.of(repositoriePath.toString(), file.getName()),
							StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}

	}

	private void convertJakartaToJavax(Path pojosPath) throws IOException {
		File[] files = pojosPath.toFile().listFiles();
		if (files != null) {
			for (File file : files) {
				String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
				String replaceContent = StringUtils.replace(content, "jakarta.persistence.Column",
						"javax.persistence.Column");
				replaceContent = StringUtils.replace(replaceContent, "jakarta.persistence.Entity",
						"javax.persistence.Entity");
				replaceContent = StringUtils.replace(replaceContent, "jakarta.persistence.Id", "javax.persistence.Id");
				replaceContent = StringUtils.replace(replaceContent, "jakarta.persistence.Table",
						"javax.persistence.Table");
				replaceContent = StringUtils.replace(replaceContent, "jakarta.persistence.UniqueConstraint",
						"javax.persistence.UniqueConstraint");
				replaceContent = StringUtils.replace(replaceContent, "jakarta.validation.constraints.NotNull",
						"javax.validation.constraints.NotNull");
				replaceContent = StringUtils.replace(replaceContent, "jakarta.validation.constraints.Size",
						"javax.validation.constraints.Size");
				Files.writeString(file.toPath(), replaceContent, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
						StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
			}
		}
	}

}
