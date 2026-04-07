package com.SCAUteam11.GYJZ.controller;

import com.SCAUteam11.GYJZ.DTO.Application.ApplicationVO;
import com.SCAUteam11.GYJZ.DTO.PlatformStatsResponse;
import com.SCAUteam11.GYJZ.entity.Result;
import com.SCAUteam11.GYJZ.entity.mysql.Organization;
import com.SCAUteam11.GYJZ.entity.mysql.RegisterApply;
import com.SCAUteam11.GYJZ.entity.mysql.User;
import com.SCAUteam11.GYJZ.mapper.mysql.OrganizationMapper;
import com.SCAUteam11.GYJZ.mapper.mysql.RegisterApplyMapper;
import com.SCAUteam11.GYJZ.mapper.mysql.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sadmin")
public class SAdminController {

    @Autowired
    private RegisterApplyMapper applyMapper;
    @Autowired
    private OrganizationMapper orgMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 1. 获取机构入驻申请列表
     */
    @GetMapping("/organizations/applications")
    public Result getApplications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {

        Page<RegisterApply> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<RegisterApply> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(RegisterApply::getApplyStatus, status);
        }
        wrapper.orderByDesc(RegisterApply::getCreateTime);

        Page<RegisterApply> applyPage = applyMapper.selectPage(pageParam, wrapper);

        // 将实体类转换为前端需要的 VO 格式
        List<ApplicationVO> voList = applyPage.getRecords().stream().map(apply -> {
            ApplicationVO vo = new ApplicationVO();
            vo.setId(apply.getId());
            vo.setName(apply.getInstitutionName());
            vo.setCode(apply.getCreditCode());
            vo.setContactPerson(apply.getContactPerson());
            vo.setContactPhone(apply.getContactPhone());
            vo.setContent(apply.getApplyReason());
            vo.setOrgStatus(apply.getApplyStatus());
            vo.setAuditRemark(apply.getAuditRemark());
            vo.setCreateTime(apply.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        Page<ApplicationVO> resultPage = new Page<>(page, size, applyPage.getTotal());
        resultPage.setRecords(voList);

        return Result.success(resultPage);
    }

    /**
     * 2. 审核机构入驻申请 (核心业务逻辑)
     */
    @PutMapping("/organizations/{id}/audit")
    @Transactional(rollbackFor = Exception.class)
    public Result auditApplication(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = (Integer) params.get("status");
        String auditRemark = (String) params.get("auditRemark");

        RegisterApply apply = applyMapper.selectById(id);
        if (apply == null) return Result.fail("申请记录不存在");
        if (apply.getApplyStatus() != 0) return Result.fail("该申请已被处理，请勿重复操作");

        // 1. 更新申请表状态
        apply.setApplyStatus(status);
        apply.setAuditRemark(auditRemark);
        apply.setAuditTime(LocalDateTime.now());
        // 假设当前操作的超管ID可以从Token里拿，这里暂时留空或写死
        applyMapper.updateById(apply);

        // 2. 核心：如果审核通过，自动生成机构和管理员账号
        if (status == 1) {
            // (A) 创建机构记录
            Organization org = new Organization();
            org.setName(apply.getInstitutionName());
            org.setCode(apply.getCreditCode());
            org.setContactPerson(apply.getContactPerson());
            org.setContactPhone(apply.getContactPhone());
            org.setContent(apply.getApplyReason()); // 把申请理由作为初始简介
            org.setCreateTime(LocalDateTime.now());
            orgMapper.insert(org);

            // (B) 创建该机构的初始管理员账号 (Role: 2)
            User adminUser = new User();
            adminUser.setUsername(apply.getPhone()); // 默认用手机号做登录名
            adminUser.setPhone(apply.getPhone());
            adminUser.setPassword(apply.getPassword()); // 假设之前申请时已加密
            adminUser.setNickname(apply.getContactPerson()); // 默认昵称为联系人姓名
            adminUser.setRole(2); // 2 代表机构管理员
            adminUser.setOrgId(org.getId()); // 绑定刚刚生成的机构ID
            adminUser.setUserStatus(1); // 账号正常
            adminUser.setCreateTime(LocalDateTime.now());
            userMapper.insert(adminUser);
        }

        return Result.success("审核处理完成");
    }

    /**
     * 3. 获取全平台用户列表 (支持多条件搜索)
     */
    @GetMapping("/users")
    public Result getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer role) {

        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (role != null) {
            wrapper.eq(User::getRole, role);
        }

        if (StringUtils.hasText(keyword)) {
            // 模糊匹配 手机号 或 账号 或 昵称
            wrapper.and(w -> w.like(User::getPhone, keyword)
                    .or().like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword));
        }

        wrapper.orderByDesc(User::getCreateTime);

        Page<User> userPage = userMapper.selectPage(pageParam, wrapper);

        // 为了安全，把密码擦除后再返回给前端
        userPage.getRecords().forEach(u -> u.setPassword(null));

        return Result.success(userPage);
    }

    /**
     * 4. 更改用户账号状态 (封禁/解禁)
     */
    @PutMapping("/users/{id}/status")
    public Result updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Integer newStatus = params.get("status"); // 1-正常, 2-封禁
        if (newStatus == null) return Result.fail("状态参数不能为空");

        User user = userMapper.selectById(id);
        if (user == null) return Result.fail("用户不存在");

        // 防御性编程：禁止封禁超级管理员自己
        if (user.getRole() != null && user.getRole() == 9) {
            return Result.fail("权限不足：无法封禁超级管理员账号");
        }

        user.setUserStatus(newStatus);
        userMapper.updateById(user);

        return Result.success(newStatus == 1 ? "用户已解禁" : "用户已封禁");
    }

    @GetMapping("/stats")
    public Result getPlatformStats() {
        PlatformStatsResponse stats = new PlatformStatsResponse();

        // 1. 待处理的入驻申请数 (apply_status = 0)
        LambdaQueryWrapper<RegisterApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(RegisterApply::getApplyStatus, 0);
        stats.setPendingApplyCount(applyMapper.selectCount(applyWrapper));

        // 2. 已入驻的机构总数
        stats.setApprovedOrgCount(orgMapper.selectCount(null));

        // 3. 全平台注册用户总数
        stats.setTotalUserCount(userMapper.selectCount(null));

        return Result.success(stats);
    }
}