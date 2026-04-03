package com.SCAUteam11.GYJZ.service;

import com.SCAUteam11.GYJZ.DTO.Donation.DonationRecordDTO;
import com.SCAUteam11.GYJZ.DTO.Donation.DonorStatisticResponse;
import com.SCAUteam11.GYJZ.entity.mysql.Donation;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IDonationService extends IService<Donation> {
   boolean addDonation(Donation donation);
   boolean deleteDonation(Long id);
   boolean updateDonation(Donation donation);
   Donation getDonationById(Long id);
   Page<Donation> getDonationList(int page, int size, Long projectId,Long orgId, String donorName, Double minAmount, Double maxAmount, LocalDate startDate, LocalDate endDate);
   DonorStatisticResponse getDonorStatistic(Long userId);
   Page<DonationRecordDTO> getMyDonationList(String phone, int page, int size, Double minAmount, Double maxAmount, LocalDate start, LocalDate end);
   boolean createDonation(Donation donation);
   // 获取最近N天的捐赠趋势
   List<Map<String, Object>> getDonationTrend(Long orgId, int days);
}
