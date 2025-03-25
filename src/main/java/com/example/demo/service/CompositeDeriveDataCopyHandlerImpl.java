package com.example.demo.service;


import com.example.demo.model.MetricMetaAtom;
import com.example.demo.model.MetricMetaComposite;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.OperationHandler;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.service.EruptUserService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author IAN
 * @date 2024/03/27
 **/
@Service
public class CompositeDeriveDataCopyHandlerImpl implements OperationHandler<MetricMetaComposite, Void> {

    @Resource
    private EruptDao         eruptDao;
    @Resource
    private EruptUserService eruptUserService;

    @Override
    public String exec(List<MetricMetaComposite> data, Void vo, String[] param) {
        try {
            String queryField = String.format("concat('copy_',metric_name),metric_zh_name,metric_type,cal_period, 0,    metric_theme,%s,    now(),      product_def,tec_def,cal_metric,cal_expression,now()"
                    , eruptUserService.getCurrentEruptUser().getId());
            String                  insertField    =          "metric_name,metric_zh_name,metric_type,cal_period,status,metric_theme,updater,update_time,product_def,tec_def,cal_metric,cal_expression,create_time";
            MetricMetaComposite metricMetaAtom = data.get(0);
            eruptDao.getJdbcTemplate().execute(String.format("insert into metric_meta(%s) select %s from metric_meta where id = '%s'", insertField,queryField,metricMetaAtom.getId()));
            return "msg.success('操作成功')";
        } catch (Exception e) {
            e.printStackTrace();
            return "msg.error('操作失败')";
        }
    }
}
