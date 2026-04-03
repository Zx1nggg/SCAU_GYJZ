package com.SCAUteam11.GYJZ.service.impl;

import com.SCAUteam11.GYJZ.DTO.OrgStatisticsResponse;
import com.SCAUteam11.GYJZ.DTO.StatisticsResponse;
import com.SCAUteam11.GYJZ.entity.mysql.Project;
import com.SCAUteam11.GYJZ.mapper.mysql.DonationMapper;
import com.SCAUteam11.GYJZ.mapper.mysql.ProjectMapper;
import com.SCAUteam11.GYJZ.service.IProjectService;
import com.SCAUteam11.GYJZ.utils.Statistics.Calculate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service

public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements IProjectService {
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private DonationMapper donationMapper;

    @Override
    public Page<Project> getProjectList(int page, int size, Long id, Long orgId, String title, String content, LocalDate startDate, LocalDate endDate, String category) {
        // 创建分页对象
        Page<Project> pageInfo = new Page<>(page, size);

        // 创建查询条件
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(id != null, Project::getId, id)
                .eq(orgId != null, Project::getOrgId, orgId)
                .like(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(title), Project::getTitle, title)
                .like(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(content), Project::getContent, content)
                .like(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(category), Project::getCategory, category);
        // 时间范围查询
        if (startDate != null && endDate != null) {
            // 两个都有：查询项目时间完全包含在查询区间内
            LocalDateTime queryStart = startDate.atStartOfDay();           // 00:00:00
            LocalDateTime queryEnd = endDate.atTime(23, 59, 59);           // 23:59:59

            // 项目开始时间 >= 查询开始时间 AND 项目结束时间 <= 查询结束时间
            queryWrapper
                    .ge(Project::getStartDate, queryStart)
                    .le(Project::getEndDate, queryEnd);

        } else if (startDate != null) {
            // 只有开始时间：精确查询开始时间等于该日期
            LocalDateTime exactStart = startDate.atStartOfDay();           // 00:00:00
            LocalDateTime exactEnd = startDate.atTime(23, 59, 59);         // 23:59:59

            queryWrapper.between(Project::getStartDate, exactStart, exactEnd);

        } else if (endDate != null) {
            // 只有结束时间：精确查询结束时间等于该日期
            LocalDateTime exactStart = endDate.atStartOfDay();             // 00:00:00
            LocalDateTime exactEnd = endDate.atTime(23, 59, 59);           // 23:59:59

            queryWrapper.between(Project::getEndDate, exactStart, exactEnd);
        }
        queryWrapper.orderByDesc(Project::getSortOrder); // 优先级高的先显示

        Page<Project> result = projectMapper.selectPage(pageInfo, queryWrapper);
        return result;
    }

    @Override
    public boolean addProject(Project project) {
        // 1. 由后端统一生成创建时间，保证绝对的服务器时间一致性
        project.setCreateTime(java.time.LocalDateTime.now());

        // 2. 安全保障：防止前端没传初始值导致数据库报错
        if (project.getProjectStatus() == null) {
            project.setProjectStatus(1); //  1 是进行中状态
        }

        // 确保新项目的当前已筹金额是 0
        if (project.getCurrentAmount() == null) {
            project.setCurrentAmount(new java.math.BigDecimal("0.00"));
        }

        // 3. 插入数据库
        int result = projectMapper.insert(project);
        return result > 0;
    }

    @Override
    public boolean updateProject(Project project) {
        // 检查项目是否存在
        Project existingProject = projectMapper.selectById(project.getId());
        if (existingProject == null) {
            throw new RuntimeException("项目不存在");
        }
        projectMapper.updateById(project);
        return true;
    }

    @Override
    public boolean deleteProject(Long id) {
        if(id==null || id <= 0){
            throw new RuntimeException("项目ID不能为空");
        }
        Project existingProject = projectMapper.selectById(id);
        if (existingProject == null) {
            throw new RuntimeException("项目不存在");
        }
        projectMapper.deleteById(id);
        return true;
    }

    @Override
    public Project getProjectById(Long id) {
        if(id==null || id <= 0){
            throw new RuntimeException("项目ID不能为空");
        }
        return projectMapper.selectById(id);
    }

    @Override
    public StatisticsResponse analyseProject(Long id) {
        // 参数校验
        if(id==null || id <= 0){
            throw new RuntimeException("项目ID不能为空");
        }
        Project project = projectMapper.selectById(id);
        // 检查项目是否存在
        if (project == null) {
            throw new RuntimeException("项目不存在");
        }
        // TODO: 实现项目分析逻辑
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(Project::getId, id);
        Project existingProject = projectMapper.selectOne(queryWrapper);
        // 检查项目是否存在
        StatisticsResponse response = new StatisticsResponse();
        response.setId(id);
        response.setTitle(existingProject.getTitle());
        response.setTargetAmount(existingProject.getTargetAmount());
        response.setCurrentAmount(existingProject.getCurrentAmount());
        response.setProgressPercentage(Calculate.calculateProgressPercentage(existingProject.getCurrentAmount(),existingProject.getTargetAmount()));
        response.setDonorCount(donationMapper.selectDonationsCountByProjectId(id));
        response.setAverageDonation(Calculate.calculateAverageDonation(existingProject.getCurrentAmount(),response.getDonorCount()));
        response.setMaxDonation(donationMapper.selectMaxDonationByProjectId(id));
        response.setMinDonation(donationMapper.selectMinDonationByProjectId(id));
        response.setDonationCount(donationMapper.selectDonationCountByProjectId(id));
        return response;
    }

    @Override
    public OrgStatisticsResponse analyseOrgProject(Long id) {
        if(id==null || id <= 0){
            throw new RuntimeException("机构ID不能为空");
        }
        // 查询机构旗下项目
        LambdaQueryWrapper<Project> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Project::getOrgId,id);
        OrgStatisticsResponse response = new OrgStatisticsResponse();
        response.setOrgId(id);
        response.setTotalProjectCount(projectMapper.selectTotalProject(id));
        response.setTotalAmount(projectMapper.selectTotalAmount(id));
        response.setTotalDonorCount(projectMapper.selectTotalDonor(id));
        return response;
    }
}
