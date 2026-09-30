package com.backend.unab.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

	private static final String BASIC_AUTH_SCHEME = "basicAuth";

	@Bean
	public OpenAPI veterinaryOpenApi() {
		return new OpenAPI()
				.info(new Info()
						.title("Veterinary System API")
						.version("1.0.0")
						.description("Formal OpenAPI 3 documentation for the veterinary system REST API.")
						.contact(new Contact().name("Veterinary System Team")))
				.addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH_SCHEME))
				.components(new Components().addSecuritySchemes(BASIC_AUTH_SCHEME,
						new SecurityScheme()
								.name(BASIC_AUTH_SCHEME)
								.type(SecurityScheme.Type.HTTP)
								.scheme("basic")));
	}

	@Bean
	public GroupedOpenApi veterinaryApiGroup() {
		return GroupedOpenApi.builder()
				.group("veterinary-system")
				.pathsToMatch("/api/**")
				.build();
	}
}
