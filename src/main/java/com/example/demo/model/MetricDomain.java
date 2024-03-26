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
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.VL;
import xyz.erupt.toolkit.handler.SqlChoiceFetchHandler;

import javax.persistence.Entity;
import javax.persistence.Table;

@Erupt(name = "数据域管理",dataProxy = BaseDataProxy.class)
@Table(name = "metric_domain")
@Entity
public class MetricDomain extends MetaBase {

    @EruptField(
            views = @View(
                    title = "数据域名"
            ),
            edit = @Edit(
                    title = "数据域名",
                    type = EditType.INPUT, search = @Search, notNull = true,
                    inputType = @InputType
            )
    )
    private String domain_name;

    @EruptField(
            views = @View(
                    title = "数据域中文名"
            ),
            edit = @Edit(
                    title = "数据域中文名",
                    type = EditType.INPUT, search = @Search, notNull = true,
                    inputType = @InputType
            )
    )
    private String domain_zh_name;

    @EruptField(
            views = @View(
                    title = "所在的层级"
            ),
            edit = @Edit(
                    title = "所在的层级",
                    type = EditType.CHOICE, search = @Search, notNull = true,
                    choiceType = @ChoiceType(vl = {@VL(value = "1", label = "1"), @VL(value = "2", label = "2")})
            )
    )
    private Integer level_at;



    @EruptField(
            views = @View(
                    title = "父级数据域"
            ),
            edit = @Edit(
                    title = "父级数据域",
                    type = EditType.CHOICE, search = @Search,
                    choiceType = @ChoiceType(
                            fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id,domain_name from metric_domain where level_at = 1"
                    )
            )
    )
    private Integer parent_id;

}
    