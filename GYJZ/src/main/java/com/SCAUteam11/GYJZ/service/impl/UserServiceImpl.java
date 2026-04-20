package com.SCAUteam11.GYJZ.service.impl;

import com.SCAUteam11.GYJZ.DTO.User.UserUpdateRequest;
import com.SCAUteam11.GYJZ.DTO.User.UserUpdateResponse;
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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (ObjectUtils.isEmpty(user.getPhone()) || !StringUtils.hasLength(user.getPassword())) {
            throw new RuntimeException("手机号或密码不能为空");
        }

        // 使用手机号作为 Redis 的 Key
        String redisKey = String.valueOf(user.getPhone());

        // 1. 尝试从Redis获取
        Object o = redisUtil.get(redisKey);
        User targetUser = null;

        if (o == null) {
            // 2. 缓存没有，查数据库
            targetUser = baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getPhone, user.getPhone()));
            if (targetUser == null) {
                throw new RuntimeException("用户名或密码错误");
            }
        } else {
            targetUser = (User) o;
        }

        // 3. 统一校验密码（修复了原先缓存为空时不校验密码的安全漏洞）
        if (passwordEncoder.matches(user.getPassword(), targetUser.getPassword())) {
            // 放在密码校验通过之后，保证空指针被修复，检验用户状态
            if (targetUser.getUserStatus() != null && targetUser.getUserStatus() == 2) {
                throw new RuntimeException("账号因为违反相关约定已被封禁，请联系管理员解封");
            }
            // 4. 如果密码正确，且数据是从数据库拿出来的，就存入 Redis
            if (o == null) {
                // 使用三个参数的 set 方法：设置 1800 秒（30分钟）的过期时间
                redisUtil.set(redisKey, targetUser, 1800);
            }
            return targetUser;
        } else {
            throw new RuntimeException("用户名或密码错误");
        }
    }

    @Override
    public User adminLogin(User user){
        if (!StringUtils.hasLength(user.getUsername()) || !StringUtils.hasLength(user.getPassword())) {
            throw new RuntimeException("用户或密码不能为空");
        }

        // 使用用户名作为 Redis 的 Key
        String redisKey = String.valueOf(user.getUsername());
        Object o = redisUtil.get(redisKey);
        User targetUser = null;

        if (o == null) {
            targetUser = baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
            if (targetUser == null) {
                throw new RuntimeException("用户名或密码错误");
            }
        } else {
            targetUser = (User) o;
        }

        // 统一校验密码
        if (passwordEncoder.matches(user.getPassword(), targetUser.getPassword())) {
            // 放在密码校验通过之后，保证空指针被修复，检验用户状态
            if (targetUser.getUserStatus() != null && targetUser.getUserStatus() == 2) {
                throw new RuntimeException("账号因为违反相关约定已被封禁，请联系管理员解封");
            }
            // 密码正确且未缓存，则存入缓存，过期时间 1800 秒
            if (o == null) {
                redisUtil.set(redisKey, targetUser, 1800);
            }
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
        if (!StringUtils.hasLength(user.getVerifyCode())) {
            throw new RuntimeException("验证码不能为空");
        }

        // 拼接当时存入 Redis 的 Key
        String redisKey = "verify:code:" + user.getPhone();
        // 取出缓存中的验证码
        String storedCode = (String) redisUtil.get(redisKey);

        if (storedCode == null) {
            throw new RuntimeException("验证码已过期或未发送，请重新获取");
        }
        if (!storedCode.equals(user.getVerifyCode())) {
            throw new RuntimeException("验证码不正确");
        }

        // 校验成功！立刻从 Redis 删除该验证码，防止被恶意二次利用（重放攻击）
        redisUtil.del(redisKey);

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
        org.setCode(apply.getCreditCode());
        org.setContactPerson(apply.getContactPerson());
        org.setContactPhone(apply.getContactPhone() != null ? apply.getContactPhone() : apply.getPhone());
        org.setContent(apply.getApplyReason());
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
        // TODO: 由于generateAccount可能生成同名的账户，已新增generateUniteAccount方法，使用此方法需实现接口AccountValidator，这里先抛异常处理，灾害等级-中
        long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count > 0) {
            throw new RuntimeException("生成的账号已存在，请联系管理员处理");
        }

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

    /**
     * 更新用户资料，并处理双表联动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserUpdateResponse updateUser(Long userId, UserUpdateRequest request) {
        // 1. 获取数据库当前用户信息
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }

        // 2. 更新 User 表基础字段 (捐赠人和管理员共有的信息)
        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());

//        user.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(user);

        // 3. 【核心判定】如果是机构管理员 (Role: 2)，需要同步更新 organization 表的联系人信息
        if (user.getRole() != null && user.getRole() == 2 && user.getOrgId() != null) {
            Organization org = organizationMapper.selectById(user.getOrgId());
            if (org != null) {
                // 将管理员修改后的 "昵称/真实姓名" 映射为机构的 "联系人"
                // TODO: 混淆了NickName和ContactPerson的联系，后面看需要修改，灾害等级-低
                if (request.getNickname() != null) {
                    org.setContactPerson(request.getNickname());
                }
                // 将管理员修改后的 "手机号" 映射为机构的 "联系电话"
                if (request.getPhone() != null) {
                    org.setContactPhone(request.getPhone());
                }

                org.setUpdateTime(LocalDateTime.now());
                organizationMapper.updateById(org);
            }
        }
        // 更新完删除旧的redis缓存
        if (StringUtils.hasLength(user.getPhone())) {
            redisUtil.del(String.valueOf(user.getPhone()));
        }
        if (StringUtils.hasLength(user.getUsername())) {
            redisUtil.del(String.valueOf(user.getUsername()));
        }

        // 4. 构造并返回结果对象
        UserUpdateResponse response = new UserUpdateResponse();
        response.setSuccess(true);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        // 1. 查询用户是否存在
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 校验旧密码
        // passwordEncoder.matches(明文, 密文) 是专门用来校验加盐密码的
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误，请重新输入");
        }

        // 防呆设计：新旧密码不能一样
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("新密码不能与原密码相同");
        }

        // 3. 加密新密码并设置到实体中
        user.setPassword(passwordEncoder.encode(newPassword));

        // 4. 更新到数据库
        int result = baseMapper.updateById(user);
        if (result <= 0) {
            throw new RuntimeException("密码更新失败，请重试");
        }
        if (user.getPhone() != null) {
            redisUtil.del(String.valueOf(user.getPhone()));
        }
        if (user.getUsername() != null) {
            redisUtil.del(user.getUsername());
        }
        // 又两种情况了
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId) {
        // 1. 查询用户是否存在
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在或已被删除");
        }

        // 2. 加密默认密码
        String defaultPassword = "GY1234";
        user.setPassword(passwordEncoder.encode(defaultPassword));

        // 3. 更新到数据库
        int result = baseMapper.updateById(user);
        if (result <= 0) {
            throw new RuntimeException("重置密码失败，请重试");
        }

        // 清理 Redis 缓存
        // 无论他是捐赠人（手机号作为Key）还是管理员（Username作为Key），进行无差别扫荡
        if (user.getPhone() != null) {
            redisUtil.del(String.valueOf(user.getPhone()));
        }
        if (user.getUsername() != null) {
            redisUtil.del(user.getUsername());
        }

    }





}
