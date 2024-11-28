<template>
  <el-container class="login">
    <NavMenu></NavMenu>
    <!-- 添加导航栏 -->
    <el-main>
      <el-form :model="form" :rules="rules" ref="form" label-width="120px">
        <el-form-item label="组织名称" prop="name">
          <el-input v-model="form.name" placeholder="组织名称"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            type="password"
            v-model="form.password"
            placeholder="密码"
          ></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submitForm">登录</el-button>
        </el-form-item>
      </el-form>
    </el-main>
  </el-container>
</template>

<script>
import NavMenu from "@/components/NavMenu.vue";

export default {
  components: { NavMenu },
  data() {
    return {
      form: {
        name: "",
        password: "",
      },
      rules: {
        name: [{ required: true, message: "请输入组织名称", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
      },
    };
  },
  methods: {
    submitForm() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.$http
            .post("/org/login", this.form)
            .then((response) => {
              if (response.data.code === "200") { // 确保响应码为成功
                const { token, name } = response.data.data;
                localStorage.setItem("token", token);
                localStorage.setItem("orgName", name); // 保存组织名
                this.$router.push({ name: "MemberList" });
              } else {
                this.$message.error("登录失败：" + response.data.msg);
              }
            })
            .catch((error) => {
              this.$message.error("登录失败：" + error.message);
            });
        }
      });
    },
  },
};
</script>
<style scoped>
.login {
  display: flex;
  flex-direction: column; /* 垂直排列 */
  height: 100vh; /* 占满整个视口高度 */
}

.el-main {
  flex: 1;
  padding: 50px;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.el-form {
  width: 100%;
  max-width: 400px; /* 限制表单最大宽度 */
}

.el-button--primary {
  color: #fff;
  background-color: #131313; 
  border-color: #131313;
}

.el-button--primary:focus,
.el-button--primary:hover {
  background: #131313; 
  border-color: #131313;
  color: #fff;
}
</style>