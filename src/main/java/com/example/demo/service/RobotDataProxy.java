package com.example.demo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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
        Integer push_order = metric.getPush_order();
        if (push_order == null) {
            metric.setPush_order(1);
        }

        String pushConf = metric.getPush_conf();
        String[] metricIdOrder = Arrays.stream(metric.getMetrics_view().split("\\|"))
                .map(part -> part.split(":")[0])
                .toArray(String[]::new);
        if (StringUtils.isBlank(pushConf)) {
            ArrayList<JSONObject> pushConfResult = new ArrayList<>();
            for (String id : metricIdOrder) {
                pushConfResult.add(JSONObject.parseObject(String.format("{\"id\": %s, \"precision\": 0, \"compare\": 1}", id)));
            }
            metric.setPush_conf(pushConfResult.toString().replace("}, {", "}\n,{").replace("[", "[\n ").replace("]", "\n]"));
        } else {
            List<String> ids      = new ArrayList<>(Arrays.asList(metricIdOrder));
            ArrayList<JSONObject>    confList = JSON.parseObject(pushConf,new TypeReference<ArrayList<JSONObject>>(){});
            confList.removeIf(e -> !ids.contains(e.getString("id")) && !"-1".equals(e.getString("id")));
            List<String> confIdList = new ArrayList<>() ;
            for (JSONObject o : confList) {
                confIdList.add(o.getString("id"));
            }
            ids.removeAll(confIdList);
            for (String id : ids) {
                confList.add(JSONObject.parseObject(String.format("{\"id\": %s, \"precision\": 0, \"compare\": 1}", id)));
            }
            metric.setPush_conf(confList.toString().replace("}, {", "}\n,{").replace("[", "[\n ").replace("]", "\n]"));
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
                            result = result + String.format(" and JSON_CONTAINS(push_conf,'{\"id\":%s}') = 1 ", id);
                        } else {
                            result = String.format(" JSON_CONTAINS(push_conf,'{\"id\":%s}') = 1 ", id);
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
            if (map.containsKey("push_conf") && map.get("push_conf") != null) {
                JSONArray     pushConf  = JSONObject.parseObject(map.get("push_conf").toString(), JSONArray.class);
                ArrayList<Object> metricIds = new ArrayList<>();
                for (Object o : pushConf) {
                    JSONObject idConf = (JSONObject) o;
                    metricIds.add(idConf.getInteger("id"));
                }
                String metricIdsView   = getMetricIdsView(Joiner.on(",").join(metricIds));
                map.put("metrics_view", metricIdsView);
            }
        });
    }

    @Override
    public void editBehavior(RobotReport metric) {
        JSONArray     pushConf  = JSONObject.parseObject(metric.getPush_conf(), JSONArray.class);
        ArrayList<Object> metricIds = new ArrayList<>();
        for (Object o : pushConf) {
            JSONObject idConf = (JSONObject) o;
            metricIds.add(idConf.getInteger("id"));
        }
        metric.setMetrics_view(getMetricIdsView(Joiner.on(",").join(metricIds)));

    }

    @Override
    public void beforeUpdate(RobotReport metric) {
        beforeAdd(metric);
    }

    private String getMetricIdsView(String metricIds) {
        if (StringUtils.isNotBlank(metricIds)) {
            String sql = "select id, metric_zh_name from metric_meta \n" +
                    "where id in (%s)";

            List<Map<String, Object>> maps = eruptDao.getJdbcTemplate()
                    .queryForList(String.format(sql, metricIds));

            HashMap<String, String> hashMap = new HashMap<>();
            for (Map<String, Object> s : maps) {
                hashMap.put(s.get("id").toString(), Joiner.on(":").join(s.get("id"), s.get("metric_zh_name")));
            }

            ArrayList<Object> resultList = new ArrayList<>();
            for (String metric : metricIds.split(",")) {
                if (!"-1".equals(metric)) {
                    resultList.add(hashMap.get(metric));
                }
            }
            return Joiner.on("|").join(resultList);
        }
        return null;
    }
}
