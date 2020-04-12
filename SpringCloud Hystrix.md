# SpringCloud Hystrix
> Hystrix是一个用于处理分布式系统的延迟和容错的开源库，在分布式系统里，许多依赖不可避免的会调用失败，比如超时、异常等。  
> Hystrix能够保证在一个依赖出问题的情况下，不会导致整体服务失败，避免级联故障，以提高分布式系统的弹性。  
> 服务异常时Hystrix会向调用方返回一个符合预期的、可处理的备选响应（FallBack），而不是长时间的等待或者抛出调用方无法处理的异常

* 包含：服务熔断、服务降级、服务限流、服务监控（hystrix-dashboard）等...
* 文档：[https://github.com/Netflix/Hystrix/wiki/Getting-Started](https://github.com/Netflix/Hystrix/wiki/Getting-Started)

# 服务熔断
* 熔断机制是应对雪崩效应的一种微服务链路保护机制。当扇出链路的某个微服务不可用或者响应时间太长时，熔断该节点微服务的调用，
快速返回"错误"的响应信息。如同 “保险丝”  
* 下面内容基于之前Eureka的基础上进行
## 新建服务提供者
* 在之前的microservicecloud-provider-8001基础上新建一个服务提供者microservicecloud-provider-hystrix-8001
* pom文件
```
        <!--hystrix-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>
```

## yml
* 与之前基本一致
```
server:
  port: 8001

#mybatis配置
mybatis:
  config-location: classpath:mybatis/mybatis.cfg.xml      # mybatis配置文件所在路径
  type-aliases-package: io.imwj.springcloud.entity        # 所有Entity别名类所在包
  mapper-locations:
    - classpath:mybatis/mapper/**/*.xml                       # mapper映射文件

#mysql数据源配置
spring:
  application:
    name: microservicecloud-dept
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource            # 当前数据源操作类型
    driver-class-name: org.gjt.mm.mysql.Driver              # mysql驱动包
    url: jdbc:mysql://localhost:3306/cloudDB01              # 数据库名称
    username: root
    password: 123456
    dbcp2:
      min-idle: 5                                           # 数据库连接池的最小维持连接数
      initial-size: 5                                       # 初始化连接数
      max-total: 5                                          # 最大连接数
      max-wait-millis: 200                                  # 等待连接获取的最大超时时间

# 打印sql语句
logging:
  level:
    io.imwj.springcloud: debug
    io.imwj.springcloud.dao: trace

#客户端注册进eureka服务列表内
eureka:
  client:
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/,http://eureka7003.com:7003/eureka/
  instance:
    instance-id: microservicecloud-dept8001-hystrix
    prefer-ip-address: true     #访问路径可以显示IP地址

info:
  app.name: microservicecloud
  company.name: blog.imwj.club
  build.artifactId: $project.artifactId$
  build.version: $project.version$
```

## controller增加注解`@HystrixCommand(fallbackMethod = "processHystrix_Get")`
一旦/dept/get方法查询为null就会抛出异常，然后调用备选的processHystrix_Get方法
```
@RestController
public class DeptController {

    @Autowired
    private DeptService deptService;

    @Autowired
    private DiscoveryClient client;

    @HystrixCommand(fallbackMethod = "processHystrix_Get")
    @RequestMapping(value = "/dept/get/{id}", method = RequestMethod.GET)
    public Dept get(@PathVariable Long id){
        Dept dept = deptService.get(id);
        if(dept == null){
            throw new RuntimeException("服务异常！");
        }
        return dept;
    }

    public Dept processHystrix_Get(@PathVariable("id") Long id){
        return new Dept().setDeptno(id)
                .setDname("服务异常延迟！")
                .setDb_source("在数据库中没有查询到数据！");
    }
}
```

## 启动类增加注解
`@EnableCircuitBreaker`
```
@EnableEurekaClient//注册到Eureka中
@EnableCircuitBreaker//对hystrixR熔断机制的支持
@SpringBootApplication
public class DeptProvider8001_Hystrix_App {

    public static void main(String[] args) {
        SpringApplication.run(DeptProvider8001_Hystrix_App.class, args);
    }
}

```

# 服务降级
* 整体资源快不够了，忍痛将某些服务先关掉，待渡过难关，再开启回来。服务降级是在客户端完成的 与服务端没有关系 
* 其实就是基于Feign接口进行统一的服务熔断，不再把熔断代码内嵌到Controller中
* 下面内容是在Feign的基础上进行的
## api项目中增加一个实现类`DeptClientServiceFallbackFactory`
```
/**
 * 此处只处理get接口的降级
 * @author langao_q
 * @create 2020-04-11 10:30
 */
@Component
public class DeptClientServiceFallbackFactory implements FallbackFactory<DeptClientService> {
    @Override
    public DeptClientService create(Throwable throwable) {
        return new DeptClientService() {
            @Override
            public Dept get(long id) {
                return new Dept().setDeptno(id)
                        .setDname("服务异常延迟！")
                        .setDb_source("在数据库中没有查询到数据！");
            }

            @Override
            public List<Dept> list() {
                return null;
            }

            @Override
            public boolean add(Dept dept) {
                return false;
            }
        };
    }
}
```

## 修改DeptClientService
* 修改@FeignClient注解
```
@FeignClient(value = "MICROSERVICECLOUD-DEPT",fallbackFactory=DeptClientServiceFallbackFactory.class)
public interface DeptClientService {

    @RequestMapping(value = "/dept/get/{id}",method = RequestMethod.GET)
    public Dept get(@PathVariable("id") long id);

    @RequestMapping(value = "/dept/list",method = RequestMethod.GET)
    public List<Dept> list();

    @RequestMapping(value = "/dept/add",method = RequestMethod.POST)
    public boolean add(Dept dept);
}
```

## consumer-dept-fegin消费者yml增加
```
feign: 
  hystrix: 
    enabled: true
```


# 服务监控
* Spring Cloud也提供了Hystrix Dashboard的整合，对监控内容转化成可视化界面。
## 新建监控项目
* microservicecloud-consumer-hystrix-dashboard
* pom
```
        <!-- hystrix和 hystrix-dashboard相关-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix-dashboard</artifactId>
        </dependency>
```

* yml 
```
server:
  port: 9001
```

* 启动类
```
@SpringBootApplication
@EnableHystrixDashboard
public class DeptConsumer_DashBoard_App {

    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer_DashBoard_App.class, args);
    }
}
```

* 启动测试[http://127.0.0.1:9001/hystrix](http://127.0.0.1:9001/hystrix)

## 被监控项目
使用我们之前熔断步骤新建的microservicecloud-provider-hystrix-8001
* pom.xml
```
        <!-- actuator监控信息完善 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <!--hystrix-->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-hystrix</artifactId>
        </dependency>
```

## 测试
* 启动Eureka、被监控项目（hystrix-8001）、监控项目（hystrix-dashboard）
* 访问http://127.0.0.1:8001/hystrix.stream可以看到实时的json数据
* 访问http://127.0.0.1:9001/hystrix是图形界面，在输入框中输入被监控项目的/hystrix.stream即可

## 图形解析

