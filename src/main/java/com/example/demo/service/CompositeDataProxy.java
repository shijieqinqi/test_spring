package com.example.demo.service;

import com.HelloTalk.model.MetricMetaComposite;
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
public class CompositeDataProxy implements DataProxy<MetricMetaComposite> {

    @Resource
    private EruptDao         eruptDao;
    @Resource
    private EruptUserService eruptUserService;

    @Override
    public void beforeAdd(MetricMetaComposite metric) {
        metric.setUpdate_time(new Date());
        metric.setUpdater(eruptUserService.getCurrentEruptUser().getId());
        String calMetricView = metric.getCal_metric_view();
        if (StringUtils.isNotBlank(calMetricView)) {
            ArrayList<String> calMetricIdArr = new ArrayList<>();
            ArrayList<String> calMetricNameArr = new ArrayList<>();
            String[] calMetricViewArr = calMetricView.split("\\|");
            for (String s : calMetricViewArr) {
                String[] calMetricArr = s.split(":");
                calMetricIdArr.add(calMetricArr[0]);
                calMetricNameArr.add(calMetricArr[1]);
            }
            metric.setCal_metric(StringUtils.join(calMetricIdArr, ","));

            // 生成技术指标
            if (StringUtils.isBlank(metric.getTec_def())) {
                String   calExpression = metric.getCal_expression();
                for (int i = 0; i < calMetricNameArr.size(); i++) {
                    calExpression = calExpression.replace(String.format("{%s}", i), calMetricNameArr.get(i));
                }
                metric.setTec_def(calExpression);
            }
        }
    }

    @Override
    public String beforeFetch(List<Condition> conditions) {
        String result    = null;
        Condition remove = null;
        for (Condition condition : conditions) {
            if ("cal_metric_view".equals(condition.getKey())) {
                remove = condition;
                String value = condition.getValue().toString();
                if (StringUtils.isNotBlank(value)) {
                    String[] idArr = Arrays.stream(value.split("\\|"))
                            .map(part -> part.split(":")[0])
                            .toArray(String[]::new);
                    for (String name : idArr) {
                        if (StringUtils.isNotBlank(result)) {
                            result = result + String.format(" and find_in_set('%s',cal_metric)>0 ", name);
                        } else {
                            result = String.format("find_in_set('%s',cal_metric)>0 ", name);
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
            if (map.containsKey("cal_metric") &&  map.get("cal_metric") != null) {
                String calMetric = map.get("cal_metric").toString();
                String calMetricView = getCalMetricView(calMetric);
                map.put("cal_metric_view", calMetricView);
            }
        });
    }

    @Override
    public void editBehavior(MetricMetaComposite metric) {
        String calMetric = metric.getCal_metric();
        metric.setCal_metric_view(getCalMetricView(calMetric));
    }

    @Override
    public void beforeUpdate(MetricMetaComposite metric) {
        beforeAdd(metric);
    }

    private String getCalMetricView(String calMetrics) {
        if (StringUtils.isNotBlank(calMetrics)) {
            String sql = "select id, metric_name ,metric_zh_name from metric_meta\n" +
                    "where id in (%s)";
            List<Map<String, Object>> maps = eruptDao.getJdbcTemplate()
                    .queryForList(String.format(sql,  calMetrics));

            HashMap<String, String> hashMap = new HashMap<>();
            for (Map<String, Object> s : maps) {
                hashMap.put(s.get("id").toString(), Joiner.on(":").join(s.get("id"),s.get("metric_name"), s.get("metric_zh_name")));
            }
            ArrayList<Object> resultList = new ArrayList<>();
            for (String metric : calMetrics.split(",")) {
                resultList.add(hashMap.get(metric));
            }
            return Joiner.on("|").join(resultList);
        }
        return null;
    }

}
