<template>
  <Header />

  <div class="main box_center cf">
    <div class="newsMain cf">
        <div class="nav_sub">
            当前位置：<a href="/"> 首页 </a> &gt; <span> 新闻公告 </span> &gt; <span>{{news.title}}</span>
        </div>
        <div class="channelWrap channelNews cf">
            <div class="news_title">
                <h2>{{news.title}}</h2>
                <!--while ... corresponds to th:text (i.e. result will be HTML-escaped), ... corresponds to th:utext-->
                <p class="from">来源：{{news.sourceName}} <span class="time">时间：{{news.updateTime}}</span> </p>
            </div>
            <div class="news_info" v-html="news.content"></div>
        </div>
    </div>
</div>
  <Footer />
</template>

<script>
import "@/assets/styles/about.css";
import { reactive, toRefs, onMounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import {getNewsById} from "@/api/news"
import {
  listVisitRankBooks,
  listUpdateRankBooks,
  listNewestRankBooks,
} from "@/api/book";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";
export default {
  name: "news",
  components: {
    Header,
    Footer,
  },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const state = reactive({
      news: {}
    });
    onMounted(async () => {
      const { data } = await getNewsById(route.params.id);
      state.news = data
    });


    return {
      ...toRefs(state)
      
    };
  }
  
};
</script>

<style>
/* 新中式 · 新闻公告 */
.newsMain .nav_sub {
  color: var(--ink-3);
  font-size: 13px;
}
.newsMain .nav_sub a {
  color: var(--ink-2);
}
.newsMain .nav_sub a:hover {
  color: var(--cinnabar);
}
.channelNews .news_title {
  text-align: center;
  border-bottom: 1px solid var(--line);
  padding-bottom: 20px;
  margin-bottom: 24px;
}
.channelNews .news_title h2 {
  font-family: var(--font-serif);
  font-size: 26px;
  font-weight: 700;
  color: var(--ink);
  letter-spacing: 2px;
  line-height: 1.5;
}
.channelNews .news_title .from {
  margin-top: 12px;
  color: var(--ink-3);
  font-size: 13px;
}
.channelNews .news_title .from .time {
  margin-left: 16px;
  color: var(--ink-3);
}
.channelNews .news_info {
  color: var(--ink-2);
  font-size: 15px;
  line-height: 2;
  font-family: var(--font-serif);
}
.channelNews .news_info a:hover {
  color: var(--cinnabar);
}
</style>

