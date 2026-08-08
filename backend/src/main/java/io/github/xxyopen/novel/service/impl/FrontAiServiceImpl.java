package io.github.xxyopen.novel.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xxyopen.novel.core.common.constant.ErrorCodeEnum;
import io.github.xxyopen.novel.core.common.resp.RestResp;
import io.github.xxyopen.novel.dto.resp.AiBookRecommendRespDto;
import io.github.xxyopen.novel.dto.resp.AiBookReviewRespDto;
import io.github.xxyopen.novel.dto.resp.BookChapterRespDto;
import io.github.xxyopen.novel.dto.resp.BookInfoRespDto;
import io.github.xxyopen.novel.dto.resp.BookRankRespDto;
import io.github.xxyopen.novel.manager.AiRateLimiter;
import io.github.xxyopen.novel.manager.cache.BookChapterCacheManager;
import io.github.xxyopen.novel.manager.cache.BookContentCacheManager;
import io.github.xxyopen.novel.manager.cache.BookInfoCacheManager;
import io.github.xxyopen.novel.manager.cache.BookRankCacheManager;
import io.github.xxyopen.novel.service.FrontAiService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * 前台门户-AI模块 服务实现类
 *
 * @author xiongxiaoyang
 * @date 2026/8/8
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FrontAiServiceImpl implements FrontAiService {

    /**
     * AI 调用最大输入长度（字符），超出部分截断，避免 token 超限
     */
    private static final int MAX_INPUT_LENGTH = 1500;

    /**
     * AI 荐书候选池大小
     */
    private static final int RECOMMEND_CANDIDATE_SIZE = 15;

    /**
     * AI 荐书最终数量
     */
    private static final int RECOMMEND_SIZE = 5;

    private final ChatClient chatClient;

    private final BookInfoCacheManager bookInfoCacheManager;

    private final BookChapterCacheManager bookChapterCacheManager;

    private final BookContentCacheManager bookContentCacheManager;

    private final BookRankCacheManager bookRankCacheManager;

    private final AiRateLimiter aiRateLimiter;

    private final ObjectMapper objectMapper;

    @Override
    public RestResp<AiBookReviewRespDto> bookReview(Long bookId) {
        if (aiRateLimiter.isLimited("bookReview:" + bookId, 3)) {
            return RestResp.fail(ErrorCodeEnum.USER_REQ_MANY);
        }
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookId);
        if (bookInfo == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }
        String systemPrompt = "你是一名资深网络小说书评人，善于从读者视角给出客观、有趣、真诚的短书评。"
            + "请严格以 JSON 格式输出，不要输出任何多余文字、注释或 markdown 代码块标记，格式如下：\n"
            + "{\"score\": 8, \"summary\": \"一句话总评\", \"pros\": [\"亮点1\", \"亮点2\", \"亮点3\"], "
            + "\"cons\": [\"槽点1\", \"槽点2\"], \"audience\": \"适合人群\"}\n"
            + "其中 score 为 1-10 的整数。";
        String userPrompt = "请为以下小说生成书评：\n"
            + "书名：《" + bookInfo.getBookName() + "》\n"
            + "作者：" + bookInfo.getAuthorName() + "\n"
            + "分类：" + bookInfo.getCategoryName() + "\n"
            + "简介：" + truncate(bookInfo.getBookDesc(), MAX_INPUT_LENGTH) + "\n";
        String content = callAi(systemPrompt, userPrompt);
        AiBookReviewRespDto.AiBookReviewRespDtoBuilder builder = AiBookReviewRespDto.builder()
            .rawText(content);
        JsonNode node = extractJson(content);
        if (node != null && node.isObject()) {
            builder.score(node.has("score") && node.get("score").canConvertToInt()
                ? node.get("score").asInt() : null);
            builder.summary(text(node, "summary"));
            builder.pros(list(node, "pros"));
            builder.cons(list(node, "cons"));
            builder.audience(text(node, "audience"));
        }
        return RestResp.ok(builder.build());
    }

    @Override
    public RestResp<String> chapterSummary(Long chapterId) {
        if (aiRateLimiter.isLimited("chapterSummary:" + chapterId, 3)) {
            return RestResp.fail(ErrorCodeEnum.USER_REQ_MANY);
        }
        BookChapterRespDto chapter = bookChapterCacheManager.getChapter(chapterId);
        if (chapter == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }
        String bookContent = bookContentCacheManager.getBookContent(chapterId);
        if (bookContent == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }
        String systemPrompt = "你是一名小说阅读助手。请根据给出的章节正文，输出该章节的导读，"
            + "包括：1.本章剧情概述（2-3句话）；2.关键剧情/伏笔提示（若存在）；3.阅读体验点评（1句话）。"
            + "语气轻松自然，用分条列表的形式输出，总字数控制在 200 字以内。";
        String userPrompt = "章节名：《" + chapter.getChapterName() + "》\n"
            + "章节正文（节选）：\n" + truncate(bookContent, MAX_INPUT_LENGTH);
        return RestResp.ok(callAi(systemPrompt, userPrompt));
    }

    @Override
    public RestResp<String> commentDraft(Long bookId, String content) {
        if (aiRateLimiter.isLimited("commentDraft:" + bookId, 5)) {
            return RestResp.fail(ErrorCodeEnum.USER_REQ_MANY);
        }
        BookInfoRespDto bookInfo = bookInfoCacheManager.getBookInfo(bookId);
        if (bookInfo == null) {
            return RestResp.fail(ErrorCodeEnum.BOOK_NOT_FOUND);
        }
        String systemPrompt = "你是一名网络小说读者，正在评论区发表对一本小说的评论。"
            + "请根据给定的书名、简介和用户已写内容，补全并润色成一条 50-120 字的读者书评，"
            + "口语化、真诚、有梗，不要出现\"作为AI\"等表述，直接输出评论内容本身。";
        String userPrompt = "书名：《" + bookInfo.getBookName() + "》\n"
            + "简介：" + truncate(bookInfo.getBookDesc(), MAX_INPUT_LENGTH) + "\n"
            + "用户已写的内容（可为空）：" + (content == null ? "" : truncate(content, 200));
        return RestResp.ok(callAi(systemPrompt, userPrompt));
    }

    @Override
    public RestResp<List<AiBookRecommendRespDto>> recommend() {
        if (aiRateLimiter.isLimited("recommend", 5)) {
            return RestResp.fail(ErrorCodeEnum.USER_REQ_MANY);
        }
        // 组装候选书单（点击榜 + 新书榜去重）
        Map<Long, BookRankRespDto> candidateMap = new LinkedHashMap<>();
        bookRankCacheManager.listVisitRankBooks().forEach(v -> candidateMap.putIfAbsent(v.getId(), v));
        bookRankCacheManager.listNewestRankBooks().forEach(v -> candidateMap.putIfAbsent(v.getId(), v));
        List<BookRankRespDto> candidates = new ArrayList<>(candidateMap.values());
        if (candidates.size() > RECOMMEND_CANDIDATE_SIZE) {
            candidates = candidates.subList(0, RECOMMEND_CANDIDATE_SIZE);
        }
        if (candidates.isEmpty()) {
            return RestResp.ok(new ArrayList<>());
        }
        StringBuilder candidateDesc = new StringBuilder();
        for (BookRankRespDto v : candidates) {
            candidateDesc.append(v.getId()).append("|《").append(v.getBookName()).append("》")
                .append("|作者:").append(v.getAuthorName())
                .append("|简介:").append(truncate(v.getBookDesc(), 50)).append("\n");
        }
        String systemPrompt = "你是一名小说推荐官。请从给出的候选书中挑选最适合读者阅读的 5 本，"
            + "并为每本写一句 15-30 字的推荐语（突出看点、不剧透、有趣）。"
            + "严格以 JSON 数组格式输出，不要输出任何多余文字或 markdown 标记，格式如下：\n"
            + "[{\"bookId\": 123, \"reason\": \"推荐语\"}, {\"bookId\": 456, \"reason\": \"推荐语\"}]";
        String userPrompt = "候选书单如下（格式：bookId|书名|作者|简介）：\n" + candidateDesc;
        String content = callAi(systemPrompt, userPrompt);
        // 解析 AI 推荐结果，失败则降级为热门榜前 5 本
        List<AiBookRecommendRespDto> result = parseRecommend(content, candidates);
        if (result.isEmpty()) {
            result = candidates.stream().limit(RECOMMEND_SIZE).map(v -> AiBookRecommendRespDto.builder()
                .bookId(v.getId())
                .bookName(v.getBookName())
                .authorName(v.getAuthorName())
                .picUrl(v.getPicUrl())
                .reason("热门人气作品，点开看看合不合口味")
                .build()).toList();
        }
        return RestResp.ok(result);
    }

    /**
     * 解析 AI 荐书 JSON 结果
     */
    private List<AiBookRecommendRespDto> parseRecommend(String content,
        List<BookRankRespDto> candidates) {
        List<AiBookRecommendRespDto> result = new ArrayList<>();
        try {
            JsonNode node = extractJson(content);
            if (node == null || !node.isArray()) {
                return result;
            }
            Map<Long, BookRankRespDto> candidateMap = new LinkedHashMap<>();
            candidates.forEach(v -> candidateMap.put(v.getId(), v));
            for (JsonNode item : node) {
                Long bookId = item.has("bookId") && item.get("bookId").canConvertToLong()
                    ? item.get("bookId").asLong() : null;
                if (bookId == null || !candidateMap.containsKey(bookId)) {
                    continue;
                }
                BookRankRespDto candidate = candidateMap.get(bookId);
                result.add(AiBookRecommendRespDto.builder()
                    .bookId(bookId)
                    .bookName(candidate.getBookName())
                    .authorName(candidate.getAuthorName())
                    .picUrl(candidate.getPicUrl())
                    .reason(item.path("reason").asText(""))
                    .build());
                if (result.size() >= RECOMMEND_SIZE) {
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("解析AI荐书结果失败，降级为热门榜:{}", e.getMessage());
        }
        return result;
    }

    /**
     * 调用 AI 模型
     */
    private String callAi(String systemPrompt, String userPrompt) {
        return chatClient.prompt()
            .system(systemPrompt)
            .user(userPrompt)
            .call()
            .content();
    }

    /**
     * 从 AI 输出中提取 JSON（兼容模型输出包裹 markdown 代码块或思考内容的情况）
     */
    private JsonNode extractJson(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        try {
            String text = content.trim();
            // 去掉 ```json ... ``` 代码块标记
            text = text.replaceAll("```json|```", "");
            int start = -1;
            int end = -1;
            char first = '[';
            char last = ']';
            for (char c : new char[]{'{', '['}) {
                int s = text.indexOf(c);
                if (s >= 0 && (start < 0 || s < start)) {
                    start = s;
                    first = c;
                    last = c == '{' ? '}' : ']';
                }
            }
            if (start < 0) {
                return null;
            }
            end = text.lastIndexOf(last);
            if (end < start) {
                return null;
            }
            return objectMapper.readTree(text.substring(start, end + 1));
        } catch (Exception e) {
            log.warn("AI输出JSON解析失败:{}", e.getMessage());
            return null;
        }
    }

    /**
     * 截断超长文本
     */
    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    /**
     * 读取 JSON 节点文本字段
     */
    private String text(JsonNode node, String field) {
        return node.has(field) ? node.get(field).asText() : null;
    }

    /**
     * 读取 JSON 节点字符串数组字段
     */
    private List<String> list(JsonNode node, String field) {
        List<String> result = new ArrayList<>();
        if (node.has(field) && node.get(field).isArray()) {
            node.get(field).forEach(v -> result.add(v.asText()));
        }
        return result;
    }

}
