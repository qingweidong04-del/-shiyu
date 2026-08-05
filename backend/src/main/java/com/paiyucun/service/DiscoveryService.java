package com.paiyucun.service;

import com.paiyucun.dto.DiscoverySaveDTO;
import com.paiyucun.vo.DiscoveryCreateVO;
import com.paiyucun.vo.DiscoveryVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DiscoveryService {

    /**
     * 完整发现流程：
     *   保存图片 → AI识别 → 匹配诗词 → 保存记录 → 返回结果
     */
    DiscoveryCreateVO create(MultipartFile image);

    /** 保存发现（已识别的） */
    void save(DiscoverySaveDTO dto);

    /** 查询历史 */
    List<DiscoveryVO> list();
}
