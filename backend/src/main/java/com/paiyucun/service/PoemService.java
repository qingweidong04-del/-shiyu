package com.paiyucun.service;

import com.paiyucun.vo.PoemVO;

/**
 * 诗词匹配服务
 */
public interface PoemService {

    /** 根据物体名匹配古诗 */
    PoemVO getByObject(String object);

    /** 随机一首（排除指定ID） */
    PoemVO getRandom(Long excludeId);
}
