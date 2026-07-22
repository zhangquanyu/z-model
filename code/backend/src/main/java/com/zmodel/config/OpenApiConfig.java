package com.zmodel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("模型管理系统 API")
                        .version("1.0.0")
                        .description("模型管理系统后端API文档，支持需求管理、模型管理、属性管理、方法管理和事件流水管理")
                        .contact(new Contact()
                                .name("Z-Model Team")
                                .email("support@z-model.com")));
    }
}
