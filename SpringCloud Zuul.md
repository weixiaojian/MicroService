# SpringCloud Zull
> Zuul包含了对请求的路由和过滤两个最主要的功能，负责所有微服务统一对外的接口。  
> 路由功能负责将外部请求转发到具体的微服务实例上，是实现外部访问统一入口的基础  
> 过滤器功能则负责对请求的处理过程进行干预，是实现请求校验、服务聚合等功能的基础.

官网：[https://github.com/Netflix/zuul/wiki](https://github.com/Netflix/zuul/wiki)

# 路由功能
* 路由功能负责将外部请求转发到具体的微服务实例上，是实现外部访问统一入口的基础  
* 新建一个zuul模块
## pom依赖
```
        <!-- zuul路由网关 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-zuul</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-eureka</artifactId>
        </dependency>
```

## yml配置
```
server:
  port: 9527

spring:
  application:
    name: microservicecloud-zuul-gateway

eureka:
  client:
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka,http://eureka7002.com:7002/eureka,http://eureka7003.com:7003/eureka
  instance:
    instance-id: gateway-9527.com
    prefer-ip-address: true

zuul:
  # 访问前缀 / 隐藏指定服务的ip访问
  prefix: /imwj
  ignored-services: "*"
  # 路由配置：/api-a对应微服务在Eureka中的名...
  routes:
    api-a:
      path: /api-a/**
      serviceId: microservicecloud-dept
    api-b:
      path: /api-a/**
      erviceId: microservicecloud-dept1
```

## 启动类
```
/**
 * @author langao_q
 * @create 2020-04-12 21:30
 */
@EnableZuulProxy
@EnableEurekaClient
@SpringBootApplication
public class Zuul_9527_StartSpringCloudApp {
    public static void main(String[] args) {
        SpringApplication.run(Zuul_9527_StartSpringCloudApp.class, args);
    }
}
```

## 测试
* `http://127.0.0.1:8001/dept/get/1`已经不能访问微服务了
* `http://zuul.com:9527/imwj/api-a/dept/get/1`可以访问

# 过滤
* 过滤器功能则负责对请求的处理过程进行干预，是实现请求校验、服务聚合等功能的基础.
* 在上面项目中继续修改
## 增加一个MyFilter 类
* filterType：返回一个字符串代表过滤器的类型，在zuul中定义了四种不同生命周期的过滤器类型，具体如下：
    * [x] pre：路由之前
    * [ ] routing：路由之时
    * [ ] post： 路由之后
    * [ ] error：发送错误调用
* filterOrder：过滤的顺序
* shouldFilter：这里可以写逻辑判断，是否要过滤，本文true,永远过滤。
* run：过滤器的具体逻辑。可用很复杂，包括查sql，nosql去判断该请求到底有没有权限访问。
```
@Component
public class MyFilter extends ZuulFilter{

    private static Logger log = LoggerFactory.getLogger(MyFilter.class);
    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        log.info(String.format("%s >>> %s", request.getMethod(), request.getRequestURL().toString()));
        Object accessToken = request.getParameter("token");
        if(accessToken == null) {
            log.warn("token is empty");
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            try {
                ctx.getResponse().getWriter().write("token is empty");
            }catch (Exception e){}

            return null;
        }
        log.info("ok");
        return null;
    }
}
```


