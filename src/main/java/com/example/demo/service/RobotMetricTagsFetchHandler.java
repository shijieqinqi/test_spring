package com.example.demo.service;


import com.google.common.base.Joiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.TagsFetchHandler;
import xyz.erupt.jpa.dao.EruptDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 修饰词管理--修饰的原子指标
 * @author IAN
 * @date 2024/03/14
 **/
@Service
public class RobotMetricTagsFetchHandler implements TagsFetchHandler {

    @Autowired
    EruptDao eruptDao;

    @Override
    public List<String> fetchTags(String[] params) {
        String sql = "select id ,metric_zh_name  from  metric_meta where metric_type in (2,3)  \n";

        List<Map<String, Object>> list = eruptDao.getJdbcTemplate()
                .queryForList(sql);
        for (int i = 1;i<10 ;i++ ){
            HashMap<String, Object> map = new HashMap<>();
            map.put("id","-1");
            map.put("metric_zh_name","换行" + i);
            list.add(map);
        }

        return list.stream()
                .map(s -> Joiner.on(":").join(s.get("id"), s.get("metric_zh_name")))
                .collect(Collectors.toList());
    }
}