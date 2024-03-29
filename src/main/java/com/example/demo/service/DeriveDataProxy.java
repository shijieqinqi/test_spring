package com.example.demo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
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


        JSONObject joinTableConfig = new JSONObject();
        joinTableConfig.put("event_upstream_metric", metric.getEvent_upstream_metric());// 关联上游指标
        joinTableConfig.put("join_condition_1", metric.getJoin_condition_1());// 关联条件字段1
        joinTableConfig.put("upstream_metric_join", metric.getUpstream_metric_join());// 右表上游指标
        joinTableConfig.put("stat_period_join", metric.getStat_period_join());// 右表统计周期
        joinTableConfig.put("join_condition_2", metric.getJoin_condition_2());// 关联条件字段2
        joinTableConfig.put("modifier_def_join", metric.getModifier_def_join());// 右表修饰id
        joinTableConfig.put("join_modifier_def", metric.getJoin_modifier_def());// 关联修饰词id
        metric.setJoin_table_config(joinTableConfig.toJSONString());




        String tecDef = metric.getTec_def();

        if (StringUtils.isBlank(tecDef)) {
            // 派生指标生成sql
            String        metricName   = metric.getMetric_name();
            String        modifier_def = metric.getModifier_def();
            String        modifier_def_join = metric.getModifier_def_join();
            Boolean       is_event_related = metric.getIs_event_related();

            StringBuilder initSql      = new StringBuilder();

            // 无join
            if (!is_event_related){
                // 依赖指标
                Integer upstreamMetricId = metric.getUpstream_metric();
                List<Map<String, Object>> upstreamMetricList = eruptDao.getJdbcTemplate()
                        .queryForList(String.format("select * from metric_meta where id = '%s'", upstreamMetricId));

                if (!upstreamMetricList.isEmpty()) {
                    Map<String, Object> upstreamMetric    = upstreamMetricList.get(0);
                    String              aggregationMethod = upstreamMetric.get("aggregation_method") + " " + metricName; // count(distinct "#account_id")　corrections
                    Object              events            = upstreamMetric.get("events");//　correction


                    int dataType = Integer.parseInt(upstreamMetric.get("data_type").toString());   //2

                    if (dataType == 2 ) {
                        events = "'" + events.toString().replace(",", "','") + "'"; //'correction'
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
            // join
            }else{
                Integer upstreamMetricId = metric.getUpstream_metric();
                Integer upstreamMetricJoinId = metric.getUpstream_metric_join();
                Integer event_upstream_metric = metric.getEvent_upstream_metric();

                List<Map<String, Object>> upstreamMetricList = eruptDao.getJdbcTemplate()
                        .queryForList(String.format("select * from metric_meta where id = '%s'", upstreamMetricId));
                List<Map<String, Object>> upstreamMetricJoinList = eruptDao.getJdbcTemplate().queryForList(String.format("select * from metric_meta where id = '%s'", upstreamMetricJoinId));
                List<Map<String, Object>> eventUpStreamList = eruptDao.getJdbcTemplate().queryForList(String.format("select * from metric_meta where id = '%s'", event_upstream_metric));


                if (!upstreamMetricList.isEmpty() && !upstreamMetricJoinList.isEmpty()&& !eventUpStreamList.isEmpty()) {
                    Map<String, Object> upstreamMetric = upstreamMetricList.get(0);
                    Map<String, Object> upstreamMetricJoin = upstreamMetricJoinList.get(0);
                    Map<String, Object> eventUpMetric = eventUpStreamList.get(0);
                    String aggregationMethod = eventUpMetric.get("aggregation_method") + " " + metricName;
                    Object              events            = upstreamMetric.get("events");
                    Object              eventsJoin        = upstreamMetricJoin.get("events");


                    Object availableDimensions = upstreamMetric.get("available_dimensions");
                    Object availableDimensionsJoin = upstreamMetricJoin.get("available_dimensions");
                    int dataType = Integer.parseInt(upstreamMetric.get("data_type").toString());

                    if (dataType == 2 ) {
                        // 左表
                        events = "'" + events.toString().replace(",", "','") + "'";
                        initSql = new StringBuilder("select %s from v_event_12 where \"$part_event\" in (%s)");
                        initSql = new StringBuilder(String.format(initSql.toString(), availableDimensions,events));
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

                        // 右表
                        StringBuilder initSqlJoin = new StringBuilder();
                        List<Map<String, Object>> statPeriodListJoin = eruptDao.getJdbcTemplate()
                                .queryForList(String.format("select * from metric_stat_period where id = '%s'", metric.getStat_period_join()));
                        String statPeriodJoin = "";

                        if  (eventsJoin.equals("v_user_12") || eventsJoin.equals("user_result_cluster_12")){
                            initSqlJoin.append("select %s from %s");
                            initSqlJoin = new StringBuilder(String.format(initSqlJoin.toString(), availableDimensionsJoin, eventsJoin));

                        }else{
                            eventsJoin = "'" + eventsJoin.toString().replace(",", "','") + "'";
                            initSqlJoin.append("select * from v_event_12 where \"$part_event\" in (%s)");
                            initSqlJoin = new StringBuilder(String.format(initSqlJoin.toString(), eventsJoin));
                        }

                        if(!statPeriodListJoin.isEmpty()){
                            statPeriodJoin = statPeriodListJoin.get(0).get("ta_tec_def").toString();
                            initSqlJoin.append(" and ").append(statPeriodJoin);
                        }
                        if(modifier_def_join != null) {
                            List<Map<String, Object>> modifierDefListJoin = eruptDao.getJdbcTemplate()
                                    .queryForList(String.format("select * from metric_modifiers where id in (%s)", modifier_def_join));
                            if (!modifierDefListJoin.isEmpty()) {
                                for (Map<String, Object> modify : modifierDefListJoin) {
                                    if (eventsJoin.equals("v_user_12") || eventsJoin.equals("user_result_cluster_12")){
                                        initSqlJoin.append(" where ").append(modify.get("tec_def"));
                                    }
                                    initSqlJoin.append(" and ").append(modify.get("tec_def"));
                                }
                            }
                        }
                        String joinCondition = String.format("t1.%s = t2.%s", metric.getJoin_condition_1(), metric.getJoin_condition_2());
                        initSql.append(") t1 ").append(metric.getEvent_related()).append(" (").append(initSqlJoin).append(") t2 on ").append(joinCondition);
                        String        join_modifier_def = metric.getJoin_modifier_def();

                        // 关联指标的过滤条件
                        if (join_modifier_def != null) {
                            List<Map<String, Object>> modifierDefList = eruptDao.getJdbcTemplate()
                                    .queryForList(String.format("select * from metric_modifiers where id in (%s)", join_modifier_def));
                            if (!modifierDefList.isEmpty()) {
                                int i = 0;
                                for (Map<String, Object> modify : modifierDefList) {
                                    if(i == 0){
                                        initSql.append(" where ").append(modify.get("tec_def"));
                                    }
                                    else{
                                        initSql.append(" and ").append(modify.get("tec_def"));
                                    }
                                    i++;
                                }
                            }
                        }
                        System.out.println(initSql.toString());
                        StringBuilder resultSql = new StringBuilder();
                        String[] strings = aggregationMethod.split(" ");
                        int i = 0;
                        resultSql.append("select ");
                        for (String s : strings) {
                            if(i == strings.length-2){
                                s = "t1." + s;
                            }
                            i++;
                            resultSql.append(s).append(" ");
                        }
                        resultSql.append(" from (").append(initSql);
                        metric.setTec_def(resultSql.toString());
                    }

                }
                else {
                    throw new EruptApiErrorTip("依赖的原子指标id不存在！");
                }
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

        String joinTableConfig = metric.getJoin_table_config();
        JSONObject joinTable = JSONObject.parseObject(joinTableConfig);
        String  modifierDefJoin = joinTable.getString("modifier_def_join");
        Integer upstreamMetricJoin = joinTable.getInteger("upstream_metric_join");
        metric.setModifier_def_view_join(getModifierView(modifierDefJoin, upstreamMetricJoin,true));

        String  joinModifierDef = joinTable.getString("join_modifier_def");
        Integer eventUpstreamMetric = joinTable.getInteger("event_upstream_metric");
        metric.setJoin_modifier_def_view(getModifierView(joinModifierDef, eventUpstreamMetric,true));

        metric.setEvent_upstream_metric(eventUpstreamMetric);
        metric.setJoin_condition_1(joinTable.getString("join_condition_1"));
        metric.setUpstream_metric_join(upstreamMetricJoin);
        metric.setStat_period_join(joinTable.getInteger("stat_period_join"));
        metric.setJoin_condition_2(joinTable.getString("join_condition_2"));
        metric.setModifier_def_join(modifierDefJoin);
        metric.setJoin_modifier_def(joinModifierDef);


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
