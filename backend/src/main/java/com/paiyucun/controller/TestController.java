package com.paiyucun.controller;

import com.paiyucun.common.Result;
import com.paiyucun.mapper.PoemMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    private final PoemMapper poemMapper;

    public TestController(PoemMapper poemMapper) {
        this.poemMapper = poemMapper;
    }

    @GetMapping("/test")
    public Result<String> test() {
        return Result.ok("拍遇存后端启动成功");
    }

    @GetMapping("/test/db")
    public Result<String> testDb() {
        long count = poemMapper.selectCount(null);
        return Result.ok("数据库连接成功，诗词总数: " + count + " 首");
    }
}
