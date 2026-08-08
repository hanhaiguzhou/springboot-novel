package io.github.xxyopen.novel.controller.front;

import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.core.constant.ApiRouterConsts;
import io.github.xxyopen.novel.dto.resp.AiBookRecommendRespDto;
import io.github.xxyopen.novel.dto.resp.AiBookReviewRespDto;
import io.github.xxyopen.novel.service.FrontAiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台门户-AI模块 API 控制器
 *
 * @author xiongxiaoyang
 * @date 2026/8/8
 */
@Tag(name = "FrontAiController", description = "前台门户-AI模块")
@RestController
@RequestMapping(ApiRouterConsts.API_FRONT_AI_URL_PREFIX)
@RequiredArgsConstructor
public class FrontAiController {

    private final FrontAiService frontAiService;

    /**
     * AI书评生成接口
     */
    @Operation(summary = "AI书评生成接口")
    @PostMapping("book_review")
    public RestResp<AiBookReviewRespDto> bookReview(
        @Parameter(description = "小说ID", required = true) @RequestParam("bookId") Long bookId) {
        return frontAiService.bookReview(bookId);
    }

    /**
     * AI章节导读接口
     */
    @Operation(summary = "AI章节导读接口")
    @PostMapping("chapter_summary")
    public RestResp<String> chapterSummary(
        @Parameter(description = "章节ID", required = true) @RequestParam("chapterId") Long chapterId) {
        return frontAiService.chapterSummary(chapterId);
    }

    /**
     * AI评论草稿生成接口
     */
    @Operation(summary = "AI评论草稿生成接口")
    @PostMapping("comment_draft")
    public RestResp<String> commentDraft(
        @Parameter(description = "小说ID", required = true) @RequestParam("bookId") Long bookId,
        @Parameter(description = "用户已写草稿") @RequestParam(value = "content", required = false) String content) {
        return frontAiService.commentDraft(bookId, content);
    }

    /**
     * AI智能荐书接口
     */
    @Operation(summary = "AI智能荐书接口")
    @GetMapping("recommend")
    public RestResp<List<AiBookRecommendRespDto>> recommend() {
        return frontAiService.recommend();
    }

}
