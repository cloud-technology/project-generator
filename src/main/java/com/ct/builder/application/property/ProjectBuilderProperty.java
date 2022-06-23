package com.ct.builder.application.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "project-builder")
public class ProjectBuilderProperty {
    private String api;
    private String repository;
    private String schemaVersioning;
}
