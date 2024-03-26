package com.example.demo.service;

import com.example.demo.model.MetricModifiers;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author IAN
 * @date 2024/03/14
 **/
@Service
public class ModifierDataProxy implements DataProxy<MetricModifiers> {

    @Resource
    private EruptDao         eruptDao;
    @Resource
    private EruptUserService eruptUserService;

    @Override
    public void beforeAdd(MetricModifiers metric) {
        metric.setUpdate_time(new Date());
        metric.setUpdater(eruptUserService.getCurrentEruptUser().getId());
        String modifierIdView = metric.getModifier_id_view();
        if (StringUtils.isNotBlank(modifierIdView)) {
            String[] idArr = Arrays.stream(modifierIdView.split("\\|"))
                    .map(part -> part.split(":")[0])
                    .toArray(String[]::new);
            metric.setModifier_id(StringUtils.join(idArr, ","));
        }
    }

    @Override
    public String beforeFetch(List<Condition> conditions) {
        String result    = null;
        Condition remove = null;
        for (Condition condition : conditions) {
            if ("modifier_id_view".equals(condition.getKey())) {
                remove = condition;
                String value = condition.getValue().toString();
                if (StringUtils.isNotBlank(value)) {
                    String[] idArr = Arrays.stream(value.split("\\|"))
                            .map(part -> part.split(":")[0])
                            .toArray(String[]::new);
                    for (String id : idArr) {
                        if (StringUtils.isNotBlank(result)) {
                            result = result + String.format(" and find_in_set(%s,modifier_id)>0 ", id);
                        } else {
                            result = String.format("find_in_set(%s,modifier_id)>0 ", id);
                        }
                    }
                }
            }
        }
        conditions.remove(remove);
        return result;
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> result) {
        result.forEach(map -> {
            if (map.containsKey("modifier_id") && map.get("modifier_id") != null) {
                String modifierDefIds = map.get("modifier_id").toString();
                String modifierIdView   = getModifierIdView(modifierDefIds);
                map.put("modifier_id_view", modifierIdView);
            }
        });
    }

    @Override
    public void editBehavior(MetricModifiers metric) {
        String modifierIds = metric.getModifier_id();
        metric.setModifier_id_view(getModifierIdView(modifierIds));
    }

    @Override
    public void beforeUpdate(MetricModifiers metric) {
        beforeAdd(metric);
    }

    private String getModifierIdView(String modifierIds) {
        if (StringUtils.isNotBlank(modifierIds)) {
            String sql = "select id, metric_zh_name from metric_meta \n" +
                    "where id in (%s)";

            List<Map<String, Object>> maps = eruptDao.getJdbcTemplate()
                    .queryForList(String.format(sql, modifierIds));

            return maps.stream()
                    .map(s -> Joiner.on(":").join(s.get("id").toString(), s.get("metric_zh_name")))
                    .collect(Collectors.joining("|"));
        }
        return null;
    }

}
