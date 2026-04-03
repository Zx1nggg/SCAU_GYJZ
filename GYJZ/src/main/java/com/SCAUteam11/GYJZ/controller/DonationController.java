package com.SCAUteam11.GYJZ.controller;

import com.SCAUteam11.GYJZ.DTO.Donation.DonationRecordDTO;
import com.SCAUteam11.GYJZ.DTO.Donation.DonorStatisticResponse;
import com.SCAUteam11.GYJZ.entity.Result;
import com.SCAUteam11.GYJZ.entity.mysql.Donation;
import com.SCAUteam11.GYJZ.entity.mysql.Project;
import com.SCAUteam11.GYJZ.service.IDonationService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/donations")
public class DonationController {
    @Autowired
    private IDonationService donationService;

    @GetMapping
    public Result getDonationList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String donorName,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
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
        Page<Donation> result = donationService.getDonationList(page, size, projectId,orgId, donorName, minAmount, maxAmount,start, end);
        // 返回数据
        return Result.success(result);
    }



    @PostMapping
    public Result addDonation(@RequestBody Donation donation) {
        boolean success = donationService.addDonation(donation);
        if (success) {
            return Result.success("添加成功");
        } else {
            return Result.fail("添加失败");
        }
    }

    @PutMapping
    public Result updateDonation(@RequestBody Donation donation) {
        boolean success = donationService.updateDonation(donation);
        if (success) {
            return Result.success("更新成功");
        } else {
            return Result.fail("更新失败");
        }

    }

    @DeleteMapping("/{id}")
    public Result deleteDonation(@PathVariable Long id) {
        boolean success = donationService.deleteDonation(id);
        if (success) {
            return Result.success("删除成功");
        } else {
            return Result.fail("删除失败");
        }
    }

    @GetMapping("/{id}")
    public Result getDonationById(@PathVariable Long id) {
        Donation donation = donationService.getDonationById(id);
        return Result.success(donation);
    }

    @GetMapping("/my")
    public Result getMyDonationList(
            @RequestParam String phone,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Double minAmount,
            @RequestParam(required = false) Double maxAmount,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate start = null;
        LocalDate end = null;

        try {
            if (StringUtils.isNotBlank(startDate)) {
                start = LocalDate.parse(startDate);
            }
            if (StringUtils.isNotBlank(endDate)) {
                end = LocalDate.parse(endDate);
            }
        } catch (Exception e) {
            throw new RuntimeException("日期格式错误");
        }

        Page<DonationRecordDTO> result = donationService.getMyDonationList(
                phone, page, size, minAmount, maxAmount, start, end
        );

        return Result.success(result);
    }

    @GetMapping("/statistic/{userId}")
    public Result getDonorStatistic(@PathVariable Long userId) {
        DonorStatisticResponse stats = donationService.getDonorStatistic(userId);
        return Result.success(stats);
    }

    @PostMapping("/create")
    public  Result createDonation(@RequestBody Donation donation){
        try {
            boolean success = donationService.createDonation(donation);
            if (success) {
                return Result.success("捐赠成功");
            } else {
                return Result.fail("捐赠失败，请稍后重试");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("捐赠异常：" + e.getMessage());
        }
    }
    /**
     * 获取图表动态趋势数据 (近N天捐赠金额)
     * 支持管理员机构隔离
     */
    @GetMapping("/donations-trend")
    public Result getDonationTrend(
            @RequestParam(required = false) Long orgId,
            @RequestParam(defaultValue = "7") int days) {

        List<Map<String, Object>> trendData = donationService.getDonationTrend(orgId, days);
        return Result.success(trendData);
    }

}
