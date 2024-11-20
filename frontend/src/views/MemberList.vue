<template>
  <div class="member-list">
    <NavMenu></NavMenu>
    <el-main>
      <h2 class="title">成员列表</h2>
      <div class="filters">
        <el-select
          v-model="searchQuery"
          placeholder="输入或选择组织名称"
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
        <el-button type="primary" @click="searchMembers">搜索</el-button>
      </div>
      <el-table
        :data="paginatedMembers"
        style="width: 100%"
        stripe
        size="medium"
      >
        <el-table-column prop="name" label="姓名"></el-table-column>
        <el-table-column prop="organization" label="组织"></el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button type="primary" size="mini" @click="editMember(scope.row)"
              >编辑</el-button
            >
            <el-button
              type="danger"
              size="mini"
              @click="deleteMember(scope.row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-size="pageSize"
          layout="prev, pager, next"
          :total="filteredMembers.length"
        ></el-pagination>
      </div>
    </el-main>
    <!-- el-dialog 组件 -->
    <el-dialog
      title="编辑成员信息"
      :visible.sync="editDialogVisible"
      width="30%"
    >
      <el-form :model="editForm">
        <el-form-item label="姓名">
          <el-input v-model="editForm.name"></el-input>
        </el-form-item>
        <el-form-item label="组织">
          <el-select v-model="editForm.organizationId" placeholder="选择组织">
            <el-option
              v-for="org in organizations"
              :key="org.organizationId"
              :label="org.name"
              :value="org.organizationId"
            ></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>
  
  <script>
import NavMenu from "@/components/NavMenu.vue";
export default {
  name: "MemberList",
  components: { NavMenu },
  data() {
    return {
      members: [],
      organizations: [],
      searchQuery: "",
      currentPage: 1,
      pageSize: 10,

      editDialogVisible: false,
      editForm: {
        memberId: null,
        name: '',
        organizationId: null,
      },
    };
  },
  computed: {
    filteredMembers() {
      return this.members.filter((member) =>
        member.organization
          .toLowerCase()
          .includes(this.searchQuery.toLowerCase())
      );
    },
    paginatedMembers() {
      const startIndex = (this.currentPage - 1) * this.pageSize;
      const endIndex = startIndex + this.pageSize;
      return this.filteredMembers.slice(startIndex, endIndex);
    },
  },
  methods: {
    fetchMembers() {
      // 从服务端获取成员列表
      this.$http
        .get("/member/search")
        .then((response) => {
          const members = response.data.data;
          this.members = members.map((member) => {
            const org = this.organizations.find(
              (org) => org.organizationId === member.organizationId
            );
            return {
              ...member,
              organization: org ? org.name : "未知组织",
            };
          });
        })
        .catch((error) => {
          console.error("获取成员数据失败：", error);
        });
    },
    fetchOrganizations() {
      // 从服务端获取组织列表
      this.$http
        .get("/org/search")
        .then((response) => {
          this.organizations = response.data.data;
        })
        .catch((error) => {
          console.error("获取组织数据失败：", error);
        });
    },
    searchMembers() {
      this.currentPage = 1;
      this.fetchMembers();
    },
    editMember(member) {
      // 打开编辑对话框并填充表单
      this.editForm = { ...member };
      this.editDialogVisible = true;
    },
    submitEdit() {
    // 发送 PUT 请求以更新成员信息
    this.$http.put(`/member/edit/${this.editForm.memberId}`, this.editForm)
      .then(() => {
        // 更新成员列表中的信息
        const index = this.members.findIndex(m => m.memberId === this.editForm.memberId);
        if (index !== -1) {
          this.members.splice(index, 1, { ...this.editForm, organization: this.getOrganizationName(this.editForm.organizationId) });
        }
        this.editDialogVisible = false;
        this.$message.success("更新成功");
      })
      .catch(error => {
        console.error("更新成员失败：", error);
        this.$message.error("更新失败");
      });
  },
    getOrganizationName(organizationId) {
      const org = this.organizations.find(org => org.organizationId === organizationId);
      return org ? org.name : '未知组织';
    },
    deleteMember(member) {
    // 发送 DELETE 请求以删除成员信息
    this.$http.delete(`/member/delete/${member.memberId}`)
      .then(() => {
        // 从成员列表中移除已删除的成员
        this.members = this.members.filter(m => m.memberId !== member.memberId);
        this.$message.success("删除成功");
      })
      .catch(error => {
        console.error("删除成员失败：", error);
        this.$message.error("删除失败");
      });
      console.log("删除成员：", member);
    },
    handleCurrentChange(page) {
      this.currentPage = page;
    },
  },
  mounted() {
    this.fetchOrganizations();
    this.fetchMembers();
  },
};
</script>
  
<style scoped>
.member-list {
  display: flex;
  flex-direction: column;
}

.el-main {
  flex: 1;
  padding: 50px;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.filters {
  display: flex;
  justify-content: center;
  width: 30%;
  margin-bottom: 20px;
}
</style>