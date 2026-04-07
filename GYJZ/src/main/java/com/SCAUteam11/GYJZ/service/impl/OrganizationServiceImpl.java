package com.SCAUteam11.GYJZ.service.impl;

import com.SCAUteam11.GYJZ.entity.mysql.Organization;
import com.SCAUteam11.GYJZ.mapper.mysql.OrganizationMapper;
import com.SCAUteam11.GYJZ.service.IOrganizationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements IOrganizationService {
}
