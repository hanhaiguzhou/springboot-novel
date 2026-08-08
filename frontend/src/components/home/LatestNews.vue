<template>
  <dl class="hot_notice" id="indexNews">
    <dd style="text-align: left" v-for="(item, index) in newsList" :key="index">
      <span>[{{ item.categoryName }}]</span>
      <a href="javascript:void(0)" @click="newsInfo(item.id)"> {{ item.title }}</a>
      
    </dd>
  </dl>
</template>

<script>
import { reactive, toRefs, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElLoading } from "element-plus";
import { listLatestNews } from "@/api/news";
export default {
  name: "LatestNews",
  setup() {
    const state = reactive({
      newsList: [],
    });
    const route = useRoute();
    const router = useRouter();
    onMounted(async () => {
      const loadingInstance = ElLoading.service({
        target: "#indexNews",
        text: "加载中。。。",
      });
      const { data } = await listLatestNews();
      loadingInstance.close();

      state.newsList = data;
    });
    const newsInfo = (newsId) => {
      router.push({ path: `/news/${newsId}` });
    };
    return {
      ...toRefs(state),
      newsInfo,
    };
  },
};
</script>

<style>
/* ------- 最新公告 · 新中式精修 ------- */
.hot_notice dd {
  line-height: 32px;
  border-bottom: 1px dashed var(--line);
  font-size: 13px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
.hot_notice dd:last-child {
  border-bottom: none;
}
.hot_notice dd span {
  color: var(--cinnabar);
  font-family: var(--font-kai);
  margin-right: 6px;
}
.hot_notice dd a {
  color: var(--ink-2);
  transition: color 0.25s;
}
.hot_notice dd a:hover {
  color: var(--cinnabar);
}
</style>