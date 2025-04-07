package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for serving static resources
 */
@Configuration("")
public class WebConfig implements WebMvcConfigurer {

    private static final String ALL = "/**";
    private static final String CLASSPATH = "classpath:/static/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(ALL)
                .addResourceLocations(CLASSPATH);
    }
}
