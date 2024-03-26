package com.example.demo.service;

import com.example.demo.model.RobotReport;
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
public class RobotDataProxy implements DataProxy<RobotReport> {

    @Resource
    private EruptDao         eruptDao;
    @Resource
    private EruptUserService eruptUserService;

    @Override
    public void beforeAdd(RobotReport metric) {
        metric.setUpdater(eruptUserService.getCurrentEruptUser().getId());
        metric.setUpdate_time(new Date());
        String metricIdsView = metric.getMetrics_view();
        if (StringUtils.isNotBlank(metricIdsView)) {
            String[] idArr = Arrays.stream(metricIdsView.split("\\|"))
                    .map(part -> part.split(":")[0])
                    .toArray(String[]::new);
            metric.setMetrics(StringUtils.join(idArr, ","));
        }
    }

    @Override
    public String beforeFetch(List<Condition> conditions) {
        String result    = null;
        Condition remove = null;
        for (Condition condition : conditions) {
            if ("metrics_view".equals(condition.getKey())) {
                remove = condition;
                String value = condition.getValue().toString();
                if (StringUtils.isNotBlank(value)) {
                    String[] idArr = Arrays.stream(value.split("\\|"))
                            .map(part -> part.split(":")[0])
                            .toArray(String[]::new);
                    for (String id : idArr) {
                        if (StringUtils.isNotBlank(result)) {
                            result = result + String.format(" and find_in_set(%s,metrics)>0 ", id);
                        } else {
                            result = String.format("find_in_set(%s,metrics)>0 ", id);
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
            if (map.containsKey("metrics") && map.get("metrics") != null) {
                String metricIds = map.get("metrics").toString();
                String metricIdsView   = getMetricIdsView(metricIds);
                map.put("metrics_view", metricIdsView);
            }
        });
    }

    @Override
    public void editBehavior(RobotReport metric) {
        String metricIds = metric.getMetrics();
        metric.setMetrics_view(getMetricIdsView(metricIds));
    }

    @Override
    public void beforeUpdate(RobotReport metric) {
        beforeAdd(metric);
    }

    private String getMetricIdsView(String modifierIds) {
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
