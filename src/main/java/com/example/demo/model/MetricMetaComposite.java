package com.example.demo.model;/*
 * Copyright © 2020-2035 erupt.xyz All rights reserved.
 * Author: YuePeng (erupts@126.com)
 */

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.toolkit.handler.SqlChoiceFetchHandler;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

@Erupt(name = "复合指标",
        filter = @Filter("MetricMetaComposite.metric_type = 3"))
@Table(name = "metric_meta")
@Entity
public class MetricMetaComposite extends BaseModel {

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
                    title = "指标中文名称"
            ),
            edit = @Edit(
                    title = "指标中文名称",
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
                    title = "需要的指标"
            ),
            edit = @Edit(
                    title = "需要的指标",
                    type = EditType.INPUT, search = @Search, notNull = true
            )

    )
    private String cal_metric;


    @EruptField(
            views = @View(
                    title = "更新人"
            ),
            edit = @Edit(
                    title = "更新人",
                    type = EditType.CHOICE, notNull = true,
                    choiceType = @ChoiceType(
                            fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id,name from e_upms_user"
                    )
            )
    )
    private String updater;

    @EruptField(
            views = @View(
                    title = "更新时间", type = ViewType.DATE_TIME
            )
    )
    private Date update_time;


    @EruptField(
            views = @View(
                    title = "状态"
            ),
            edit = @Edit(
                    title = "状态",
                    type = EditType.BOOLEAN, search = @Search,
                    boolType = @BoolType
            )
    )
    private Boolean status;

    @EruptField(
            views = @View(
                    title = "复合指标计算表达式"
            ),
            edit = @Edit(
                    title = "复合指标计算表达式",
                    type = EditType.TEXTAREA, search = @Search, notNull = true
            )

    )
    private String cal_expression;

    @EruptField(
            views = @View(
                    title = "业务指标定义"
            ),
            edit = @Edit(
                    title = "业务指标定义",
                    type = EditType.TEXTAREA, search = @Search, notNull = true
            )
    )
    private @Lob
    String product_def;


    @EruptField(
            views = @View(
                    title = "技术指标定义"
            ),
            edit = @Edit(
                    title = "技术指标定义",
                    type = EditType.CODE_EDITOR, notNull = true,
                    codeEditType = @CodeEditorType(language = "sql")
            )
    )
    private @Lob
    String tec_def;
}
