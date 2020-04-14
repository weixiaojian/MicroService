package io.imwj.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author langao_q
 * @create 2020-04-14 15:05
 */
@SpringBootApplication
@EnableEurekaServer
public class Config_Git_EurekaServerApplication_7001 {
    public static void main(String[] args) {
        SpringApplication.run(Config_Git_EurekaServerApplication_7001.class, args);
    }
}
