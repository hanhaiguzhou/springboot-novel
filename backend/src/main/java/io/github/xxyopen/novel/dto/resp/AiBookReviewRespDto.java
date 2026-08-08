package io.github.xxyopen.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * AI书评 响应DTO
 *
 * @author xiongxiaoyang
 * @date 2026/8/8
 */
@Data
@Builder
public class AiBookReviewRespDto {

    /**
     * 评分（1-10分）
     */
    @Schema(description = "评分（1-10分）")
    private Integer score;

    /**
     * 一句话总评
     */
    @Schema(description = "一句话总评")
    private String summary;

    /**
     * 亮点
     */
    @Schema(description = "亮点")
    private List<String> pros;

    /**
     * 槽点
     */
    @Schema(description = "槽点")
    private List<String> cons;

    /**
     * 适合人群
     */
    @Schema(description = "适合人群")
    private String audience;

    /**
     * AI返回原文（JSON解析失败时的降级展示内容）
     */
    @Schema(description = "AI返回原文")
    private String rawText;

}
