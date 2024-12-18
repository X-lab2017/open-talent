<template>
  <div>
    <NavMenu></NavMenu>
    <div class="container">
      <div class="content">
        <h1 class="main-title"><span class="highlight">Open</span>Talent</h1>
        <div class="rounded-line"></div>
        <p class="sub-title">贡献导向的全球开源人才评价系统</p>
        <p class="description">
          OpenTalent: A Global Talent Evaluation System Based on Open Source
          Contribution
        </p>
        <p class="slogan">北极照耀星空，开源点亮未来</p>

        <div class="search-box">
          <el-select v-model="value" placeholder="请选择">
            <el-option
                v-for="item in options"
                :key="item.value"
                :label="item.label"
                :value="item.value">
            </el-option>
          </el-select>
          <input type="text" class="transparent-input">
        </div>

        <div class="powered-by">
          <div style="font-size: 20px; display: inline-block; margin-right: 30px" >Powered by</div>
          <img src="@/assets/ecnuLogo-white.png" class="powered-logo">
          <div class="short-line"></div>
          <img src="@/assets/XLabLogo-white.png" class="powered-logo">
          <div class="short-line"></div>
          <img src="@/assets/openTalentLogo-white.png" class="powered-logo">
        </div>

    </div>
    </div>
  </div>

</template>

<script>
import NavMenu from "@/components/NavMenu.vue";

export default {
  components: {
    'NavMenu': NavMenu
  },
  data() {
    return {
      options: [{
        value: '选项1',
        label: '关键指标'
      }],
      value: '选项1'
    }
  }

};
</script>

<style>

.search-box .el-select {
  width: 130px;
}

.search-box .el-input__inner {
  border-top-left-radius: 10px;
  border-bottom-left-radius: 10px;
  text-align: center;
  color: #fff !important;
  border: 1px solid #326AFF;
  background: #6c63ff !important;
  font-size: 16px;
  letter-spacing: 2px;
}

.search-box /deep/.el-select-dropdown__item.selected {
  color: #6c63ff !important;
}

.el-select-dropdown__item{
  color:#6c63ff !important;
}
.container {
  position: relative;
  height: 100vh;
  width: 100%;
  background-color: #000;
  overflow: hidden;
  color: #fff;
  display: flex;
}

.content{
  position: absolute;
  left: 10%;
  top: 5%;
}

.main-title {
  font-size: 6rem;
  font-weight: bold;
  margin-bottom: 40px;
}
.main-title .highlight {
  color: #6c63ff;
}

.rounded-line {
  width: 100px;
  height: 3px;
  background-color: #FFF;
  border-radius: 1px;
  overflow: hidden;
}

.sub-title {
  font-size: 1.6rem;
  margin-top: 30px;
  margin-bottom: 0px;
}

.description {
  font-size: 1.1rem;
  opacity: 0.9;
  margin-bottom: 80px;
}

.slogan{
  font-size: 1.3rem;
  opacity: 0.9;
  margin-bottom: 30px;
}

.powered-by{
  display: flex;
  align-items: center;
  letter-spacing: 1px;
}

.powered-logo{
  height: 45px;
}

.short-line {
  width: 1px;
  height: 30px;
  background-color: white;
  margin: 30px;
}

.search-box {
  margin-top: 40px;
  margin-bottom: 40px;
  width: 660px;
  height: 50px;
  border: 2px solid #FFFFFF; /* 边框颜色可以根据需要调整 */
  display: flex;
  align-items: center;
  border-radius: 10px;
  padding-left: 6px;
}

.transparent-input {
  width: 520px;
  margin: 0 10px;
  border: none;
  background-color: transparent;
  color: #FFF;
  font-size: 17px;
  outline: none;
  box-shadow: none;
}

.transparent-input::placeholder {
  color: #FFF; /* 占位符文字颜色，可以根据需要调整 */
}

</style>
