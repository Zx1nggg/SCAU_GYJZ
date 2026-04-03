package com.SCAUteam11.GYJZ.controller;

import com.SCAUteam11.GYJZ.DTO.OrgStatisticsResponse;
import com.SCAUteam11.GYJZ.DTO.StatisticsResponse;
import com.SCAUteam11.GYJZ.entity.Result;
import com.SCAUteam11.GYJZ.entity.mysql.Project;
import com.SCAUteam11.GYJZ.service.IProjectService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    @Autowired
    private IProjectService projectService;

    @GetMapping
    public Result getProjectList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String category) {
        // String的时间转LocalDate
        LocalDate start = null;
        LocalDate end = null;
        try {
            if (StringUtils.isNotBlank(startDate)) {
                start = LocalDate.parse(startDate);  // 支持 2026-01-01 格式
            }
            if (StringUtils.isNotBlank(endDate)) {
                end = LocalDate.parse(endDate);
            }
        } catch (Exception e) {
            throw new RuntimeException("日期格式错误");
        }
        Page<Project> result = projectService.getProjectList(page, size, id, orgId, title, content, start, end, category);
        // 返回数据
        return Result.success(result);
    }

    @PostMapping
    public Result addProject(@RequestBody Project project) {
        boolean success = projectService.addProject(project);
        if (success) {
            return Result.success("添加成功");
        } else {
            return Result.fail("添加失败");
        }
    }

    @PutMapping
    public Result updateProject(@RequestBody Project project) {
        boolean success = projectService.updateProject(project);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.fail("更新失败");
        }

    }

    @DeleteMapping("/{id}")
    public Result deleteProject(@PathVariable Long id) {
        boolean success = projectService.deleteProject(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.fail("删除失败");
        }
    }

    @GetMapping("/{id}")
    public Result getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return Result.success(project);
    }

    @GetMapping("/{id}/statistics")
    public Result getProjectStatistics(@PathVariable Long id) {
        StatisticsResponse statistics = projectService.analyseProject(id);
        return Result.success(statistics);
    }

    @GetMapping("/{orgId}/OrgStatistics")
    public Result getOrgProjectStatistics(@PathVariable Long orgId) {
        OrgStatisticsResponse statistics = projectService.analyseOrgProject(orgId);
        return Result.success(statistics);
    }

    /**
     * 管理员手动设置项目状态（例如手动终止、取消、恢复）
     * 接口路径：PUT /api/v1/projects/{id}/status?status=2
     */
    @PutMapping("/projects/{id}/status")
    public Result updateProjectStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        Project project = projectService.getById(id);
        if (project == null) return Result.fail("项目不存在");

        // 逻辑校验：已筹满的项目如果被手动结束是合理的
        project.setProjectStatus(status);

        boolean success = projectService.updateById(project);
        return success ? Result.success("状态更新成功") : Result.fail("状态更新失败");
    }

}
