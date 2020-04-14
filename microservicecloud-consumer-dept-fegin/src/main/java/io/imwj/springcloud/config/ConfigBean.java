package io.imwj.springcloud.config;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RoundRobinRule;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

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
    @LoadBalanced
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    /**
     * 负载均衡：更改默认选择算法
     * RoundRobinRule：轮询
     * RandomRule：随机
     * RetryRule：先按照RoundRobinRule获取服务，如果有一个服务挂掉 那么就直接跳过 不再使用挂掉的服务
     * AvailabilityFilteringRule：根据服务是否死掉或者服务处于高并发来分配权重
     * WeightedResponseTimeRule：根据响应时间分配权重
     * BestAvailableRule：过滤挂掉的服务，然后选择一个并发量最小的服务
     * ZoneAvoidanceRule：默认规则，复合判断server所在区域的性能和server的可用性来选择
     * @return
     */
    @Bean
    public IRule myRule()
    {
        return new RoundRobinRule();
    }
}
