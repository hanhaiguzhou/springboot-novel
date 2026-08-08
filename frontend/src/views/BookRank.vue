<template>
  <Header />

  <div class="main box_center cf mb50">
    <div class="channelRankingContent cf">
      <div class="wrap_left fl">
        <div class="wrap_bg">
          <!--榜单详情 start-->
          <div class="pad20">
            <div class="book_tit">
              <div class="fl">
                <h3 class="font26 mt5 mb5" id="rankName">{{ rankName }}</h3>
              </div>
              <a class="fr"></a>
            </div>
            <div class="updateTable rankTable">
              <table cellpadding="0" cellspacing="0">
                <thead>
                  <tr>
                    <th class="rank">排名</th>
                    <th class="style">类别</th>
                    <th class="name">书名</th>
                    <th class="chapter">最新章节</th>
                    <th class="author">作者</th>
                    <th class="word">字数</th>
                  </tr>
                </thead>
                <tbody id="bookRankList">
                  <tr v-for="(item, index) in books" :key="index">
                    <td class="rank">
                      <i :class="'num' + (Number(`${index}`) + 1)">{{
                        index + 1
                      }}</i>
                    </td>
                    <td class="style">
                      <a href="javascript:void(0)" @click="bookDetail(item.id)"
                        >[{{ item.categoryName }}]</a
                      >
                    </td>
                    <td class="name">
                      <a
                        @click="bookDetail(item.id)"
                        href="javascript:void(0)"
                        >{{ item.bookName }}</a
                      >
                    </td>
                    <td class="chapter">
                      <a
                        @click="bookDetail(item.id)"
                        href="javascript:void(0)"
                        >{{ item.lastChapterName }}</a
                      >
                    </td>
                    <td class="author">
                      <a href="javascript:void(0)">{{ item.authorName }}</a>
                    </td>
                    <td class="word">{{ wordCountFormat(item.wordCount) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
          <!--榜单详情 end-->
        </div>
      </div>

      <div class="wrap_right fr">
        <div class="wrap_inner wrap_right_cont mb20">
          <div class="title cf noborder">
            <h3 class="on">排行榜</h3>
          </div>
          <div class="rightList2">
            <ul id="rankType">
              <li>
                <a :class="`${rankType == 1 ? 'on' : ''}`" href="javascript:void(0)" @click="visitRank"
                  >点击榜</a
                >
              </li>
              <li>
                <a :class="`${rankType == 2 ? 'on' : ''}`" href="javascript:void(0)" @click="newestRank">新书榜</a>
              </li>
              <li>
                <a :class="`${rankType == 3 ? 'on' : ''}`" href="javascript:void(0)" @click="updateRank">更新榜</a>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Footer />
</template>

<script>
import "@/assets/styles/book.css";
import { reactive, toRefs, onMounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  listVisitRankBooks,
  listUpdateRankBooks,
  listNewestRankBooks,
} from "@/api/book";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";
export default {
  name: "bookRank",
  components: {
    Header,
    Footer,
  },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const state = reactive({
      books: [],
      rankName: "点击榜",
      rankType: 1,
    });
    onMounted(() => {
      visitRank();
    });

    const visitRank = async () => {
      const { data } = await listVisitRankBooks();
      state.books = data;
      state.rankName = "点击榜";
      state.rankType = 1;
    };

    const newestRank = async () => {
      const { data } = await listNewestRankBooks();
      state.books = data;
      state.rankName = "新书榜";
      state.rankType = 2;
    };

    const updateRank = async () => {
      const { data } = await listUpdateRankBooks();
      state.books = data;
      state.rankName = "更新榜";
      state.rankType = 3;
    };

    const bookDetail = (bookId) => {
      router.push({ path: `/book/${bookId}` });
    };

    return {
      ...toRefs(state),
      bookDetail,
      newestRank,
      visitRank,
      updateRank,
    };
  },
  computed: {
    wordCountFormat(wordCount) {
      return (wordCount) => {
        if (wordCount.length > 5) {
          return parseInt(wordCount / 10000) + "万";
        }
        if (wordCount.length > 4) {
          return parseInt(wordCount / 1000) + "千";
        }
        return wordCount;
      };
    },
  },
};
</script>

<style>
/* ------- 排行榜 · 新中式精修 ------- */
/* 榜单名：宋体 + 左侧朱砂竖条 */
.book_tit .fl h3 {
  font-family: var(--font-serif);
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--ink);
  padding-left: 12px;
  position: relative;
}
.book_tit .fl h3::before {
  content: "";
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: var(--cinnabar);
  border-radius: 1px;
}

/* 榜单表格 */
.channelRankingContent .rankTable th {
  font-family: var(--font-serif);
  letter-spacing: 1px;
  color: var(--ink);
  background: var(--paper-deep);
}
.channelRankingContent .rankTable tbody tr:hover td {
  background: var(--cinnabar-fade);
}
.channelRankingContent .rankTable .name a,
.channelRankingContent .rankTable .chapter a {
  color: var(--ink);
}
.channelRankingContent .rankTable .name a:hover,
.channelRankingContent .rankTable .chapter a:hover,
.channelRankingContent .rankTable .style a:hover {
  color: var(--cinnabar);
}

/* 右侧榜单切换 */
.wrap_right_cont .title h3 {
  font-family: var(--font-serif);
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--ink);
  padding-left: 12px;
  position: relative;
}
.wrap_right_cont .title h3::before {
  content: "";
  position: absolute;
  left: 0;
  top: 2px;
  width: 4px;
  height: 17px;
  background: var(--cinnabar);
  border-radius: 1px;
}
.rightList2 li a {
  border-top: 1px dashed var(--line);
  border-left: 3px solid transparent;
  padding-left: 12px;
  transition: color 0.25s, background 0.25s, border-color 0.25s;
}
.rightList2 li a:hover {
  color: var(--cinnabar);
}
.rightList2 li a.on {
  color: var(--cinnabar);
  background: var(--cinnabar-fade);
  border-left-color: var(--cinnabar);
}

/* 分页 */
.el-pagination {
  justify-content: center;
}
.el-pagination.is-background .el-pager li:not(.is-disabled).is-active {
  background-color: var(--cinnabar) !important;
}
.el-pagination {
  --el-pagination-hover-color: var(--cinnabar) !important;
}
</style>
