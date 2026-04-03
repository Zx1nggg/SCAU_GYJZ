package com.SCAUteam11.GYJZ.service;

import com.SCAUteam11.GYJZ.entity.mysql.RegisterApply;
import com.SCAUteam11.GYJZ.entity.mysql.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IUserService extends IService<User> {
    User donorLogin(User user); // 捐赠人使用手机号登录
    User adminLogin(User user); // 管理员使用机构账号登录
    boolean donorRegister(User user); // 捐赠人注册方法
    void submitAdminApply(RegisterApply apply); // 公益机构管理员提交注册申请
    void approveAdminApply(Long applyId, Long auditorId); //超级管理员批准注册申请
    void rejectAdminApply(Long applyId, String reason,Long auditorId); //超级管理员拒绝注册申请

}
