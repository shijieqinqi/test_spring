package com.example.demo.service;

import com.HelloTalk.model.MetricMetaDerive;
import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.exception.EruptApiErrorTip;
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
public class DeriveDataProxy implements DataProxy<MetricMetaDerive> {

    @Resource
    private EruptDao         eruptDao;
    @Resource
    private EruptUserService eruptUserService;

    @Override
    public void beforeAdd(MetricMetaDerive metric) {
        metric.setUpdate_time(new Date());
        metric.setUpdater(eruptUserService.getCurrentEruptUser().getId());

        String modifierDefView = metric.getModifier_def_view();
        if (StringUtils.isNotBlank(modifierDefView)) {
            String[] idArr = Arrays.stream(modifierDefView.split("\\|"))
                    .map(part -> part.split(":")[0])
                    .toArray(String[]::new);
            metric.setModifier_def(StringUtils.join(idArr, ","));
        }

        String tecDef = metric.getTec_def();
        if (StringUtils.isBlank(tecDef)) {
            // 派生指标生成sql
            String        metricName   = metric.getMetric_name();
            String        modifier_def = metric.getModifier_def();
            StringBuilder initSql      = new StringBuilder();

            // 依赖指标
            Integer upstreamMetricId = metric.getUpstream_metric();
            List<Map<String, Object>> upstreamMetricList = eruptDao.getJdbcTemplate()
                    .queryForList(String.format("select * from metric_meta where id = '%s'", upstreamMetricId));

            if (!upstreamMetricList.isEmpty()) {
                Map<String, Object> upstreamMetric    = upstreamMetricList.get(0);
                String              aggregationMethod = upstreamMetric.get("aggregation_method") + " " + metricName;
                Object              events            = upstreamMetric.get("events");

                int dataType = Integer.parseInt(upstreamMetric.get("data_type").toString());

                if (dataType == 2) {
                    events = "'" + events.toString().replace(",", "','") + "'";
                    initSql = new StringBuilder("select %s from v_event_12 where \"$part_event\" in (%s)");
                    initSql = new StringBuilder(String.format(initSql.toString(), aggregationMethod, events));
                    // 统计周期
                    List<Map<String, Object>> statPeriodList = eruptDao.getJdbcTemplate()
                            .queryForList(String.format("select * from metric_stat_period where id = '%s'", metric.getStat_period()));
                    String statPeriod = "";
                    statPeriod = statPeriodList.get(0).get("ta_tec_def").toString();
                    initSql.append(" and ").append(statPeriod);

                    if (modifier_def != null) {
                        List<Map<String, Object>> modifierDefList = eruptDao.getJdbcTemplate()
                                .queryForList(String.format("select * from metric_modifiers where id in (%s)", modifier_def));

                        if (!modifierDefList.isEmpty()) {
                            for (Map<String, Object> modify : modifierDefList) {
                                initSql.append(" and ").append(modify.get("tec_def"));
                            }
                        }
                    }
                    metric.setTec_def(initSql.toString());
                }
            } else {
                throw new EruptApiErrorTip("依赖的原子指标id不存在！");
            }
        }
    }

    @Override
    public String beforeFetch(List<Condition> conditions) {
        String    result = null;
        Condition remove = null;
        for (Condition condition : conditions) {
            if ("modifier_def".equals(condition.getKey())) {
                remove = condition;
                String value = condition.getValue().toString();
                if (StringUtils.isNotBlank(value)) {
                    result = String.format("find_in_set(%s,modifier_def)>0 ", value);
                }
            }
        }
        conditions.remove(remove);
        return result;
    }

    @Override
    public void afterFetch(Collection<Map<String, Object>> result) {
        result.forEach(map -> {
            if (map.containsKey("modifier_def") && map.get("modifier_def") != null) {
                String modifierDef    = map.get("modifier_def").toString();
                int    upstreamMetric = (int) map.get("upstream_metric");
                String modifierView   = getModifierView(modifierDef, upstreamMetric,false);
                map.put("modifier_def_view", modifierView);
            }
        });
    }

    @Override
    public void editBehavior(MetricMetaDerive metric) {
        String  modifierDef    = metric.getModifier_def();
        Integer upstreamMetric = metric.getUpstream_metric();
        metric.setModifier_def_view(getModifierView(modifierDef, upstreamMetric,true));
    }

    @Override
    public void beforeUpdate(MetricMetaDerive metric) {
        beforeAdd(metric);
    }

    private String getModifierView(String modifierDef, int upstreamMetric,boolean needMetric ) {
        if (StringUtils.isNotBlank(modifierDef)) {
            String sql = "select t1.id,modifier_zh_name,metric_zh_name from " +
                    "(select * from metric_modifiers where id in (%s)) t1 " +
                    "left join " +
                    "(select id, metric_zh_name from metric_meta where id = %s) t2  " +
                    "on FIND_IN_SET(t2.id, t1.modifier_id)>0";
            List<Map<String, Object>> maps = eruptDao.getJdbcTemplate()
                    .queryForList(String.format(sql, modifierDef, upstreamMetric));

            return maps.stream()
                    .map(s -> needMetric
                                ? Joiner.on(":").join(s.get("id").toString(), s.get("modifier_zh_name"), s.getOrDefault("metric_zh_name", "null"))
                                :Joiner.on(":").join(s.get("id").toString(), s.get("modifier_zh_name")))
                    .collect(Collectors.joining("|"));
        }
        return null;
    }

}
