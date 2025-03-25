package com.example.demo.service;


import com.example.demo.model.RobotReport;
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
public class RobotDeriveDataCopyHandlerImpl implements OperationHandler<RobotReport, Void> {

    @Resource
    private EruptDao         eruptDao;
    @Resource
    private EruptUserService eruptUserService;

    @Override
    public String exec(List<RobotReport> data, Void vo, String[] param) {
        try {
            String queryField = String.format("concat('copy_',title),0     ,push_order,report_period,metrics,token,dashboard_url, %s,    now(),      report_minute,report_time,push_conf,push_style"
                    , eruptUserService.getCurrentEruptUser().getId());
            String                  insertField    =          "title,status,push_order,report_period,metrics,token,dashboard_url,updater,update_time,report_minute,report_time,push_conf,push_style";
            RobotReport metricMetaDerive = data.get(0);
            eruptDao.getJdbcTemplate().execute(String.format("insert into robot_report_meta(%s) select %s from robot_report_meta where id = '%s'", insertField, queryField, metricMetaDerive.getId()));
            return "msg.success('操作成功')";
        } catch (Exception e) {
            e.printStackTrace();
            return "msg.error('操作失败')";
        }
    }
}
