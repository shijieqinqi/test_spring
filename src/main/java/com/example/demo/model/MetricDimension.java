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
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.toolkit.handler.SqlChoiceFetchHandler;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.util.Date;

@Erupt(name = "维度字典")
@Table(name = "metric_dimension")
@Entity
public class MetricDimension extends BaseModel {
    @EruptField(
            views = @View(
                    title = "维度名"
            ),
            edit = @Edit(
                    title = "维度名",
                    type = EditType.INPUT, search = @Search, notNull = true,
                    inputType = @InputType
            )
    )
    private String dm_name;

    @EruptField(
            views = @View(
                    title = "维度中文名"
            ),
            edit = @Edit(
                    title = "维度中文名",
                    type = EditType.INPUT, search = @Search, notNull = true,
                    inputType = @InputType
            )
    )
    private String dm_zh_name;

    @EruptField(
            views = @View(
                    title = "维度类型"
            ),
            edit = @Edit(
                    title = "维度类型",
                    type = EditType.CHOICE,  notNull = true,
                    choiceType = @ChoiceType(
                            vl = {
                                    @VL(value = "1", label = "数值"), @VL(value = "2", label = "字符串")
                            }
                    ))
    )
    private String data_type;

    @EruptField(
            views = @View(
                    title = "维度描述"
            ),
            edit = @Edit(
                    title = "维度描述",
                    type = EditType.TEXTAREA, search = @Search, notNull = true
            )
    )
    private @Lob String description;

    @EruptField(
            views = @View(
                    title = "更新人"
            ),
            edit = @Edit(
                    title = "更新人",
                    type = EditType.CHOICE, search = @Search, notNull = true,
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
    private Date update_time ;

}
    