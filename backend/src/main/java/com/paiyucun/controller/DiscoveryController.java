package com.paiyucun.controller;

import com.paiyucun.common.Result;
import com.paiyucun.dto.DiscoverySaveDTO;
import com.paiyucun.service.DiscoveryService;
import com.paiyucun.vo.DiscoveryVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 发现记录接口
 */
@RestController
@RequestMapping("/api/discovery")
@Validated
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    public DiscoveryController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * 保存发现记录
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody @Valid DiscoverySaveDTO dto) {
        discoveryService.save(dto);
        return Result.ok("保存成功");
    }

    /**
     * 查询历史记录（时间线）
     */
    @GetMapping("/list")
    public Result<List<DiscoveryVO>> list() {
        List<DiscoveryVO> list = discoveryService.list();
        return Result.ok(list);
    }
}
