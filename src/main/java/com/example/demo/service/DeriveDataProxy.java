package com.example.demo.service;

import com.example.demo.model.MetricMetaDerive;
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
    private EruptDao eruptDao;
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
        } else {
            metric.setModifier_def(null);
        }
        String tecDef = metric.getTec_def();
        Boolean isAutogenerate = metric.getIs_autogenerate();

        if(!isAutogenerate){
            metric.setModifier_def("");
            metric.setUpstream_metric(0);
            metric.setStat_period(0);
        }

        if (StringUtils.isBlank(tecDef)&&isAutogenerate) {
            // 派生指标生成sql
            Integer upstreamMetricId = metric.getUpstream_metric();
            String metricName        = metric.getMetric_name();
            String modifierDef       = metric.getModifier_def();
            StringBuilder initSql    = new StringBuilder();

            List<Map<String, Object>> upstreamMetricList = eruptDao.getJdbcTemplate()
                    .queryForList(String.format("select * from metric_meta where id = '%s'", upstreamMetricId));

            if (!upstreamMetricList.isEmpty()) {
                List<Map<String, Object>> statPeriodList = eruptDao.getJdbcTemplate()
                        .queryForList(String.format("select * from metric_stat_period where id = '%s'", metric.getStat_period()));
                Map<String, Object> upstreamMetric = upstreamMetricList.get(0);
                String aggregationMethod           = upstreamMetric.get("aggregation_method") + " " + metricName;
                Object events                      = upstreamMetric.get("events");
                Object projectName                 = upstreamMetric.get("project_name");
                int dataType = Integer.parseInt(upstreamMetric.get("data_type").toString());
                //ta
                if (dataType == 2) {
                    projectName = projectName == null ? "v_event_12" : projectName;
                    events = "'" + events.toString().replace(",", "','") + "'";
                    initSql = new StringBuilder(String.format("select %s from %s where \"$part_event\" in (%s)", aggregationMethod, projectName, events));

                    String statPeriod = "";
                    statPeriod = statPeriodList.get(0).get("ta_tec_def").toString();
                    initSql.append(" and ").append(statPeriod);
                    if (modifierDef != null) {
                        List<Map<String, Object>> modifierDefList = eruptDao.getJdbcTemplate()
                                .queryForList(String.format("select * from metric_modifiers where id in (%s)", modifierDef));

                        if (!modifierDefList.isEmpty()) {
                            for (Map<String, Object> modify : modifierDefList) {
                                initSql.append(" and ").append(modify.get("tec_def"));
                            }
                        }
                    }
                  // doris
                } else if (dataType == 1) {
                    projectName = projectName == null ? "dw_ht_data" : projectName;
                    initSql     = new StringBuilder(String.format("select %s from %s", aggregationMethod, projectName + "." + events));
                    //修饰词
                    if (modifierDef != null) {
                        List<Map<String, Object>> modifierDefList = eruptDao.getJdbcTemplate()
                                .queryForList(String.format("select * from metric_modifiers where id in (%s)", modifierDef));
                        String condition = modifierDefList.isEmpty() ? "" : " where ";
                        String modifierSql = modifierDefList.stream()
                                .map(modify -> modify.get("tec_def").toString())
                                .collect(Collectors.joining(" and ", condition, ""));
                        initSql.append(modifierSql);
                    }
                    //统计周期
                    String statPeriod = "";
                    if (!statPeriodList.isEmpty()) {
                        statPeriod = statPeriodList.get(0).get("doris_tec_def").toString();
                        if (initSql.toString().contains("where")) {
                            initSql.append(" and ").append(statPeriod);
                        } else {
                            initSql.append(" where ").append(statPeriod);
                        }
                    }
                }
                metric.setTec_def(initSql.toString());
            } else {
                throw new EruptApiErrorTip("依赖的原子指标id不存在！");
            }
        }
    }

    @Override
    public String beforeFetch(List<Condition> conditions) {
        String result = null;
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
                String modifierDef = map.get("modifier_def").toString();
                int upstreamMetric = (int) map.get("upstream_metric");
                String modifierView = getModifierView(modifierDef, upstreamMetric, false);
                map.put("modifier_def_view", modifierView);
            }
        });
    }

    @Override
    public void editBehavior(MetricMetaDerive metric) {
        if (metric.getIs_autogenerate()){
            String modifierDef = metric.getModifier_def();
            Integer upstreamMetric = metric.getUpstream_metric();
            metric.setModifier_def_view(getModifierView(modifierDef, upstreamMetric, true));
        }
    }

    @Override
    public void beforeUpdate(MetricMetaDerive metric) {
        beforeAdd(metric);
    }

    private String getModifierView(String modifierDef, int upstreamMetric, boolean needMetric) {
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
                            : Joiner.on(":").join(s.get("id").toString(), s.get("modifier_zh_name")))
                    .collect(Collectors.joining("|"));
        }
        return null;
    }
}
