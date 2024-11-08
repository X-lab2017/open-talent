<template>
  <div class="rank-list">
    <!-- Header -->
    <el-header>
      <div class="logo">Logo</div>
      <el-menu mode="horizontal">
        <el-menu-item @click="goToRankList">排行榜</el-menu-item>
        <el-menu-item>待定</el-menu-item>
        <el-menu-item>待定</el-menu-item>
      </el-menu>
      <el-button-group>
        <el-button type="primary" plain>组织注册</el-button>
        <el-button type="primary">成员注册</el-button>
      </el-button-group>
    </el-header>

    <!-- Main Content -->
    <el-main>
      <h2 class="title">成员贡献度排行榜</h2>
      <div class="filters">
        <el-input
          v-model="orgFilter"
          placeholder="组织"
          class="filter-input"
          clearable
        ></el-input>
        <el-input
          v-model="communityFilter"
          placeholder="参与社区"
          class="filter-input"
          clearable
        ></el-input>
        <el-button type="primary" @click="resetFilters">重置过滤</el-button>
      </div>
      <el-table
        :data="paginatedMembers"
        style="width: 100%"
        stripe
        size="medium"
      >
        <el-table-column prop="rank" label="排名"></el-table-column>
        <el-table-column prop="name" label="姓名"></el-table-column>
        <el-table-column label="OpenRank">
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
            {{ scope.row.openRank }}
          </template>
        </el-table-column>
        <el-table-column prop="activeMonths" label="活跃月数"></el-table-column>
        <el-table-column label="详情页面">
          <template slot-scope="scope">
            <el-button
              type="primary"
              size="mini"
              @click="goToDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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
export default {
  name: "MemberContributionRank",
  data() {
    return {
      members: [
        {
          rank: 1,
          name: "张三",
          openRank: 50,
          activeMonths: 10,
          organization: "组织A",
          community: "社区X",
        },
        {
          rank: 2,
          name: "李四",
          openRank: 45,
          activeMonths: 8,
          organization: "组织B",
          community: "社区Y",
        },
        {
          rank: 3,
          name: "王五",
          openRank: 42,
          activeMonths: 7,
          organization: "组织A",
          community: "社区X",
        },
        {
          rank: 4,
          name: "张三",
          openRank: 39,
          activeMonths: 10,
          organization: "组织A",
          community: "社区X",
        },
        {
          rank: 5,
          name: "李四",
          openRank: 35,
          activeMonths: 8,
          organization: "组织B",
          community: "社区Y",
        },
        {
          rank: 6,
          name: "王五",
          openRank: 30,
          activeMonths: 7,
          organization: "组织A",
          community: "社区X",
        },
        {
          rank: 7,
          name: "张三",
          openRank: 20,
          activeMonths: 10,
          organization: "组织A",
          community: "社区X",
        },
        {
          rank: 8,
          name: "李四",
          openRank: 18,
          activeMonths: 8,
          organization: "组织B",
          community: "社区Y",
        },
        {
          rank: 9,
          name: "王五",
          openRank: 15,
          activeMonths: 7,
          organization: "组织A",
          community: "社区X",
        },
        {
          rank: 10,
          name: "张三",
          openRank: 13,
          activeMonths: 10,
          organization: "组织A",
          community: "社区X",
        },
        {
          rank: 11,
          name: "李四",
          openRank: 11,
          activeMonths: 8,
          organization: "组织B",
          community: "社区Y",
        },
        {
          rank: 12,
          name: "王五",
          openRank: 10,
          activeMonths: 7,
          organization: "组织A",
          community: "社区X",
        },
        // 更多成员数据...
      ],
      orgFilter: "",
      communityFilter: "",
      currentPage: 1,
      pageSize: 10,
      sortOrder: "desc", // 默认降序
    };
  },
  computed: {
    filteredMembers() {
      let filtered = this.members.filter((member) => {
        const orgMatch = member.organization
          .toLowerCase()
          .includes(this.orgFilter.toLowerCase());
        const communityMatch = member.community
          .toLowerCase()
          .includes(this.communityFilter.toLowerCase());
        return orgMatch && communityMatch;
      });

      // 排序
      if (this.sortOrder === "asc") {
        filtered.sort((a, b) => a.openRank - b.openRank);
      } else if (this.sortOrder === "desc") {
        filtered.sort((a, b) => b.openRank - a.openRank);
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
    goToRankList() {
      // 检查当前路径是否与目标路径相同
      if (this.$route.name !== "RankList") {
        this.$router.push({ name: "RankList" });
      } else {
        // 如果已经在 RankList 页面，刷新
        this.$router.go(0);
      }
    },
    goToDetail(member) {
      // 假设使用 Vue Router 进行导航
      // 根据实际路由调整路径
      this.$router.push({ name: "MemberDetail", params: { id: member.rank } });
    },
    handleCurrentChange(page) {
      this.currentPage = page;
    },
    setSortOrder(order) {
      this.sortOrder = order;
    },
    resetFilters() {
      this.orgFilter = "";
      this.communityFilter = "";
    },
  },
};
</script>
  
  <style scoped>
.rank-list {
  display: flex;
  flex-direction: column;
  /* height: 100vh; */
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

.el-main {
  flex: 1;
  padding: 50px;
  display: flex;
  flex-direction: column;
  align-items: center; /* 使内容居中 */
  width: 100%; /* 确保主内容占满宽度 */
}

.filters {
  display: flex;
  justify-content: center; /* 使过滤按钮居中 */
  width: 30%; /* 确保过滤器占满宽度 */
  margin-bottom: 20px; /* 添加一些底部间距 */
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
</style>