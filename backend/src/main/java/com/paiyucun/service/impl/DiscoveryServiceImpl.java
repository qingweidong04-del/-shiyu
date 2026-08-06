package com.paiyucun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paiyucun.dto.DiscoverySaveDTO;
import com.paiyucun.entity.Discovery;
import com.paiyucun.mapper.DiscoveryMapper;
import com.paiyucun.service.AiRecognitionService;
import com.paiyucun.service.DiscoveryService;
import com.paiyucun.service.PoemService;
import com.paiyucun.util.FileUploadUtil;
import com.paiyucun.vo.AiRecognitionVO;
import com.paiyucun.vo.DiscoveryCreateVO;
import com.paiyucun.vo.DiscoveryVO;
import com.paiyucun.vo.PoemVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscoveryServiceImpl implements DiscoveryService {

    private final DiscoveryMapper discoveryMapper;
    private final AiRecognitionService aiRecognitionService;
    private final PoemService poemService;

    @Value("${app.upload-path:./uploads}")
    private String uploadPath;

    public DiscoveryServiceImpl(DiscoveryMapper discoveryMapper,
                                AiRecognitionService aiRecognitionService,
                                PoemService poemService) {
        this.discoveryMapper = discoveryMapper;
        this.aiRecognitionService = aiRecognitionService;
        this.poemService = poemService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DiscoveryCreateVO create(MultipartFile image) {
        // ① 保存图片
        String imageUrl = FileUploadUtil.saveImage(image, uploadPath);

        // ② AI 识别物体
        AiRecognitionVO recognition = aiRecognitionService.recognize(imageUrl);

        // ③ 根据物体匹配诗词
        PoemVO poem = poemService.getByObject(recognition.getObjectName());

        // ④ 保存发现记录
        Discovery entity = new Discovery();
        entity.setImageUrl(imageUrl);
        entity.setObjectName(recognition.getObjectName());
        if (poem != null) {
            entity.setPoemId(poem.getPoemId());
            entity.setPoemLine(poem.getContent());
            entity.setPoemSource(poem.getAuthor() + "《" + poem.getTitle() + "》");
        }
        discoveryMapper.insert(entity);

        // ⑤ 组装返回
        DiscoveryCreateVO result = new DiscoveryCreateVO();
        result.setId(entity.getId());
        result.setImageUrl(imageUrl);
        result.setObjectName(recognition.getObjectName());
        result.setConfidence(recognition.getConfidence());

        if (poem != null) {
            DiscoveryCreateVO.PoemInfo info = new DiscoveryCreateVO.PoemInfo();
            info.setPoemId(poem.getPoemId());
            info.setTitle(poem.getTitle());
            info.setContent(poem.getContent());
            info.setAuthor(poem.getAuthor());
            info.setDynasty(poem.getDynasty());
            info.setPinyin(poem.getPinyin());
            info.setTranslation(poem.getTranslation());
            info.setSource(poem.getAuthor() + "《" + poem.getTitle() + "》");
            info.setFullContent(poem.getFullContent());
            info.setFullPinyin(poem.getFullPinyin());
            info.setFullExplanation(poem.getFullExplanation());
            result.setPoem(info);
        }

        return result;
    }

    @Override
    public void save(DiscoverySaveDTO dto) {
        Discovery entity = new Discovery();
        entity.setImageUrl(dto.getImageUrl());
        entity.setObjectName(dto.getObjectName());
        entity.setPoemId(dto.getPoemId());
        entity.setPoemLine(dto.getPoemLine());
        entity.setPoemSource(dto.getPoemSource());
        discoveryMapper.insert(entity);
    }

    @Override
    public List<DiscoveryVO> list() {
        return discoveryMapper.selectList(
                new LambdaQueryWrapper<Discovery>().orderByDesc(Discovery::getCreateTime)
        ).stream().map(e -> {
            DiscoveryVO vo = new DiscoveryVO();
            vo.setId(e.getId());
            vo.setImageUrl(e.getImageUrl());
            vo.setObjectName(e.getObjectName());
            vo.setPoemLine(e.getPoemLine());
            vo.setPoemSource(e.getPoemSource());
            vo.setCreateTime(e.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }
}
