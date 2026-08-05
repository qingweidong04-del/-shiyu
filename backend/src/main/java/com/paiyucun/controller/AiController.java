package com.paiyucun.controller;

import com.paiyucun.common.Result;
import com.paiyucun.service.AiRecognitionService;
import com.paiyucun.vo.AiRecognitionVO;
import org.springframework.web.bind.annotation.*;

/**
 * AI 识别接口
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiRecognitionService aiRecognitionService;

    public AiController(AiRecognitionService aiRecognitionService) {
        this.aiRecognitionService = aiRecognitionService;
    }

    /**
     * 识别图片中的物体
     *
     * @param imageUrl 图片地址
     * @return 识别结果
     */
    @PostMapping("/recognize")
    public Result<AiRecognitionVO> recognize(@RequestParam String imageUrl) {
        AiRecognitionVO result = aiRecognitionService.recognize(imageUrl);
        return Result.ok(result);
    }
}
