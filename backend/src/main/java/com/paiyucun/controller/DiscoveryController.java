package com.paiyucun.controller;

import com.paiyucun.common.Result;
import com.paiyucun.dto.DiscoverySaveDTO;
import com.paiyucun.service.DiscoveryService;
import com.paiyucun.vo.DiscoveryCreateVO;
import com.paiyucun.vo.DiscoveryVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/discovery")
@Validated
public class DiscoveryController {

    private final DiscoveryService discoveryService;

    public DiscoveryController(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    /**
     * 完整发现流程 — 拍照 → 识别 → 匹配 → 保存
     */
    @PostMapping("/create")
    public Result<DiscoveryCreateVO> create(@RequestParam("image") MultipartFile image) {
        DiscoveryCreateVO result = discoveryService.create(image);
        return Result.ok(result);
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
     * 查询历史记录
     */
    @GetMapping("/list")
    public Result<List<DiscoveryVO>> list() {
        return Result.ok(discoveryService.list());
    }
}
