package com.SCAUteam11.GYJZ.service.impl;

import com.SCAUteam11.GYJZ.DTO.Donation.DonationRecordDTO;
import com.SCAUteam11.GYJZ.DTO.Donation.DonorStatisticResponse;
import com.SCAUteam11.GYJZ.entity.mysql.*;
import com.SCAUteam11.GYJZ.mapper.mysql.DonationMapper;
import com.SCAUteam11.GYJZ.mapper.mysql.ProjectMapper;
import com.SCAUteam11.GYJZ.mapper.mysql.SubscriptionMapper;
import com.SCAUteam11.GYJZ.mapper.mysql.UserMapper;
import com.SCAUteam11.GYJZ.service.IDonationService;
import com.SCAUteam11.GYJZ.utils.HuaweiPushUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DonationServiceImpl extends ServiceImpl<DonationMapper, Donation> implements IDonationService {
    @Autowired
    private DonationMapper donationMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private SubscriptionMapper subscriptionMapper;
    @Autowired
    private HuaweiPushUtil huaweiPushUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PushRecordService pushRecordService; // 注入推送记录服务

    @Override
    public boolean addDonation(Donation donation) {
        donationMapper.insert(donation);
        return true;
    }

    @Override
    public boolean deleteDonation(Long id) {
        Donation existingDonation = donationMapper.selectById(id);
        if (existingDonation == null){
            throw new RuntimeException("捐赠记录不存在");
        }
        donationMapper.deleteById(id);
        return true;
    }

    @Override
    public boolean updateDonation(Donation donation) {
        Donation existingDonation = donationMapper.selectById(donation.getId());
        if (existingDonation == null){
            throw new RuntimeException("捐赠记录不存在");
        }
        donationMapper.updateById(donation);
        return true;
    }

    @Override
    public Donation getDonationById(Long id) {
        if(id==null || id <= 0){
            throw new RuntimeException("项目ID不能为空");
        }
        return donationMapper.selectById(id);
    }

    @Override
    public Page<Donation> getDonationList(int page, int size, Long projectId, Long orgId, String donorName, Double minAmount, Double maxAmount, LocalDate startDate, LocalDate endDate) {
        // 创建分页对象
        Page<Donation> pageInfo = new Page<>(page, size);
        LambdaQueryWrapper<Donation> queryWrapper = new LambdaQueryWrapper<>();

        // ========== 1. 机构隔离 (取交集：查出该机构的所有项目ID) ==========
        if (orgId != null) {
            LambdaQueryWrapper<Project> pWrapper = new LambdaQueryWrapper<>();
            pWrapper.eq(Project::getOrgId, orgId);
            List<Project> orgProjects = projectMapper.selectList(pWrapper);

            // 如果该机构一个项目都没有，说明不可能有捐赠记录，直接返回空分页
            if (orgProjects.isEmpty()) {
                return pageInfo;
            }

            // 提取出项目ID列表，放入 IN 查询条件中
            List<Long> orgProjectIds = orgProjects.stream().map(Project::getId).collect(Collectors.toList());
            queryWrapper.in(Donation::getProjectId, orgProjectIds);
        }

        // ========== 2. 组合查询逻辑 ==========
        queryWrapper
                .eq(projectId != null, Donation::getProjectId, projectId)
                .like(com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(donorName), Donation::getDonorName, donorName);

        // 金额范围查询
        if (minAmount != null && maxAmount != null) {
            queryWrapper.between(Donation::getAmount, minAmount, maxAmount);
        } else if (minAmount != null) {
            queryWrapper.ge(Donation::getAmount, minAmount);
        } else if (maxAmount != null) {
            queryWrapper.le(Donation::getAmount, maxAmount);
        }

        // 时间范围查询
        if (startDate != null && endDate != null) {
            LocalDateTime queryStart = startDate.atStartOfDay();
            LocalDateTime queryEnd = endDate.atTime(23, 59, 59);
            queryWrapper.ge(Donation::getDonationTime, queryStart)
                    .le(Donation::getDonationTime, queryEnd);
        } else if (startDate != null) {
            LocalDateTime exactStart = startDate.atStartOfDay();
            LocalDateTime exactEnd = startDate.atTime(23, 59, 59);
            queryWrapper.between(Donation::getDonationTime, exactStart, exactEnd);
        } else if (endDate != null) {
            LocalDateTime exactStart = endDate.atStartOfDay();
            LocalDateTime exactEnd = endDate.atTime(23, 59, 59);
            queryWrapper.between(Donation::getDonationTime, exactStart, exactEnd);
        }

        // 默认按时间倒序
        queryWrapper.orderByDesc(Donation::getDonationTime);

        // ========== 3. 执行核心查询 ==========
        Page<Donation> result = donationMapper.selectPage(pageInfo, queryWrapper);

        // ========== 4. 数据回显 (给记录加上项目名称) ==========
        List<Donation> records = result.getRecords();
        if (records != null && !records.isEmpty()) {
            // 提取当前页所有的 projectId 并去重
            List<Long> pIds = records.stream()
                    .map(Donation::getProjectId)
                    .distinct()
                    .collect(Collectors.toList());

            if (!pIds.isEmpty()) {
                // 批量查询项目信息
                List<Project> projects = projectMapper.selectBatchIds(pIds);
                // 转为 Map <项目ID, 项目标题> 方便后续匹配
                Map<Long, String> titleMap = projects.stream()
                        .collect(Collectors.toMap(Project::getId, Project::getTitle));

                // 把项目名称塞回给每一条捐赠记录
                for (Donation d : records) {
                    d.setProjectTitle(titleMap.get(d.getProjectId()));
                }
            }
        }

        return result;
    }

    @Override
    public DonorStatisticResponse getDonorStatistic(Long userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        return donationMapper.getDonorStatistic(userId);
    }

    @Override
    public Page<DonationRecordDTO> getMyDonationList(String phone, int page, int size, Double minAmount, Double maxAmount, LocalDate start, LocalDate end) {
        Page<Donation> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Donation> wrapper = new LambdaQueryWrapper<>();


        wrapper.eq(Donation::getDonorPhone, phone);
        if (minAmount != null) wrapper.ge(Donation::getAmount, minAmount);
        if (maxAmount != null) wrapper.le(Donation::getAmount, maxAmount);
        if (start != null) wrapper.ge(Donation::getDonationTime, start.atStartOfDay());
        if (end != null) wrapper.le(Donation::getDonationTime, end.atTime(23, 59, 59));
        wrapper.eq(Donation::getDonationStatus, 1);
        wrapper.orderByDesc(Donation::getDonationTime);

        // 1. 先查出原始的 Page<Donation>
        Page<Donation> donationPage = this.page(pageObj, wrapper);

        // 2. 创建一个新的 Page<DonationRecordDTO> 用于返回
        Page<DonationRecordDTO> dtoPage = new Page<>(donationPage.getCurrent(), donationPage.getSize(), donationPage.getTotal());

        // 3. 将 Donation 列表转换为 DonationRecordDTO 列表，并查出 projectName
        List<DonationRecordDTO> dtoList = donationPage.getRecords().stream().map(donation -> {
            DonationRecordDTO dto = new DonationRecordDTO();
            // 复制同名字段 (id, amount, donationTime 等)
            BeanUtils.copyProperties(donation, dto);

            // 查询并设置项目名称
            if (donation.getProjectId() != null) {

                 Project project = projectMapper.selectById(donation.getProjectId());
                 if (project != null) {
                     dto.setProjectName(project.getTitle());
                 } else {
                     dto.setProjectName("未知项目");
                 }


            }
            return dto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createDonation(Donation donation) {
        // 1. 基础信息校验
        if (donation.getProjectId() == null || donation.getAmount() == null || donation.getDonorPhone() == null) {
            throw new RuntimeException("捐赠信息不完整，缺少项目ID、金额或手机号");
        }

        // 2. 检查项目状态，确保合法性
        Project project = projectMapper.selectById(donation.getProjectId());
        if (project == null) {
            throw new RuntimeException("捐赠项目不存在");
        }
        if (project.getProjectStatus() != 1) { // 1-进行中
            throw new RuntimeException("该项目已结束募捐，无法继续捐赠");
        }

        // 3. 补全后端负责生成的属性
        // 将凭证号总长度控制在20个字符以内。GY(2位) + 时间戳(13位) + 随机字符(4位) = 19位
        String certNo = "GY" + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        donation.setCertificateNo(certNo);

        donation.setDonationTime(java.time.LocalDateTime.now());
        donation.setCreateTime(java.time.LocalDateTime.now());

        // 确保状态正确
        if (donation.getDonationStatus() == null) {
            donation.setDonationStatus(1); // 1-成功
        }
        if (donation.getSource() == null) {
            donation.setSource(1); // 1-线上
        }
        if (donation.getPayment() == null || donation.getPayment().isEmpty()) {
            donation.setPayment("模拟支付");
        }

        // 4. 执行 SQL 一：插入捐赠记录表
        int insertResult = baseMapper.insert(donation);
        if (insertResult <= 0) {
            throw new RuntimeException("生成捐赠记录失败");
        }

        // 5. 执行 SQL 二：更新项目的当前筹款金额
        java.math.BigDecimal newAmount = project.getCurrentAmount().add(donation.getAmount());
        project.setCurrentAmount(newAmount);

        // 新增一个标记，用于记录本次捐款是否刚好让项目达标
        boolean isTargetReached = false;

        // 检查是否筹款满额
        if (newAmount.compareTo(project.getTargetAmount()) >= 0) {
            System.out.println(">>> 项目 [" + project.getTitle() + "] 筹款已达标，自动切换为结束状态");
            project.setProjectStatus(2); // 2-已结束
            isTargetReached = true;      // 标记达标
        }

        int updateResult = projectMapper.updateById(project);
        if (updateResult <= 0) {
            // 如果更新项目金额失败，抛出异常，Spring 会自动回滚上面插入的捐赠记录！
            throw new RuntimeException("更新项目筹款金额失败，事务将回滚");
        }

        // 6. 核心触发器：只有当项目金额成功更新到数据库，且刚好达标时，才发送推送！
        // 这样可以彻底避免“事务回滚但推送却发出去了”的幽灵 Bug
        if (isTargetReached) {
            sendTargetReachedPush(project);
        }

        return true;
    }

    /**
     * 辅助方法：去数据库捞出订阅者并发起推送
     */
    /**
     * 辅助方法：去数据库捞出订阅者并发起推送，同时记录到数据库
     */
    private void sendTargetReachedPush(Project project) {
        System.out.println("项目《" + project.getTitle() + "》已达标，准备给订阅者发送推送！");

        // 1. 去 subscription 表查出所有订阅了这个项目的人
        List<Subscription> subs = subscriptionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Subscription>()
                        .eq(Subscription::getProjectId, project.getId())
        );

        if (subs.isEmpty()) {
            System.out.println("该项目没有人订阅，取消推送。");
            return;
        }

        // 2. 提取出这批用户的 userId 集合
        List<Long> userIds = subs.stream().map(Subscription::getUserId).collect(java.util.stream.Collectors.toList());

        // 3. 去 user 表，把这些人的 pushToken 查出来
        List<User> users = userMapper.selectBatchIds(userIds);
        List<String> pushTokens = users.stream()
                .map(User::getPushToken)
                // 过滤掉那些没绑定鸿蒙设备（token为空）的用户
                .filter(token -> token != null && !token.trim().isEmpty())
                .collect(java.util.stream.Collectors.toList());

        String title = "感谢有您，项目圆满达标！";
        String body = "您一直牵挂的公益项目《" + project.getTitle() + "》已成功筹集全部善款，爱心微风已经送达！";

        // 4. 发送系统底层推送 (横幅通知)
        if (!pushTokens.isEmpty()) {
            huaweiPushUtil.sendPushMessage(pushTokens, title, body);
        } else {
            System.out.println("订阅者都没有绑定鸿蒙设备 Token，跳过系统推送，但仍生成站内信记录。");
        }

        // 5. 新增：将消息记录批量写入 push_record 表，供前端“消息中心”拉取！
        List<PushRecord> pushRecords = new java.util.ArrayList<>();
        for (Long userId : userIds) {
            PushRecord record = new PushRecord();
            record.setUserId(userId);
            record.setProjectId(project.getId());
            record.setTitle(title);
            record.setContent(body);
            record.setType(3); // 3 代表“捐赠成功/项目达标”
            record.setSendTime(java.time.LocalDateTime.now());
            record.setSendStatus(1); // 1 代表发送成功
            pushRecords.add(record);
        }

        // 使用 MyBatis-Plus 的 saveBatch 批量插入数据库
        if (!pushRecords.isEmpty()) {
            pushRecordService.saveBatch(pushRecords);
            System.out.println("✅ 已成功将 " + pushRecords.size() + " 条消息记录写入 push_record 表！");
        }
    }

    @Override
    public List<Map<String, Object>> getDonationTrend(Long orgId, int days) {
        // 计算起始日期：比如往前推 7 天
        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        LocalDateTime startDateTime = startDate.atStartOfDay();

        LambdaQueryWrapper<Donation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(Donation::getDonationTime, startDateTime);
        // 只统计已支付成功的捐赠
        queryWrapper.eq(Donation::getDonationStatus, 1);

        // 机构隔离逻辑：取出该机构所有项目ID
        if (orgId != null) {
            LambdaQueryWrapper<Project> pWrapper = new LambdaQueryWrapper<>();
            pWrapper.eq(Project::getOrgId, orgId);
            List<Project> orgProjects = projectMapper.selectList(pWrapper);

            // 如果该机构一个项目都没建，直接返回连续7天金额为0的数据
            if (orgProjects.isEmpty()) {
                return generateEmptyTrend(startDate, days);
            }
            List<Long> pIds = orgProjects.stream().map(Project::getId).collect(Collectors.toList());
            queryWrapper.in(Donation::getProjectId, pIds);
        }

        // 查询近N天的所有捐赠记录
        List<Donation> donations = donationMapper.selectList(queryWrapper);

        // 核心：按照日期分组，并将每天的金额进行累加求和
        Map<LocalDate, BigDecimal> dailySum = donations.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getDonationTime().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Donation::getAmount, BigDecimal::add)
                ));

        // 构建返回结果（循环 N 天，确保即使某一天没有数据，也能填充 0，保证折线图不断连）
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            Map<String, Object> map = new HashMap<>();
            map.put("date", date.toString()); // 返回格式如: "2026-03-22"
            map.put("amount", dailySum.getOrDefault(date, BigDecimal.ZERO));
            result.add(map);
        }
        return result;
    }

    // 辅助方法：生成金额全为0的空趋势数据
    private List<Map<String, Object>> generateEmptyTrend(LocalDate startDate, int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", startDate.plusDays(i).toString());
            map.put("amount", BigDecimal.ZERO);
            result.add(map);
        }
        return result;
    }

}
