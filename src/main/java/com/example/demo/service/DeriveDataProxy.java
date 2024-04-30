package com.example.demo.service;

import com.alibaba.fastjson.JSONObject;
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
        }

        String modifierDefViewJoin = metric.getModifier_def_view_join();
        if (StringUtils.isNotBlank(modifierDefViewJoin)) {
            String[] idArr = Arrays.stream(modifierDefViewJoin.split("\\|"))
                    .map(part -> part.split(":")[0])
                    .toArray(String[]::new);
            metric.setModifier_def_join(StringUtils.join(idArr, ","));
        }

        String joinModifierDefView = metric.getJoin_modifier_def_view();
        if (StringUtils.isNotBlank(joinModifierDefView)) {
            String[] idArr = Arrays.stream(joinModifierDefView.split("\\|"))
                    .map(part -> part.split(":")[0])
                    .toArray(String[]::new);
            metric.setJoin_modifier_def(StringUtils.join(idArr, ","));
        }

        JSONObject joinTableConfigJson = new JSONObject();
        joinTableConfigJson.put("join_condition_1", metric.getJoin_condition_1());
        joinTableConfigJson.put("upstream_metric_join", metric.getUpstream_metric_join());
        joinTableConfigJson.put("stat_period_join", metric.getStat_period_join());
        joinTableConfigJson.put("join_condition_2", metric.getJoin_condition_2());
        joinTableConfigJson.put("modifier_def_join", metric.getModifier_def_join());
        joinTableConfigJson.put("join_modifier_def", metric.getJoin_modifier_def());
        metric.setJoin_table_config(joinTableConfigJson.toJSONString());

        String tecDef = metric.getTec_def();

        if (StringUtils.isBlank(tecDef)) {
            // 派生指标生成sql
            String metricName = metric.getMetric_name();
            String modifier_def = metric.getModifier_def();
            String modifier_def_join = metric.getModifier_def_join();
            Boolean is_event_related = metric.getIs_event_related();

            StringBuilder initSql = new StringBuilder();
            Integer upstreamMetricId = metric.getUpstream_metric();
            Integer upstreamMetricJoinId = metric.getUpstream_metric_join();

            List<Map<String, Object>> upstreamMetricList = eruptDao.getJdbcTemplate()
                    .queryForList(String.format("select * from metric_meta where id = '%s'", upstreamMetricId));
            List<Map<String, Object>> upstreamMetricJoinList = eruptDao.getJdbcTemplate().queryForList(String.format("select * from metric_meta where id = '%s'", upstreamMetricJoinId));

            if (!upstreamMetricList.isEmpty()) {
                List<Map<String, Object>> statPeriodList = eruptDao.getJdbcTemplate()
                        .queryForList(String.format("select * from metric_stat_period where id = '%s'", metric.getStat_period()));
                Map<String, Object> upstreamMetric = upstreamMetricList.get(0);
                String aggregationMethod = upstreamMetric.get("aggregation_method") + " " + metricName;
                Object events = upstreamMetric.get("events");
                Object availableDimensions = upstreamMetric.get("available_dimensions");
                Object projectName = upstreamMetric.get("project_name");
                int dataType = Integer.parseInt(upstreamMetric.get("data_type").toString());
                Object aggFunc = is_event_related?availableDimensions:aggregationMethod;
                //ta
                if (dataType == 2) {
                    // 左表
                    projectName = projectName == null ? "v_event_12" : projectName;
                    events = "'" + events.toString().replace(",", "','") + "'";
                    initSql = new StringBuilder(String.format("select %s from %s where \"$part_event\" in (%s)", aggFunc, projectName, events));

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
                    if (!is_event_related) {
                        metric.setTec_def(initSql.toString());
                    } else {
                        List<Map<String, Object>> statPeriodListJoin = eruptDao.getJdbcTemplate()
                                .queryForList(String.format("select * from metric_stat_period where id = '%s'", metric.getStat_period_join()));
                        Map<String, Object> upstreamMetricJoin = upstreamMetricJoinList.get(0);
                        Object projectNameJoin = upstreamMetricJoin.get("project_name");
                        String eventsJoin = upstreamMetricJoin.get("events").toString();
                        Object availableDimensionsJoin = upstreamMetricJoin.get("available_dimensions");

                        StringBuilder initSqlJoin = new StringBuilder();
                        String statPeriodJoin = "";
                        if (eventsJoin.contains("v_user_") || eventsJoin.contains("user_result_cluster_")) {
                            initSqlJoin.append("select %s from %s");
                            initSqlJoin = new StringBuilder(String.format(initSqlJoin.toString(), availableDimensionsJoin, eventsJoin));
                        } else {
                            projectNameJoin = projectNameJoin == null ? "v_event_12" : projectNameJoin;
                            eventsJoin = "'" + eventsJoin.replace(",", "','") + "'";
                            initSqlJoin.append("select %s from %s where \"$part_event\" in (%s)");
                            initSqlJoin = new StringBuilder(String.format(initSqlJoin.toString(), availableDimensionsJoin, projectNameJoin, eventsJoin));
                        }

                        if (!statPeriodListJoin.isEmpty()) {
                            statPeriodJoin = statPeriodListJoin.get(0).get("ta_tec_def").toString();
                            initSqlJoin.append(" and ").append(statPeriodJoin);
                        }
                        if (modifier_def_join != null) {
                            List<Map<String, Object>> modifierDefListJoin = eruptDao.getJdbcTemplate()
                                    .queryForList(String.format("select * from metric_modifiers where id in (%s)", modifier_def_join));
                            if (!modifierDefListJoin.isEmpty()) {
                                int i = 0;
                                String condition = eventsJoin.contains("v_user_") || eventsJoin.contains("user_result_cluster_") ? " where " : " and ";
                                for (Map<String, Object> modify : modifierDefListJoin) {
                                    initSqlJoin.append(i == 0 ? condition : " and ").append(modify.get("tec_def"));
                                    i++;
                                }
                            }
                        }
                        String joinCondition = String.format("t1.%s = t2.%s", metric.getJoin_condition_1(), metric.getJoin_condition_2());
                        initSql.append(") t1 ").append(metric.getEvent_related()).append(" (").append(initSqlJoin).append(") t2 on ").append(joinCondition);
                        String join_modifier_def = metric.getJoin_modifier_def();

                        initSql.append(generateModifierSql(join_modifier_def, eruptDao));

                        metric.setTec_def(String.format("select %s from (select t1.* from (%s) t", aggregationMethod, initSql));
                    }
                    // doris
                } else if (dataType == 1) {
                    // 左表
                    projectName = projectName == null ? "dw_ht_data" : projectName;
                    initSql = new StringBuilder("select %s from %s");
                    initSql = new StringBuilder(String.format(initSql.toString(), aggFunc, projectName + "." + events));
                    //修饰词
                    initSql.append(generateModifierSql(modifier_def, eruptDao));
                    String statPeriod = "";
                    if (!statPeriodList.isEmpty()) {
                        statPeriod = statPeriodList.get(0).get("doris_tec_def").toString();
                        if (initSql.toString().contains("where")) {
                            initSql.append(" and ").append(statPeriod);
                        } else {
                            initSql.append(" where ").append(statPeriod);
                        }
                    }
                    if (!is_event_related) {
                        metric.setTec_def(initSql.toString());
                    } else {
                        List<Map<String, Object>> statPeriodListJoin = eruptDao.getJdbcTemplate()
                                .queryForList(String.format("select * from metric_stat_period where id = '%s'", metric.getStat_period_join()));
                        Map<String, Object> upstreamMetricJoin = upstreamMetricJoinList.get(0);
                        Object projectNameJoin = upstreamMetricJoin.get("project_name");
                        String eventsJoin = upstreamMetricJoin.get("events").toString();
                        Object availableDimensionsJoin = upstreamMetricJoin.get("available_dimensions");
                        // 右表
                        StringBuilder initSqlJoin = new StringBuilder();
                        String statPeriodJoin = "";
                        initSqlJoin.append("select %s from %s");
                        projectNameJoin = projectNameJoin == null ? "dw_ht_data" : projectNameJoin;
                        initSqlJoin = new StringBuilder(String.format(initSqlJoin.toString(), availableDimensionsJoin, projectNameJoin + "." + eventsJoin));
                        initSqlJoin.append(generateModifierSql(modifier_def_join, eruptDao));
                        if (!statPeriodListJoin.isEmpty()) {
                            statPeriodJoin = statPeriodListJoin.get(0).get("doris_tec_def").toString();
                            if (initSql.toString().contains("where")) {
                                initSql.append(" and ").append(statPeriodJoin);
                            } else {
                                initSql.append(" where ").append(statPeriodJoin);
                            }
                        }
                        String joinCondition = String.format("t1.%s = t2.%s", metric.getJoin_condition_1(), metric.getJoin_condition_2());
                        initSql.append(") t1 ").append(metric.getEvent_related()).append(" (").append(initSqlJoin).append(") t2 on ").append(joinCondition);
                        String join_modifier_def = metric.getJoin_modifier_def();
                        initSql.append(generateModifierSql(join_modifier_def, eruptDao));

                        metric.setTec_def(String.format("select %s from (select t1.* from (%s) t", aggregationMethod, initSql));
                    }
                }
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
        String modifierDef = metric.getModifier_def();
        Integer upstreamMetric = metric.getUpstream_metric();
        metric.setModifier_def_view(getModifierView(modifierDef, upstreamMetric, true));

        String joinTableConfig = metric.getJoin_table_config();
        if (StringUtils.isNotBlank(joinTableConfig)) {
            JSONObject joinTable = JSONObject.parseObject(joinTableConfig);
            String modifierDefJoin = joinTable.getString("modifier_def_join");
            Integer upstreamMetricJoin = joinTable.getInteger("upstream_metric_join");
            if (upstreamMetricJoin != null && modifierDefJoin != null) {
                metric.setModifier_def_view_join(getModifierView(modifierDefJoin, upstreamMetricJoin, true));
            }

            String joinModifierDef = joinTable.getString("join_modifier_def");
            if (joinModifierDef != null) {
                metric.setJoin_modifier_def_view(getModifierView(joinModifierDef, upstreamMetric, true));
            }

            metric.setJoin_condition_1(joinTable.getString("join_condition_1"));
            metric.setUpstream_metric_join(upstreamMetricJoin);
            metric.setStat_period_join(joinTable.getInteger("stat_period_join"));
            metric.setJoin_condition_2(joinTable.getString("join_condition_2"));
            metric.setModifier_def_join(modifierDefJoin);
            metric.setJoin_modifier_def(joinModifierDef);
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

    public static StringBuilder generateModifierSql(String modifierDef, EruptDao eruptDao) {
        StringBuilder initSql = new StringBuilder();
        if (modifierDef != null) {
            List<Map<String, Object>> modifierDefList = eruptDao.getJdbcTemplate()
                    .queryForList(String.format("select * from metric_modifiers where id in (%s)", modifierDef));
            String condition = modifierDefList.isEmpty() ? "" : " where ";
            String modifierSql = modifierDefList.stream()
                    .map(modify -> modify.get("tec_def").toString())
                    .collect(Collectors.joining(" and ", condition, ""));
            initSql.append(modifierSql);
        }
        return initSql;
    }
}
