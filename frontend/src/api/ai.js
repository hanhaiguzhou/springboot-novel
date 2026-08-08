import request from '../utils/request'

// AI书评生成
export function aiBookReview(bookId) {
    return request.post('/front/ai/book_review', null, {
        params: { bookId },
        timeout: 60000
    });
}

// AI章节导读
export function aiChapterSummary(chapterId) {
    return request.post('/front/ai/chapter_summary', null, {
        params: { chapterId },
        timeout: 60000
    });
}

// AI评论草稿生成
export function aiCommentDraft(bookId, content) {
    return request.post('/front/ai/comment_draft', null, {
        params: { bookId, content },
        timeout: 60000
    });
}

// AI智能荐书
export function aiRecommend() {
    return request.get('/front/ai/recommend', null, {
        timeout: 60000
    });
}
