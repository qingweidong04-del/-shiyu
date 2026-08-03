package com.paiyucun.controller;

import com.paiyucun.common.Result;
import com.paiyucun.service.PoemService;
import com.paiyucun.vo.PoemVO;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 诗词匹配接口
 */
@RestController
@RequestMapping("/api")
@Validated
public class PoemController {

    private final PoemService poemService;

    public PoemController(PoemService poemService) {
        this.poemService = poemService;
    }

    /**
     * 根据 AI 识别的物体名匹配古诗
     *
     * @param object 物体名，如"荷花"、"鸟"
     */
    @GetMapping("/poem")
    public Result<PoemVO> getPoem(
            @RequestParam("object")
            @NotBlank(message = "object 不能为空")
            String object
    ) {
        PoemVO poem = poemService.getByObject(object);
        return Result.ok(poem);
    }
}
