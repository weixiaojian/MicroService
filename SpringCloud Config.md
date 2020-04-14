# SpringCloud Config
> SpringCloud Config为微服务架构中的微服务提供集中化的外部配置支持，配置服务器为各个不同微服务应用的所有环境提供了一个中心化的外部配置。

## 简介
* 统一为所有微服务管理application.yml配置文件，把配置文件都放到GitHub上 当其他微服务需要时 通过第三者来调取
* 优点：
    * 集中管理配置文件
    * 不同环境不同配置，配置文件动态更新dev/prod
    * 运行期间动态调整配置，直接修改GitHub上的内容
    * 配置改变不需要重启服务
    * 配置信息以REST的接口形式暴露
 
![image](https://blog.imwj.club//upload/2020/04/pg4mduufouibsru1obgn2ehoti.png)

## 创建一个SpringCloud Config服务端
### GitHub配置文件
[https://github.com/weixiaojian/MicroService/blob/master/microservicecloud-config/application.yml](https://github.com/weixiaojian/MicroService/blob/master/microservicecloud-config/application.yml)

### pom文件
```
 <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
    </dependencies>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>Camden.SR6</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

    <repositories>
        <repository>
            <id>spring-milestones</id>
            <name>Spring Milestones</name>
            <url>https://repo.spring.io/milestone</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </repository>
    </repositories>
```

### yml配置
* uri：GitHub地址
* search-paths：仓库路径
* label：仓库分支
* username：用户名（私有仓库才需要填写）
* password：密码
```
server:
  port: 3344

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/weixiaojian/MicroService/
          search-paths: microservicecloud-config
      label: master
```

### 启动类
```
/**
 * @author langao_q
 * @create 2020-04-14 11:12
 */
@SpringBootApplication
@EnableConfigServer
public class Config_3344_StartSpringCloudApp {

    public static void main(String[] args) {
        SpringApplication.run(Config_3344_StartSpringCloudApp.class, args);
    }
}
```

### 测试
启动项目以后访问：[http://localhost:3344/application-dev.yml](http://localhost:3344/application-dev.yml)

## 创建一个SpringCloud Config客户端
### GitHub配置文件
[https://github.com/weixiaojian/MicroService/blob/master/microservicecloud-config/microservicecloud-config-client.yml](https://github.com/weixiaojian/MicroService/blob/master/microservicecloud-config/microservicecloud-config-client.yml)

### pom文件
```
    <artifactId>micoservicecloud-config-client-3355</artifactId>

    <dependencies>
        <!-- SpringCloud Config客户端 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jetty</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>springloaded</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
        </dependency>
    </dependencies>
</project>
```

### yml配置
* application.yml
applicaiton.yml是用户级的资源配置项
```
spring:
  application:
    name: microservicecloud-config-client
```
* bootstrap.yml
bootstrap.yml是系统级的，优先级更加高
```
spring:
  cloud:
    config:
      name: microservicecloud-config-client #需要从github上读取的资源名称（对应github上的microservicecloud-config-client.yml）
      profile: dev   #本次访问的配置项
      label: master
      uri: http://config-3344.com:3344  #本微服务启动后先去找3344号服务，通过SpringCloudConfig获取GitHub的服务地址
```

### 启动类
```
/**
 * @author langao_q
 * @create 2020-04-14 11:52
 */
@SpringBootApplication
public class ConfigClient_3355_StartSpringCloudApp {
    public static void main(String[] args)
    {
        SpringApplication.run(ConfigClient_3355_StartSpringCloudApp.class,args);
    }
}
```

### 增加一个controller
```
/**
 * 验证是否能从github上获取配置信息
 * @author langao_q
 * @create 2020-04-14 11:51
 */
@RestController
public class ConfigClientRest {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${eureka.client.service-url.defaultZone}")
    private String eurekaServers;

    @Value("${server.port}")
    private String port;

    @RequestMapping("/config")
    public String getConfig()
    {
        String str = "applicationName: "+applicationName+"\t eurekaServers:"+eurekaServers+"\t port: "+port;
        System.out.println("******str: "+ str);
        return "applicationName: "+applicationName+"\t eurekaServers:"+eurekaServers+"\t port: "+port;
    }
}
```

### 测试
* 启动config-server-3344项目
* 启动启动本项目（config-client）
* [http://localhost:8201/config](http://localhost:8201/config)

# 创建Eureka使用Config
## pom文件
```
    <artifactId>microservicecloud-config-eureka-client-7001</artifactId>

    <dependencies>
        <!-- SpringCloudConfig配置 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka-server</artifactId>
        </dependency>
        <!-- 热部署插件 -->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>springloaded</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-devtools</artifactId>
        </dependency>
    </dependencies>
</project>
```

## yml配置
### application.yml
```
spring:
  cloud:
    config:
      name: microservicecloud-config-eureka-client  #需要从github上读取的资源名称，注意没有yml后缀名
      profile: dev
      label: master
      uri: http://config-3344.com:3344      #SpringCloudConfig获取的服务地址
```
### bootstrap.yml
```
spring:
  cloud:
    config:
      name: microservicecloud-config-eureka-client #需要从github上读取的资源名称（对应github上的microservicecloud-config-client.yml）
      profile: dev   #本次访问的配置项
      label: master
      uri: http://config-3344.com:3344  #本微服务启动后先去找3344号服务，通过SpringCloudConfig获取GitHub的服务地址
```

## 启动类
```
/**
 * @author langao_q
 * @create 2020-04-14 15:05
 */
@SpringBootApplication
@EnableEurekaServer
public class Config_Git_EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(Config_Git_EurekaServerApplication.class, args);
    }
}
```
## 测试
* 启动config-server-3344项目
* 启动config-client项目
* 启动本项目(Eureka-7001)
[http://eureka7001.com:7001/](http://eureka7001.com:7001/)

# GitHub
[https://github.com/weixiaojian/MicroService](https://github.com/weixiaojian/MicroService)