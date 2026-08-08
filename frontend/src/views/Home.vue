<template>
  <Header />
  <div class="main box_center cf">
    <div class="channelWrap channelBanner cf">
      <div class="leftBox">
        <div class="sliderContent">
          <dl class="scBigImg" id="carouseBig">
            <dd
              v-for="(item, index) in sliderContent"
              :key="index"
              :class="{ on: index == 0 }"
            >
              <a href="javascript:void(0)" @click="bookDetail(item.bookId)">
                <img
                  :src="`${imgBaseUrl}` + `${item.picUrl}`"
                  :alt="item.bookName"
                  onerror="this.src='default.gif';this.onerror=null"
                />
              </a>
            </dd>
          </dl>
          <div class="scSmallImg" id="carouseSmall">
            <ul>
              <li
                v-for="(item, index) in sliderContent"
                :key="index"
                :class="{ on: index == 0 }"
              >
                <img
                  :src="`${imgBaseUrl}` + `${item.picUrl}`"
                  :alt="item.bookName"
                  onerror="this.src='default.gif';this.onerror=null"
                />
              </li>
            </ul>
          </div>
        </div>
        <div class="hot_articles">
          <dl class="hot_recommend" id="topBooks1">
            <dt>
              <a href="javascript:void(0)" @click="bookDetail(topBooks1[0].bookId)" v-if="topBooks1[0]">{{
                topBooks1[0].bookName
              }}</a>
            </dt>
            <dd>
              <a href="javascript:void(0)" @click="bookDetail(topBooks1[1].bookId)" v-if="topBooks1[1]">{{
                topBooks1[1].bookName
              }}</a
              ><a href="javascript:void(0)" @click="bookDetail(topBooks1[2].bookId)" v-if="topBooks1[2]">{{
                topBooks1[2].bookName
              }}</a>
            </dd>
            <dd>
              <a href="javascript:void(0)" @click="bookDetail(topBooks1[3].bookId)" v-if="topBooks1[3]">{{
                topBooks1[3].bookName
              }}</a
              ><a href="javascript:void(0)" @click="bookDetail(topBooks1[4].bookId)" v-if="topBooks1[4]">{{
                topBooks1[4].bookName
              }}</a>
            </dd>
          </dl>
          <dl class="hot_recommend" id="topBooks2">
            <dt>
              <a href="javascript:void(0)" @click="bookDetail(topBooks1[5].bookId)" v-if="topBooks1[5]">{{
                topBooks1[5].bookName
              }}</a>
            </dt>
            <dd>
              <a href="javascript:void(0)" @click="bookDetail(topBooks1[6].bookId)" v-if="topBooks1[6]">{{
                topBooks1[6].bookName
              }}</a
              ><a href="javascript:void(0)" @click="bookDetail(topBooks1[7].bookId)" v-if="topBooks1[7]">{{
                topBooks1[7].bookName
              }}</a>
            </dd>
            <dd>
              <a href="javascript:void(0)" @click="bookDetail(topBooks1[8].bookId)" v-if="topBooks1[8]">{{
                topBooks1[8].bookName
              }}</a
              ><a href="javascript:void(0)" @click="bookDetail(topBooks1[9].bookId)" v-if="topBooks1[9]">{{
                topBooks1[9].bookName
              }}</a>
            </dd>
          </dl>

          <LatestNews />
        </div>
      </div>
      <div class="rightBox">
        <div class="title cf" id="weekcommend">
          <h3>本周强推</h3>
        </div>
        <div class="rightList">
          <ul id="currentWeek">
            <li
              v-for="(item, index) in weekcommend"
              :key="index"
              :class="['num' + (Number(`${index}`) + 1), { on: index == 0 }]"
            >
              <div class="book_name">
                <i>{{ index + 1 }}</i
                ><a class="name" href="javascript:void(0)" @click="bookDetail(item.bookId)">{{
                  item.bookName
                }}</a>
              </div>
              <div class="book_intro">
                <div class="cover">
                  <a href="javascript:void(0)" @click="bookDetail(item.bookId)"
                    ><img
                      :src="`${imgBaseUrl}` + `${item.picUrl}`"
                      :alt="item.bookName"
                      onerror="this.src='default.gif';this.onerror=null"
                  /></a>
                </div>
                <a
                  class="txt"
                  href="javascript:void(0)" @click="bookDetail(item.bookId)"
                  v-html="item.bookDesc"
                ></a>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <div class="channelWrap channelPic cf">
      <div class="leftBox">
        <div class="title">
          <h2 class="on">热门推荐</h2>
        </div>
        <div class="picRecommend cf" id="hotRecBooks">
          <div
            class="itemsList"
            v-for="(item, index) in hotRecommend"
            :key="index"
          >
            <a class="items_img" href="javascript:void(0)" @click="bookDetail(item.bookId)">
              <img
                :src="`${imgBaseUrl}` + `${item.picUrl}`"
                onerror="this.src='default.gif';this.onerror=null"
                :alt="item.bookName"
              />
            </a>
            <div class="items_txt">
              <h4>
                <a href="javascript:void(0)" @click="bookDetail(item.bookId)">{{ item.bookName }}</a>
              </h4>
              <p class="author">
                <a href="javascript:void(0)">作者：{{ item.authorName }}</a>
              </p>
              <p class="intro">
                <a
                  href="javascript:void(0)" @click="bookDetail(item.bookId)"
                  v-html="item.bookDesc"
                ></a>
              </p>
            </div>
          </div>
        </div>
      </div>
      <BookVisitRank />
    </div>
    <div class="channelWrap channelPic cf">
      <div class="leftBox">
        <div class="title">
          <h2>精品推荐</h2>
        </div>

        <div class="picRecommend cf" id="classicBooks">
          <div
            class="itemsList"
            v-for="(item, index) in goodRecommend"
            :key="index"
          >
            <a class="items_img" href="javascript:void(0)" @click="bookDetail(item.bookId)">
              <img
                :src="`${imgBaseUrl}` + `${item.picUrl}`"
                onerror="this.src='default.gif';this.onerror=null"
              />
            </a>
            <div class="items_txt">
              <h4>
                <a href="javascript:void(0)" @click="bookDetail(item.bookId)">{{ item.bookName }}</a>
              </h4>
              <p class="author">
                <a href="javascript:void(0)">作者：{{ item.authorName }}</a>
              </p>
              <p class="intro">
                <a
                  href="javascript:void(0)" @click="bookDetail(item.bookId)"
                  v-html="item.bookDesc"
                ></a>
              </p>
            </div>
          </div>
        </div>
      </div>
      <BookNewestRank />
    </div>

    <BookUpdateRank />

    <!--AI 为你荐书 start-->
    <div class="channelWrap channelPic cf">
      <div class="leftBox">
        <div class="title">
          <h2 class="on">AI 为你荐书</h2>
          <span class="ai_rec_tag">AI 精选 · 仅供参考</span>
        </div>
        <div class="picRecommend cf" id="aiRecBooks" v-loading="aiRecLoading">
          <div
            class="itemsList"
            v-for="(item, index) in aiRecommendBooks"
            :key="index"
          >
            <a class="items_img" href="javascript:void(0)" @click="bookDetail(item.bookId)">
              <img
                :src="`${imgBaseUrl}` + `${item.picUrl}`"
                onerror="this.src='default.gif';this.onerror=null"
                :alt="item.bookName"
              />
            </a>
            <div class="items_txt">
              <h4>
                <a href="javascript:void(0)" @click="bookDetail(item.bookId)">{{ item.bookName }}</a>
              </h4>
              <p class="author">
                <a href="javascript:void(0)">作者：{{ item.authorName }}</a>
              </p>
              <p class="ai_reason">「{{ item.reason }}」</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!--AI 为你荐书 end-->
  </div>
  <FriendLink />

  <Footer />
</template>

<script>
import { reactive, toRefs, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { listHomeBooks } from "@/api/home";
import { aiRecommend } from "@/api/ai";
import { ElMessage, ElLoading } from "element-plus";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";
import LatestNews from "@/components/home/LatestNews";
import FriendLink from "@/components/home/FriendLink";
import BookVisitRank from "@/components/home/BookVisitRank";
import BookNewestRank from "@/components/home/BookNewestRank";
import BookUpdateRank from "@/components/home/BookUpdateRank";
export default {
  name: "home",
  components: {
    Header,
    LatestNews,
    FriendLink,
    BookVisitRank,
    BookNewestRank,
    BookUpdateRank,
    Footer,
  },
  setup() {
    const route = useRoute();
    const router = useRouter();
    const state = reactive({
      // 轮播图
      sliderContent: [],
      // 顶部栏
      topBooks1: [],
      //本周强推
      weekcommend: [],
      // 热门推荐
      hotRecommend: [],
      // 精品推荐
      goodRecommend: [],
      // AI 智能荐书
      aiRecommendBooks: [],
      aiRecLoading: false,
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
    });

    onMounted(async () => {
      const loadingInstance = ElLoading.service({
        target: "#topBooks2",
        text: "加载中。。。",
      });
      const { data } = await listHomeBooks();
      loadingInstance.close();

      await data.forEach((book) => {
        if (book.type == 0) {
          // 轮播图
          state.sliderContent[state.sliderContent.length] = book;
        }
        if (book.type == 1) {
          // 顶部栏
          state.topBooks1[state.topBooks1.length] = book;
        }
        if (book.type == 2) {
          //本周强推
          state.weekcommend[state.weekcommend.length] = book;
        }
        if (book.type == 3) {
          //热门推荐
          state.hotRecommend[state.hotRecommend.length] = book;
        }
        if (book.type == 4) {
          //精品推荐
          state.goodRecommend[state.goodRecommend.length] = book;
        }
      });

      var $div = $(".scBigImg dl"); //放置大图容器
      var $nav = $(".scSmallImg li"); //放置缩略图容器
      var num = -1;
      var open;

      function changeKv() {
        if (num >= $nav.length - 1) {
          num = 0;
        } else {
          num++;
        }
        $nav.eq(num).trigger("mouseenter");
        open = setTimeout(changeKv, 3000);
      }

      changeKv();
      $nav.each(function (index) {
        $(this)
          .off("mouseenter")
          .on("mouseenter", function () {
            clearTimeout(open);

            $(this).addClass("on").siblings().removeClass("on");
            $(".scBigImg dd")
              .eq(index)
              .addClass("on")
              .siblings()
              .removeClass("on");
            $(".scSmallImg")
              .off("mouseleave")
              .on("mouseleave", function () {
                num = index;
                setTimeout(function () {
                  changeKv();
                }, 3000);
              });
          });
      });
      $div.each(function () {
        $(this)
          .off("mouseenter")
          .on("mouseenter", function () {
            clearTimeout(open);
          });
      });

      // 加载 AI 智能荐书（失败不影响首页其它模块）
      loadAiRecommend();
    });

    const loadAiRecommend = async () => {
      state.aiRecLoading = true;
      try {
        const { data } = await aiRecommend();
        state.aiRecommendBooks = data || [];
      } catch (error) {
        // 错误信息已由 request 拦截器统一提示，首页静默降级
      } finally {
        state.aiRecLoading = false;
      }
    };

    const bookDetail = (bookId) => {
      router.push({ path: `/book/${bookId}` });
    };

    return {
      ...toRefs(state),
      bookDetail,
    };
  }
};
</script>

<style>
/* ------- 首页 · 新中式精修 ------- */
/* 板块标题：宋体 + 左侧朱砂竖条 */
.channelPic .leftBox .title h2,
.channelBanner .rightBox .title h3 {
  font-family: var(--font-serif);
  font-size: 21px;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--ink);
  padding-left: 12px;
  position: relative;
}
.channelPic .leftBox .title h2::before,
.channelBanner .rightBox .title h3::before {
  content: "";
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 18px;
  background: var(--cinnabar);
  border-radius: 1px;
}
.channelPic .leftBox .title,
.channelBanner .rightBox .title {
  border-bottom: 1px solid var(--line);
  margin-bottom: 4px;
}

/* 轮播区热推：朱砂标题 + 宋体 */
.hot_articles .hot_recommend dt a {
  font-family: var(--font-serif);
  letter-spacing: 1px;
}
.hot_articles dl {
  border-bottom: 1px dashed var(--line-deep);
}

/* 封面卡片：细边框 + hover 浮起阴影 */
.scBigImg img,
.itemsList .items_img img {
  border: 1px solid var(--line);
  border-radius: 3px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}
.itemsList .items_img:hover img,
.scBigImg dd a:hover img {
  box-shadow: var(--shadow-lift);
}
.itemsList .items_txt h4 a {
  font-family: var(--font-serif);
}

/* 本周强推展开卡片 */
.channelBanner .rightList .book_intro {
  border: 1px solid var(--line);
  border-radius: 3px;
  background: var(--paper-deep);
}
.channelBanner .rightList li.on .cover img {
  border: 1px solid var(--line);
  border-radius: 2px;
}
.channelBanner .rightList li.on .cover a:hover img {
  box-shadow: var(--shadow-lift);
}

/* AI 荐书 */
.ai_rec_tag {
  float: right;
  color: var(--ink-4);
  font-size: 12px;
  font-weight: normal;
  margin-top: 14px;
  font-family: var(--font-kai);
  letter-spacing: 1px;
}

.ai_reason {
  color: var(--cinnabar);
  font-size: 13px;
  line-height: 1.6;
  margin-top: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-family: var(--font-kai);
}
</style>
