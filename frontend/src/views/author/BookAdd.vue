<template>
  <AuthorHeader />
  <div class="main box_center cf">
    <div class="userBox cf">
      <div class="my_l">
        <ul class="log_list">
          <li>            <router-link class="link_4 on" :to="{'name':'authorBookList'}">小说管理</router-link>
</li>
          <!--<li><a class="link_1 " href="/user/userinfo.html">批量小说爬取</a></li>
<li><a class="link_4 " href="/user/favorites.html">单本小说爬取</a></li>-->
        </ul>
      </div>
      <div class="my_r">
        <div class="my_bookshelf">
          <div class="userBox cf">
            <form method="post" action="./register.html" id="form2">
              <div class="user_l">
                <div></div>
                <h3>小说基本信息填写</h3>
                <ul class="log_list">
                  <li><span id="LabErr"></span></li>
                  <b>作品方向：</b>
                  <li>
                    <select
                    v-model="book.workDirection"
                      class="s_input"
                      id="workDirection"
                      name="workDirection"
                      @change="loadCategoryList()"
                    >
                      <option value="0">男频</option>
                      <option value="1">女频</option>
                    </select>
                  </li>
                  <b>分类：</b>
                  <li>
                    <select class="s_input" id="catId" name="catId" v-model="book.categoryId" @change="categoryChange">
                      <option :value="item.id" v-for="(item,index) in bookCategorys" :key="index">{{item.name}}</option>
                      
                    </select>
                  </li>
                  <input
                    type="hidden"
                    id="catName"
                    name="catName"
                    value="玄幻奇幻"
                  />
                  <b>小说名：</b>
                  <li>
                    <input
                      v-model="book.bookName"
                      type="text"
                      id="bookName"
                      name="bookName"
                      class="s_input"
                    />
                  </li>
                  <b>小说封面：</b>
                  <li style="position: relative">
                    <el-upload
                      class="avatar-uploader"
                      :action="baseUrl + '/front/resource/image'"
                      :show-file-list="false"
                      :on-success="handleAvatarSuccess"
                      :before-upload="beforeAvatarUpload"
                    >
                      <img
                        :src="
                          book.picUrl ? imgBaseUrl + book.picUrl : picUpload
                        "
                        class="avatar"
                      />
                    </el-upload>
                  </li>
                  <b>小说介绍：</b>

                  <li>
                    <textarea
                      v-model="book.bookDesc"
                      name="bookDesc"
                      rows="5"
                      cols="53"
                      id="bookDesc"
                      class="textarea"
                    ></textarea>
                  </li>

                  <li>
                    <input
                      type="button"
                      @click="saveBook"
                      name="btnRegister"
                      value="提交"
                      id="btnRegister"
                      class="btn_red"
                    />
                  </li>
                </ul>
              </div>
            </form>
          </div>
          <!--<div id="divData" class="updateTable">
                    <table cellpadding="0" cellspacing="0">
                        <thead>
                        <tr>

                            <th class="name">
                                爬虫源（已开启的爬虫源）
                            </th>
                            <th class="chapter">
                                成功爬取数量（websocket实现）
                            </th>
                            <th class="time">
                            目标爬取数量
                            </th>
                            <th class="goread">
                                状态（正在运行，已停止）（一次只能运行一个爬虫源）
                            </th>
                            <th class="goread">
                                操作（启动，停止）
                            </th>
                        </tr>
                        </thead>
                        <tbody id="bookShelfList">



                        </tbody>
                    </table>
                    <div class="pageBox cf" id="shellPage">
                    </div>
                </div>-->
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import "@/assets/styles/book.css";
import { reactive, toRefs, onMounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { publishBook } from "@/api/author";
import { listCategorys } from "@/api/book";
import AuthorHeader from "@/components/author/Header.vue";
import picUpload from "@/assets/images/pic_upload.png";
export default {
  name: "authorBookAdd",
  components: {
    AuthorHeader,
  },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const state = reactive({
      book: {'workDirection' : 0,'isVip':0},
      bookCategorys: [],
      baseUrl: process.env.VUE_APP_BASE_API_URL,
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
    });

    onMounted(() => {
      loadCategoryList()
    })

    const beforeAvatarUpload = (rawFile) => {
      if (rawFile.type !== "image/jpeg") {
        ElMessage.error("必须上传 JPG 格式的图片!");
        return false;
      } else if (rawFile.size / 1024 / 1024 > 5) {
        ElMessage.error("图片大小最多 5MB!");
        return false;
      }
      return true;
    };

    const handleAvatarSuccess = (response, uploadFile) => {
      state.book.picUrl = response.data;
    };

    const loadCategoryList = async () => {
      const { data } = await listCategorys({ workDirection: state.book.workDirection });
      state.book.categoryId = data[0].id
      state.book.categoryName = data[0].name
      state.bookCategorys = data;
    };

    const categoryChange = async (event) => {
      console.log("categoryChange======",event.target.value)
     state.bookCategorys.forEach((category)=>{
        if(category.id == event.target.value){
          state.book.categoryName = category.name
          return
        }
      });
    }

    const saveBook = async () => {
      console.log("sate=========",state.book)
      if (!state.book.bookName) {
        ElMessage.error("书名不能为空！");
        return;
      }
      if (!state.book.picUrl) {
        ElMessage.error("封面不能为空！");
        return;
      }
      if (!state.book.bookDesc) {
        ElMessage.error("简介不能为空！");
        return;
      }
      await publishBook(state.book)
      router.push({'name':'authorBookList'})
    }

    return {
      ...toRefs(state),
      picUpload,
      beforeAvatarUpload,
      handleAvatarSuccess,
      loadCategoryList,
      categoryChange,
      saveBook
    };
  },
};
</script>

<style>
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

<style scoped>
.redBtn {
  display: inline-block;
  padding: 7px 18px;
  border-radius: 2px;
  border: 1px solid var(--cinnabar);
  background: var(--cinnabar);
  color: #FFFDF8;
  font-size: 13px;
  transition: background 0.25s, border-color 0.25s;
}
a.redBtn:hover {
  background: var(--cinnabar-deep);
  border-color: var(--cinnabar-deep);
  color: #FFFDF8;
}

.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--line-deep);
  border-radius: 3px;
  background: var(--paper);
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--cinnabar);
  box-shadow: 0 0 0 3px var(--cinnabar-fade);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

.updateTable .style a {
  color: var(--ink-3);
}
.updateTable .author a {
  color: var(--ink-3);
  cursor: text;
}
.bind,
.updateTable .style a:hover {
  color: var(--cinnabar);
}
.userBox {
  /*width: 998px; border: 1px solid var(--line);*/
  margin: 0 auto 50px;
  background: var(--card);
  border: 1px solid var(--line);
  border-radius: 4px;
  box-shadow: var(--shadow-soft);
}
.channelViewhistory .userBox {
  margin: 0 auto;
}
.user_l {
  width: 420px;
  float: none;
  margin: 0 auto;
  padding: 36px 0 44px;
}
.user_l h3 {
  font-family: var(--font-serif);
  font-size: 22px;
  font-weight: normal;
  line-height: 1;
  text-align: center;
  color: var(--ink);
  letter-spacing: 4px;
  margin-bottom: 28px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--line);
}
.user_l h3::before {
  content: "";
  display: inline-block;
  width: 8px;
  height: 8px;
  background: var(--cinnabar);
  margin-right: 10px;
  vertical-align: 3px;
}
.user_l b {
  display: block;
  margin: 14px 0 6px;
  font-family: var(--font-serif);
  font-size: 14px;
  font-weight: normal;
  color: var(--ink);
  letter-spacing: 1px;
}
.user_l #LabErr {
  color: var(--cinnabar-deep);
  display: block;
  height: 40px;
  line-height: 40px;
  text-align: center;
  font-size: 14px;
}
.user_l .log_list {
  width: 100%;
}
.user_l .s_input {
  width: 100%;
  margin-bottom: 18px;
  font-size: 14px;
}
.s_input {
  box-sizing: border-box;
  width: 348px;
  height: 38px;
  line-height: 38px\9;
  vertical-align: middle;
  border: 1px solid var(--line-deep);
  border-radius: 2px;
  background: var(--card);
  color: var(--ink);
  padding: 0 12px;
  transition: border-color 0.25s, box-shadow 0.25s;
}
.s_input:focus {
  border-color: var(--cinnabar);
  box-shadow: 0 0 0 3px var(--cinnabar-fade);
}
.textarea:focus {
  border-color: var(--cinnabar);
  box-shadow: 0 0 0 3px var(--cinnabar-fade);
}
.icon_name,
.icon_key,
.icon_code {
  width: 312px;
  padding-left: 36px;
}
.icon_key {
  background-position: 13px -51px;
}
.icon_code {
  background-position: 13px -117px;
  width: 200px;
  float: left;
}
.code_pic {
  height: 38px;
  float: right;
}
.btn_phone {
  height: 40px;
  width: 100px;
  float: right;
  cursor: pointer;
  padding: 0;
  text-align: center;
  border-radius: 2px;
  background: #dfdfdf;
}
.log_code {
  *padding-bottom: 25px;
}
.user_l .btn_red {
  width: 100%;
  font-family: var(--font-serif);
  font-size: 17px;
  letter-spacing: 6px;
  padding: 12px;
}
.autologin {
  color: var(--ink-3);
  line-height: 1;
  margin-bottom: 18px;
}
.autologin em {
  vertical-align: 2px;
  margin-left: 4px;
}
.user_r {
  width: 259px;
  margin: 80px 0;
  padding: 20px 70px;
  border-left: 1px dotted var(--line);
  float: right;
  text-align: center;
}
.user_r .tit {
  font-size: 16px;
  line-height: 1;
  padding: 6px 0 25px;
}
.user_r .btn_ora {
  padding: 10px 34px;
}
.fast_login {
  padding: 60px 0 0;
}
.fast_list {
  text-align: center;
  padding: 0.5rem;
}
.fast_list li {
  display: inline-block;
  *display: inline;
  zoom: 1;
}
.fast_list li .img {
  width: 48px;
  height: 48px;
  margin: 20px 0 5px;
}
.fast_list li a:hover {
  opacity: 0.8;
  filter: alpha(opacity=80);
  -moz-opacity: 0.8;
}
.fast_list li span {
  display: block;
}
.fast_list .login_qq {
  margin: 0 42px;
}
.fast_list .login_wb a {
  color: #f55c5b;
}
.fast_list .login_qq a {
  color: #51b7ff;
}
.fast_list .login_wx a {
  color: #66d65e;
}
.fast_tit {
  position: relative;
  overflow: hidden;
}
.fast_tit .lines {
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  height: 1px;
  line-height: 1;
  background: var(--line);
}
.fast_tit .title {
  background: var(--card);
  font-size: 16px;
  padding: 3px 14px;
  position: relative;
  display: inline-block;
  z-index: 999;
}
/*userinfo*/
.my_l {
  width: 198px;
  float: left;
  font-size: 13px;
  padding-top: 20px;
}
.my_l li a {
  display: block;
  height: 42px;
  line-height: 42px;
  padding-left: 62px;
  border-left: 4px solid transparent;
  margin-bottom: 5px;
  color: var(--ink-2);
  transition: color 0.25s, background 0.25s;
}
.my_l li a:hover {
  color: var(--cinnabar);
}
.my_l li .on {
  background-color: var(--paper-deep);
  border-left: 2px solid var(--cinnabar);
  color: var(--ink);
  border-radius: 0 2px 2px 0;
}
.my_l .link_1 {
  background-position: 32px -188px;
}
.my_l .link_2 {
  background-position: 32px -230px;
}
.my_l .link_3 {
  background-position: 32px -272px;
}
.my_l .link_4 {
  background-position: 32px -314px;
}
.my_l .link_5 {
  background-position: 32px -356px;
}
.my_l .link_6 {
  background-position: 32px -397px;
}
.my_l .link_7 {
  background-position: 32px -440px;
}
.my_l .link_8 {
  background-position: 32px -481px;
}
.my_r {
  width: 739px;
  padding: 0 30px 30px;
  float: right;
  border-left: 1px solid var(--line);
  min-height: 470px;
}
.my_info {
  padding: 30px 0 5px;
}
.user_big_head {
  /*width:110px; height:110px; padding:4px; border:1px solid var(--line);*/
  margin-right: 30px;
  float: left;
  width: 80px;
  height: 80px;
  border-radius: 50%;
}
.my_r .my_name {
  font-size: 18px;
  line-height: 1;
  padding: 5px 0 12px 0;
}
.my_r .s_input {
  width: 318px;
  padding: 0 10px;
}
.my_list li {
  line-height: 28px;
}
.my_list li i,
.my_list li em.red {
  margin-right: 6px;
}
.my_list .binded {
  color: var(--ink-3);
  margin-left: 6px;
}
.my_list .btn_link {
  margin-left: 12px;
}
.mytab_list li {
  line-height: 30px;
  padding: 10px 0;
  font-size: 14px;
}
.mytab_list li .tit {
  width: 70px;
  color: #aaa;
  text-align: right;
  display: inline-block;
  margin-right: 18px;
}
.mytab_list .user_img {
  width: 48px;
  height: 48px;
  vertical-align: middle;
  border-radius: 50%;
}
.my_bookshelf .title {
  padding: 20px 0 15px;
  line-height: 30px;
}
.my_bookshelf h4 {
  font-size: 14px;
  color: var(--ink-2);
}
.my_bookshelf h2 {
  font-size: 18px;
  font-weight: normal;
}
.updateTable {
  width: 739px;
  color: var(--ink-3);
}
.updateTable table {
  width: 100%;
  margin-bottom: 14px;
}
.updateTable th,
.updateTable td {
  height: 40px;
  line-height: 40px;
  vertical-align: middle;
  padding-left: 6px;
  font-weight: normal;
  text-align: left;
}
.updateTable th {
  background: var(--paper-deep);
  color: var(--ink);
  border-top: 1px solid var(--line);
}
.updateTable td {
  height: 40px;
  line-height: 40px;
}
.updateTable .style {
  width: 80px;
  padding-left: 10px;
}
.updateTable .name {
  width: 178px;
  padding-right: 10px;
}
.updateTable .name a,
.updateTable .chapter a {
  max-width: 168px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.updateTable .chapter {
  padding-right: 5px;
}
.updateTable .chapter a {
  max-width: 220px;
  float: left;
}
.updateTable .author {
  width: 72px;
  text-align: left;
}
.updateTable .goread {
  width: 80px;
  text-align: center;
}
.updateTable .time {
  width: 86px;
}
.updateTable .word {
  width: 64px;
  padding-right: 10px;
  text-align: right;
}
.updateTable .rank {
  width: 30px;
  padding-right: 10px;
  text-align: center;
}
.updateTable .name a,
.updateTable .chapter a,
.updateTable .author a {
  height: 40px;
  line-height: 40px;
  display: inline-block;
  overflow: hidden;
}
.updateTable tr:nth-child(2n) td {
  background: var(--paper-deep);
}
.dataTable {
  width: 739px;
}
.dataTable table {
  width: 100%;
  margin-bottom: 14px;
  border-collapse: collapse;
}
.dataTable th,
.dataTable td {
  height: 40px;
  line-height: 40px;
  vertical-align: middle;
  padding: 0 10px;
  font-weight: normal;
  text-align: center;
  border: 1px solid var(--line);
}
.dataTable th {
  background: var(--paper-deep);
}
.nodate {
  border-top: 1px solid var(--line);
  padding: 60px 0;
}
.viewhistoryBox {
  /*padding: 0 30px 30px; */
  padding: 0 20px 10px;
}
.viewhistoryBox .updateTable {
  width: 100%;
}
/*.btn_gray, .btn_red, .btn_ora { font-size:14px; padding:8px 28px }*/
.book_tit {
  height: 48px;
  line-height: 48px;
  margin: 0 14px;
  border-bottom: 1px solid var(--line);
  overflow: hidden;
}
.book_tit .fl {
  font-size: 14px;
  color: var(--ink-3);
}
.book_tit .fl h3 {
  font-size: 18px;
  color: var(--ink);
  font-weight: normal;
  margin-right: 5px;
  display: inline;
}
.book_tit .fr {
  font-size: 14px;
}

.commentBar,
.feedback_list {
  border-top: 1px solid var(--line);
  margin-bottom: 15px;
}
/*.comment_list { padding: 16px 0; border-bottom: 1px solid var(--line) }
.comment_list .user_head { width:54px; height:54px; border-radius:50%; float: left; margin-right: 14px }
.comment_list .li_1 { overflow: hidden }
.comment_list .user_name { color: var(--cinnabar) }
.comment_list .li_2 { padding:3px 0; color:var(--ink-3) }
.comment_list .li_3, .comment_list .li_4 { margin-left:68px }
.comment_list .reply { padding-left: 12px }
.comment_list .num { color: var(--cinnabar); margin: 0 3px }
.comment_list .li_4 { line-height:34px; padding-top:8px; margin-top:15px; border-top:1px solid var(--line) }
.comment_list .li_4 .more { background:var(--paper-deep); border-radius:2px; color:var(--cinnabar); text-align:center }*/
.no_contet {
  padding: 190px 0 40px;
  text-align: center;
  color: var(--ink-3);
  border-top: 1px solid var(--line);
}

.comment_list {
  padding: 20px 0;
  border-bottom: 1px solid var(--line);
}
.comment_list:last-child {
  border: none;
}
.comment_list .user_heads {
  /*width: 54px; height: 54px; float: left;*/
  position: relative;
  margin-right: 20px;
}
.comment_list .user_head {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: var(--paper-deep);
}
.comment_list .user_heads span {
  display: block;
  margin: 0;
  position: absolute;
  left: 12px;
  bottom: 0;
}
.comment_list ul {
  /*width: 640px;*/
  width: 660px;
}
.comment_list .li_0 {
  font-family: "宋体";
}
.comment_list .li_0 strong {
  font-size: 14px;
  color: var(--cinnabar);
}
.comment_list .li_1 {
  overflow: hidden;
}
.comment_list .user_name {
  color: var(--cinnabar);
}
.comment_list .li_2 {
  padding: 6px 0;
}
.comment_list .li_3 {
  color: var(--ink-3);
}
.comment_list .reply {
  padding-left: 12px;
}
.comment_list .num {
  color: var(--cinnabar);
  margin: 0 3px;
}
.comment_list .li_4 {
  line-height: 34px;
  padding-top: 8px;
  margin-top: 15px;
  border-top: 1px solid var(--line);
}
.pl_bar li {
  display: block;
}
.pl_bar .name {
  color: var(--ink-2);
  padding-top: 2px;
  font-size: 14px;
}
.pl_bar .dec {
  font-size: 14px;
  line-height: 1.8;
  padding: 12px 0;
}
.pl_bar .other {
  line-height: 24px;
  color: var(--ink-3);
  font-size: 13px;
}
.pl_bar .other a {
  display: inline-block;
  color: var(--ink-3);
}
.pl_bar .reply {
  padding-left: 22px;
}
/*.no_comment { padding: 70px 14px 115px; color: #CCCCCC; text-align: center; font-size: 14px; }*/
.reply_bar {
  background: var(--paper-deep);
  border: 1px solid var(--line);
  border-radius: 6px;
  padding: 10px;
  line-height: 1.8;
}
</style>
