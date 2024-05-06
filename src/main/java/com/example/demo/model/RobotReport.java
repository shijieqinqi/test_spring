package com.example.demo.model;


import com.example.demo.service.RobotDataProxy;
import com.example.demo.service.RobotMetricTagsFetchHandler;
import lombok.Data;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Data
@Erupt(name = "机器人推送配置",
        dataProxy = RobotDataProxy.class)
@Table(name = "robot_report_meta")
@Entity
public class RobotReport extends MetaBase {

    @EruptField(
            views = @View(
                    title = "标题"
            ),
            edit = @Edit(
                    title = "标题",
                    type = EditType.INPUT, search = @Search, notNull = true,
                    inputType = @InputType
            )
    )
    private String title;

    @EruptField(
            views = @View(
                    title = "机器人token"
            ),
            edit = @Edit(
                    title = "机器人token",
                    type = EditType.INPUT, search = @Search, notNull = true,
                    inputType = @InputType
            )
    )
    private String token;

    @EruptField(
            views = @View(
                    title = "推送周期"
            ),
            edit = @Edit(
                    title = "推送周期",
                    type = EditType.CHOICE, search = @Search, notNull = true,
                    choiceType = @ChoiceType(vl = { @VL(value = "1", label = "day"), @VL(value = "2", label = "week"),
                            @VL(value = "3", label = "month"), @VL(value = "4", label = "hour")})
            )
    )
    private Integer report_period;

    @EruptField(
            views = @View(
                    title = "推送小时"
            ),
            edit = @Edit(
                    title = "推送小时",
                    type = EditType.CHOICE, search = @Search, notNull = true,
                    choiceType = @ChoiceType(vl = {@VL(value = "1", label = "00"), @VL(value = "2", label = "01"),
                            @VL(value = "3", label = "02"), @VL(value = "4", label = "03"),@VL(value = "5", label = "04"), @VL(value = "6", label = "05"),
                            @VL(value = "7", label = "06"), @VL(value = "8", label = "07"),@VL(value = "9", label = "08"), @VL(value = "10", label = "09"),
                            @VL(value = "11", label = "10"), @VL(value = "12", label = "11"),@VL(value = "13", label = "12"), @VL(value = "14", label = "13"),
                            @VL(value = "15", label = "14"), @VL(value = "16", label = "15"),@VL(value = "17", label = "16"), @VL(value = "18", label = "17"),
                            @VL(value = "19", label = "18"), @VL(value = "20", label = "19"),@VL(value = "21", label = "20"), @VL(value = "22", label = "21"),
                            @VL(value = "23", label = "22"), @VL(value = "24", label = "23")})
            )
    )
    private Integer report_time;


    @EruptField(
            views = @View(
                    title = "推送分钟"
            ),
            edit = @Edit(
                    title = "推送分钟",
                    type = EditType.CHOICE, search = @Search, notNull = true,
                    choiceType = @ChoiceType(vl = {@VL(value = "1", label = "00"), @VL(value = "2", label = "30")})
            )
    )
    private Integer report_minute;

    @EruptField(
            views = @View(
                    title = "报表url"
            ),
            edit = @Edit(
                    title = "报表url",
                    type = EditType.INPUT, search = @Search,
                    inputType = @InputType
            )
    )
    private String dashboard_url;

    @EruptField(
            views = @View(
                    title = "开启推送"
            ),
            edit = @Edit(
                    title = "开启推送",
                    type = EditType.BOOLEAN, notNull = true, search = @Search,
                    boolType = @BoolType
            )
    )
    private Boolean status;

    @EruptField(
            views = @View(
                    title = "推送指标",show = false
            )
    )
    private String metrics;

    @EruptField(
            views = @View(title = "推送指标",desc = "格式(id:指标名)"),
            edit = @Edit(title = "推送指标", desc = "格式(id:指标名)",
                    type = EditType.TAGS, notNull = true, search = @Search,
                    tagsType = @TagsType(fetchHandler = RobotMetricTagsFetchHandler.class,allowExtension = false))
    )
    @Transient
    private String metrics_view;

}
