package com.paiyucun.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.paiyucun.common.BusinessException;
import com.paiyucun.entity.Poem;
import com.paiyucun.mapper.PoemMapper;
import com.paiyucun.service.PoemService;
import com.paiyucun.vo.PoemVO;
import org.springframework.stereotype.Service;

@Service
public class PoemServiceImpl implements PoemService {

    private final PoemMapper poemMapper;

    public PoemServiceImpl(PoemMapper poemMapper) {
        this.poemMapper = poemMapper;
    }

    @Override
    public PoemVO getByObject(String object) {
        // ① object_name 精确匹配
        Poem poem = poemMapper.selectOne(
                new LambdaQueryWrapper<Poem>()
                        .eq(Poem::getObjectName, object)
                        .last("LIMIT 1")
        );

        // ② 降级：keywords 模糊匹配
        if (poem == null) {
            poem = poemMapper.selectOne(
                    new LambdaQueryWrapper<Poem>()
                            .like(Poem::getKeywords, object)
                            .last("LIMIT 1")
            );
        }

        // ③ 未匹配 → 抛业务异常
        if (poem == null) {
            throw new BusinessException(404, "未找到与「" + object + "」匹配的古诗");
        }

        return toVO(poem);
    }

    private PoemVO toVO(Poem poem) {
        PoemVO vo = new PoemVO();
        vo.setTitle(poem.getTitle());
        vo.setAuthor(poem.getAuthor());
        vo.setDynasty(poem.getDynasty());
        vo.setContent(poem.getMatchLine());
        vo.setPinyin(poem.getPinyin());
        vo.setTranslation(poem.getTranslation());
        vo.setAudioUrl(null);
        return vo;
    }
}
