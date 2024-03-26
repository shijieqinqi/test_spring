package com.example.demo.model;/*
 * Copyright © 2020-2035 erupt.xyz All rights reserved.
 * Author: YuePeng (erupts@126.com)
 */


import com.example.demo.service.CompositeDataProxy;
import com.example.demo.service.CompositeTagsFetchHandler;
import lombok.Data;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.toolkit.handler.SqlChoiceFetchHandler;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

@Data
@Erupt(name = "复合指标",
        filter = @Filter("MetricMetaComposite.metric_type = 3")
        ,dataProxy = CompositeDataProxy.class)
@Table(name = "metric_meta")
@Entity
public class MetricMetaComposite extends MetaBase {

    @EruptField(
            views = @View(
                    title = "指标名"
            ),
            edit = @Edit(
                    title = "指标名",
                    type = EditType.INPUT, search = @Search, notNull = true,
                    inputType = @InputType
            )
    )
    private String metric_name;

    @EruptField(
            views = @View(
                    title = "中文名"
            ),
            edit = @Edit(
                    title = "中文名",
                    type = EditType.INPUT, search = @Search, notNull = true,
                    inputType = @InputType
            )
    )
    private String metric_zh_name;

    private String metric_type = "3";


    @EruptField(
            views = @View(
                    title = "指标主题"
            ),
            edit = @Edit(
                    title = "指标主题",
                    type = EditType.CHOICE, search = @Search, notNull = true,
                    choiceType = @ChoiceType(
                            fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id,domain_zh_name from metric_domain where level_at = 2"
                    )
            )
    )
    private String metric_theme;

    @EruptField(
            views = @View(
                    title = "计算周期"
            ),
            edit = @Edit(
                    title = "计算周期",
                    type = EditType.CHOICE, search = @Search, notNull = true,
                    choiceType = @ChoiceType(vl = {@VL(value = "0", label = "不计算"), @VL(value = "1", label = "day"), @VL(value = "2", label = "week"),
                            @VL(value = "3", label = "month"), @VL(value = "4", label = "hour")})
            )
    )
    private Integer cal_period;

    @EruptField(
            views = @View(
                    title = "状态"
            ),
            edit = @Edit(
                    title = "状态",
                    type = EditType.BOOLEAN, search = @Search, notNull = true,
                    boolType = @BoolType
            )
    )
    private Boolean status;

    @EruptField(
            views = @View(
                    title = "需要的指标", show = false
            )
    )
    private String cal_metric;


    @EruptField(
            views = @View(title = "需要的指标",desc = "格式(指标id:指标名:中文名)"),
            edit = @Edit(title = "需要的指标", desc = "格式(指标id:指标名:中文名)",
                    type = EditType.TAGS, notNull = true, search = @Search(vague = true),
                    tagsType = @TagsType(fetchHandler = CompositeTagsFetchHandler.class,allowExtension = false))
    )
    @Transient
    private String cal_metric_view;

    @EruptField(
            views = @View(
                    title = "计算表达式",desc = "(示例：({0} - {0})/{1} ，{0}代表选中的第一个指标)"
            ),
            edit = @Edit(
                    title = "计算表达式(示例：({0} - {0})/{1} ，{0}代表选中的第一个指标)",
                    type = EditType.TEXTAREA, notNull = true
            )

    )
    private String cal_expression;

    @EruptField(
            views = @View(
                    title = "业务定义"
            ),
            edit = @Edit(
                    title = "业务定义",
                    type = EditType.TEXTAREA, notNull = true
            )
    )
    private @Lob
    String product_def;


    @EruptField(
            views = @View(
                    title = "技术定义"
            ),
            edit = @Edit(
                    title = "技术定义(需清空才会重新自动生成)",
                    type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "sql")
            )
    )
    private @Lob
    String tec_def;

    @EruptField(
            views = @View(
                    title = "指标最近计算时间",type = ViewType.DATE_TIME
            )
    )
    private Date recent_cal_time ;
}
