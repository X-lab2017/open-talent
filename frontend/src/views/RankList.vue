<template>
  <div class="rank-list">
    <!-- Header -->
    <NavMenu></NavMenu>

    <!-- Main Content -->
    <div>
      <el-main>
        <h2 class="title">成员贡献度排行榜</h2>
        <div class="filters">
          <el-select
              v-model="orgFilter"
              placeholder="选择组织"
              class="filter-input"
              filterable
              clearable
          >
            <el-option
                v-for="org in organizations"
                :key="org.organizationId"
                :label="org.name"
                :value="org.name"
            ></el-option>
          </el-select>
          <el-button type="primary" plain @click="searchMembers">搜索</el-button>
        </div>
        <div style="margin: 0 30px">
          <el-table
              :row-class-name="tableRowClassName"

              :header-cell-style="{ color: '#ffffff', fontSize: '18px', backgroundColor: '#111E33' }"
              :data="paginatedMembers"
              style="width: 100%"
              size="medium"
          >
            <el-table-column prop="rank" label="排名" width="80"></el-table-column>
            <el-table-column prop="name" label="姓名" width="80"></el-table-column>
            <el-table-column prop="openrankValue" label="OpenRank">
              <template slot="header">
                <span>OpenRank</span>
                <el-button
                    type="text"
                    icon="el-icon-caret-top"
                    @click="setSortOrder('asc')"
                    :class="{ active: sortOrder === 'asc' }"
                ></el-button>
                <el-button
                    type="text"
                    icon="el-icon-caret-bottom"
                    @click="setSortOrder('desc')"
                    :class="{ active: sortOrder === 'desc' }"
                ></el-button>
              </template>
              <template slot-scope="scope">
                {{ scope.row.lastOpenrank }}
                <img :src="scope.row.openrankValue > scope.row.lastOpenrank ? 
                require('@/assets/increase.svg') : require('@/assets/decrease.svg')" style="margin: 0 7px">
                {{ scope.row.openrankValue }}
              </template>
            </el-table-column>
            <el-table-column prop="organization" label="学校">
              <template slot-scope="scope">
                {{ scope.row.organization }}
              </template>
            </el-table-column>
            <el-table-column prop="community" label="参与社区">
              <template slot-scope="scope">
                {{ scope.row.community }}
              </template>
            </el-table-column>
            <el-table-column prop="activeMonths" label="活跃月数" width="100">
              <template slot-scope="scope">
                {{ scope.row.activeMonths }}
              </template>
            </el-table-column>
            <el-table-column label="详情页面">
              <!-- <template slot-scope="scope"> -->
              <template>
                <el-button
                    type="primary"
                    size="mini"
                    v-on:click="goToDetail"
                >
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>


        <!-- Pagination -->
        <div class="pagination-container">
          <el-pagination
              @current-change="handleCurrentChange"
              :current-page="currentPage"
              :page-size="pageSize"
              layout="prev, pager, next"
              :total="filteredMembers.length"
          >
          </el-pagination>
        </div>
      </el-main>
    </div>



    <!-- Footer -->
    <el-footer>
      <div class="footer-content">
        <img src="/images/lab-logo.png" alt="Xlab Logo" class="footer-logo" />
        <p>官方支持</p>
        <p>Copyright © 2024 X-lab</p>
      </div>
    </el-footer>
  </div>
</template>
    
<script>
import NavMenu from "@/components/NavMenu.vue";
export default {
  name: "MemberContributionRank",
  components: {NavMenu},
  data() {
    return {
      members: [],
      organizations: [],
      orgFilter: "",
      // communityFilter: "",
      currentPage: 1,
      pageSize: 10,
      sortOrder: "desc", // 默认降序
      // randomValues: {} // 新增：随机生成，并在前端显示每个成员的openrank和活跃月数变化后的值
    };
  },
  computed: {
    filteredMembers() {
      let filtered = this.members.filter((member) => {
        return member.organization.toLowerCase().includes(this.orgFilter.toLowerCase());
      });

      // 排序
      if (this.sortOrder === "asc") {
        filtered.sort((a, b) => a.openrankValue - b.openrankValue);
      } else if (this.sortOrder === "desc") {
        filtered.sort((a, b) => b.openrankValue - a.openrankValue);
      }

      return filtered;
    },
    paginatedMembers() {
      const startIndex = (this.currentPage - 1) * this.pageSize;
      const endIndex = startIndex + this.pageSize;
      return this.filteredMembers.slice(startIndex, endIndex);
    },
  },
  methods: {
    async fetchOrganizations() {
      try {
        const response = await this.$http.get("/org/search");
        console.log("组织数据：", response.data); // 调试输出
        const organizations = response.data.data; // 获取实际的组织列表
        if (organizations && organizations.length > 0) {
          this.organizations = organizations;
        } else {
          console.error("组织数据为空或格式不正确");
        }
      } catch (error) {
        console.error("获取组织数据失败：", error);
      }
    },
    async fetchMembers() {
      try {
        // 确保组织数据已经加载完毕
        await this.fetchOrganizations();
        const response = await this.$http.get("/member/search");
        console.log("成员数据：", response.data); // 调试输出
        const members = response.data.data; // 假设返回的数据结构是 { code, msg, data }
        if (Array.isArray(members)) {
          // 将 organizationId 对应 organization 名称
          this.members = members.map(member => {
            const org = this.organizations.find(org => org.organizationId === member.organizationId);
            return {
              ...member,
              organization: org ? org.name : '未知组织'
            };
          });
          // 按 openrankValue 排序并分配排名
          this.members.sort((a, b) => b.openrankValue - a.openrankValue);
          this.members.forEach((member, index) => {
            member.rank = index + 1; // 分配排名，从1开始
          });
        } else {
          console.error("成员数据格式不正确");
          this.members = []; // 确保 members 始终是一个数组
        }
      } catch (error) {
        console.error("获取成员数据失败：", error);
        this.members = []; // 确保在错误情况下 members 也是一个数组
      }
    },
    searchMembers() {
      this.currentPage = 1; // 重置到第一页
      this.fetchMembers(); // 重新获取成员数据
    },
    goToRankList() {
      // 检查当前路径是否与目标路径相同
      if (this.$route.name !== "RankList") {
        this.$router.push({ name: "RankList" });
      } else {
        // 如果已经在 RankList 页面，刷新
        this.$router.go(0);
      }
    },
    goToDetail() {
      this.$router.push({ name: "MemberProfile"});
    },
    handleCurrentChange(page) {
      this.currentPage = page;
    },
    setSortOrder(order) {
      this.sortOrder = order;
    },
    // resetFilters() {
    //   this.orgFilter = "";
    //   this.communityFilter = "";
    // },
    tableRowClassName({ row, rowIndex }) {
        console.log(rowIndex)
      if (rowIndex % 2 === 0) {
        return 'even-row' //这是类名
      } else  {
        return 'odd-row'
      }
    },
  },
  // 需要配置后端接口
  mounted() {
    this.fetchOrganizations();
    this.fetchMembers();
  },
};
</script>

<style>
.rank-list {
  display: flex;
  flex-direction: column;
  justify-content: center; /* 水平居中 */
  text-align: center;
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



.rank-list .filters {
  display: flex;
  justify-content: center; /* 使过滤按钮居中 */
  text-align: center;
  margin-bottom: 20px; /* 添加一些底部间距 */
}
.rank-list .title{
  color: #ACC5DB;
  margin: 0 auto;
}
.rank-list .el-main{
  display: inline-block;
  background-color: #111E33; /* 深蓝色背景 */
  border-radius: 15px; /* 圆角半径 */
  padding: 50px 0;
  width: 70%;
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

.el-button--primary {
  color: #fff;
  background-color: #131313;
  border-color: #cdcdcd;
}

.el-button--primary:focus,
.el-button--primary:hover {
  background: #e8e8e8;
  border-color: #131313;
  color: #131313;
}

.el-button--primary.is-plain {
  color: #5f5f5f;
  background: #fff;
  border-color: #cdcdcd;
}

.el-button--primary.is-plain:focus,
.el-button--primary.is-plain:hover {
  background: #131313;
  border-color: #131313;
  color: #fff;
}


.el-pager li {
  background: #111E33;
}

.el-pagination{
  text-align: center;
}

.el-pagination button:disabled {
  background-color: #111E33;
  color: #ACC5D3
}

.el-pagination button {
  background-color: #111E33 !important;
  color: #FFF !important;
}

.el-pager li.active {
  color: #FFF !important;
}

.el-pager li {
  color: #ACC5DB !important;
  background-color: #111E33 !important;
}

.el-pager li:hover {
  color: #FFF !important;
  background-color: #111E33 !important;
}

.rank-list .el-table td.el-table__cell, .el-table th.el-table__cell.is-leaf {
  border-bottom: 1px solid #FFF;
}

.rank-list .el-table .even-row {
  background-color:#213046 !important;
  color: #FFF;
  font-size: 16px;
  height: 50px;        /* 添加固定行高 */
  line-height: 50px;   /* 添加行高 */
}

.rank-list .el-table .odd-row {
  background-color:#111E33 !important;
  color: #FFF;
  font-size: 16px;
  height: 50px;        /* 添加固定行高 */
  line-height: 50px;   /* 添加行高 */
}

.rank-list .el-table tbody tr:hover>td {
  background-color: transparent !important;
}
</style>