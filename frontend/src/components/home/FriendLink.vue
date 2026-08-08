<template>
  <div class="friend_link">
    <div class="box_center cf" id="friendLink">
      <span>友情链接：</span>
      <a
        v-for="(item, index) in friendLinks"
        :key="index"
        target="_blank"
        :href="item.linkUrl"
        >{{ item.linkName }}
      </a>
    </div>
  </div>
</template>

<script>
import { reactive, toRefs, onMounted } from "vue";
import { listHomeFriendLinks } from "@/api/home";
export default {
  name: "FriendLink",
  setup() {
    const state = reactive({
      friendLinks: [],
    });

    onMounted(async () => {
      const { data } = await listHomeFriendLinks();
      state.friendLinks = data;
    });

    return {
      ...toRefs(state),
    };
  },
};
</script>

<style>
/* ------- 友情链接 · 新中式精修 ------- */
.friend_link {
  padding: 18px 0 6px;
  border-top: 1px solid var(--line);
  margin-top: 8px;
}
.friend_link span {
  font-family: var(--font-serif);
  font-size: 14px;
  letter-spacing: 2px;
  color: var(--ink);
}
.friend_link a {
  color: var(--ink-3);
  font-size: 13px;
  padding: 2px 4px;
  border-bottom: 1px solid transparent;
  transition: color 0.25s, border-color 0.25s;
}
.friend_link a:hover {
  color: var(--cinnabar);
  border-bottom-color: var(--cinnabar-soft);
}
</style>