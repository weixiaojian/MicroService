# SpringCloud Eureka
> spring cloud 为开发人员提供了快速构建分布式系统的一些工具，  
包括配置管理、服务发现、断路器、路由、微代理、事件总线、全局锁、决策竞选、分布式会话等等
* Eureka：服务治理组件，包含服务注册与发现  
* Hystrix：容错管理组件，实现了熔断器  
* Ribbon：客户端负载均衡的服务调用组件  
* Feign：基于Ribbon和Hystrix的声明式服务调用组件  
* Zuul：网关组件，提供智能路由、访问过滤等功能  
* Archaius：外部化配置组件  
* Spring Cloud Config：配置管理工具，实现应用配置的外部化存储，支持客户端配置信息刷新、加密/解密配置内容等。  
* Spring Cloud Bus：事件、消息总线，用于传播集群中的状态变化或事件，以及触发后续的处理  
* Spring Cloud Security：基于spring security的安全工具包，为我们的应用程序添加安全控制  
* Spring Cloud Consul : 封装了Consul操作，Consul是一个服务发现与配置工具（与Eureka作用类似），与Docker容器可以无缝集成  

## 服务注册中心Eureka
* 1.导入maven依赖
```
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>
```

* 2.配置yml  
通过eureka.client.registerWithEureka：false和fetchRegistry：false来表明自己是一个eureka server.
```
server:
  port: 8761
eureka:
  instance:
    hostname: eureka-server  # eureka实例的主机名
  client:
    register-with-eureka: false #不把自己注册到eureka上
    fetch-registry: false #不从eureka上来获取服务的注册信息
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

* 3.配置启动类`@EnableEurekaServer`
```
@EnableEurekaServer
@SpringBootApplication
public class CurekaServcerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurekaServcerApplication.class, args);
    }
}
```

* 4.访问Eureka [http://localhost:8761](http://localhost:8761)

## 服务提供者
* 1.导入maven依赖
```
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
```

* 2.配置yml  
需要指明spring.application.name,这个很重要，这在以后的服务与服务之间相互调用一般都是根据这个name
```
server:
  port: 8001
spring:
  application:
    name: cloud-provider
eureka:
  instance:
    prefer-ip-address: true # 注册服务的时候使用服务的ip地址
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

* 3.controller&service    
其实就和普通的web工程没有区别，消费者根据controller提供的接口调用即可
```
@RestController
public class BookController {

    @Autowired
    BookService bookService;

    @RequestMapping("/book")
    public String book(){
        return bookService.getBook();
    }
}

@Service
public class BookService {

    public String getBook(){
        return "《MacBook Pro》";
    }
}
```

## 服务消费者
* 1.导入maven依赖
```
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>

    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
```

* 2.配置yml
```
spring:
  application:
    name: cloud-admin
server:
  port: 8200

eureka:
  instance:
    prefer-ip-address: true # 注册服务的时候使用服务的ip地址
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

* 3.配置启动类`@EnableDiscoveryClient` 开启发现服务功能  
```
@SpringBootApplication
@EnableDiscoveryClient //开启发现服务功能
public class CloudAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudAdminApplication.class, args);
    }

    @LoadBalanced //使用负载均衡机制
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
```

* 4.注入使用
```
@RestController
public class HelloController {

    @Autowired
    RestTemplate restTemplate;

    @RequestMapping("/hello")
    public String hello(@RequestParam(defaultValue = "langao")String name){
        String book = restTemplate.getForObject("http://cloud-provider/book", String.class);
        return name + book;
    }
}
```

* 5.项目访问 [http://localhost:8200/hello](http://localhost:8200/hello)
