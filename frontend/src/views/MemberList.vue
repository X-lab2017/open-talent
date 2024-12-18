<template>
  <div class="member-list">
    <NavMenu></NavMenu>
    <el-main>
      <h2 class="title" style="text-align: center;">成员列表</h2>
      <div class="filters">
        <el-select
          v-model="searchQuery"
          placeholder="输入或选择组织名称"
          class="filter-input"
          filterable
          clearable
          :disabled="!isAdmin"
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
              @click="confirmDelete(scope.row)"
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
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="姓名">
              <el-input v-model="editForm.name"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
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
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer" style="text-align: center;">
        <el-button type="primary" plain @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" plain @click="submitEdit">保存</el-button>
      </div>
    </el-dialog>
    <!-- 新增的删除确认弹窗 -->
    <el-dialog
      title="确认删除"
      :visible.sync="deleteDialogVisible"
      width="30%"
    >
      <span>您确定要删除该成员吗？</span>
      <div slot="footer" class="dialog-footer" style="text-align: center;">
        <el-button type="primary" plain @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="deleteMember">确认删除</el-button>
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
      
      currentPage: 1,
      pageSize: 10,

      editDialogVisible: false,
      deleteDialogVisible: false,
      memberToDelete: null,
      editForm: {
        memberId: null,
        name: '',
        organizationId: null,
      },
      searchQuery: "",
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
    isAdmin() {
      return localStorage.getItem("orgName") === "admin"; // 判断是否为admin
    },
  },
  methods: {
    async fetchMembers() {
      try {
        // 确保组织数据已经被获取
        if (this.organizations.length === 0) {
          await this.fetchOrganizations();
        }
        // 从服务端获取成员列表
        const response = await this.$http.get("/member/search");
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
        // 在获取成员后设置 searchQuery
        if (!this.isAdmin) {
          this.searchQuery = localStorage.getItem("orgName") || "";
        }
      } catch (error) {
        console.error("获取成员数据失败：", error);
      }
    },
    async fetchOrganizations() {
      try {
        // 从服务端获取组织列表
        const response = await this.$http.get("/org/search");
        this.organizations = response.data.data;
      } catch (error) {
        console.error("获取组织数据失败：", error);
      }
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
    confirmDelete(member) {
      this.memberToDelete = member;
      this.deleteDialogVisible = true;
    },
    deleteMember() {
      if (!this.memberToDelete) return;
      // 发送 DELETE 请求以删除成员信息
      this.$http.delete(`/member/delete/${this.memberToDelete.memberId}`)
        .then(() => {
          // 从成员列表中移除已删除的成员
          this.members = this.members.filter(m => m.memberId !== this.memberToDelete.memberId);
          this.$message.success("删除成功");
        })
        .catch(error => {
          console.error("删除成员失败：", error);
          this.$message.error("删除失败");
        })
        .finally(() => {
          this.deleteDialogVisible = false;
          this.memberToDelete = null;
        });
    },
    handleCurrentChange(page) {
      this.currentPage = page;
    },
  },
  mounted() {
    // this.fetchOrganizations();
    this.searchMembers();
    // this.fetchMembers();
  },
};
</script>
  
<style>
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

.member-list .filters {
  display: flex;
  justify-content: center;
  width: 100%;
  margin-bottom: 20px;
}
/* 新增样式 */
.member-list .el-button--primary {
  color: #fff;
  background-color: #131313; 
  border-color: #131313;
}

.member-list .el-button--primary:focus,
.member-list .el-button--primary:hover {
  background: #131313; 
  border-color: #131313;
  color: #fff;
}
.member-list .el-table .el-table__row {
  height: 50px;        /* 设置固定行高为100px */
  line-height: 50px;   /* 设置行高为100px */
}
.member-list .el-table .even-row,
.member-list .el-table .odd-row {
  height: 50px;        /* 确保行高一致为100px */
  line-height: 50px;   /* 确保行高一致为100px */
}
.member-list .el-table th {
  color: #000; /* 设置表头字体颜色为黑色 */
}
</style>