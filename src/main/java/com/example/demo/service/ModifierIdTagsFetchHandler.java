package com.example.demo.service;


import com.google.common.base.Joiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.TagsFetchHandler;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 修饰词管理--修饰的原子指标
 * @author IAN
 * @date 2024/03/14
 **/
@Service
public class ModifierIdTagsFetchHandler implements TagsFetchHandler {

    @Autowired
    EruptDao eruptDao;

    @Override
    public List<String> fetchTags(String[] params) {
        String sql = "select id ,metric_zh_name  from  metric_meta where metric_type = 1  ";

        List<Map<String, Object>> list = eruptDao.getJdbcTemplate()
                .queryForList(sql);
        return list.stream()
                .map(s -> Joiner.on(":").join(s.get("id"), s.get("metric_zh_name")))
                .collect(Collectors.toList());
    }
}