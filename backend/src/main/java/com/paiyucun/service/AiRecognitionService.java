package com.paiyucun.service;

import com.paiyucun.vo.AiRecognitionVO;

/**
 * AI 图像识别服务接口
 *
 * 未来替换为真实视觉大模型时，只需新增实现类即可。
 */
public interface AiRecognitionService {

    /**
     * 识别图片中的主要物体
     *
     * @param imageUrl 图片地址
     * @return 识别结果
     */
    AiRecognitionVO recognize(String imageUrl);
}
