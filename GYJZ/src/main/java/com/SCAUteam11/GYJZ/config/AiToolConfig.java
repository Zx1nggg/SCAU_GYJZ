package com.SCAUteam11.GYJZ.config;

import com.SCAUteam11.GYJZ.entity.mysql.Donation;
import com.SCAUteam11.GYJZ.entity.mysql.Project;
import com.SCAUteam11.GYJZ.entity.mysql.User;
import com.SCAUteam11.GYJZ.mapper.mysql.DonationMapper;
import com.SCAUteam11.GYJZ.mapper.mysql.ProjectMapper;
import com.SCAUteam11.GYJZ.mapper.mysql.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.List;
import java.util.function.Function;

@Configuration
public class AiToolConfig {

    // ==========================================
    // 工具1：根据手机号查询用户信息
    // ==========================================
    public record UserQueryRequest(String phone) {}

    @Bean
    @Description("当用户要求查询自己的账号状态、个人资料时调用此工具。注意：必须先在聊天中温柔地询问用户的注册手机号！")
    public Function<UserQueryRequest, String> queryUserInfo(UserMapper userMapper) {
        return request -> {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, request.phone());
            User user = userMapper.selectOne(wrapper);

            if (user == null) {
                return "数据库未找到该手机号，请告诉用户可能是号码记错了，或者还没有注册哦。";
            }
            String role = user.getRole() == 1 ? "爱心捐赠者" : (user.getRole() == 2 ? "机构管理员" : "超级管理员");
            String status = user.getUserStatus() == 1 ? "状态健康正常" : "账号已被封禁";

            return String.format("查询成功！用户昵称：【%s】，平台身份：【%s】，当前账号状态：【%s】。请用观铃的语气把结果告诉用户。",
                    user.getNickname(), role, status);
        };
    }

    // ==========================================
    // 工具2：查询具体的公益项目进度 (真实连库版)
    // ==========================================
    public record ProjectQueryRequest(String projectName) {}

    @Bean
    @Description("当用户询问某个具体的公益项目进度、详情或筹款情况时调用此工具。入参是用户提到的项目名称关键字。")
    public Function<ProjectQueryRequest, String> queryProjectInfo(ProjectMapper projectMapper) {
        return request -> {
            System.out.println("🦕 观铃正在努力查询项目：" + request.projectName());

            LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
            // 模糊匹配项目名称，只取最符合的一条
            wrapper.like(Project::getTitle, request.projectName()).last("LIMIT 1");
            Project project = projectMapper.selectOne(wrapper);

            if (project == null) {
                return "没有找到名字里包含“" + request.projectName() + "”的公益项目，请让用户检查一下名字对不对。";
            }

            String status = project.getProjectStatus() == 1 ? "正在火热募集中" : "已经圆满结束啦";

            return String.format("查到啦！项目【%s】(%s类)，目前状态：【%s】。目标筹集 %s 元，当前已经筹集了 %s 元。简单介绍：%s。请用观铃充满夏日活力的语气告诉用户！",
                    project.getTitle(), project.getCategory(), status, project.getTargetAmount(), project.getCurrentAmount(), project.getContent());
        };
    }

    // ==========================================
    // 工具3：查询用户的捐赠记录 (全新添加)
    // ==========================================
    public record DonationQueryRequest(String phone) {}

    @Bean
    @Description("当用户询问'我捐了多少钱'、'帮我查我的捐赠记录'、'我的证书'时调用此工具。必须先知道用户的注册手机号才能查！")
    public Function<DonationQueryRequest, String> queryMyDonations(DonationMapper donationMapper, ProjectMapper projectMapper) {
        return request -> {
            System.out.println("🦕 观铃正在努力翻找捐赠账本，手机号：" + request.phone());

            LambdaQueryWrapper<Donation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Donation::getDonorPhone, request.phone())
                    .eq(Donation::getDonationStatus, 1) // 只查支付成功的
                    .orderByDesc(Donation::getDonationTime)
                    .last("LIMIT 3"); // 最多返回最近3条，防止上下文撑爆大模型

            List<Donation> donations = donationMapper.selectList(wrapper);

            if (donations == null || donations.isEmpty()) {
                return "查过了，这个手机号还没有任何捐赠记录哦，鼓励ta像勇敢的恐龙一样迈出第一步吧！";
            }

            StringBuilder sb = new StringBuilder("查到最近的爱心记录啦：\n");
            for (Donation d : donations) {
                Project p = projectMapper.selectById(d.getProjectId());
                String pName = (p != null) ? p.getTitle() : "未知神秘项目";
                sb.append(String.format("- 在 %s 为【%s】捐赠了 %s 元。\n",
                        d.getDonationTime().toLocalDate().toString(), pName, d.getAmount()));
            }
            sb.append("请用观铃那温柔又有点笨拙的语气，好好夸奖并感谢用户的善举（记得加上嘎哦）！");

            return sb.toString();
        };
    }
}