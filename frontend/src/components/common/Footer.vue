<template>
  <div class="footer">
    <div class="box_center cf">
      <div class="copyright">
        <ul>
          <li class="menu">
            <router-link :to="{ name: 'home' }">首页</router-link>
            <i class="line">|</i>
            <a @click="goFeedBack" href="javascript:void(0)">意见反馈</a>
          </li>
          <li>笔阁版权所有 · Copyright (C) bige.com All rights reserved</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from "vue-router";
import { getToken } from "@/utils/auth";
export default {
  name: "Footer",
  setup() {
    const router = useRouter();
    const goFeedBack = () => {
      if (!getToken()) {
        router.push({ name: "login" });
      } else {
        router.push({ name: "feadback" });
      }
    };
    return {
      goFeedBack,
    };
  },
};
</script>
