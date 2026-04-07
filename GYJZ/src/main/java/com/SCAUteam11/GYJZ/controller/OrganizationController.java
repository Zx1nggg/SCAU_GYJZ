package com.SCAUteam11.GYJZ.controller;

import com.SCAUteam11.GYJZ.entity.Result;
import com.SCAUteam11.GYJZ.entity.mysql.Organization;
import com.SCAUteam11.GYJZ.service.IOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {
    @Autowired
    private IOrganizationService organizationService;

    /**
     * 获取机构详情
     */
    @GetMapping("/{id}")
    public Result getOrgInfo(@PathVariable Long id) {
        Organization org = organizationService.getById(id);
        if (org == null) return Result.fail("机构不存在");
        return Result.success(org);
    }

    /**
     * 更新机构信息
     */
    @PutMapping
    public Result updateOrgInfo(@RequestBody Organization org) {
        if (org.getId() == null) return Result.fail("ID不能为空");

        // 执行更新
        boolean success = organizationService.updateById(org);
        return success ? Result.success("机构资料更新成功") : Result.fail("资料更新失败");
    }
}
