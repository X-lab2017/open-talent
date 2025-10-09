<template>
  <div class="system-settings">
    <NavMenu />
    <div class="main-content">
      <div class="page-header">
        <h1>系统设置</h1>
        <p>管理系统用户、权限配置和系统参数</p>
      </div>

      <el-tabs v-model="activeTab" type="card" class="settings-tabs">
        <!-- 用户管理 -->
        <el-tab-pane label="用户管理" name="users">
          <div class="user-management">
            <div class="management-header">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-input
                    v-model="userSearchKeyword"
                    placeholder="搜索用户名或邮箱"
                    prefix-icon="el-icon-search"
                    @input="searchUsers">
                  </el-input>
                </el-col>
                <el-col :span="6">
                  <el-select v-model="userRoleFilter" placeholder="筛选角色" @change="filterUsers">
                    <el-option label="全部角色" value=""></el-option>
                    <el-option label="管理员" value="admin"></el-option>
                    <el-option label="教师" value="teacher"></el-option>
                    <el-option label="学生" value="student"></el-option>
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-button type="primary" @click="showAddUserDialog">添加用户</el-button>
                </el-col>
              </el-row>
            </div>

            <el-table :data="filteredUsers" stripe class="user-table">
              <el-table-column prop="id" label="ID" width="80"></el-table-column>
              <el-table-column prop="username" label="用户名" width="150"></el-table-column>
              <el-table-column prop="email" label="邮箱" width="200"></el-table-column>
              <el-table-column prop="role" label="角色" width="120">
                <template slot-scope="scope">
                  <el-tag :type="getRoleType(scope.row.role)">{{ getRoleLabel(scope.row.role) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template slot-scope="scope">
                  <el-tag :type="scope.row.status === 'active' ? 'success' : 'danger'">
                    {{ scope.row.status === 'active' ? '激活' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="lastLogin" label="最后登录" width="180"></el-table-column>
              <el-table-column prop="createdAt" label="创建时间" width="180"></el-table-column>
              <el-table-column label="操作" width="200">
                <template slot-scope="scope">
                  <el-button size="mini" @click="editUser(scope.row)">编辑</el-button>
                  <el-button 
                    size="mini" 
                    :type="scope.row.status === 'active' ? 'warning' : 'success'"
                    @click="toggleUserStatus(scope.row)">
                    {{ scope.row.status === 'active' ? '禁用' : '启用' }}
                  </el-button>
                  <el-button size="mini" type="danger" @click="deleteUser(scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
              @size-change="handleUserSizeChange"
              @current-change="handleUserCurrentChange"
              :current-page="userCurrentPage"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="userPageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="userTotal"
              class="pagination">
            </el-pagination>
          </div>
        </el-tab-pane>

        <!-- 权限配置 -->
        <el-tab-pane label="权限配置" name="permissions">
          <div class="permission-management">
            <div class="permission-header">
              <el-row :gutter="20">
                <el-col :span="12">
                  <h3>角色权限配置</h3>
                </el-col>
                <el-col :span="12" style="text-align: right;">
                  <el-button type="primary" @click="showAddRoleDialog">添加角色</el-button>
                </el-col>
              </el-row>
            </div>

            <el-row :gutter="20">
              <el-col :span="8">
                <el-card class="role-list">
                  <div slot="header">角色列表</div>
                  <el-menu 
                    :default-active="selectedRole"
                    @select="selectRole"
                    class="role-menu">
                    <el-menu-item 
                      v-for="role in roles" 
                      :key="role.id" 
                      :index="role.id">
                      <span>{{ role.name }}</span>
                      <el-tag size="mini" style="margin-left: 10px;">{{ role.userCount }}人</el-tag>
                    </el-menu-item>
                  </el-menu>
                </el-card>
              </el-col>
              <el-col :span="16">
                <el-card class="permission-config" v-if="selectedRoleData">
                  <div slot="header">
                    <span>{{ selectedRoleData.name }} - 权限配置</span>
                    <el-button style="float: right;" type="primary" size="small" @click="savePermissions">保存权限</el-button>
                  </div>
                  
                  <div class="permission-tree">
                    <el-tree
                      :data="permissionTree"
                      :props="treeProps"
                      show-checkbox
                      node-key="id"
                      ref="permissionTree"
                      :default-checked-keys="selectedRoleData.permissions">
                    </el-tree>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <!-- 系统参数 -->
        <el-tab-pane label="系统参数" name="parameters">
          <div class="system-parameters">
            <el-form :model="systemConfig" label-width="200px" class="config-form">
              <el-card class="config-section">
                <div slot="header">基础配置</div>
                <el-form-item label="系统名称">
                  <el-input v-model="systemConfig.systemName" placeholder="请输入系统名称"></el-input>
                </el-form-item>
                <el-form-item label="系统描述">
                  <el-input 
                    type="textarea" 
                    v-model="systemConfig.systemDescription" 
                    placeholder="请输入系统描述"
                    :rows="3">
                  </el-input>
                </el-form-item>
                <el-form-item label="系统版本">
                  <el-input v-model="systemConfig.systemVersion" placeholder="请输入系统版本"></el-input>
                </el-form-item>
                <el-form-item label="维护模式">
                  <el-switch v-model="systemConfig.maintenanceMode"></el-switch>
                  <span style="margin-left: 10px; color: #909399;">开启后系统将进入维护模式</span>
                </el-form-item>
              </el-card>

              <el-card class="config-section">
                <div slot="header">评分配置</div>
                <el-form-item label="默认评分算法">
                  <el-select v-model="systemConfig.defaultAlgorithm" placeholder="选择默认算法">
                    <el-option label="加权平均算法" value="weighted_average"></el-option>
                    <el-option label="层次分析法" value="ahp"></el-option>
                    <el-option label="模糊综合评价" value="fuzzy_evaluation"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="最小评分周期(天)">
                  <el-input-number v-model="systemConfig.minEvaluationPeriod" :min="1" :max="30"></el-input-number>
                </el-form-item>
                <el-form-item label="自动计算">
                  <el-switch v-model="systemConfig.autoCalculation"></el-switch>
                  <span style="margin-left: 10px; color: #909399;">开启后系统将自动执行评分计算</span>
                </el-form-item>
                <el-form-item label="计算频率" v-if="systemConfig.autoCalculation">
                  <el-select v-model="systemConfig.calculationFrequency" placeholder="选择计算频率">
                    <el-option label="每日" value="daily"></el-option>
                    <el-option label="每周" value="weekly"></el-option>
                    <el-option label="每月" value="monthly"></el-option>
                  </el-select>
                </el-form-item>
              </el-card>

              <el-card class="config-section">
                <div slot="header">数据配置</div>
                <el-form-item label="数据保留期限(月)">
                  <el-input-number v-model="systemConfig.dataRetentionPeriod" :min="1" :max="120"></el-input-number>
                </el-form-item>
                <el-form-item label="备份频率">
                  <el-select v-model="systemConfig.backupFrequency" placeholder="选择备份频率">
                    <el-option label="每日" value="daily"></el-option>
                    <el-option label="每周" value="weekly"></el-option>
                    <el-option label="每月" value="monthly"></el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="自动清理过期数据">
                  <el-switch v-model="systemConfig.autoCleanup"></el-switch>
                </el-form-item>
              </el-card>

              <el-card class="config-section">
                <div slot="header">通知配置</div>
                <el-form-item label="邮件通知">
                  <el-switch v-model="systemConfig.emailNotification"></el-switch>
                </el-form-item>
                <el-form-item label="SMTP服务器" v-if="systemConfig.emailNotification">
                  <el-input v-model="systemConfig.smtpServer" placeholder="请输入SMTP服务器地址"></el-input>
                </el-form-item>
                <el-form-item label="SMTP端口" v-if="systemConfig.emailNotification">
                  <el-input-number v-model="systemConfig.smtpPort" :min="1" :max="65535"></el-input-number>
                </el-form-item>
                <el-form-item label="发送邮箱" v-if="systemConfig.emailNotification">
                  <el-input v-model="systemConfig.senderEmail" placeholder="请输入发送邮箱"></el-input>
                </el-form-item>
              </el-card>

              <div class="form-actions">
                <el-button type="primary" @click="saveSystemConfig">保存配置</el-button>
                <el-button @click="resetSystemConfig">重置配置</el-button>
                <el-button type="warning" @click="exportConfig">导出配置</el-button>
                <el-button type="info" @click="importConfig">导入配置</el-button>
              </div>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog 
      :title="userDialogTitle" 
      :visible.sync="userDialogVisible" 
      width="500px">
      <el-form :model="userForm" :rules="userRules" ref="userForm" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!userForm.id">
          <el-input type="password" v-model="userForm.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="选择角色">
            <el-option label="管理员" value="admin"></el-option>
            <el-option label="教师" value="teacher"></el-option>
            <el-option label="学生" value="student"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="userForm.status" placeholder="选择状态">
            <el-option label="激活" value="active"></el-option>
            <el-option label="禁用" value="inactive"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </div>
    </el-dialog>

    <!-- 添加角色对话框 -->
    <el-dialog 
      title="添加角色" 
      :visible.sync="roleDialogVisible" 
      width="400px">
      <el-form :model="roleForm" :rules="roleRules" ref="roleForm" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="roleForm.name" placeholder="请输入角色名称"></el-input>
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input 
            type="textarea" 
            v-model="roleForm.description" 
            placeholder="请输入角色描述"
            :rows="3">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import NavMenu from '@/components/NavMenu.vue'

export default {
  name: 'SystemSettings',
  components: {
    NavMenu
  },
  data() {
    return {
      activeTab: 'users',
      
      // 用户管理相关
      users: [
        { id: 1, username: 'admin', email: 'admin@example.com', role: 'admin', status: 'active', lastLogin: '2024-01-15 10:30:00', createdAt: '2024-01-01 09:00:00' },
        { id: 2, username: 'teacher1', email: 'teacher1@example.com', role: 'teacher', status: 'active', lastLogin: '2024-01-14 16:45:00', createdAt: '2024-01-02 10:00:00' },
        { id: 3, username: 'student1', email: 'student1@example.com', role: 'student', status: 'active', lastLogin: '2024-01-14 20:15:00', createdAt: '2024-01-03 11:00:00' }
      ],
      filteredUsers: [],
      userSearchKeyword: '',
      userRoleFilter: '',
      userCurrentPage: 1,
      userPageSize: 10,
      userTotal: 0,
      
      // 用户对话框
      userDialogVisible: false,
      userDialogTitle: '添加用户',
      userForm: {
        id: null,
        username: '',
        email: '',
        password: '',
        role: '',
        status: 'active'
      },
      userRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        role: [{ required: true, message: '请选择角色', trigger: 'change' }]
      },
      
      // 权限管理相关
      roles: [
        { id: 'admin', name: '管理员', userCount: 1, permissions: ['user_manage', 'course_manage', 'evaluation_config', 'data_collection', 'score_calculation', 'result_display', 'system_settings'] },
        { id: 'teacher', name: '教师', userCount: 5, permissions: ['course_manage', 'evaluation_config', 'data_collection', 'score_calculation', 'result_display'] },
        { id: 'student', name: '学生', userCount: 50, permissions: ['result_display'] }
      ],
      selectedRole: 'admin',
      selectedRoleData: null,
      permissionTree: [
        {
          id: 'user_manage',
          label: '用户管理',
          children: [
            { id: 'user_create', label: '创建用户' },
            { id: 'user_edit', label: '编辑用户' },
            { id: 'user_delete', label: '删除用户' }
          ]
        },
        {
          id: 'course_manage',
          label: '课程管理',
          children: [
            { id: 'course_create', label: '创建课程' },
            { id: 'course_edit', label: '编辑课程' },
            { id: 'course_delete', label: '删除课程' }
          ]
        },
        {
          id: 'evaluation_config',
          label: '评价配置',
          children: [
            { id: 'dimension_config', label: '维度配置' },
            { id: 'weight_config', label: '权重配置' },
            { id: 'rule_config', label: '规则配置' }
          ]
        },
        {
          id: 'data_collection',
          label: '数据采集',
          children: [
            { id: 'git_config', label: 'Git配置' },
            { id: 'data_sync', label: '数据同步' },
            { id: 'data_preprocess', label: '数据预处理' }
          ]
        },
        {
          id: 'score_calculation',
          label: '评分计算',
          children: [
            { id: 'algorithm_config', label: '算法配置' },
            { id: 'calculation_execute', label: '执行计算' },
            { id: 'result_review', label: '结果审核' }
          ]
        },
        {
          id: 'result_display',
          label: '结果展示',
          children: [
            { id: 'personal_report', label: '个人报告' },
            { id: 'team_comparison', label: '团队对比' },
            { id: 'trend_analysis', label: '趋势分析' }
          ]
        },
        {
          id: 'system_settings',
          label: '系统设置',
          children: [
            { id: 'user_management', label: '用户管理' },
            { id: 'permission_config', label: '权限配置' },
            { id: 'system_config', label: '系统配置' }
          ]
        }
      ],
      treeProps: {
        children: 'children',
        label: 'label'
      },
      
      // 角色对话框
      roleDialogVisible: false,
      roleForm: {
        name: '',
        description: ''
      },
      roleRules: {
        name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
      },
      
      // 系统配置
      systemConfig: {
        systemName: '学生贡献度计算系统',
        systemDescription: '基于多维度数据分析的学生贡献度评估系统',
        systemVersion: '1.0.0',
        maintenanceMode: false,
        defaultAlgorithm: 'weighted_average',
        minEvaluationPeriod: 7,
        autoCalculation: true,
        calculationFrequency: 'weekly',
        dataRetentionPeriod: 12,
        backupFrequency: 'weekly',
        autoCleanup: true,
        emailNotification: true,
        smtpServer: 'smtp.example.com',
        smtpPort: 587,
        senderEmail: 'system@example.com'
      }
    }
  },
  mounted() {
    this.initializeData()
  },
  methods: {
    initializeData() {
      this.filteredUsers = [...this.users]
      this.userTotal = this.users.length
      this.selectedRoleData = this.roles.find(role => role.id === this.selectedRole)
    },
    
    // 用户管理方法
    searchUsers() {
      this.filterUsers()
    },
    
    filterUsers() {
      let filtered = this.users
      
      if (this.userSearchKeyword) {
        filtered = filtered.filter(user => 
          user.username.toLowerCase().includes(this.userSearchKeyword.toLowerCase()) ||
          user.email.toLowerCase().includes(this.userSearchKeyword.toLowerCase())
        )
      }
      
      if (this.userRoleFilter) {
        filtered = filtered.filter(user => user.role === this.userRoleFilter)
      }
      
      this.filteredUsers = filtered
      this.userTotal = filtered.length
    },
    
    handleUserSizeChange(size) {
      this.userPageSize = size
    },
    
    handleUserCurrentChange(page) {
      this.userCurrentPage = page
    },
    
    showAddUserDialog() {
      this.userDialogTitle = '添加用户'
      this.userForm = {
        id: null,
        username: '',
        email: '',
        password: '',
        role: '',
        status: 'active'
      }
      this.userDialogVisible = true
    },
    
    editUser(user) {
      this.userDialogTitle = '编辑用户'
      this.userForm = { ...user }
      this.userDialogVisible = true
    },
    
    saveUser() {
      this.$refs.userForm.validate((valid) => {
        if (valid) {
          if (this.userForm.id) {
            // 编辑用户
            const index = this.users.findIndex(u => u.id === this.userForm.id)
            if (index !== -1) {
              this.users.splice(index, 1, { ...this.userForm })
            }
            this.$message.success('用户更新成功')
          } else {
            // 添加用户
            this.userForm.id = Date.now()
            this.userForm.createdAt = new Date().toLocaleString()
            this.userForm.lastLogin = '-'
            this.users.push({ ...this.userForm })
            this.$message.success('用户添加成功')
          }
          
          this.userDialogVisible = false
          this.filterUsers()
        }
      })
    },
    
    toggleUserStatus(user) {
      user.status = user.status === 'active' ? 'inactive' : 'active'
      this.$message.success(`用户已${user.status === 'active' ? '启用' : '禁用'}`)
    },
    
    deleteUser(user) {
      this.$confirm('确定要删除该用户吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const index = this.users.findIndex(u => u.id === user.id)
        if (index !== -1) {
          this.users.splice(index, 1)
          this.filterUsers()
          this.$message.success('用户删除成功')
        }
      })
    },
    
    getRoleType(role) {
      const types = {
        admin: 'danger',
        teacher: 'warning',
        student: 'info'
      }
      return types[role] || 'info'
    },
    
    getRoleLabel(role) {
      const labels = {
        admin: '管理员',
        teacher: '教师',
        student: '学生'
      }
      return labels[role] || role
    },
    
    // 权限管理方法
    selectRole(roleId) {
      this.selectedRole = roleId
      this.selectedRoleData = this.roles.find(role => role.id === roleId)
      
      this.$nextTick(() => {
        if (this.$refs.permissionTree) {
          this.$refs.permissionTree.setCheckedKeys(this.selectedRoleData.permissions)
        }
      })
    },
    
    savePermissions() {
      const checkedKeys = this.$refs.permissionTree.getCheckedKeys()
      this.selectedRoleData.permissions = checkedKeys
      this.$message.success('权限保存成功')
    },
    
    showAddRoleDialog() {
      this.roleForm = {
        name: '',
        description: ''
      }
      this.roleDialogVisible = true
    },
    
    saveRole() {
      this.$refs.roleForm.validate((valid) => {
        if (valid) {
          const newRole = {
            id: Date.now().toString(),
            name: this.roleForm.name,
            description: this.roleForm.description,
            userCount: 0,
            permissions: []
          }
          this.roles.push(newRole)
          this.roleDialogVisible = false
          this.$message.success('角色添加成功')
        }
      })
    },
    
    // 系统配置方法
    saveSystemConfig() {
      this.$message.success('系统配置保存成功')
    },
    
    resetSystemConfig() {
      this.$confirm('确定要重置系统配置吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 重置为默认配置
        this.systemConfig = {
          systemName: '学生贡献度计算系统',
          systemDescription: '基于多维度数据分析的学生贡献度评估系统',
          systemVersion: '1.0.0',
          maintenanceMode: false,
          defaultAlgorithm: 'weighted_average',
          minEvaluationPeriod: 7,
          autoCalculation: true,
          calculationFrequency: 'weekly',
          dataRetentionPeriod: 12,
          backupFrequency: 'weekly',
          autoCleanup: true,
          emailNotification: true,
          smtpServer: 'smtp.example.com',
          smtpPort: 587,
          senderEmail: 'system@example.com'
        }
        this.$message.success('配置重置成功')
      })
    },
    
    exportConfig() {
      const config = JSON.stringify(this.systemConfig, null, 2)
      const blob = new Blob([config], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = 'system-config.json'
      a.click()
      URL.revokeObjectURL(url)
      this.$message.success('配置导出成功')
    },
    
    importConfig() {
      const input = document.createElement('input')
      input.type = 'file'
      input.accept = '.json'
      input.onchange = (e) => {
        const file = e.target.files[0]
        if (file) {
          const reader = new FileReader()
          reader.onload = (e) => {
            try {
              const config = JSON.parse(e.target.result)
              this.systemConfig = { ...this.systemConfig, ...config }
              this.$message.success('配置导入成功')
            } catch (error) {
              this.$message.error('配置文件格式错误')
            }
          }
          reader.readAsText(file)
        }
      }
      input.click()
    }
  }
}
</script>

<style scoped>
.system-settings {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.main-content {
  padding: 20px;
  margin-top: 60px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h1 {
  color: #2c3e50;
  font-size: 32px;
  margin-bottom: 10px;
}

.page-header p {
  color: #7f8c8d;
  font-size: 16px;
}

.settings-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.management-header,
.permission-header {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 6px;
}

.user-table {
  margin-bottom: 20px;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}

.role-list {
  height: 500px;
}

.role-menu {
  border: none;
}

.permission-config {
  height: 500px;
}

.permission-tree {
  max-height: 400px;
  overflow-y: auto;
}

.config-form {
  max-width: 800px;
  margin: 0 auto;
}

.config-section {
  margin-bottom: 20px;
}

.form-actions {
  text-align: center;
  margin-top: 30px;
  padding: 20px;
  border-top: 1px solid #ebeef5;
}

.form-actions .el-button {
  margin: 0 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 10px;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  .config-form {
    max-width: 100%;
  }
  
  .role-list,
  .permission-config {
    height: auto;
    min-height: 300px;
  }
}
</style>