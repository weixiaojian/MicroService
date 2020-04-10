package io.imwj.myrule;

import com.netflix.loadbalancer.IRule;
import org.springframework.context.annotation.Bean;

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
