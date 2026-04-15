package com.SCAUteam11.GYJZ.service;

import com.SCAUteam11.GYJZ.DTO.OrgStatisticsResponse;
import com.SCAUteam11.GYJZ.DTO.Project.ProjectVO;
import com.SCAUteam11.GYJZ.DTO.StatisticsResponse;
import com.SCAUteam11.GYJZ.entity.mysql.Project;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface IProjectService extends IService<Project> {
    Page<ProjectVO> getProjectList(int page, int size,
                                   Long id, Long orgId, String title, String content, LocalDate startDate, LocalDate endDate, String category);
    boolean addProject(Project project);
    boolean updateProject(Project project);
    boolean deleteProject(Long id);
    Project getProjectById(Long id); // 查询项目详情
    StatisticsResponse analyseProject(Long id); // 得到具体项目的统计数据
    OrgStatisticsResponse analyseOrgProject(Long id); // 得到具体组织的统计数据


}
