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
import com.SCAUteam11.GYJZ.service.IUserService;
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
    @Autowired
    private IUserService userService;
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
            vo.setQualification(apply.getQualification()); // 补充之前忘记加的条件
            return vo;
        }).collect(Collectors.toList());

        Page<ApplicationVO> resultPage = new Page<>(page, size, applyPage.getTotal());
        resultPage.setRecords(voList);

        return Result.success(resultPage);
    }


    /**
     * 统一的审核接口：前端传 applyId, status (1通过/2拒绝), auditRemark(仅拒绝时需要), auditorId
     */
    @PostMapping("/registerApply/audit")
    public Result auditRegisterApply(@RequestBody Map<String, Object> params) {
        // 安全地解析参数
        Long applyId = Long.valueOf(params.get("applyId").toString());
        Integer status = (Integer) params.get("status");
        Long auditorId = params.get("auditorId") != null ? Long.valueOf(params.get("auditorId").toString()) : null;
        String auditRemark = (String) params.get("auditRemark");

        try {
            // 根据状态分发到到写好的那两个 Service 方法
            if (status == 1) {
                // 通过
                userService.approveAdminApply(applyId, auditorId);
            } else if (status == 2) {
                // 拒绝 (如果是拒绝，必须要有理由)
                if (auditRemark == null || auditRemark.trim().isEmpty()) {
                    return Result.fail("拒绝审核必须填写原因");
                }
                userService.rejectAdminApply(applyId, auditRemark, auditorId);
            } else {
                return Result.fail("未知的审核状态");
            }
            return Result.success("审核处理成功");

        } catch (RuntimeException e) {
            // 精准捕获在 Service 里抛出的 "该手机号已被注册" 等业务提示
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("系统异常，审核失败");
        }
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