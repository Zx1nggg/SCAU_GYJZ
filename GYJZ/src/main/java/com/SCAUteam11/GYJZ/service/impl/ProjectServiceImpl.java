package com.SCAUteam11.GYJZ.service.impl;

import com.SCAUteam11.GYJZ.DTO.OrgStatisticsResponse;
import com.SCAUteam11.GYJZ.DTO.Project.ProjectVO;
import com.SCAUteam11.GYJZ.DTO.StatisticsResponse;
import com.SCAUteam11.GYJZ.entity.mysql.Organization;
import com.SCAUteam11.GYJZ.entity.mysql.Project;
import com.SCAUteam11.GYJZ.mapper.mysql.DonationMapper;
import com.SCAUteam11.GYJZ.mapper.mysql.OrganizationMapper;
import com.SCAUteam11.GYJZ.mapper.mysql.ProjectMapper;
import com.SCAUteam11.GYJZ.service.IProjectService;
import com.SCAUteam11.GYJZ.utils.Statistics.Calculate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service

public class ProjectServiceImpl extends ServiceImpl<ProjectMapper, Project> implements IProjectService {
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private DonationMapper donationMapper;
    @Autowired
    private OrganizationMapper organizationMapper;

    @Override
    public Page<ProjectVO> getProjectList(int page, int size, Long id, Long orgId, String title, String content, LocalDate startDate, LocalDate endDate, String category) {
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
// ... existing code ...
        } else if (endDate != null) {
            // 只有结束时间：精确查询结束时间等于该日期
            LocalDateTime exactStart = endDate.atStartOfDay();             // 00:00:00
            LocalDateTime exactEnd = endDate.atTime(23, 59, 59);           // 23:59:59

            queryWrapper.between(Project::getEndDate, exactStart, exactEnd);
        }
        queryWrapper
                .orderByDesc(Project::getSortOrder)
                .orderByAsc(Project::getProjectStatus)
                .orderByDesc(Project::getCreateTime);

        // 1. 原本的查询语句，拿到 Page<Project>
        Page<Project> result = projectMapper.selectPage(pageInfo, queryWrapper);

        // 2. 核心改造：创建一个新的 Page<ProjectVO>，继承原有分页参数
        Page<ProjectVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());

        List<Project> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return voPage;
        }

        // 3. 防 N+1 优化：提取当前页所有不重复的 orgId
        Set<Long> orgIds = records.stream()
                .map(Project::getOrgId)
                .filter(org -> org != null)
                .collect(Collectors.toSet());

        // 4. 批量查询机构表，组装成 Map<orgId, orgName> 以提高内存读取性能
        Map<Long, String> orgNameMap = new HashMap<>();
        if (!orgIds.isEmpty()) {
            // 注意：这里需要你在这个 Service 里面注入 organizationMapper
            List<Organization> orgList = organizationMapper.selectBatchIds(orgIds);
            for (Organization org : orgList) {
                orgNameMap.put(org.getId(), org.getName());
            }
        }

        // 5. 将 Project 列表转换为 ProjectVO 列表，并塞入 orgName
        List<ProjectVO> voList = records.stream().map(project -> {
            ProjectVO vo = new ProjectVO();
            BeanUtils.copyProperties(project, vo);

            // 从刚刚建好的 Map 中极速查找机构名称
            if (project.getOrgId() != null) {
                vo.setOrgName(orgNameMap.getOrDefault(project.getOrgId(), "公益组织"));
            }
            return vo;
        }).collect(Collectors.toList());

        // 6. 把装配好 orgName 的列表塞进 VO 分页对象并返回
        voPage.setRecords(voList);
        return voPage;
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

    // 找到你 ProjectServiceImpl 里的这个方法，改造它！
    @Override
    public Project getProjectById(Long id) {
        // 1. 先用 MyBatis-Plus 自带的 getById 查出项目本身
        Project project = this.getById(id);
        if (project != null && project.getOrgId() != null) {
            // 2. 拿着 orgId 去机构表查对应的机构对象
            // (注：需要在这个 Service 里 @Autowired private OrganizationMapper organizationMapper;)
            Organization org = organizationMapper.selectById(project.getOrgId());

            // 3. 把机构的名字塞进项目对象里！
            if (org != null) {
                project.setOrgName(org.getName());
            }
        }
        return project;
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
        response.setTotalDonationCount(projectMapper.selectTotalDonationCount(id));
        return response;
    }
}
