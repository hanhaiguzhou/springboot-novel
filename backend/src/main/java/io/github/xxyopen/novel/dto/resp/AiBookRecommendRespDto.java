package io.github.xxyopen.novel.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * AI智能荐书 响应DTO
 *
 * @author xiongxiaoyang
 * @date 2026/8/8
 */
@Data
@Builder
public class AiBookRecommendRespDto {

    /**
     * 小说ID
     */
    @Schema(description = "小说ID")
    private Long bookId;

    /**
     * 小说名
     */
    @Schema(description = "小说名")
    private String bookName;

    /**
     * 作者名
     */
    @Schema(description = "作者名")
    private String authorName;

    /**
     * 小说封面地址
     */
    @Schema(description = "小说封面地址")
    private String picUrl;

    /**
     * AI推荐语
     */
    @Schema(description = "AI推荐语")
    private String reason;

}
