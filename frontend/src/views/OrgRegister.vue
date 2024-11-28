<template>
  <el-container class="org-register">
    <NavMenu></NavMenu>

    <el-main>
      <!-- <el-form ref="form" :model="form" label-width="120px" @submit.prevent="submitForm"> -->
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20" justify="center">
          <el-col :span="16">
            <el-form-item label="组织名称" prop="name">
              <el-input v-model="form.name" placeholder="名称" class="full-width-input"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="组织介绍">
              <el-input v-model="form.link" placeholder="链接" class="full-width-input"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="16">
            <el-form-item label="联系邮箱">
              <el-input v-model="form.contactEmail" placeholder="邮箱" class="full-width-input"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="16">
              <el-form-item label="输入密码" prop="password">
                  <el-input type="password" v-model="form.password" placeholder="密码" class="full-width-input"></el-input>
              </el-form-item>
          </el-col>
          <el-col :span="16">
              <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input type="password" v-model="form.confirmPassword" placeholder="确认密码" class="full-width-input"></el-input>
              </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="项目范围">
              <el-radio-group v-model="form.category1">
                <el-radio label="国家重点发展项目">国家重点发展项目</el-radio>
                <el-radio label="全球影响力排名前10000项目">全球影响力排名前10000项目</el-radio>
                <el-radio label="CCF开源项目分级列表">CCF开源项目分级列表</el-radio>
                <el-radio label="个性化定制">个性化定制</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="平台权重">
              <el-radio-group v-model="form.category2">
                <el-radio label="全球化模式：1:1:1">全球化模式：1:1:1</el-radio>
                <el-radio label="中国优先模式：2:2:1">中国优先模式：2:2:1</el-radio>
                <el-radio label="基金会优先模式：5:3:2">基金会优先模式：5:3:2</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="评价模型">
              <el-radio-group v-model="form.category3">
                <el-radio label="工信部标准院标准：网络模型">工信部标准院标准：网络模型</el-radio>
                <el-radio label="工信部标准院标准：统计模型">工信部标准院标准：统计模型</el-radio>
                <el-radio label="个性化定制">个性化定制</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="数据时间范围" prop="dateRange">
              <el-date-picker
                v-model="form.dateRange"
                type="daterange"
                unlink-panels
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :picker-options="datePickerOptions"
                class="full-width-input"
              ></el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="分析报告">
              <el-radio-group v-model="form.category4">
                <el-radio label="普通版">普通版</el-radio>
                <el-radio label="专业版">专业版</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row justify="center">
          <el-col :span="24" class="button-group">
            <el-button type="primary" plain>返回</el-button>
            <!-- <el-button type="primary" native-type="submit">完成注册</el-button> -->
            <el-button type="primary" v-on:click="submitForm">完成注册</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-main>

    <el-footer>
      <div class="footer-content">
        <img
          src="/images/lab-logo.png"
          alt="Xlab Logo"
          class="footer-logo"
        />
        <p>官方支持</p>
        <p>Copyright © 2024 X-lab</p>
      </div>
    </el-footer>
  </el-container>
</template>

<script>
import axios from 'axios';
import NavMenu from "@/components/NavMenu.vue";

export default {
  components: {NavMenu},
  data() {
    return {
      form: {
        name: "",
        link: "",
        contactEmail: "",
        category1: "国家重点发展项目",
        category2: "全球化模式：1:1:1",
        category3: "工信部标准院标准：网络模型",
        dateRange: [],
        startDate: "",
        endDate: "",
        category4: "普通版",
        password: "",  // 新增
        confirmPassword: ""  // 新增
      },
      rules: {
        name: [
          { required: true, message: '组织名称是必填项', trigger: 'blur' }
        ],
        startDate: [
          { required: true, message: '开始日期是必填项', trigger: 'change' }
        ],
        endDate: [
          { required: true, message: '结束日期是必填项', trigger: 'change' }
        ],
        dateRange: [
          { type: 'array', required: true, message: '请选择数据时间范围', trigger: 'change' }
        ],
        password: [
           { required: true, message: '请输入密码', trigger: 'blur' }
         ],
         confirmPassword: [
           { required: true, message: '请确认密码', trigger: 'blur' },
           { validator: this.validatePassword, trigger: 'blur' }
         ],
      },
      datePickerOptions: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        }
      },
    };
  },
  methods: {
    goToRankList() {
      this.$router.push({ name: "RankList" }); // 确保路由名称为 RankList
    },
    // submitForm() {
    //   // axios.post('/api/org/register', this.form)
    //   // this.$http.post('http://localhost:8081/org/register', this.form)
    //   this.$http.post("/org/register",this.form)
    //     .then(response => {
    //       console.log('注册成功', response.data);
    //       // 可添加成功提示或执行其他操作
    //       this.$message.success('注册成功！');
    //     })
    //     .catch(error => {
    //       console.error('注册失败', error);
    //       // 可添加错误提示或执行其他操作
    //       this.$message.error('注册失败：' + error.message);
    //     });
    // },
    validatePassword(rule, value, callback) {
       if (value !== this.form.password) {
         callback(new Error('两次输入的密码不一致'));
       } else {
         callback();
       }
     },
    submitForm() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.form.startDate = this.form.dateRange[0];
          this.form.endDate = this.form.dateRange[1];
          this.$http.post("/org/register", this.form)
            .then(response => {
              console.log('注册成功', response.data);
              this.$message.success('注册成功！');
              this.$router.push({ name: 'OrgLogin' }); // 注册成功后跳转到登录页面
            })
            .catch(error => {
              console.error('注册失败', error);
              this.$message.error('注册失败：' + error.message);
            });
        } else {
          this.$message.error('请填写所有必填项');
          return false;
        }
      });
    },
  },
};
</script>

<style scoped>
.org-register {
  display: flex;
  flex-direction: column;
  /* height: 100vh; */
}

.el-main {
  flex: 1;
  padding: 70px 90px 50px; /* 顶部和底部填充 */
  /* width: 70%; */
  max-width: 1000px;
  margin: 0 auto; /* 水平居中 */
  /* justify-content: center; */
  /* align-items: center; */
  /* width: 100%; */
  /* max-width: 600px; */
  /* background: #f0f2f5; */
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

.button-group {
  text-align: center;
  margin-top: 20px;
}

.el-button:first-child {
  margin-left: 0;
}

</style>