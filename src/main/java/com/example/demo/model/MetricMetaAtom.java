package com.example.demo.model;

import com.example.demo.service.AtomDataCopyHandlerImpl;
import com.example.demo.service.BaseDataProxy;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.RowOperation;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.toolkit.handler.SqlChoiceFetchHandler;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Erupt(name = "原子指标",
        filter = @Filter("MetricMetaAtom.metric_type = 1"),
        rowOperation = {
        @RowOperation(
                title = "复制",
                icon = "fa fa-clone",
                mode = RowOperation.Mode.SINGLE,
                operationHandler = AtomDataCopyHandlerImpl.class),}
        ,dataProxy = BaseDataProxy.class)
@Table(name = "metric_meta")
@Entity
public class MetricMetaAtom extends MetaBase {

    @EruptField(
            views = @View(
                    title = "指标名"
            ),
            edit = @Edit(
                    title = "指标名",
                    type = EditType.INPUT, search = @Search(vague = true), notNull = true,
                    inputType = @InputType
            )
    )
    private String metric_name;

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
                    type = EditType.INPUT, search = @Search,
                    inputType = @InputType
            )
    )
    private String aggregation_method;

    @EruptField(
            views = @View(
                    title = "数据源"
            ),
            edit = @Edit(
                    title = "数据源",
                    type = EditType.CHOICE, search = @Search, notNull = true,
                    choiceType = @ChoiceType(vl = {@VL(value = "1", label = "doris"), @VL(value = "2", label = "ta")})
            )
    )
    private String data_type;

    @EruptField(
            views = @View(
                    title = "项目名/库名",desc = "为空时：数据源为 ta 时默认为：v_event_12，数据源为 doris 时默认为：dw_ht_data"
            ),
            edit = @Edit(
                    title = "项目名/库名",desc = "当数据源为 ta 时默认为：v_event_12，当数据源为 doris 时默认为：dw_ht_data",
                    type = EditType.INPUT,search = @Search,
                    inputType = @InputType
            )
    )
    private String project_name;

    @EruptField(
            views = @View(
                    title = "事实表"
            ),
            edit = @Edit(
                    title = "事实表",
                    type = EditType.INPUT, search = @Search(vague = true),
                    inputType = @InputType
            )
    )
    private String events;

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
                    title = "业务定义"
            ),
            edit = @Edit(
                    title = "业务定义",
                    type = EditType.TEXTAREA, notNull = true
            )
    )
    private @Lob String product_def;


}
