package com.SCAUteam11.GYJZ.mapper.mysql;

import com.SCAUteam11.GYJZ.DTO.Donation.DonorStatisticResponse;
import com.SCAUteam11.GYJZ.entity.mysql.Donation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

public interface DonationMapper extends BaseMapper<Donation> {
    Long selectDonationsCountByProjectId(Long id); // 根据捐赠项目id查询捐赠人数
    Double selectMaxDonationByProjectId(Long id); // 根据捐赠项目id查询最大捐赠金额
    Double selectMinDonationByProjectId(Long id); // 根据捐赠项目id查询最小捐赠金额
    Long selectDonationCountByProjectId(Long id);
    DonorStatisticResponse getDonorStatistic(Long id); //去donation表查找该捐赠人id的相关数据

}
