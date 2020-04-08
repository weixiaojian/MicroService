# SpringCloud Eureka
>Eureka是Netflix的一个子模块，也是核心模块之一。Eureka是一个基于REST的服务，用于定位服务，以实现云端中间层服务发现和故障转移。  
> 服务注册与发现对于微服务架构来说是非常重要的，有了服务发现与注册，只需要使用服务的标识符，就可以访问到服务，类似zookeeper

## 三大角色：
* Eureka Server提供服务注册和发现
* Eureka Provider提供方法服务将自身的服务注册到Eureka，从而提供服务
* Service Consumer服务消费者从Eureka获取注册服务的列表，从而消费服务

## Euraka架构
* Eureka包含两个组件：Eureka Server和Eureka Client
EurekaClient是一个Java客户端，具备一个内置的、使用轮询(round-robin)负载算法的负载均衡器。在应用启动后，将会向Eureka Server发送心跳(默认周期为30秒)。如果Eureka Server在多个心跳周期内没有接收到某个节点的心跳，EurekaServer将会从服务注册表中把这个服务节点移除（默认90秒）


# 一个例子
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

* 4.配置启动类
```
@EnableEurekaClient//开启发现服务功能
@SpringBootApplication
public class CloudAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudAdminApplication.class, args);
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
    public RestTemplate restTemplate(RestTemplateBuilder builder

){
        return builder.build();

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

# 其他
## 注解使用
* `@EnableDiscoveryClient` 和`@EnableEurekaClient`，@EnableEurekaClient只适用于Eureka作为注册中心，@EnableDiscoveryClient 可以是其他注册中心。
* 从Spring Cloud Edgware开始，@EnableDiscoveryClient 或@EnableEurekaClient 可省略，只需加上相关依赖，并进行相应配置，即可将微服务注册到服务发现组件上。

## 自我保护
*  默认情况下，如果EurekaServer在一定时间内没有接收到某个微服务实例的心跳，EurekaServer将会注销该实例（默认90秒）。
*  在自我保护模式中，Eureka Server会保护服务注册表中的信息，不再注销任何服务实例。当它收到的心跳数重新恢复到阈值以上时，该Eureka Server节点就会自动退出自我保护模式。它的设计哲学就是宁可保留错误的服务注册信息，也不盲目注销任何可能健康的服务实例。-好死不如赖活着
*  `eureka.server.enable-self-preservation = false`  禁用保护模式

## 集群配置
* Eureka Server服务的yml
```
server: 
  port: 7001
eureka: 
  instance:
    hostname: eureka7001.com #eureka服务端的实例名称
  client: 
    register-with-eureka: false     #false表示不向注册中心注册自己。
    fetch-registry: false     #false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
    service-url: 
      defaultZone: http://eureka7002.com:7002/eureka/,http://eureka7003.com:7003/eureka/
      
server: 
  port: 7002
eureka: 
  instance:
    hostname: eureka7002.com #eureka服务端的实例名称
  client: 
    register-with-eureka: false     #false表示不向注册中心注册自己。
    fetch-registry: false     #false表示自己端就是注册中心，我的职责就是维护服务实例，并不需要去检索服务
    service-url: 
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7003.com:7003/eureka/
```

* 服务提供者的yml
```
eureka:
  client: #客户端注册进eureka服务列表内
    service-url: 
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/,http://eureka7003.com:7003/eureka/
  instance:
    instance-id: microservicecloud-dept8001   #自定义服务名称信息
    prefer-ip-address: true     #访问路径可以显示IP地址
```
## 和zookeeper对比
* CAP：C（一致性），A（可用性），P（分区容错）；三个指标不可能同时做到，P是无法避免的 根据情况衡量C和A
* zookeeper：保证CP，主从节点 服务器down掉就直接不可用，剩余节点会进行leader选举 选举时间内不可用（30~120s）
* Eureka：保证AP，各个节点平等，几个节点挂掉不会影响使用 只要有一台可用即可，但查询到的信息不是最新的。
1.Eureka不移除注册列表从长时间没有心跳的服务
2.Eureka仍然接受新服务的注册，但不进行同步（保证当前可用）
3.当网络稳定时，新注册服务会进行同步
* 总结：Eureka可以很好的应对因为网络故障导致部分节点失去联系的情况，而zookeeper则会直接瘫痪