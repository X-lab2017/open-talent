<template>
  <div>
    <el-header>
      <div class="logo">Logo</div>
      <el-menu mode="horizontal">
        <el-menu-item>排行榜</el-menu-item>
        <el-menu-item>待定</el-menu-item>
        <el-menu-item>待定</el-menu-item>
      </el-menu>
      <el-button-group>
        <el-button type="primary" plain>组织注册</el-button>
        <el-button type="primary">成员注册</el-button>
      </el-button-group>
    </el-header>


    <div class="d1">
      <h3>成员注册</h3>
      <el-form :model="form" label-position="left">
        <el-row :gutter="180">
          <!-- 第一行，三列 -->
          <el-col :span="8">
            <div class="input-label">姓名</div>
            <el-form-item>
              <el-input v-model="form.name" placeholder="名字"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <div class="input-label">国家</div>
            <el-form-item>
              <el-select v-model="form.country" placeholder="选择国家">
                <el-option label="中国" value="China"></el-option>
                <el-option label="美国" value="USA"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <div class="input-label">学校</div>
            <el-form-item>
              <el-input v-model="form.school" placeholder="学校"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="180">
          <!-- 第二行，三列 -->
          <el-col :span="8">
            <div class="input-label">Github账号</div>
            <el-form-item>
              <el-input v-model="form.github" placeholder="账号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <div class="input-label">Gitee账号</div>
            <el-form-item>
              <el-input v-model="form.gitee" placeholder="账号"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <div class="input-label">AtomGit账号</div>
            <el-form-item>
              <el-input v-model="form.atomGit" placeholder="账号"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="180">
          <!-- 第三行，两列（剩下一列空白） -->
          <el-col :span="8">
            <div class="input-label">联系邮箱</div>
            <el-form-item>
              <el-input v-model="form.email" placeholder="邮箱"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <div class="input-label">通讯地址（可选）</div>
            <el-form-item>
              <el-input v-model="form.address" placeholder="地址"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <!-- 可以选择添加其他表单项或者留空 -->
          </el-col>
        </el-row>
      </el-form>

      <h3>批量注册</h3>
      <div>
        <el-button icon="el-icon-upload2" type="primary" plain>批量导入</el-button>
        <el-button icon="el-icon-download" type="primary" plain>模板下载</el-button>
      </div>


      <div style="margin:40px 0px ">
        <el-button type="primary" >返回</el-button>
        <el-button type="primary" plain>完成注册</el-button>
      </div>



    </div>

    <el-footer>
      <div class="footer-content">
        <img
            src="/images/lab-logo.png"
            alt="Xlab Logo"
            class="footer-logo"
        />        <p>官方支持</p >
        <p>Copyright © 2024 X-lab</p >
      </div>
    </el-footer>
  </div>


</template>

<script>
export default {
  data() {
    return {
      form: {
        name: '',
        github: '',
        email: '',
        country: '',
        gitee: '',
        address: '',
        school: '',
        atomGit: '',
      },
    };
  },
};
</script>

<style scoped>
.el-select{
  display: block;
}
.input-label{
  text-align: left; /* 显式设置文本靠左，虽然这是默认行为 */
  margin-top: 15px; /* 上边距 */
  margin-bottom: 8px; /* 下边距 */
  margin-left: 2px; /* 左边距 */
  font-size: 14px;
}
.d1{
  margin: 0px 10%;
}
.el-row{
  margin-bottom: 20px;
}

.el-button--primary {
  color: #FFF;
  background-color: #131313;
  border-color: #CDCDCD;
}
.el-button--primary:focus, .el-button--primary:hover {
  background: #E8E8E8;
  border-color: #131313;
  color: #131313;
}

.el-button--primary.is-plain {
  color: #5F5F5F;
  background: #FFF;
  border-color: #CDCDCD;
}

.el-button--primary.is-plain:focus, .el-button--primary.is-plain:hover {
  background: #131313;
  border-color: #131313;
  color: #FFF;
}

.el-header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.logo {
  font-size: 24px;
  font-weight: bold;
}

.el-menu {
  flex: 1;
  margin-left: 20px;
}
.el-footer {
  text-align: center;
  padding: 30px 0;
  background: #fff;
  border-top: 1px solid #eaeaea;
}
.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.footer-logo {
  width: 50px;
  height: 50px;
  margin-bottom: 10px;
}
</style>
