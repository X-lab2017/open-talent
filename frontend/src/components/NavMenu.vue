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
    }
  },
  methods: {
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
  }
}
</script>

<template>
  <el-header>
    <div class="logo">OpenTalent</div>
    <el-menu mode="horizontal">
      <el-menu-item v-on:click="goToRankList">排行榜</el-menu-item>
      <!-- <el-menu-item>待定</el-menu-item>
      <el-menu-item>待定</el-menu-item> -->
    </el-menu>
    <el-button-group>
      <el-button 
        type="primary" 
        :plain="isPlain && !isOrgRegisterActive" 
        v-on:click="goToOrgRegister"
      >
        组织注册
      </el-button>
      <el-button 
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
  position: fixed; /* 固定在页面顶部 */
  top: 0; /* 距离页面顶部0 */
  left: 0; /* 距离页面左边0 */
  right: 0; /* 距离页面右边0 */
  z-index: 1000; /* 确保在其他元素之上 */
  /* border: none; */
}

.logo {
  font-size: 24px;
  font-weight: bold;
}

.el-menu {
  flex: 1;
  margin-left: 20px;
}
</style>