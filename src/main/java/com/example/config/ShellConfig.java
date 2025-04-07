package com.example.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.boot.SpringShellAutoConfiguration;

@Configuration
@ConditionalOnProperty(name = "app.mode", havingValue = "shell")
@EnableAutoConfiguration(exclude = {SpringShellAutoConfiguration.class})
public class ShellConfig {
    /*
    this class must be empty
     */

}
