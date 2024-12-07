<script>
export default {
  name: 'NavMenu',
  data() {
    return {
      isPlain: true,
    }
  },
  computed: {
    isOrgRegisterActive() {
      return this.$route.name === 'OrgRegister';
    },
    isMemberRegisterActive() {
      return this.$route.name === 'MemberRegister';
    },
    isLoggedIn() {
      return !!localStorage.getItem('token'); // 检查是否存在 token
    },
    isOrgLoginPage() {
      return this.$route.name === 'OrgLogin';
    },
  },
  methods: {
    goToMemberList() {
      const currentRouteName = this.$route.name;
      if (currentRouteName !== 'MemberList') {
        this.$router.push({name: 'MemberList'})
      }
    },
    goToOrgRegister() {
      const currentRouteName = this.$route.name;
      if (currentRouteName !== 'OrgRegister') {
        this.$router.push({name: 'OrgRegister'})
        this.isPlain = !this.isPlain
      }
    },
    goToMemberRegister() {
      const currentRouteName = this.$route.name;
      if (currentRouteName !== 'MemberRegister') {
        this.$router.push({name: 'MemberRegister'})
        this.isPlain = !this.isPlain
      }
    },
    goToRankList() {
      const currentRouteName = this.$route.name;
      if (currentRouteName !== 'RankList') {
        this.$router.push({name: 'RankList'})
      }
    },
    goToOrgRankList() {
      const currentRouteName = this.$route.name;
      if (currentRouteName !== 'OrgRankList') {
        this.$router.push({name: 'OrgRankList'})
      }
    },
    goToLogin() {
      const currentRouteName = this.$route.name;
      if (currentRouteName !== 'OrgLogin') {
        this.$router.push({name: 'OrgLogin'})
      }
    },
    goToAtomboard() {
      const currentRouteName = this.$route.name;
      if (currentRouteName !== 'Atomboard') {
        this.$router.push({name: 'Atomboard'})
      }
    },
    logout() {
      localStorage.removeItem('token'); // 移除 token
      this.$router.push({name: 'OrgLogin'}); // 跳转到登录页面
    }
  }
}
</script>

<template>
  <el-header>
    <div class="logo">
      <img src="@/assets/headerlogo.png" alt="OpenTalent Logo" class="logo-image" />
      <span class="logo-text">OpenTalent</span>
    </div>
    <el-menu mode="horizontal">
      <el-menu-item v-on:click="goToRankList">成员贡献度排行榜</el-menu-item>
      <el-menu-item v-on:click="goToOrgRankList">高校贡献度排行榜</el-menu-item>
      <el-menu-item v-on:click="goToAtomboard">中国高校开源贡献全景图</el-menu-item>
      <el-menu-item v-if="isLoggedIn" v-on:click="goToMemberList">成员列表</el-menu-item>
    </el-menu>
    <el-button-group>
      <el-button v-if="!isLoggedIn" 
        type="primary"
        :plain="isPlain && !isOrgLoginPage"
        @click="goToLogin">登录</el-button>
      <el-button v-else 
        type="primary" 
        :plain="isPlain && !isOrgLoginPage"
        @click="logout">退出登录</el-button>
      <el-button
        v-if="!isLoggedIn"
        type="primary"
        :plain="isPlain && !isOrgRegisterActive"
        v-on:click="goToOrgRegister"
      >
        组织注册
      </el-button>
      <el-button
        v-if="isLoggedIn"
        type="primary"
        :plain="isPlain && !isMemberRegisterActive"
        v-on:click="goToMemberRegister"
      >
        成员注册
      </el-button>
    </el-button-group>
  </el-header>
</template>

<style scoped>
.el-header {
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.logo {
  display: flex; /* 使 logo 和文字在同一行 */
  align-items: center; /* 垂直居中对齐 */
}
.logo-text {
  font-size: 24px; /* 文字大小 */
  font-weight: bold; /* 文字加粗 */
  color: #33548d; /* 设置文字颜色 */
}
.logo-image {
  height: 40px; /* 根据需要调整图片大小 */
}

.el-menu {
  flex: 1;
  margin-left: 20px;
}

/* 新增样式 */
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