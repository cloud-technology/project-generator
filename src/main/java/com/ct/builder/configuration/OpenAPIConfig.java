package com.ct.builder.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;

@Configuration
@Profile({"dev", "unittest"})

// https://deepak-shinde.medium.com/migrating-from-springfox-swagger-2-to-springdoc-openapi-3-79a79757b8d1
// https://www.jianshu.com/p/0c09b675c2d3
// https://stackoverflow.com/questions/60869480/default-response-class-in-springdoc
// https://chenhe.me/post/springdoc-apidoc-gen-migrate-from-springfox/
// https://stackoverflow.com/questions/61792071/how-to-create-a-link-in-openapi-3-0-with-springdoc
// https://swagger.io/docs/specification/links/
// https://github.com/springdoc/springdoc-openapi-gradle-plugin

public class OpenAPIConfig {

  private static final String TITLE = "Init project Demo";
  private static final String DESCRIPTION = "Quickly complete the initial project setup";
  private static final String VERSION = "1.0.1";
  private static final String TERMS_OF_SERVICE_URL = "";
  private static final String LICENSE_NAME = "Apache 2.0";
  private static final String LICENSE_URL = "https://www.apache.org/licenses/LICENSE-2.0";
  private static final String CONTACT_NAME = "sam";
  private static final String CONTACT_URL = "https://blog.samzhu.dev";
  private static final String CONTACT_EMAIL = "spike19820318@gmail.com";
  private static final String SECURITY_SCHEME_NAME = "OAuth";
  private static final String BASIC_AUTH_NAME = "BasicAuth";
  private static final String BEARER_TOKEN_NAME = "BearerToken";

  private static final String AUTHORIZATION_URL = "https://accounts.google.com/o/oauth2/v2/auth";

  @Bean
  public OpenAPI springShopOpenAPI() {
    OAuthFlow oAuthFlow = new OAuthFlow();
    oAuthFlow.authorizationUrl(AUTHORIZATION_URL);

    return new OpenAPI()
        .info(this.apiInfo())
        .externalDocs(
            new ExternalDocumentation()
                .description("SpringShop Wiki Documentation")
                .url("https://springshop.wiki.github.org/docs"))
        // .components(
        //     new Components()
        //         .addSecuritySchemes(
        //             SECURITY_SCHEME_NAME,
        //             new SecurityScheme()
        //                 .type(Type.OAUTH2)
        //                 .scheme("bearer")
        //                 .bearerFormat("jwt")
        //                 .in(In.HEADER)
        //                 .name("Authorization")
        //                 .flows(new OAuthFlows().implicit(oAuthFlow))))
        .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
  }

  private Info apiInfo() {
    Contact contact = new Contact();
    contact.setEmail(CONTACT_EMAIL);
    contact.setName(CONTACT_NAME);
    contact.setUrl(CONTACT_URL);
    return new Info()
        .title(TITLE)
        .version(VERSION)
        .contact(contact)
        .description(DESCRIPTION)
        .license(new License().name(LICENSE_NAME).url(LICENSE_URL));
  }

//   @Bean
//   public OpenApiCustomiser customerGlobalHeaderOpenApiCustomiser() {
//     Schema<ErrorMessage> errorResponseSchema = new Schema<ErrorMessage>();
//     errorResponseSchema.setName("Error");
//     errorResponseSchema.set$ref("#/components/schemas/ErrorMessage");
//     return openApi ->
//         openApi
//             .getPaths()
//             .values()
//             .forEach(
//                 pathItem ->
//                     pathItem
//                         .readOperations()
//                         .forEach(
//                             operation -> {
//                               ApiResponses apiResponses = operation.getResponses();
//                               this.createApiResponse(
//                                   apiResponses, HttpStatus.BAD_REQUEST, errorResponseSchema);
//                               this.createApiResponse(
//                                   apiResponses, HttpStatus.UNAUTHORIZED, errorResponseSchema);
//                               this.createApiResponse(
//                                   apiResponses, HttpStatus.FORBIDDEN, errorResponseSchema);
//                               this.createApiResponse(
//                                   apiResponses, HttpStatus.NOT_FOUND, errorResponseSchema);
//                               this.createApiResponse(
//                                   apiResponses,
//                                   HttpStatus.INTERNAL_SERVER_ERROR,
//                                   errorResponseSchema);
//                             }));
//   }

//   private void createApiResponse(ApiResponses apiResponses, HttpStatus httpStatus, Schema schema) {
//     MediaType mediaType = new MediaType();
//     mediaType.schema(schema);
//     ApiResponse ApiResponse =
//         new ApiResponse()
//             .description(httpStatus.getReasonPhrase())
//             .content(new Content().addMediaType("application/json", mediaType));
//     apiResponses.addApiResponse(String.valueOf(httpStatus.value()), ApiResponse);
//   }

//   private ApiResponse createApiResponse(String message, Schema<ErrorMessage> schema) {
//     MediaType mediaType = new MediaType();
//     mediaType.schema(schema);
//     return new ApiResponse()
//         .description(message)
//         .content(new Content().addMediaType("application/json", mediaType));
//   }

  // @Bean
  // public GroupedOpenApi publicApi() {
  // return GroupedOpenApi.builder()
  // .group("public-apis")
  // .pathsToMatch("/**")
  // .pathsToExclude("/actuator/**")
  // .build();
  // }

  // @Bean
  // public GroupedOpenApi actuatorApi() {
  // return GroupedOpenApi.builder()
  // .group("actuators")
  // .pathsToMatch("/actuators/**")
  // .build();
  // }

}
