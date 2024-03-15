package com.example.demo.model;/*
 * Copyright © 2020-2035 erupt.xyz All rights reserved.
 * Author: YuePeng (erupts@126.com)
 */

import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.toolkit.handler.SqlChoiceFetchHandler;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

@Erupt(name = "修饰词字典")
@Table(name = "metric_modifiers")
@Entity
public class MetricModifiers extends BaseModel {

    @EruptField(
            views = @View(
                    title = "修饰词名"
            ),
            edit = @Edit(
                    title = "修饰词名",
                    type = EditType.INPUT, search = @Search, notNull = true,
                    inputType = @InputType
            )
    )
    private String modifier_name;

    @EruptField(
            views = @View(
                    title = "修饰词中文名"
            ),
            edit = @Edit(
                    title = "修饰词中文名",
                    type = EditType.INPUT, search = @Search, notNull = true,
                    inputType = @InputType
            )
    )
    private String modifier_zh_name;

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
                    title = "技术指标定义"
            ),
            edit = @Edit(
                    title = "技术指标定义",
                    type = EditType.CODE_EDITOR,notNull = true,
                    codeEditType = @CodeEditorType(language = "sql")
            )
    )
    private @Lob String tec_def;

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
                    title = "更新时间",type = ViewType.DATE_TIME
            )
    )
    private Date update_time;
}
    