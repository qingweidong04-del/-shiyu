package com.paiyucun.vo;

import lombok.Data;

/**
 * AI 识别结果
 */
@Data
public class AiRecognitionVO {

    /** 识别到的物体名，如"荷花" */
    private String objectName;

    /** 置信度 0~1 */
    private Double confidence;

    public static AiRecognitionVO of(String objectName, Double confidence) {
        AiRecognitionVO vo = new AiRecognitionVO();
        vo.setObjectName(objectName);
        vo.setConfidence(confidence);
        return vo;
    }
}
