<template>
  <div id="bookrank2_ShowBookRank">
    <div class="rightBox">
      <div class="title cf">
        <h3 class="on">新书榜单</h3>
      </div>
      <div class="rightList">
        <ul id="newRankBooks">
          <li
            v-for="(item, index) in books"
            :key="index"
            :class="['num' + (Number(`${index}`) + 1), { on: index == 0 }]"
          >
            <div class="book_name">
              <i>{{ index + 1 }}</i
              ><a
                class="name"
                href="javascript:void(0)"
                @click="bookDetail(item.id)"
                >{{ item.bookName }}</a
              >
            </div>
            <div class="book_intro">
              <div class="cover">
                <a href="javascript:void(0)" @click="bookDetail(item.id)"
                  ><img
                    :src="`${imgBaseUrl}` + `${item.picUrl}`"
                    :alt="item.bookName"
                    onerror="this.src='default.gif';this.onerror=null"
                /></a>
              </div>
              <a
                class="txt"
                href="javascript:void(0)"
                @click="bookDetail(item.id)"
                v-html="item.bookDesc"
              ></a>
            </div>
          </li>
        </ul>
        <div class="more">
          <router-link :to="{ name: 'bookRank' }">查看更多&gt;</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { listNewestRankBooks } from "@/api/book";
export default {
  name: "BookNewestRank",
  setup() {
    const route = useRoute();
    const router = useRouter();
    const state = reactive({
      books: [],
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
    });

    onMounted(async () => {
      const { data } = await listNewestRankBooks();

      await data.forEach((book) => {
        if (state.books.length < 10) {
          state.books[state.books.length] = book;
        }
      });
    });
    const bookDetail = (bookId) => {
      router.push({ path: `/book/${bookId}` });
    };
    return {
      ...toRefs(state),
      bookDetail,
    };
  },
};
</script>

<style>
/* ------- 新书榜单 · 新中式精修 ------- */
#bookrank2_ShowBookRank .title h3 {
  font-family: var(--font-serif);
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 2px;
  color: var(--ink);
  padding-left: 12px;
  position: relative;
}
#bookrank2_ShowBookRank .title h3::before {
  content: "";
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 17px;
  background: var(--cinnabar);
  border-radius: 1px;
}
#bookrank2_ShowBookRank .book_name .name:hover {
  color: var(--cinnabar);
}
#bookrank2_ShowBookRank .book_intro {
  border: 1px solid var(--line);
  border-radius: 3px;
  background: var(--paper-deep);
  padding: 10px;
}
#bookrank2_ShowBookRank li.on .cover img {
  border: 1px solid var(--line);
  border-radius: 2px;
  transition: box-shadow 0.3s ease;
}
#bookrank2_ShowBookRank li.on .cover a:hover img {
  box-shadow: var(--shadow-lift);
}
#bookrank2_ShowBookRank .rightList .more {
  border-radius: 3px;
  transition: background 0.25s;
}
#bookrank2_ShowBookRank .rightList .more a:hover {
  color: var(--cinnabar);
}
</style>