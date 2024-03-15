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

@Erupt(name = "原子指标",
        filter = @Filter("MetricMetaAtom.metric_type = 1"))
@Table(name = "metric_meta")
@Entity
public class MetricMetaAtom extends BaseModel {

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


    private String metric_type = "1";



    @EruptField(
            views = @View(
                    title = "指标主题"
            ),
            edit = @Edit(
                    title = "指标主题",
                    type = EditType.CHOICE, search = @Search,notNull = true,
                    choiceType = @ChoiceType(
                            fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id,domain_zh_name from metric_domain where level_at = 2"
                    )
            )
    )
    private String metric_theme;

    @EruptField(
            views = @View(
                    title = "聚合方法"
            ),
            edit = @Edit(
                    title = "聚合方法",
                    type = EditType.INPUT, search = @Search,notNull = true,
                    inputType = @InputType
            )
    )
    private String aggregation_method;


    @EruptField(
            views = @View(
                    title = "业务指标定义"
            ),
            edit = @Edit(
                    title = "业务指标定义",
                    type = EditType.TEXTAREA, search = @Search, notNull = true
            )
    )
    private @Lob String product_def;


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
    private @Lob String tec_def;

    @EruptField(
            views = @View(
                    title = "关联的事件/表"
            ),
            edit = @Edit(
                    title = "关联的事件/表",
                    type = EditType.INPUT, search = @Search,
                    inputType = @InputType
            )
    )
    private String data_tb;

    @EruptField(
            views = @View(
                    title = "关联字段属性"
            ),
            edit = @Edit(
                    title = "关联字段属性",
                    type = EditType.INPUT, search = @Search,
                    inputType = @InputType
            )
    )
    private String data_col;

    @EruptField(
            views = @View(
                    title = "关联的数据类型"
            ),
            edit = @Edit(
                    title = "关联的数据类型",
                    type = EditType.CHOICE, search = @Search, notNull = true,
                    choiceType = @ChoiceType(vl = {@VL(value = "1", label = "doris"), @VL(value = "2", label = "ta")})
            )
    )
    private String data_type;

    @EruptField(
            views = @View(
                    title = "更新人"
            ),
            edit = @Edit(
                    title = "更新人",
                    type = EditType.CHOICE,  notNull = true,
                    choiceType = @ChoiceType(
                            fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id,name from e_upms_user"
                    )
            )
    )
    private String updater;

    @EruptField(
            views = @View(
                    title = "更新时间",type = ViewType.DATE_TIME
            )
    )
    private Date update_time;

}
