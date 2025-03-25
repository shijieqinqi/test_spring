package com.example.demo.model;/*
 * Copyright © 2020-2035 erupt.xyz All rights reserved.
 * Author: YuePeng (erupts@126.com)
 */

import com.example.demo.service.ModifierDataProxy;
import com.example.demo.service.ModifierIdTagsFetchHandler;
import lombok.Data;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

@Data
@Erupt(name = "修饰词字典",dataProxy = ModifierDataProxy.class)
@Table(name = "metric_modifiers")
@Entity
public class MetricModifiers extends MetaBase {

    @EruptField(
            views = @View(
                    title = "修饰词名"
            ),
            edit = @Edit(
                    title = "修饰词名",
                    type = EditType.INPUT, search = @Search(vague = true), notNull = true,
                    inputType = @InputType
            )
    )
    private String modifier_name;

    @EruptField(
            views = @View(
                    title = "显示名"
            ),
            edit = @Edit(
                    title = "显示名",
                    type = EditType.INPUT, search = @Search(vague = true), notNull = true,
                    inputType = @InputType
            )
    )
    private String modifier_zh_name;

    @EruptField(
            views = @View(
                    title = "修饰的指标id",
                    show = false
            ),
            edit = @Edit(
                    title = "修饰的指标id",
                    type = EditType.INPUT,
                    show = false,
                    inputType = @InputType
            )
    )
    private String modifier_id;

    @EruptField(
            views = @View(title = "修饰的指标",desc = "格式（原子指标id：原子指标中文名）"),
            edit = @Edit(title = "修饰的指标", type = EditType.TAGS, notNull = true,
                    desc = "格式（原子指标id：原子指标中文名）", search = @Search,
                    tagsType = @TagsType(fetchHandler = ModifierIdTagsFetchHandler.class,allowExtension = false))
    )
    @Transient
    private String modifier_id_view;

    @EruptField(
            views = @View(
                    title = "业务定义"
            ),
            edit = @Edit(
                    title = "业务定义",
                    type = EditType.TEXTAREA, notNull = true
            )
    )
    private @Lob String product_def;

    @EruptField(
            views = @View(
                    title = "技术定义"
            ),
            edit = @Edit(
                    title = "技术定义",
                    type = EditType.CODE_EDITOR,notNull = true,search = @Search(vague = true),
                    codeEditType = @CodeEditorType(language = "sql")
            )
    )
    private @Lob String tec_def;

    @EruptField(
            views = @View(
                    title = "创建时间", type = ViewType.DATE_TIME
            )
    )
    private final Date create_time = new Date();
}
    