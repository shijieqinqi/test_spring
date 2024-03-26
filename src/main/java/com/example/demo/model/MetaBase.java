package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.View;
import xyz.erupt.annotation.sub_field.ViewType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;
import xyz.erupt.toolkit.handler.SqlChoiceFetchHandler;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.Date;

/**
 * @author IAN
 * @date 2024/03/19
 **/
@Getter
@Setter
@MappedSuperclass
public class MetaBase {
    @Id
    @GeneratedValue(generator = "generator")
    @GenericGenerator(name = "generator", strategy = "native")
    @Column(name = "id")
    @EruptField(
            views = @View(
                    title = "id"
            )
    )
    private Long id;

    @EruptField(
            views = @View(
                    title = "更新人"
            ),
            edit = @Edit(
                    title = "更新人",
                    type = EditType.CHOICE,
                    show = false,
                    choiceType = @ChoiceType(
                            fetchHandler = SqlChoiceFetchHandler.class,
                            fetchHandlerParams = "select id,name from e_upms_user"
                    )
            )
    )
    private Long updater;

    @EruptField(
            views = @View(
                    title = "更新时间",type = ViewType.DATE_TIME
            )
    )
    private Date update_time ;
}
