package com.SCAUteam11.GYJZ.service.impl;

import com.SCAUteam11.GYJZ.entity.mysql.Organization;
import com.SCAUteam11.GYJZ.entity.mysql.RegisterApply;
import com.SCAUteam11.GYJZ.entity.mysql.User;
import com.SCAUteam11.GYJZ.mapper.mysql.OrganizationMapper;
import com.SCAUteam11.GYJZ.mapper.mysql.RegisterApplyMapper;
import com.SCAUteam11.GYJZ.mapper.mysql.UserMapper;
import com.SCAUteam11.GYJZ.service.IUserService;
import com.SCAUteam11.GYJZ.utils.RedisUtil;
import com.SCAUteam11.GYJZ.utils.UsernameGenerator;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RegisterApplyMapper registerApplyMapper;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;// 密码加密器
    @Autowired
    private OrganizationMapper organizationMapper;

    // =========== 登录相关 ===========
    @Override
    public User donorLogin(User user) {
        // 检查用户ID是否为空或密码是否为空字符串
        if (ObjectUtils.isEmpty(user.getPhone()) || !StringUtils.hasLength(user.getPassword())) {
            throw new RuntimeException("手机号或密码不能为空");
        }
        // 尝试从Redis缓存中获取用户信息
        Object o = redisUtil.get(String.valueOf(user.getPhone()));
        if (o == null) {
            // 缓存中没有数据，就要区数据库里面找
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(User::getPhone, user.getPhone());
            User targetUser = userMapper.selectOne(queryWrapper);
            if (targetUser != null) {
                // 将用户信息存进Redis
                redisUtil.set(String.valueOf(user.getPhone()), targetUser);
                return targetUser;
            }else{
                throw new RuntimeException("用户名或密码错误");
            }
        }
        // 如果缓存中有数据，用户信息更新需要删除Redis中的数据
        User targetUser = (User) o;
        // 检查用户ID和密码是否匹配
        if (passwordEncoder.matches(user.getPassword(), targetUser.getPassword())) {
            return targetUser;
        } else {
            throw new RuntimeException("用户名或密码错误");
        }
    }

    @Override
    public User adminLogin(User user){
        // 检查用户名是否为空或密码是否为空字符串
        if (!StringUtils.hasLength(user.getUsername()) || !StringUtils.hasLength(user.getPassword())) {
            throw new RuntimeException("用户或密码不能为空");
        }
        // 尝试从Redis缓存中获取用户信息
        Object o = redisUtil.get(String.valueOf(user.getUsername()));
        if (o == null) {
            // 缓存中没有数据，就要区数据库里面找
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(User::getUsername, user.getUsername());
            User targetUser = userMapper.selectOne(queryWrapper);
            if (targetUser != null) {
                // 将用户信息存进Redis
                redisUtil.set(String.valueOf(user.getUsername()), targetUser);
                return targetUser;
            }else{
                throw new RuntimeException("用户名或密码错误");
            }
        }
        // 如果缓存中有数据，用户信息更新需要删除Redis中的数据
        User targetUser = (User) o;
        // 检查用户ID和密码是否匹配
        if (passwordEncoder.matches(user.getPassword(), targetUser.getPassword())) {
            return targetUser;
        } else {
            throw new RuntimeException("用户名或密码错误");
        }
    }
    // ========== 注册相关 ==========
    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized boolean donorRegister(User user){
        // 检查手机号和密码是否为空
        if (!StringUtils.hasLength(user.getPhone()) || !StringUtils.hasLength(user.getPassword())) {
            throw new RuntimeException("手机号或密码不能为空");
        }
        // 手机号格式校验
        if (!user.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }

        // 检查数据库中是否已存在该用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, user.getPhone());
        User user1 = baseMapper.selectOne(queryWrapper);
        if(user1 !=null){
            // 如果数据库中已存在该用户，返回注册失败
            throw new RuntimeException("用户已经存在");
        }else {
            user.setRole(1);  // 捐赠者
            user.setUserStatus(1);  // 正常状态
            user.setPushEnabled(1);  // 开启推送
            user.setCreateTime(LocalDateTime.now());  // 手动设置创建时间
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            // 如果数据库中不存在该用户，则插入新记录
            baseMapper.insert(user);
            log.info("捐赠人注册成功，手机号：{}，昵称：{}", user.getPhone(), user.getNickname());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized void submitAdminApply(RegisterApply apply){
        // ========== 1. 参数校验 ==========
        // 手机号校验
        if (!StringUtils.hasLength(apply.getPhone())) {
            throw new RuntimeException("手机号不能为空");
        }
        if (!apply.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确");
        }

        // 密码校验
        if (!StringUtils.hasLength(apply.getPassword())) {
            throw new RuntimeException("密码不能为空");
        }
        if (apply.getPassword().length() < 6 || apply.getPassword().length() > 30) {
            throw new RuntimeException("密码长度应为6-30位");
        }

        // 机构信息校验
        if (!StringUtils.hasLength(apply.getInstitutionName())) {
            throw new RuntimeException("机构名称不能为空");
        }
        if (!StringUtils.hasLength(apply.getCreditCode())) {
            throw new RuntimeException("统一社会信用代码不能为空");
        }
        if (apply.getCreditCode().length() != 18) {
            throw new RuntimeException("统一社会信用代码应为18位");
        }
        if (!StringUtils.hasLength(apply.getContactPerson())) {
            throw new RuntimeException("联系人不能为空");
        }
        if (!StringUtils.hasLength(apply.getContactPhone())) {
            throw new RuntimeException("联系电话不能为空");
        }
        if (!StringUtils.hasLength(apply.getQualification())) {
            throw new RuntimeException("请上传资质文件");
        }
        // ========== 2. 业务校验 ==========
        // 检查手机号是否已经是管理员或超级管理员
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getPhone, apply.getPhone())
                .in(User::getRole, Arrays.asList(2, 9));
        User existUser = userMapper.selectOne(userWrapper);
        if (existUser != null) {
            throw new RuntimeException("该手机号已是管理员，请直接登录");
        }

        // 检查手机号是否已有待审核的申请
        LambdaQueryWrapper<RegisterApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(RegisterApply::getPhone, apply.getPhone())
                .eq(RegisterApply::getApplyStatus, 0);
        RegisterApply existApply = registerApplyMapper.selectOne(applyWrapper);
        if (existApply != null) {
            throw new RuntimeException("您已提交过申请，请等待审核");
        }

        // ========== 3. 保存申请 ==========
        apply.setPassword(passwordEncoder.encode(apply.getPassword()));
        apply.setApplyStatus(0);  // 0-待审核
        apply.setCreateTime(LocalDateTime.now());

        int result = registerApplyMapper.insert(apply);
        if (result <= 0) {
            throw new RuntimeException("提交申请失败，请稍后重试");
        }

        log.info("管理员注册申请已提交，申请ID：{}，手机号：{}，机构：{}",
                apply.getId(), apply.getPhone(), apply.getInstitutionName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveAdminApply(Long applyId, Long auditorId) { // 通过申请
        // ========== 1. 查询申请 ==========
        RegisterApply apply = registerApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new RuntimeException("申请不存在");
        }
        if (apply.getApplyStatus() != 0) {
            throw new RuntimeException("该申请已审核");
        }
        // 再次检查手机号是否已经被注册
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, apply.getPhone());
        User user = userMapper.selectOne(queryWrapper);
        if (user != null) {
            apply.setApplyStatus(2); //审核不通过
            apply.setAuditRemark("该手机号已被注册");
            apply.setAuditTime(LocalDateTime.now());
            apply.setAuditorId(auditorId);
            registerApplyMapper.updateById(apply);
            throw new RuntimeException("该手机号已被注册，审核不通过");
        }

        // ========== 2. 新增：创建该机构的专属档案 ==========
        Organization org = new Organization();
        org.setName(apply.getInstitutionName());
        // 先用手机号作为联系电话
        org.setContactPhone(apply.getPhone());
        org.setOrgStatus(1); // 1-正常
        org.setCreateTime(LocalDateTime.now());

        int orgResult = organizationMapper.insert(org);
        if (orgResult <= 0) {
            throw new RuntimeException("创建机构记录失败");
        }
        // MyBatis-Plus 会在 insert 成功后，自动将数据库生成的自增 ID 回填到 org 对象中
        Long newOrgId = org.getId();

        // ========== 3. 创建管理员用户 ==========
        // 生成随机账号
        String username = UsernameGenerator.generateAccount(apply.getInstitutionName());

        User adminUser = new User();
        adminUser.setUsername(username);
        adminUser.setPhone(apply.getPhone());

        // 包一层 passwordEncoder.encode(apply.getPassword())
        adminUser.setPassword(apply.getPassword());

        adminUser.setNickname(apply.getInstitutionName());
        adminUser.setRole(2);  // 管理员角色
        adminUser.setUserStatus(1);
        adminUser.setPushEnabled(1);
        adminUser.setCreateTime(LocalDateTime.now());

        // 核心绑定：将刚才生成的机构ID绑定给这个新管理员
        adminUser.setOrgId(newOrgId);

        int result = userMapper.insert(adminUser);
        if (result <= 0) {
            throw new RuntimeException("创建管理员账号失败");
        }

        // ========== 4. 更新申请状态 ==========
        apply.setApplyStatus(1);  // 1-已通过
        apply.setAuditRemark("审核通过，账号：" + username);
        apply.setAuditTime(LocalDateTime.now());
        apply.setAuditorId(auditorId);
        registerApplyMapper.updateById(apply);

        // 模拟发送短信通知
        log.info("========== 审核通过通知 ==========");
        log.info("手机号：{}", apply.getPhone());
        log.info("机构名称：{} (分配ID: {})", org.getName(), newOrgId);
        log.info("账号：{}", username);
        log.info("密码：用户设置的密码");
        log.info("=================================");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectAdminApply(Long applyId, String reason,Long auditorId) {
        // 获取申请信息
        RegisterApply apply = registerApplyMapper.selectById(applyId);
        if(apply == null){
            throw new RuntimeException("申请不存在");
        }
        if(apply.getApplyStatus() != 0){
            throw new RuntimeException("申请已处理");
        }
        apply.setApplyStatus(2);  // 2-已拒绝
        apply.setAuditRemark(reason);
        apply.setAuditTime(LocalDateTime.now());
        apply.setAuditorId(auditorId);
        int result = registerApplyMapper.updateById(apply);
        if(result <= 0){
            throw new RuntimeException("拒绝申请失败");
        }
        // 模拟发送短信通知
        log.info("========== 审核不通过通知 ==========");
        log.info("手机号：{}", apply.getPhone());
        log.info("不通过原因：{}", reason);
        log.info("=================================");

    }


}
