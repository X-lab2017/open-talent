import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import axios from "axios";
import './assets/shared.css'
import * as echarts from 'echarts'

axios.defaults.baseURL = '/api'

Vue.prototype.$http=axios
Vue.prototype.$echarts = echarts
Vue.config.productionTip = false
Vue.use(ElementUI)

// 添加 Axios 请求拦截器
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token'); // 从 localStorage 中获取 token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`; // 如果 token 存在，将其添加到请求头中
  }
  return config; // 返回配置对象
}, error => {
  return Promise.reject(error); // 如果请求错误，返回一个被拒绝的 Promise
});

// // 添加 Axios 响应拦截器
// axios.interceptors.response.use(response => {
//   return response;
// }, error => {
//   if (error.response && error.response.status === 401) {
//     // 如果响应状态码是 401，表示未授权或 token 过期
//     localStorage.removeItem('token'); // 移除过期的 token
//     localStorage.removeItem('orgName'); // 移除组织名
//     router.push({ name: 'OrgLogin' }); // 重定向到登录页面
//     Vue.prototype.$message.error('登录已过期，请重新登录');
//   }
//   return Promise.reject(error);
// });

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')
