package com.example.demo.service;

import com.example.demo.model.MetaBase;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author IAN
 * @date 2024/03/14
 **/
@Service
public class BaseDataProxy implements DataProxy<MetaBase> {
    @Resource
    private EruptUserService eruptUserService;
    @Override
    public void beforeAdd(MetaBase metric) {
        metric.setUpdate_time(new Date());
        metric.setUpdater(eruptUserService.getCurrentEruptUser().getId());
    }

    @Override
    public void beforeUpdate(MetaBase metric) {
        metric.setUpdate_time(new Date());
        metric.setUpdater(eruptUserService.getCurrentEruptUser().getId());
    }

}
