package com.example.demo.model;


import com.example.demo.service.SearchDataProxy;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.*;
import xyz.erupt.annotation.sub_field.sub_edit.*;
import xyz.erupt.toolkit.handler.SqlChoiceFetchHandler;

import javax.persistence.*;
import java.util.Date;

@Data
@Erupt(name = "指标搜索",
        filter = @Filter("MetricMetaSearch.status = 1"),
        power = @Power(add = false, edit = false, delete = false, viewDetails = false, export = true),
        dataProxy = SearchDataProxy.class)
@Table(name = "metric_meta")
@Entity
public class MetricMetaSearch {
    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "native")
    @Column(name = "id")
    @EruptField(
            views = @View(
                    title = "id"
            ),
            edit = @Edit(
                    title = "id",
                    search = @Search,
                    readonly=@Readonly(add = true,edit = true)
            )
    )
    private Long id;

    @EruptField(
            views = @View(
                    title = "指标名"
            ),
            edit = @Edit(
                    title = "指标名",
                    type = EditType.INPUT, search = @Search(vague = true) , notNull = true,
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

    @EruptField(
            views = @View(
                    title = "指标类型"
            ),
            edit = @Edit(
                    title = "计算周期",
                    type = EditType.CHOICE, notNull = true,
                    choiceType = @ChoiceType(vl = {@VL(value = "1", label = "原子指标"),
                            @VL(value = "2", label = "派生指标"),
                            @VL(value = "3", label = "复合指标")})
            )
    )
    private String metric_type ;

    @EruptField(
            views = @View(
                    title = "计算周期"
            ),
            edit = @Edit(
                    title = "计算周期",
                    type = EditType.CHOICE, notNull = true,
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
                    title = "创建时间", type = ViewType.DATE_TIME
            )
    )
    private Date create_time;

    @EruptField(
            views = @View(
                    title = "指标最近计算时间", type = ViewType.DATE_TIME
            )
    )
    private Date recent_cal_time;

    @EruptField(
            views = @View(title = "筛选",desc = "",show = false),
            edit = @Edit(title = "筛选", desc = "没有被推送、复合指标使用的指标",
                    type = EditType.CHOICE,
                    choiceType = @ChoiceType(vl = {
                            @VL(value = "1", label = "没有被推送、复合指标使用的指标"),
                    }
                    ),

                    search = @Search)
    )
    @Transient
    private Boolean is_use;
}
