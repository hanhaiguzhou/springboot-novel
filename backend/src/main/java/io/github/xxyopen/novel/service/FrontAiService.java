package io.github.xxyopen.novel.service;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.dto.resp.AiBookRecommendRespDto;
import io.github.xxyopen.novel.dto.resp.AiBookReviewRespDto;
import java.util.List;

/**
 * 前台门户-AI模块 服务类
 *
 * @author xiongxiaoyang
 * @date 2026/8/8
 */
public interface FrontAiService {

    /**
     * AI书评生成
     *
     * @param bookId 小说ID
     * @return AI书评
     */
    RestResp<AiBookReviewRespDto> bookReview(Long bookId);

    /**
     * AI章节导读
     *
     * @param chapterId 章节ID
     * @return 章节要点总结
     */
    RestResp<String> chapterSummary(Long chapterId);

    /**
     * AI评论草稿生成
     *
     * @param bookId  小说ID
     * @param content 用户已输入的草稿内容（可为空）
     * @return AI生成的评论草稿
     */
    RestResp<String> commentDraft(Long bookId, String content);

    /**
     * AI智能荐书
     *
     * @return AI精选推荐书单（含推荐语）
     */
    RestResp<List<AiBookRecommendRespDto>> recommend();

}
