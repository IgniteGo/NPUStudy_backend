package com.HHStudy.npustudy;

import com.HHStudy.npustudy.configuration.property.SystemConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author HHStudyGroup
 */
@SpringBootApplication
@EnableTransactionManagement
@EnableConfigurationProperties(value = { SystemConfig.class})
@EnableCaching
public class NPUStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(NPUStudyApplication.class, args);
    }
}
