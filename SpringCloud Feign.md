# SpringCloud Feign
>  Feign是一个声明式WebService客户端。使用Feign能让编写Web Service客户端更加简单，只需要创建一个接口，然后在上面添加注解即可。
Feign通过接口的方法调用Rest服务（之前是Ribbon+RestTemplate）

## Feign简介
* 在使用Ribbon+RestTemplate时，利用RestTemplate对http请求的封装处理，形成了一套模版化的调用方法。
但是在实际开发中，由于对服务依赖的调用可能不止一处，<u>往往一个接口会被多处调用，
所以通常都会针对每个微服务自行封装一些客户端类来包装这些依赖服务的调用</u>。
所以，Feign在此基础上做了进一步封装，由他来帮助我们定义和实现依赖服务接口的定义。
在Feign的实现下，<u>我们只需创建一个接口并使用注解的方式来配置它(以前是Dao接口上面标注Mapper注解,
现在是一个微服务接口上面标注一个Feign注解即可)</u>，即可完成对服务提供方的接口绑定，简化了使用Spring cloud Ribbon时，
自动封装服务调用客户端的开发量。
 
 
* Feign集成了Ribbon
利用Ribbon维护了MicroServiceCloud-Dept的服务列表信息，并且通过轮询实现了客户端的负载均衡。
而与Ribbon不同的是，通过feign只需要定义服务绑定接口且以声明式的方法，优雅而简单的实现了服务调用

## Feign使用
### api项目修改
* 1.导入pom
```
   <dependency>
     <groupId>org.springframework.cloud</groupId>
     <artifactId>spring-cloud-starter-feign</artifactId>
   </dependency>
```

* 2.新建接口
```
/**
 * MICROSERVICECLOUD-DEPT服务调用接口
 * Feign实现
 * @RequestMapping指向服务提供者
 * @author langao_q
 * @create 2020-04-10 16:06
 */
@FeignClient(value = "MICROSERVICECLOUD-DEPT")
public interface DeptClientService {

    @RequestMapping(value = "/dept/get/{id}",method = RequestMethod.GET)
    public Dept get(@PathVariable("id") long id);

    @RequestMapping(value = "/dept/list",method = RequestMethod.GET)
    public List<Dept> list();

    @RequestMapping(value = "/dept/add",method = RequestMethod.POST)
    public boolean add(Dept dept);
}
```

### 新建消费者项目
microservicecloud-consumer-dept-fegin
* 1.引入pom
```
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-feign</artifactId>
        </dependency>
```

* 2.注入DeptClientService接口，并使用
```
/**
 * Feign方式远程调用
 * @author langao_q
 * @create 2020-04-07 14:44
 */
@RestController
public class DeptController_Consumer {

    @Autowired
    private DeptClientService deptClientService;


    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/consumer/dept/add")
    public boolean add(Dept dept) {
        return deptClientService.add(dept);
    }

    @RequestMapping(value = "/consumer/dept/get/{id}")
    public Dept get(@PathVariable Long id) {
        return deptClientService.get(id);
    }

    @RequestMapping(value = "/consumer/dept/list")
    public List<Dept> list(){
        return deptClientService.list();
    }
}
```

* 3.yml配置
```
server:
  port: 80

spring:
  application:
    name: microservicecloud-consumer-dept-fegin
#客户端注册进eureka服务列表内
eureka:
  client:
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/,http://eureka7003.com:7003/eureka/
  instance:
    instance-id: microservicecloud-consumer-dept-80
    prefer-ip-address: true     #访问路径可以显示IP地址
```

* 4.启动类
```
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
```


