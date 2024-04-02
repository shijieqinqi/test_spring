package com.example.demo.service;



import com.example.demo.model.MetricMetaDerive;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.OperationHandler;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

/**
 * @author IAN
 * @date 2024/03/27
 **/
@Service
public class BatchInitTecDefHandlerImpl implements OperationHandler<MetricMetaDerive, Void> {

    @Resource
    DeriveDataProxy dataProxy;
    @Resource
    Environment     environment;

    @Override
    public String exec(List<MetricMetaDerive> data, Void vo, String[] param) {
        Connection conn = null;
        Statement  stmt = null;
        try {
            conn = DriverManager.getConnection(Objects.requireNonNull(environment.getProperty("spring.datasource.url")),
                    environment.getProperty("spring.datasource.username"),
                    environment.getProperty("spring.datasource.password"));
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            for (MetricMetaDerive datum : data) {
                datum.setTec_def(null);
                dataProxy.beforeAdd(datum);
                stmt.executeUpdate(String.format("update metric_meta set tec_def = '%s' where id = %s", datum.getTec_def().replace("'", "''"), datum.getId()));
            }
            conn.commit();
            return "msg.success('操作成功')";
        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            e.printStackTrace();
            return "msg.error('操作失败')";
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se2) {
            }
        }
    }
}
