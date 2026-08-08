<template>
  <div class="header">
    <div class="mainNav" id="mainNav">
      <div class="box_center cf author_bar">
        <img class="author_logo" :src="logoWhite" alt="笔阁" />
        <span class="author_title">笔阁作家管理</span>
      </div>
    </div>
  </div>
</template>

<script>
import logoWhite from "@/assets/images/logo-white.svg";
export default {
  name: "AuthorHeader",
  setup() {
    return { logoWhite };
  },
};
</script>

<style scoped>
.author_bar {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 50px;
}
.author_logo {
  height: 32px;
  width: auto;
  margin-right: 10px;
}
.author_title {
  color: #f7f3ec;
  font-family: var(--font-serif);
  font-size: 17px;
  letter-spacing: 4px;
}
</style>
