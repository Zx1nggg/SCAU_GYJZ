package com.SCAUteam11.GYJZ.UserManage;

import com.SCAUteam11.GYJZ.entity.mysql.RegisterApply;
import com.SCAUteam11.GYJZ.entity.mysql.User;
import com.SCAUteam11.GYJZ.mapper.mysql.UserMapper;
import com.SCAUteam11.GYJZ.service.IUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class UserManageTest {
    @Autowired
    private IUserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;// 密码加密器
    @Test
    public void donorLoginTest(){
        //测试捐赠人登录
        User donorUser = new User();
        donorUser.setPhone("13912340001");
        donorUser.setPassword("123");
        User result = userService.donorLogin(donorUser);
        System.out.println("捐赠人登录成功: " + result);

        // 断言验证
        assertNotNull(result);
        assertEquals("13912340001", result.getPhone());
        assertEquals("爱心人士张三", result.getNickname());
    }
    @Test
    public void adminLoginTest(){
        //测试管理员登录
        User adminUser = new User();
        adminUser.setUsername("hope_2344");
        adminUser.setPassword("123");
        User result = userService.adminLogin(adminUser);
        System.out.println("管理员登录成功: " + result);

        // 断言验证
        assertNotNull(result);
        assertEquals("hope_2344", result.getUsername());
        assertEquals("希望公益基金会", result.getNickname());
    }
    @Test
    public void sadminLoginTest(){
        //测试超级管理员登录
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword("123");
        User result = userService.adminLogin(adminUser);
        System.out.println("超级管理员登录成功: " + result);

        // 断言验证
        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("超级管理员", result.getNickname());
    }
    @Test
    public void submitAdminApplyTest(){
        // 创建注册申请对象
        RegisterApply apply = new RegisterApply();
        apply.setPhone("13912340003");              // 申请人手机号
        apply.setPassword("123456");                 // 密码
        apply.setInstitutionName("阳光公益基金会");   // 机构名称
        apply.setCreditCode("91440000123456789X");   // 统一社会信用代码（18位）
        apply.setContactPerson("王五");               // 联系人
        apply.setContactPhone("020-12345678");       // 机构联系电话（可选）
        apply.setQualification("/upload/qualification/sunshine.pdf");  // 资质文件URL
        apply.setApplyReason("希望为贫困山区儿童提供教育支持");  // 申请说明（可选）

        // 调用注册申请方法
        userService.submitAdminApply(apply);

        System.out.println("管理员注册申请提交成功: " + apply);

        // 断言验证
        assertNotNull(apply.getId());  // 提交后应该自动生成ID
        assertNotNull(apply.getCreateTime());  // 创建时间应该自动生成
        assertEquals(0, apply.getApplyStatus());  // 状态应为0（待审核）

    }
    @Test
    public void approveAdminApplyTest(){
        // 假设申请ID（根据实际数据库中的ID修改）
        Long applyId = 6L;
        // 超级管理员ID（假设为1）
        Long auditorId = 1L;

        // 调用批准审核方法
        userService.approveAdminApply(applyId, auditorId);

        System.out.println("管理员注册申请审核通过，申请ID: " + applyId);
    }
    @Test
    public void rejectAdminApplyTest() {
        // 假设申请ID（根据实际数据库中的ID修改）
        Long applyId = 2L;
        // 超级管理员ID（假设为1）
        Long auditorId = 1L;
        // 不通过原因
        String reason = "资质文件不清晰，请重新上传";

        // 调用拒绝审核方法
        userService.rejectAdminApply(applyId, reason, auditorId);

        System.out.println("管理员注册申请审核不通过，申请ID: " + applyId);
        System.out.println("不通过原因: " + reason);

        // 验证：可以通过查询数据库确认申请状态变为2（不通过）
    }
    @Test
    public void submitNewPassword(){
        Long userId= 1L;
        String newPassword = "qb8888";
        User user = new User();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }
}

