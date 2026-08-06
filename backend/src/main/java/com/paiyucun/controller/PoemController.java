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

    @GetMapping("/poem")
    public Result<PoemVO> getPoem(
            @RequestParam("object") @NotBlank(message = "object 不能为空") String object
    ) {
        return Result.ok(poemService.getByObject(object));
    }

    /** 随机另一首诗（换一句） */
    @GetMapping("/poem/random")
    public Result<PoemVO> random(@RequestParam(defaultValue = "0") Long exclude) {
        return Result.ok(poemService.getRandom(exclude));
    }
}
