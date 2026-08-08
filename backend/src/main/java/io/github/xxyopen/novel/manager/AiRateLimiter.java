package io.github.xxyopen.novel.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * AI 接口本地限流组件
 * <p>
 * 基于 Caffeine 实现简单的滑动窗口计数限流，用于保护 AI 第三方调用成本，
 * 防止同一资源（如同一本书/同一章节）被频繁请求。
 *
 * @author xiongxiaoyang
 * @date 2026/8/8
 */
@Component
@RequiredArgsConstructor
public class AiRateLimiter {

    /**
     * 统计窗口缓存：key -> 窗口内调用次数
     */
    private final Cache<String, AtomicInteger> counterCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(1))
        .maximumSize(10000)
        .build();

    /**
     * 判断指定 key 在 1 分钟窗口内是否超过最大调用次数
     *
     * @param key      限流维度 key，如 bookReview:123
     * @param maxTimes 1 分钟内允许的最大调用次数
     * @return true-已触发限流 false-可继续调用
     */
    public boolean isLimited(String key, int maxTimes) {
        AtomicInteger counter = counterCache.get(key, k -> new AtomicInteger(0));
        return counter.incrementAndGet() > maxTimes;
    }

}
