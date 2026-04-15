package com.SCAUteam11.GYJZ.DTO.Project;

import com.SCAUteam11.GYJZ.entity.mysql.Project;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectVO extends Project {

    // 新增专门给前端展示的机构名称字段
    private String orgName;

}
