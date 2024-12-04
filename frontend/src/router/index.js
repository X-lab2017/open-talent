import Vue from 'vue'
import VueRouter from 'vue-router'
import HomeView from '../views/HomeView.vue'
import OrgRegister from '@/views/OrgRegister.vue'
import MemberRegister from "@/views/MemberRegister.vue"
import RankList from '@/views/RankList.vue'
import MemberProfile from "@/views/MemberProfile.vue";
import MemberList from '@/views/MemberList.vue'
import OrgRankList from "@/views/OrgRankList.vue";
import OrgLogin from '@/views/OrgLogin.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/about',
    name: 'about',
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    component: () => import(/* webpackChunkName: "about" */ '../views/AboutView.vue')
  },
  {
    path: '/org-register',
    name: 'OrgRegister',
    component: OrgRegister,
    beforeEnter: (to, from, next) => {
      const loggedIn = localStorage.getItem('token');
      if (loggedIn) {
        return next({ name: 'MemberList' }); // 如果组织已登录，重定向到列表页面
      }
      next();
    }
  },
  {
    path: '/member-register',
    name: 'MemberRegister',
    component: MemberRegister,
    beforeEnter: (to, from, next) => {
      const loggedIn = localStorage.getItem('token');
      if (!loggedIn) {
        return next({ name: 'OrgLogin' }); // 如果组织未登录，重定向到登录页面
      }
      next();
    }
  },
  {
    path: '/rank-list',
    name: 'RankList',
    component: RankList,
  },
  {
    path: '/member-profile',
    name: 'MemberProfile',
    component: MemberProfile
  },
  {
    path: '/org-rank-list',
    name: 'OrgRankList',
    component: OrgRankList
  },
  {
    path: '/member-list',
    name: 'MemberList',
    component: MemberList,
  },
  {
    path: '/org-login',
    name: 'OrgLogin',
    component: OrgLogin,
  },
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  const publicPages = ['RankList', 'OrgRankList', 'OrgRegister', 'MemberRegister', 'OrgLogin', 'MemberProfile'];
  const authRequired = !publicPages.includes(to.name);
  const loggedIn = localStorage.getItem('token');

  if (authRequired && !loggedIn) {
    return next({ name: 'OrgLogin' });
  }

  next();
});

export default router
