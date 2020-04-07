# 微服务与微服务架构
* 1.微服务：强调的是服务的大小，它关注的是某一个点，是具体解决某一个问题/提供落地对应服务的一个服务应用。如IDEA中的一个项目  
* 2.微服务架构：微服务架构是⼀种架构模式，它提倡将单⼀应⽤程序划分成⼀组⼩的服务，服务之间互相协调、互相配合，能够被独⽴的部署来提供服务（基于RESTful API）。-马丁.福勒
  
# springcloud
> 分布式微服务架构下的一站式解决方案，是各个微服务架构落地技术的集合体，俗称微服务全家桶  
SpringBoot专注于快速、方便的开发单个微服务个体，SpringCloud关注全局的服务治理框架。

## 五大基本组件
* 服务发现——Netflix Eureka
* 客服端负载均衡——Netflix Ribbon
* 断路器——Netflix Hystrix
* 服务网关——Netflix Zuul
* 分布式配置——Spring Cloud Config

## 微服务架构
![微服务架构](https://blog.imwj.club//upload/2020/04/v63kua8rn8g9lpotmot0u6m084.png)

## 与dubbo相比
* 1.最大区别：SpringCloud抛弃了Dubbo的RPC通信，采用的是基于HTTP的REST方式。
* 2.dubbo只有注册中心、服务监控等，而springcloud包含注册中心、监控、断路器、网关、分布式配置、跟踪...等等

## 相关网站
* 官网：[https://spring.io/projects/spring-cloud](https://spring.io/projects/spring-cloud)  
* 中文文档：[https://www.springcloud.cc/](https://www.springcloud.cc/)  
* 中文社区：[http://www.springcloud.cn/](http://www.springcloud.cn/)  
