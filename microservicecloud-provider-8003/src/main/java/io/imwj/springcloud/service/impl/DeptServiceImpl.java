package io.imwj.springcloud.service.impl;

import io.imwj.springcloud.dao.DeptDao;
import io.imwj.springcloud.entity.Dept;
import io.imwj.springcloud.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author langao_q
 * @create 2020-04-07 13:58
 */
@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private DeptDao deptDao;

    @Override
    public boolean add(Dept dept) {
        return deptDao.addDept(dept);
    }

    @Override
    public Dept get(Long id) {
        return deptDao.findById(id);
    }

    @Override
    public List<Dept> list() {
        List<Dept> all = deptDao.findAll();
        return all;
    }
}
