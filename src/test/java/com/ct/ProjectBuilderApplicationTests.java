package com.ct;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("unittest")
@ExtendWith(SpringExtension.class)
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ProjectBuilderApplicationTests {

	@Container
	private static final PostgreSQLContainer database = new PostgreSQLContainer("postgres:14.2")
			.withDatabaseName("testdb");

	@Autowired
	private TestRestTemplate testRestTemplate;

	@DynamicPropertySource
	static void mysqlProperties(DynamicPropertyRegistry registry) {
		log.debug(
				"database url={}, username={}, password={}",
				database.getJdbcUrl(),
				database.getUsername(),
				database.getPassword());
		// registry.add("spring.datasource.url", database::getJdbcUrl);
		// registry.add("spring.datasource.username", database::getUsername);
		// registry.add("spring.datasource.password", database::getPassword);
	}

	@Test
	@DisplayName("All test")
	public void contextLoads() {
		assertAll("All test", () -> assertEquals(1, 1));
	}

}
