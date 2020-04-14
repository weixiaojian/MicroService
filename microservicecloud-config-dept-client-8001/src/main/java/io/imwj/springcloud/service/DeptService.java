package io.imwj.springcloud.service;

import io.imwj.springcloud.entity.Dept;

import java.util.List;

/**
 * @author langao_q
 * @create 2020-04-07 13:56
 */
public interface DeptService {

    public boolean add(Dept dept);

    public Dept    get(Long id);

    public List<Dept> list();

}
