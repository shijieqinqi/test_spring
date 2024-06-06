package com.example.demo.service;


import com.example.demo.model.RobotReport;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import xyz.erupt.annotation.fun.OperationHandler;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class PushRobotHandlerImpl implements OperationHandler<RobotReport, Void> {
    @Resource
    Environment environment;
    private static final String API_URL = "http://10.40.2.218:12345/dolphinscheduler/projects/PROD_METRIC_DATA/executors/start-process-instance";
    private static final String TOKEN = "929af71c019a72ea530b919ec57f189c";

    @Override
    public String exec(List<RobotReport> data, Void vo, String[] param) {
        Connection conn = null;
        Statement stmt = null;
        if (environment == null) {
            System.out.println("Environment is not injected!");
        }
        try {
            conn = DriverManager.getConnection(Objects.requireNonNull(environment.getProperty("spring.datasource.url")),
                    environment.getProperty("spring.datasource.username"),
                    environment.getProperty("spring.datasource.password"));
            conn.setAutoCommit(false);
            stmt = conn.createStatement();
            for (RobotReport rebot : data) {
                stmt.executeUpdate(String.format("update robot_report_meta set is_triggered_push = 1 where id = %s", rebot.getId()));
            }
            startWorkFlow();
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

    public void startWorkFlow() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(API_URL);
        httpPost.addHeader("token", TOKEN);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        ArrayList<BasicNameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair("processDefinitionId", "464"));
        list.add(new BasicNameValuePair("projectName", "PROD_METRIC_DATA"));
        list.add(new BasicNameValuePair("failureStrategy", "END"));
        list.add(new BasicNameValuePair("processInstancePriority", "MEDIUM"));
        list.add(new BasicNameValuePair("warningGroupId", "1"));
        list.add(new BasicNameValuePair("warningType", "NONE"));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(list));
            httpClient.execute(httpPost);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
            }
        }

    }
}
