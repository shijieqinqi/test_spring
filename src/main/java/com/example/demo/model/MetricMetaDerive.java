package com.example.demo.model;


import com.example.demo.service.BatchInitTecDefHandlerImpl;
import com.example.demo.service.DeriveDataCopyHandlerImpl;
import com.example.demo.service.DeriveDataProxy;
import com.example.demo.service.DeriveModifierTagsFetchHandler;
import lombok.Data;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.RowOperation;
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
@Erupt(name = "派生指标",
        filter = @Filter("MetricMetaDerive.metric_type = 2"),
        rowOperation = {
                @RowOperation(
                        title = "复制",
                        icon = "fa fa-clone",
                        mode = RowOperation.Mode.SINGLE,
                        operationHandler = DeriveDataCopyHandlerImpl.class),
                @RowOperation(
                        title = "批量重新生成sql",
                        tip = "请确认选中数据无自定义sql",
                        mode = RowOperation.Mode.MULTI,
                        operationHandler = BatchInitTecDefHandlerImpl.class)},
        dataProxy = DeriveDataProxy.class)
@Table(name = "metric_meta")
@Entity
public class MetricMetaDerive extends MetaBase {

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

    private String metric_type = "2";
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
    private Integer metric_theme;
    @EruptField(
            views = @View(
                    title = "状态"
            ),
            edit = @Edit(
                    title = "状态",
                    type = EditType.BOOLEAN, notNull = true, search = @Search,
                    boolType = @BoolType
            )
    )
    private Boolean status;


    @Transient
    @EruptField(
            edit = @Edit(title = "表配置", type = EditType.DIVIDE)
    )
    private String divide;

    @EruptField(
            views = @View(
                    title = "上游指标"
            ),
            edit = @Edit(
                    title = "上游指标",
                    type = EditType.CHOICE, search = @Search, notNull = true,
                    choiceType = @ChoiceType(
                            fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id,metric_zh_name from metric_meta where metric_type = 1"
                    )
            )
    )
    private Integer upstream_metric;



    @EruptField(
            views = @View(
                    title = "统计周期"
            ),
            edit = @Edit(
                    title = "统计周期",
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(
                            fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id,stat_period_zh_name from metric_stat_period"
                    )
            )
    )
    private Integer stat_period;



    @EruptField(
            views = @View(
                    title = "修饰词id", show = false
            ),
            edit = @Edit(
                    title = "修饰词id",
                    type = EditType.CHOICE, show = false, search = @Search,
                    choiceType = @ChoiceType(
                            fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id,id from metric_modifiers order by id "
                    )
            )
    )
    private String modifier_def;

    @EruptField(
            views = @View(title = "修饰词描述", desc = "格式(id:修饰名)"),
            edit = @Edit(title = "修饰词描述", desc = "格式(id:修饰名:所属原子指标名)",
                    type = EditType.TAGS,
                    tagsType = @TagsType(fetchHandler = DeriveModifierTagsFetchHandler.class, allowExtension = false))
    )
    @Transient
    private String modifier_def_view;

    @EruptField(
            views = @View(
                    title = "业务定义"
            ),
            edit = @Edit(
                    title = "业务定义",
                    type = EditType.TEXTAREA, search = @Search, notNull = true
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
                    title = "指标最近计算时间", type = ViewType.DATE_TIME
            )
    )
    private Date recent_cal_time;


}
