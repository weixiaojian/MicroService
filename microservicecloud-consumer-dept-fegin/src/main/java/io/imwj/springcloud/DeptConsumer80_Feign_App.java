package io.imwj.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * io.imwj.springcloud是api所在包 已经启动类所在包
 * @author langao_q
 * @create 2020-04-07 14:52
 */
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients(basePackages= {"io.imwj.springcloud"})
@ComponentScan("io.imwj.springcloud")
public class DeptConsumer80_Feign_App {

    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer80_Feign_App.class, args);
    }
}
