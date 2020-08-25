package com.java.client;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Scanner;

@Configuration
@EnableConfigurationProperties({AppConfig.Manager.class})
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Scanner scanner() {
        return new Scanner(System.in);
    }

    @Data
    @ConfigurationProperties("messenger")
    public static class Manager {
        private String host;
        private String port;

        public String getCompleteUrl() {
            return "http://" + host + ":" + port;
        }
    }
}
