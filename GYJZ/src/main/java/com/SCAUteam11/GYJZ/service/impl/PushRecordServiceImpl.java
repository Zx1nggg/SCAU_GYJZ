package com.SCAUteam11.GYJZ.service.impl;

import com.SCAUteam11.GYJZ.entity.mysql.PushRecord;
import com.SCAUteam11.GYJZ.mapper.mysql.PushRecordMapper;
import com.SCAUteam11.GYJZ.service.IPushRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PushRecordServiceImpl extends ServiceImpl<PushRecordMapper, PushRecord> implements IPushRecordService {
}
