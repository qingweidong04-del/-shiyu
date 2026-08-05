package com.paiyucun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paiyucun.dto.DiscoverySaveDTO;
import com.paiyucun.entity.Discovery;
import com.paiyucun.mapper.DiscoveryMapper;
import com.paiyucun.service.DiscoveryService;
import com.paiyucun.vo.DiscoveryVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscoveryServiceImpl implements DiscoveryService {

    private final DiscoveryMapper discoveryMapper;

    public DiscoveryServiceImpl(DiscoveryMapper discoveryMapper) {
        this.discoveryMapper = discoveryMapper;
    }

    @Override
    public void save(DiscoverySaveDTO dto) {
        Discovery entity = new Discovery();
        entity.setImageUrl(dto.getImageUrl());
        entity.setObjectName(dto.getObjectName());
        entity.setPoemId(dto.getPoemId());
        entity.setPoemLine(dto.getPoemLine());
        entity.setPoemSource(dto.getPoemSource());
        discoveryMapper.insert(entity);
    }

    @Override
    public List<DiscoveryVO> list() {
        List<Discovery> list = discoveryMapper.selectList(
                new LambdaQueryWrapper<Discovery>()
                        .orderByDesc(Discovery::getCreateTime)
        );

        return list.stream().map(e -> {
            DiscoveryVO vo = new DiscoveryVO();
            vo.setId(e.getId());
            vo.setImageUrl(e.getImageUrl());
            vo.setObjectName(e.getObjectName());
            vo.setPoemLine(e.getPoemLine());
            vo.setPoemSource(e.getPoemSource());
            vo.setCreateTime(e.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }
}
