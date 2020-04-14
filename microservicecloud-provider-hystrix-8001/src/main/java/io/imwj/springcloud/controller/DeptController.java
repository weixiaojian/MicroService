package io.imwj.springcloud.controller;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import io.imwj.springcloud.entity.Dept;
import io.imwj.springcloud.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

/**
 * @author langao_q
 * @create 2020-04-07 13:59
 */
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
