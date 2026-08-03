package com.paiyucun.service;

import com.paiyucun.vo.PoemVO;

/**
 * 诗词匹配服务
 */
public interface PoemService {

    /**
     * 根据 AI 识别的物体名匹配古诗
     *
     * @param object 物体名，如"荷花"、"鸟"
     * @return 匹配的诗词，未匹配返回 null
     */
    PoemVO getByObject(String object);
}
