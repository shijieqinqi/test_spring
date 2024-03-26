package com.example.demo.model;/*
 * Copyright © 2020-2035 erupt.xyz All rights reserved.
 * Author: YuePeng (erupts@126.com)
 */

import com.HelloTalk.service.BaseDataProxy;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Erupt(name = "统计周期元数据表",dataProxy = BaseDataProxy.class)
@Table(name = "metric_stat_period")
@Entity
public class MetricStatPeriod extends MetaBase {

    @EruptField(
            views = @View(
                    title = "周期标识"
            ),
            edit = @Edit(
                    title = "周期标识",
                    type = EditType.INPUT, search = @Search, notNull = true,
                    inputType = @InputType
            )
    )
    private String stat_period_name;

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
    private String stat_period_zh_name;

    @EruptField(
            views = @View(
                    title = "业务定义"
            ),
            edit = @Edit(
                    title = "业务定义",
                    type = EditType.TEXTAREA, search = @Search, notNull = true
            )
    )
    private @Lob String product_def;

    @EruptField(
            views = @View(
                    title = "ta技术定义"
            ),
            edit = @Edit(
                    title = "ta技术定义",
                    type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "sql")
            )
    )
    private @Lob String ta_tec_def;

    @EruptField(
            views = @View(
                    title = "doris技术定义"
            ),
            edit = @Edit(
                    title = "doris技术定义",
                    type = EditType.CODE_EDITOR,
                    codeEditType = @CodeEditorType(language = "sql")
            )
    )
    private @Lob String doris_tec_def;
}
    