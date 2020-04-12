package io.imwj.springcloud.service;

import feign.hystrix.FallbackFactory;
import io.imwj.springcloud.entity.Dept;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 此处只完成了get一个接口
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
