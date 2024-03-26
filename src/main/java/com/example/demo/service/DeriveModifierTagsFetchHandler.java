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
 * @author IAN
 * @date 2024/03/14
 **/
@Service
public class DeriveModifierTagsFetchHandler implements TagsFetchHandler {

    @Autowired
    EruptDao eruptDao;

    @Override
    public List<String> fetchTags(String[] params) {
        String sql = "select metric_modifiers.id,modifier_zh_name,ifnull(metric_zh_name,'null') metric_zh_name " +
                "from metric_modifiers \n" +
                "left join metric_meta \n" +
                "on find_in_set(metric_meta.id,metric_modifiers.modifier_id)>0";
        List<Map<String, Object>> list = eruptDao.getJdbcTemplate()
                .queryForList(sql);
        return list.stream()
                .map(s -> Joiner.on(":").join(s.get("id").toString(), s.get("modifier_zh_name"),s.getOrDefault("metric_zh_name","null")))
                .collect(Collectors.toList());
    }
}