package com.paiyucun.service;

import com.paiyucun.dto.DiscoverySaveDTO;
import com.paiyucun.vo.DiscoveryVO;

import java.util.List;

/**
 * 发现记录服务
 */
public interface DiscoveryService {

    /** 保存发现 */
    void save(DiscoverySaveDTO dto);

    /** 查询历史（时间倒序） */
    List<DiscoveryVO> list();
}
