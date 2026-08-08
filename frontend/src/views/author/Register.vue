<template>
  <div class="reg_wrap">
    <div id="main">
      <table
        width="100%"
        border="0"
        cellpadding="8"
        cellspacing="0"
        class="tableBasic"
        style="line-height: 40px"
      >
        <tbody>
          <tr>
            <td colspan="3" style="text-align: left">
              <div class="reg_title">
                我是网络小说写手，我要注册为笔阁签约作者：
              </div>
            </td>
          </tr>

          <tr>
            <td align="right">作者笔名：</td>
            <td>
              <input
                v-model="penName"
                name="penName"
                
                type="text"
                maxlength="8"
                id="TxtNiceName"
                class="
                  easyui-validatebox
                  inpMain
                  validatebox-text validatebox-invalid
                "
                data-options="required:true"
                validtype="checkPenName"
              />
            </td>
            <td><span class="notes"> *</span> 长度为2到8位的中英文</td>
          </tr>
          <tr>
            <td align="right">手机号码：</td>
            <td>
              <input
              v-model="telPhone"
                name="telPhone"
                
                type="text"
                id="TxtMobile"
                class="
                  easyui-validatebox
                  inpMain
                  validatebox-text validatebox-invalid
                "
                data-options="required:true"
                validtype="chinaMobile"
              />
            </td>
            <td>
              <span class="notes"> *</span>
              笔阁的编辑会通过这个号码与您联系
            </td>
          </tr>
          <tr>
            <td align="right">QQ或微信：</td>
            <td>
              <input
              v-model="chatAccount"
                name="chatAccount"
                
                type="text"
                id="TxtQQ"
                class="
                  easyui-validatebox
                  inpMain
                  validatebox-text validatebox-invalid
                "
                data-options="required:true"
              />
            </td>
            <td>
              <span class="notes"> *</span>
              笔阁的编辑会通过这个号码与您联系
            </td>
          </tr>
          <tr>
            <td align="right">电子邮箱：</td>
            <td>
              <input
              v-model="email"
                name="email"
                
                type="text"
                id="TxtEmail"
                class="
                  easyui-validatebox
                  inpMain
                  w300
                  validatebox-text validatebox-invalid
                "
                data-options="required:true"
                validtype="email"
                title=""
              />
            </td>
            <td><span class="notes"> *</span> 长度为2到15位的中英文，数字</td>
          </tr>
          <tr>
            <td align="right">男女主角：</td>
            <td>
              <div>
                <ul class="ipage">
                  <input v-model="workDirection" type="radio" name="workDirection" value="0" />男频
                  <input v-model="workDirection" type="radio" name="workDirection" value="1" />女频
                </ul>
              </div>
            </td>
            <td><span class="notes"> *</span> 请选择作品方向</td>
          </tr>
        </tbody>
      </table>
    </div>
    <table
      width="100%"
      border="0"
      cellpadding="8"
      cellspacing="0"
      class="tableBasic"
    >
      <tbody>
        <tr>
          <td>
            <div
              style="padding-left: 110px; margin-top: 10px; margin-bottom: 5px"
            >
              <input
                @click="registerAuthor"
                type="button"
                name="btnSubmit"
                value="立即开始您的作者生涯"
                id="btnSubmit"
                class="btn"
              />
            </div>
          </td>
        </tr>
        <tr>
          <td>
            <span id="LabErr"></span>
          </td>
        </tr>
      </tbody>
    </table>
    <input type="hidden" name="HidUId" id="HidUId" value="1095" />
  </div>
</template>

<script>
import { reactive, toRefs, onMounted, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { getImgVerifyCode } from "@/api/resource";
import { register } from "@/api/author";
import { setToken, setNickName, setUid } from "@/utils/auth";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";
export default {
  name: "authorRegister",
  components: {
    Header,
    Footer,
  },
  setup() {
    const route = useRoute();
    const router = useRouter();

    const state = reactive({
      penName: "",
      telPhone: "",
      chatAccount: "",
      email: "",
      workDirection: "0",
    });

    const registerAuthor = async () => {
      if (!state.penName) {
        ElMessage.error("笔名不能为空！");
        return;
      }
      if (!state.telPhone) {
        ElMessage.error("手机号不能为空！");
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(state.telPhone)) {
        ElMessage.error("手机号格式不正确！");
        return;
      }
      if (!state.chatAccount) {
        ElMessage.error("QQ或微信账号不能为空！");
        return;
      }
      if (!state.email) {
        ElMessage.error("电子邮箱不能为空！");
        return;
      }
      if (
        !/^[A-Za-z0-9\-_]+[A-Za-z0-9\.\-_]*[A-Za-z0-9\-_]+@[A-Za-z0-9]+[A-Za-z0-9\.\-_]*(\.[A-Za-z0-9\.\-_]+)*[A-Za-z0-9]+\.[A-Za-z0-9]+[A-Za-z0-9\.\-_]*[A-Za-z0-9]+$/.test(
          state.email
        )
      ) {
        ElMessage.error("电子邮箱格式不正确！");
        return;
      }
      
      const { data } = await register(state);

      router.push({ name: "authorBookList" });
    };

    return {
      ...toRefs(state),
      registerAuthor,
    };
  },
};
</script>


<style scoped>
.reg_wrap {
    width: 800px;
    margin: 80px auto 60px;
    background: var(--card);
    border: 1px solid var(--line);
    border-radius: 4px;
    box-shadow: var(--shadow-soft);
    padding: 36px 40px 30px;
}
.reg_title {
    position: relative;
    padding-left: 18px;
    height: 40px;
    line-height: 40px;
    font-family: var(--font-serif);
    font-size: 18px;
    color: var(--ink);
    letter-spacing: 1px;
}
.reg_title::before {
    content: "";
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 8px;
    height: 8px;
    background: var(--cinnabar);
}
#main {
    border: 1px solid var(--line);
    border-radius: 3px;
    background: var(--paper-deep);
    padding: 12px 0;
}
.btn {
    display: inline-block;
    background: var(--cinnabar);
    border: 1px solid var(--cinnabar);
    border-radius: 2px;
    color: #FFFDF8;
    font-family: var(--font-serif);
    font-size: 15px;
    letter-spacing: 2px;
    line-height: 1;
    padding: 12px 34px;
    cursor: pointer;
    transition: background .25s, border-color .25s;
}
.btn:hover {
    background: var(--cinnabar-deep);
    border-color: var(--cinnabar-deep);
}
.inpMain {
    width: 220px;
    border: 1px solid var(--line-deep);
    background: var(--card);
    padding: 6px 10px;
    color: var(--ink);
    font-size: 13px;
    line-height: 20px;
    border-radius: 2px;
    transition: border-color .25s, box-shadow .25s;
    -webkit-appearance: none;
}
.inpMain:focus {
    border-color: var(--cinnabar);
    box-shadow: 0 0 0 3px var(--cinnabar-fade);
}
.inpMain.w300 {
    width: 300px;
}
.ipage input {
    margin-right: 6px;
    accent-color: var(--cinnabar);
}
/*- tableBasic -*/
.tableBasic {
    background: transparent;
    border: none;
}
.tableBasic td, .tableBasic th {
    border: none;
    padding: 4px 8px;
    color: var(--ink-2);
    font-size: 13px;
}
.tableBasic th {
    text-align: right;
    width: 120px;
    font-weight: normal;
}
.notes { color: var(--cinnabar); line-height: 20px; }
</style>