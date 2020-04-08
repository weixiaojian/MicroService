package io.imwj.springcloud.controller;

import io.imwj.springcloud.entity.Dept;
import io.imwj.springcloud.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author langao_q
 * @create 2020-04-07 13:59
 */
@RestController
public class DeptController {

    @Autowired
    private DeptService deptService;

    @RequestMapping(value = "/dept/add", method = RequestMethod.POST)
    public boolean add(@RequestBody Dept dept){
        return deptService.add(dept);
    }

    @RequestMapping(value = "/dept/get/{id}", method = RequestMethod.GET)
    public Dept get(@PathVariable Long id){
        return deptService.get(id);
    }

    @RequestMapping(value = "/dept/list", method = RequestMethod.GET)
    public List<Dept> list(){
        List<Dept> list = deptService.list();
        return list;
    }



}
