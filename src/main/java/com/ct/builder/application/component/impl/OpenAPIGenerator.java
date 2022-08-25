package com.ct.builder.application.component.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.ct.builder.application.component.ApiGenerator;
import com.ct.builder.domain.valueobjects.ApiVO;

import lombok.extern.slf4j.Slf4j;

/**
 * 產生 api 檔案
 * https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-gradle-plugin/src/main/kotlin/org/openapitools/generator/gradle/plugin/tasks/GenerateTask.kt
 * https://github.com/OpenAPITools/openapi-generator/blob/master/modules/openapi-generator-maven-plugin/src/main/java/org/openapitools/codegen/plugin/CodeGenMojo.java
 */

@Slf4j
@Component
public class OpenAPIGenerator implements ApiGenerator {
    protected Map<String, String> globalProperties = new HashMap<>();
    protected Map<String, String> environmentVariables = new HashMap<>();
    private String configurationFile;
    private String generatorName = "spring";

    @Override
    public void generate(ApiVO apiVO) throws IOException {
        // 輸入規格檔案
        File specFile = apiVO.specSource().toFile();

        if (globalProperties == null) {
            globalProperties = new HashMap<>();
            // globalProperties.put("interfaceOnly", "true");
        }

        if (environmentVariables != null && environmentVariables.size() > 0) {
            globalProperties.putAll(environmentVariables);
            System.out.println(
                    "environmentVariables is deprecated and will be removed in version 5.1. Use globalProperties instead.");
        }

        // attempt to read from config file
        CodegenConfigurator configurator = CodegenConfigurator.fromFile(configurationFile);
        // if a config file wasn't specified or we were unable to read it
        if (configurator == null) {
            configurator = new CodegenConfigurator();
        }
        if (!ObjectUtils.isEmpty(specFile)) {
            configurator.setInputSpec(specFile.getAbsolutePath());
        }

        if (StringUtils.hasText(generatorName)) {
            configurator.setGeneratorName(generatorName);
        } else {
            log.error("A generator name (generatorName) is required.");
            throw new RuntimeException(
                    "The generator requires 'generatorName'. Refer to documentation for a list of options.");
        }
        // 客製化部分
        configurator.addAdditionalProperty("disallowAdditionalPropertiesIfNotPresent", false);
        configurator.addAdditionalProperty("hateoas", false);
        configurator.addAdditionalProperty("interfaceOnly", true);
        configurator.addAdditionalProperty("singleContentTypes", true);
        configurator.addAdditionalProperty("skipDefaultInterface", true);
        // configurator.addAdditionalProperty("responseWrapper", "CompletableFuture");
        //
        configurator.setLibrary("spring-boot");
        
        configurator.setTemplateDir(Path.of(new ClassPathResource("templates/api").getURI()).toString());
        configurator.setInvokerPackage(apiVO.packageName());
        configurator.setApiPackage(apiVO.packageName()+".interfaces.rest");
        configurator.setModelPackage(apiVO.packageName()+".interfaces.rest.dto");
        Map<String, String> typeMappings = new HashMap<String,String>();
        typeMappings.put("set", "List");
        configurator.setTypeMappings(typeMappings);
        Map<String, String> instantiationTypes = new HashMap<String,String>();
        instantiationTypes.put("set", "ArrayList");
        configurator.setInstantiationTypes(instantiationTypes);
        Map<String, String> importMappings = new HashMap<String,String>();
        instantiationTypes.put("LinkedHashSet", "java.util.ArrayList");
        configurator.setImportMappings(importMappings);

        

        configurator.setOutputDir(apiVO.projectTempPath().toFile().getAbsolutePath());

        for (Map.Entry<String, String> globalPropertiesEntry : globalProperties.entrySet()) {
            String key = globalPropertiesEntry.getKey();
            String value = globalPropertiesEntry.getValue();
            if (value != null) {
                configurator.addGlobalProperty(key, value);
            }
        }

        ClientOptInput input = configurator.toClientOptInput();
        new DefaultGenerator().opts(input).generate();

        apiVO.projectTempPath().resolve("pom.xml").toFile().delete();
        
    }

}
