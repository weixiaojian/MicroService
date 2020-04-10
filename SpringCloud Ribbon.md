# SpringCloud Ribbon
> 基于Netflix Ribbon实现的一套 客户端 负载均衡的工具，Controller调用Service（负载均衡）

* 负载均衡(Load Balance)：在微服务或分布式集群中经常用的一种应用，有两种方式
* 集中式(Load Balance)：通过硬件设施达到负载均衡的效果
* 进程内(Load Balance)：将(Load Balance)逻辑集成到消费方，消费方从服务注册中心获知有哪些地址可用，然后自己再从这些地址中选择出一个合适的服务器。

官网：[https://github.com/Netflix/ribbon/wiki/Getting-Started](https://github.com/Netflix/ribbon/wiki/Getting-Started[)

## 初步配置
* 1.导入pom依赖
```
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-ribbon</artifactId>
        </dependency>
```

* 2.给消费者yml也加上Eureka配置
```
spring:
  application:
    name: microservicecloud-consumer-dept-80
#客户端注册进eureka服务列表内
eureka:
  client:
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/,http://eureka7003.com:7003/eureka/
  instance:
    instance-id: microservicecloud-consumer-dept-80
    prefer-ip-address: true     #访问路径可以显示IP地址
```

* 3.建立一个配置类ConfigBean
```
/**
 * @author langao_q
 * @create 2020-04-07 14:42
 * RestTemplate提供了多种便捷访问远程Http服务的方法，
 * 是一种简单便捷的访问restful服务模板类
 * 是Spring提供的用于访问Rest服务的客户端模板工具集
 */
@Configuration
public class ConfigBean {

    /**
     * (url, requestMap, ResponseBean.class)这三个参数分别代表
     * REST请求地址、请求参数、HTTP响应转换被转换成的对象类型。
     * @return
     */
    @Bean
    @LoadBalanced  负载均衡
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
```

* 4.消费者调用服务是直接根据名称调用
```
@RestController
public class DeptController_Consumer {

    private static final String REST_URL_PREFIX = "http://MICROSERVICECLOUD-DEPT";

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping(value = "/consumer/dept/add")
    public boolean add(Dept dept) {
        return restTemplate.postForObject(REST_URL_PREFIX + "/dept/add", dept, Boolean.class);
    }
}
```

* 5.小结：Ribbon和Eureka整合之后消费者可以直接调用服务 而不需要关心地址和端口号，同时能实现`客户端`的负载均衡

## Ribbon核心IRule（负载均衡算法）
常用的几种负载均衡算法：
* RoundRobinRule：轮询
* RandomRule：随机
* RetryRule：先按照RoundRobinRule获取服务，如果有一个服务挂掉 那么就直接跳过 不再使用挂掉的服务
* AvailabilityFilteringRule：根据服务是否死掉或者服务处于高并发来分配权重
* WeightedResponseTimeRule：根据响应时间分配权重
* BestAvailableRule：过滤挂掉的服务，然后选择一个并发量最小的服务
* ZoneAvoidanceRule：默认规则，复合判断server所在区域的性能和server的可用性来选择

在ConfigBean中增加代码
```
    /**
     * 负载均衡：更改默认选择算法
     * @return
     */
    @Bean
    public IRule myRule()
    {
        //return new RoundRobinRule();
        return new RandomRule();//随机算法
        //return new RetryRule();
    }
```

## 自定义负载均衡算法
* 1.消费者启动类增加注解`@RibbonClient(name="MICROSERVICECLOUD-DEPT",configuration= MySelfRule.class)`
```
@EnableDiscoveryClient
@SpringBootApplication
@RibbonClient(name="MICROSERVICECLOUD-DEPT",configuration= MySelfRule.class)
public class DeptConsumer80_App {

    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer80_App.class, args);
    }
}
```

* 2.实现MySelfRule类  
注意：自定义配置类不能放在@ComponentScan（启动类）所扫描的当前包下以及子包下，要放在启动类之前的包
```
/**
 * 自定义Ribbon算法类
 * @author langao_q
 * @create 2020-04-10 14:58
 */
public class MySelfRule {

    @Bean
    public IRule myRule()
    {
        return new RandomRule_IMWJ();//自定义负载均衡算法实现
    }
}
```

* 3.实现自定义算法
```
/**
 * 自定义负载均衡算法实现
 * 在轮询的基础上每一个服务调用五次
 * @author langao_q
 * @create 2020-04-10 15:00
 */
public class RandomRule_IMWJ extends AbstractLoadBalancerRule {

    private int total = 0;    //总共被调用的次数，目前要求每台被调用5次
    private int currentIndex = 0;//当前提供服务的机器号

    public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            return null;
        }
        Server server = null;

        while (server == null) {
            if (Thread.interrupted()) {
                return null;
            }
            List<Server> upList = lb.getReachableServers();
            List<Server> allList = lb.getAllServers();

            int serverCount = allList.size();
            if (serverCount == 0) {
                /*
                 * No servers. End regardless of pass, because subsequent passes
                 * only get more restrictive.
                 */
                return null;
            }

            //=========关键代码===========
            if(total < 5){
                server = upList.get(currentIndex);
                total++;
            }else {
                total = 0;
                currentIndex++;
                if(currentIndex >= upList.size())
                {
                    currentIndex = 0;
                }
            }

            if (server == null) {
                /*
                 * The only time this should happen is if the server list were
                 * somehow trimmed. This is a transient condition. Retry after
                 * yielding.
                 */
                Thread.yield();
                continue;
            }
            if (server.isAlive()) {
                return (server);
            }
            server = null;
            Thread.yield();
        }
        return server;
    }

    @Override
    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {

    }
}
```

