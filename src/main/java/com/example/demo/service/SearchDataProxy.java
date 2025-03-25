package com.example.demo.service;

import com.example.demo.model.MetricMetaDerive;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.jpa.dao.EruptDao;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author IAN
 * @date 2024/03/14
 **/
@Service
public class SearchDataProxy implements DataProxy<MetricMetaDerive> {

    @Resource
    private EruptDao eruptDao;

    @Override
    public String beforeFetch(List<Condition> conditions) {
        String result = null;
        Condition remove = null;
        for (Condition condition : conditions) {
            if ("is_use".equals(condition.getKey())) {
                remove = condition;
                List<Map<String, Object>> list = eruptDao.getJdbcTemplate().queryForList(
                        "select t1.id as  id " +
                                " from " +
                                " (select id,metric_name,metric_zh_name,metric_type,cal_period,metric_theme,status,product_def,tec_def,create_time,recent_cal_time " +
                                " from metric_meta " +
                                " where metric_type in (2,3) and status = 1) t1 " +
                                " left join  " +
                                " (select id,title,push_conf from robot_report_meta  " +
                                " where status = 1) t2 " +
                                " on FIND_IN_SET(t1.id,replace(replace(replace(json_extract( push_conf,'$[*].id'),'[',''),']',''),' ','')) > 0 " +
                                " left join  " +
                                " (select id,cal_metric from metric_meta  " +
                                " where metric_type in (3) and status = 1) t3 " +
                                " on FIND_IN_SET(t1.id,t3.cal_metric) > 0 " +
                                " where t3.id is null and t2.id is null " +
                                " group by t1.id, t1.metric_name, t1.metric_zh_name, t1.metric_type, t1.cal_period,  " +
                                "   t1.metric_theme, t1.status, t1.product_def, t1.tec_def, t1.create_time, t1.recent_cal_time");
                ArrayList<Object> ids = new ArrayList<>();
                for (Map<String, Object> map : list) {
                    ids.add(map.get("id"));
                }
                result = "id in ( -99," + StringUtils.join(ids, ',') + ")";
            }
        }
        conditions.remove(remove);
        return result;
    }
}
