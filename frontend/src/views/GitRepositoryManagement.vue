<template>
  <div>
    <NavMenu />
    <div class="git-repository-management">
    <div class="page-header">
      <h2>Git仓库管理</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <i class="el-icon-plus"></i>
        添加仓库
      </el-button>
    </div>

    <!-- GitHub Token配置区域 -->
    <div class="github-config-section">
      <el-card class="config-card">
        <div slot="header" class="config-header">
          <span>GitHub Token配置</span>
          <el-button type="text" @click="toggleConfigSection">
            <i :class="showConfigSection ? 'el-icon-arrow-up' : 'el-icon-arrow-down'"></i>
          </el-button>
        </div>
        <div v-show="showConfigSection" class="config-content">
          <el-form :model="githubConfigForm" :rules="githubConfigRules" ref="githubConfigForm" label-width="120px">
            <el-form-item label="选择课程" prop="courseId">
              <div style="display: flex; align-items: center; gap: 10px;">
                <el-select 
                  v-model="githubConfigForm.courseId" 
                  placeholder="选择要配置Token的课程" 
                  style="width: 300px" 
                  :disabled="courseSelectionDisabled"
                  @change="onCourseChange">
                  <el-option
                    v-for="course in courses"
                    :key="course.courseId"
                    :label="course.courseName"
                    :value="course.courseId">
                  </el-option>
                </el-select>
                <el-button 
                  v-if="courseSelectionLocked" 
                  type="text" 
                  size="small" 
                  @click="resetCourseSelection"
                  style="color: #409EFF;">
                  重新选择课程
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="GitHub Token" prop="githubToken">
              <el-input
                v-model="githubConfigForm.githubToken"
                type="password"
                placeholder="请输入GitHub Personal Access Token"
                style="width: 400px"
                :readonly="isTokenConfigured"
                show-password>
              </el-input>
              <el-button 
                v-if="!isTokenConfigured"
                type="primary" 
                size="small" 
                @click="validateToken" 
                :loading="validatingToken"
                style="margin-left: 10px">
                验证Token
              </el-button>
              <el-button 
                v-if="isTokenConfigured"
                type="warning" 
                size="small" 
                @click="editToken" 
                style="margin-left: 10px">
                修改Token
              </el-button>
            </el-form-item>
            <el-form-item label="Token状态">
              <el-tag 
                :type="getTokenStatusType(githubConfigForm.tokenStatus)" 
                size="small">
                {{ getTokenStatusText(githubConfigForm.tokenStatus) }}
              </el-tag>
              <span v-if="githubConfigForm.tokenStatus === 'VALID'" style="margin-left: 10px; color: #67C23A; font-size: 12px;">
                Token验证成功，可以正常使用GitHub API
              </span>
              <span v-else-if="githubConfigForm.tokenStatus === 'INVALID'" style="margin-left: 10px; color: #F56C6C; font-size: 12px;">
                Token验证失败，请检查Token是否正确
              </span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveGithubConfig" :loading="savingConfig">保存配置</el-button>
              <el-button @click="resetGithubConfigForm">重置</el-button>
              <el-button type="danger" @click="deleteGithubConfig" v-if="githubConfigForm.id">删除配置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-card>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filters">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索仓库名称或URL"
            prefix-icon="el-icon-search"
            @input="handleSearch">
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="selectedCourse" placeholder="选择课程" @change="loadRepositories">
            <el-option label="全部课程" value=""></el-option>
            <el-option
              v-for="course in courses"
              :key="course.courseId"
              :label="course.courseName"
              :value="course.courseId">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="statusFilter" placeholder="仓库状态" @change="handleSearch">
            <el-option label="全部状态" value=""></el-option>
            <el-option label="活跃" value="ACTIVE"></el-option>
            <el-option label="非活跃" value="INACTIVE"></el-option>
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button @click="resetFilters">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 仓库列表 -->
    <div class="repository-list" v-loading="loading">
      <el-table 
        :data="filteredRepositories" 
        stripe 
        border
        :header-cell-style="{background:'#f5f7fa',color:'#606266',fontWeight:'bold'}"
        style="width: 100%">
        <el-table-column prop="repositoryName" label="仓库名称" min-width="150" show-overflow-tooltip align="left">
          <template slot-scope="scope">
            <span>{{ scope.row.repositoryName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="repositoryUrl" label="仓库URL" min-width="200" show-overflow-tooltip align="left">
          <template slot-scope="scope">
            <a :href="scope.row.repositoryUrl" target="_blank" style="color: #409EFF; text-decoration: none;">
              {{ scope.row.repositoryUrl }}
            </a>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="所属课程" min-width="120" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.courseName || '未分配' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="platform" label="平台" min-width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.platform === 'GitHub' ? 'success' : 'primary'" size="small">
              {{ scope.row.platform }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="branchName" label="分支" min-width="100" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.branchName || 'main' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="syncStatus" label="同步状态" min-width="120" align="center">
          <template slot-scope="scope">
            <el-tag :type="getSyncStatusType(scope.row.syncStatus)" size="small">
              {{ getSyncStatusText(scope.row.syncStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isActive" label="状态" min-width="100" align="center">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isActive ? 'success' : 'danger'" size="small">
              {{ scope.row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastSyncTime" label="最后同步" min-width="100" align="center">
          <template slot-scope="scope">
            <span>{{ scope.row.lastSyncTime ? formatDate(scope.row.lastSyncTime) : '未同步' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" align="center">
          <template slot-scope="scope">
            <el-button size="mini" @click="syncRepository(scope.row)">同步</el-button>
            <el-button size="mini" type="primary" @click="editRepository(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="deleteRepository(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页功能已移除，因为后端不支持分页 -->
    </div>

    <!-- 添加/编辑仓库对话框 -->
    <el-dialog
      :title="editingRepository ? '编辑仓库' : '添加仓库'"
      :visible.sync="showAddDialog"
      width="600px"
      @close="resetForm">
      <el-form :model="repositoryForm" :rules="formRules" ref="repositoryForm" label-width="120px">
        <el-form-item label="仓库名称" prop="name">
          <el-input v-model="repositoryForm.name" placeholder="请输入仓库名称"></el-input>
        </el-form-item>
        <el-form-item label="仓库URL" prop="url">
          <el-input v-model="repositoryForm.url" placeholder="请输入Git仓库URL"></el-input>
        </el-form-item>
        <el-form-item label="所属课程" prop="courseId">
          <el-select v-model="repositoryForm.courseId" placeholder="选择课程" style="width: 100%">
            <el-option
              v-for="course in courses"
              :key="course.courseId"
              :label="course.courseName"
              :value="course.courseId">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="Git平台" prop="platform">
          <el-select v-model="repositoryForm.platform" placeholder="选择Git平台" style="width: 100%">
            <el-option label="GitHub" value="GitHub"></el-option>
            <el-option label="GitLab" value="GitLab"></el-option>
            <el-option label="Gitee" value="Gitee"></el-option>
            <el-option label="其他" value="Other"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="repositoryForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入仓库描述">
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRepository" :loading="saving">确定</el-button>
      </div>
    </el-dialog>
    </div>
  </div>
</template>

<script>
import NavMenu from '@/components/NavMenu.vue'

export default {
  name: 'GitRepositoryManagement',
  components: {
    NavMenu
  },
  data() {
    return {
      loading: false,
      saving: false,
      showAddDialog: false,
      editingRepository: null,
      searchKeyword: '',
      selectedCourse: '',
      statusFilter: '',
      repositories: [],
      courses: [],
      organizationId: null, // 动态获取组织ID
      pagination: {
        current: 1,
        pageSize: 20,
        total: 0
      },
      repositoryForm: {
        name: '',
        url: '',  
        courseId: '',
        platform: 'GitHub',
        description: ''
      },
      // GitHub配置相关数据
      showConfigSection: true,
      savingConfig: false,
      validatingToken: false,
      courseSelectionLocked: false, // 控制课程选择是否被锁定
      githubConfigForm: {
        id: null,
        courseId: '',
        githubToken: '',
        tokenStatus: 'UNKNOWN'
      },
      githubConfigRules: {
        courseId: [
          { required: true, message: '请选择课程', trigger: 'change' }
        ],
        githubToken: [
          { required: true, message: '请输入GitHub Token', trigger: 'blur' },
          { 
            validator: (rule, value, callback) => {
              // 如果Token值为"已配置"，则跳过长度验证
              if (value === '已配置') {
                callback()
                return
              }
              // 对于新输入的Token，进行长度验证
              if (value && value.length < 40) {
                callback(new Error('GitHub Token长度至少40位'))
                return
              }
              callback()
            }, 
            trigger: 'blur' 
          }
        ]
      },
      formRules: {
        name: [
          { required: true, message: '请输入仓库名称', trigger: 'blur' }
        ],
        url: [
          { required: true, message: '请输入仓库URL', trigger: 'blur' },
          { pattern: /^https?:\/\/.+/, message: '请输入有效的URL', trigger: 'blur' }
        ],
        courseId: [
          { required: true, message: '请选择课程', trigger: 'change' }
        ],
        platform: [
          { required: true, message: '请选择Git平台', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    filteredRepositories() {
      let filtered = this.repositories
      
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        filtered = filtered.filter(repo => 
          repo.repositoryName.toLowerCase().includes(keyword) || 
          repo.repositoryUrl.toLowerCase().includes(keyword)
        )
      }
      
      if (this.statusFilter) {
        filtered = filtered.filter(repo => repo.status === this.statusFilter)
      }
      
      if (this.selectedCourse) {
        filtered = filtered.filter(repo => repo.courseId === this.selectedCourse)
      }
      
      return filtered
    },
    
    // 控制课程选择框是否禁用
    courseSelectionDisabled() {
      return this.courseSelectionLocked
    },
    
    // 判断Token是否已配置
    isTokenConfigured() {
      return this.githubConfigForm.githubToken === '已配置'
    }
  },
  async mounted() {
    this.organizationId = this.getOrganizationId()
    // 先加载课程列表，然后自动加载所有仓库
    await this.loadCourses()
    await this.loadRepositories()
  },
  methods: {
    async loadCourses() {
      try {
        const response = await this.$http.get(`/courses?organizationId=${this.organizationId}`)
        if (response.data.code === '200') {
          this.courses = response.data.data?.records || response.data.data || []
          console.log('[DEBUG] 获取到的课程列表:', this.courses.length, '个课程')
        }
      } catch (error) {
        console.error('加载课程列表失败:', error)
        this.$message.error('加载课程列表失败')
      }
    },
    
    async loadRepositories() {
      this.loading = true
      try {
        // 如果没有课程列表，先加载课程
        if (!this.courses || this.courses.length === 0) {
          console.log('[DEBUG] 没有课程列表，先加载课程')
          await this.loadCourses()
        }
        
        // 如果仍然没有课程，返回空数组
        if (!this.courses || this.courses.length === 0) {
          console.log('[DEBUG] 没有可用的课程，返回空数组')
          this.repositories = []
          return
        }
        
        console.log('[DEBUG] 开始为所有课程加载仓库，课程数量:', this.courses.length)
        
        // 为所有课程并发获取仓库
        const repositoryPromises = this.courses.map(async (course) => {
          try {
            const url = '/git-data/repositories'
            const courseIdValue = course.courseId
            const params = { courseId: courseIdValue }
            
            console.log(`[DEBUG] 为课程 ${course.courseName}(courseId: ${courseIdValue}) 获取仓库`)
            
            const response = await this.$http.get(url, { params })
            
            console.log(`[DEBUG] 课程 ${course.courseName} 的API响应:`, {
              status: response.status,
              data: response.data
            })
            
            if (response.data.code === '200') {
              const repositories = response.data.data || []
              // 为每个仓库添加课程信息
              return repositories.map(repo => ({
                ...repo,
                courseName: course.courseName,
                courseId: courseIdValue
              }))
            } else {
              console.log(`[DEBUG] 课程 ${course.courseName} API返回失败:`, response.data)
              return []
            }
          } catch (error) {
            console.error(`[DEBUG] 课程 ${course.courseName} 加载仓库失败:`, error)
            return []
          }
        })
        
        // 等待所有请求完成并合并结果
        const allRepositories = await Promise.all(repositoryPromises)
        this.repositories = allRepositories.flat()
        
        console.log('[DEBUG] 所有仓库加载完成，总数量:', this.repositories.length)
        console.log('[DEBUG] 合并后的仓库列表:', this.repositories)
        
      } catch (error) {
        console.error('[DEBUG] 加载仓库列表失败 - 详细错误信息:', {
          message: error.message,
          response: error.response,
          request: error.request,
          config: error.config
        })
        this.$message.error('加载仓库列表失败')
      } finally {
        this.loading = false
      }
    },
    

    
    async saveRepository() {
      console.log('[DEBUG] saveRepository方法被调用')
      console.log('[DEBUG] 表单验证开始')
      
      this.$refs.repositoryForm.validate(async (valid) => {
        console.log('[DEBUG] 表单验证结果:', valid)
        if (!valid) {
          console.log('[DEBUG] 表单验证失败，终止提交')
          return
        }
        
        console.log('[DEBUG] 开始保存仓库，当前表单数据:', JSON.stringify(this.repositoryForm, null, 2))
        console.log('[DEBUG] 是否为编辑模式:', !!this.editingRepository)
        if (this.editingRepository) {
          console.log('[DEBUG] 编辑的仓库信息:', JSON.stringify(this.editingRepository, null, 2))
        }
        
        this.saving = true
        try {
          let response
          if (this.editingRepository) {
            // 更新仓库
            const updateUrl = `/git-data/repositories/${this.editingRepository.repositoryId}`
            console.log('[DEBUG] 发送PUT请求更新仓库')
            console.log('[DEBUG] 请求URL:', updateUrl)
            console.log('[DEBUG] 请求参数:', JSON.stringify(this.repositoryForm, null, 2))
            
            // 将前端表单字段映射为后端期望的字段名
            const requestData = {
              repositoryName: this.repositoryForm.name,
              repositoryUrl: this.repositoryForm.url,
              courseId: this.repositoryForm.courseId,
              platform: this.repositoryForm.platform,
              description: this.repositoryForm.description
            }
            console.log('[DEBUG] 映射后的请求参数:', JSON.stringify(requestData, null, 2))
            
            response = await this.$http.put(updateUrl, requestData)
            console.log('[DEBUG] 更新仓库响应:', JSON.stringify(response, null, 2))
            this.$message.success('仓库更新成功')
          } else {
            // 创建仓库
            const createUrl = '/git-data/repositories'
            console.log('[DEBUG] 发送POST请求创建仓库')
            console.log('[DEBUG] 请求URL:', createUrl)
            console.log('[DEBUG] 请求参数:', JSON.stringify(this.repositoryForm, null, 2))
            
            // 将前端表单字段映射为后端期望的字段名
            const requestData = {
              repositoryName: this.repositoryForm.name,
              repositoryUrl: this.repositoryForm.url,
              courseId: this.repositoryForm.courseId,
              platform: this.repositoryForm.platform,
              description: this.repositoryForm.description
            }
            console.log('[DEBUG] 映射后的请求参数:', JSON.stringify(requestData, null, 2))
            
            response = await this.$http.post(createUrl, requestData)
            console.log('[DEBUG] 创建仓库响应:', JSON.stringify(response, null, 2))
            this.$message.success('仓库创建成功')
          }
          
          console.log('[DEBUG] 仓库保存成功，关闭对话框并重新加载数据')
          this.showAddDialog = false
          this.loadRepositories()
        } catch (error) {
          console.error('[DEBUG] 保存仓库失败 - 详细错误信息:', {
            message: error.message,
            response: error.response,
            request: error.request,
            config: error.config,
            stack: error.stack
          })
          
          if (error.response) {
            console.error('[DEBUG] 服务器响应错误:', {
              status: error.response.status,
              statusText: error.response.statusText,
              data: error.response.data,
              headers: error.response.headers
            })
          } else if (error.request) {
            console.error('[DEBUG] 请求发送失败，未收到响应:', error.request)
          } else {
            console.error('[DEBUG] 请求配置错误:', error.message)
          }
          
          this.$message.error('保存仓库失败')
        } finally {
          console.log('[DEBUG] 保存操作完成，重置saving状态')
          this.saving = false
        }
      })
    },
    
    editRepository(repository) {
      this.editingRepository = repository
      this.repositoryForm = {
        name: repository.repositoryName,
        url: repository.repositoryUrl,
        courseId: repository.courseId,
        platform: repository.platform || 'GitHub',
        description: repository.description || ''
      }
      this.showAddDialog = true
    },
    
    async deleteRepository(repository) {
      this.$confirm('确定要删除这个仓库吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await this.$http.delete(`/git-data/repositories/${repository.repositoryId}`)
          this.$message.success('仓库删除成功')
          this.loadRepositories()
        } catch (error) {
          console.error('删除仓库失败:', error)
          this.$message.error('删除仓库失败')
        }
      })
    },
    
    async syncRepository(repository) {
      try {
        await this.$http.post(`/git-data/repositories/${repository.repositoryId}/sync`)
        this.$message.success('仓库同步成功')
        this.loadRepositories()
      } catch (error) {
        console.error('同步仓库失败:', error)
        this.$message.error('同步仓库失败')
      }
    },
    
    handleSearch() {
      // 搜索在computed中处理
    },
    
    resetFilters() {
      this.searchKeyword = ''
      this.selectedCourse = ''
      this.statusFilter = ''
    },
    
    resetForm() {
      this.editingRepository = null
      this.repositoryForm = {
        name: '',
        url: '',
        courseId: '',
        platform: 'GitHub',
        description: ''
      }
      this.$refs.repositoryForm && this.$refs.repositoryForm.resetFields()
    },
    
    handleSizeChange(size) {
      // 后端不支持分页，保留方法但不执行操作
    },
    
    handleCurrentChange(page) {
      // 后端不支持分页，保留方法但不执行操作
    },
    
    formatDate(dateString) {
      if (!dateString) return '未同步'
      return new Date(dateString).toLocaleString('zh-CN')
    },
    
    // 获取组织ID（从store或localStorage获取）
    getOrganizationId() {
      return this.$store?.state?.user?.organizationId || localStorage.getItem('organizationId') || 1
    },
    
    // 获取同步状态对应的标签类型
    getSyncStatusType(syncStatus) {
      switch (syncStatus) {
        case 'success':
          return 'success'
        case 'failed':
          return 'danger'
        case 'syncing':
          return 'warning'
        default:
          return 'info'
      }
    },
    
    // 获取同步状态对应的文本
    getSyncStatusText(syncStatus) {
      switch (syncStatus) {
        case 'success':
          return '成功'
        case 'failed':
          return '失败'
        case 'syncing':
          return '同步中'
        default:
          return '待同步'
      }
    },

    // GitHub配置相关方法
    toggleConfigSection() {
      this.showConfigSection = !this.showConfigSection
    },

    async loadGithubConfig() {
      if (!this.githubConfigForm.courseId) {
        this.resetGithubConfigForm()
        this.courseSelectionLocked = false
        return
      }
      
      try {
        const response = await this.$http.get(`/github-config/course/${this.githubConfigForm.courseId}`)
        console.log('=== GitHub配置API响应 ===');
        console.log('API响应完整数据:', response.data);
        
        if (response.data.code === '200' && response.data.data) {
          const config = response.data.data
          console.log('=== 获取到的Token配置信息 ===');
          console.log('配置ID:', config.id);
          console.log('课程ID:', config.courseId);
          console.log('Token状态:', config.tokenStatus);
          console.log('创建时间:', config.createdAt || config.created_at);
          console.log('更新时间:', config.updatedAt || config.updated_at);
          // 检查Token是否为脱敏数据
          const isDesensitizedToken = config.githubToken && (config.githubToken === '已配置' || config.githubToken.includes('已配置'));
          console.log('Token原始值:', config.githubToken);
          console.log('是否为脱敏数据:', isDesensitizedToken);
          console.log('Token长度:', isDesensitizedToken ? '已配置(脱敏)' : (config.githubToken ? config.githubToken.length + '字符' : '0字符'));
          console.log('Token前缀:', isDesensitizedToken ? '已配置...' : (config.githubToken ? (config.githubToken.length > 8 ? config.githubToken.substring(0, 8) + '...' : config.githubToken) : '无'));
          
          this.githubConfigForm = {
            id: config.id,
            courseId: config.courseId,
            githubToken: config.githubToken || '',
            tokenStatus: this.normalizeTokenStatus(config.tokenStatus)
          }
          
          // 如果token状态为有效，则锁定课程选择
          const isValidToken = this.isTokenValid(config.tokenStatus)
          
          if (isValidToken) {
            this.courseSelectionLocked = true
            this.$message.success('已加载该课程的有效GitHub配置')
            console.log('✅ Token状态有效，已锁定课程选择，原始状态:', config.tokenStatus, '标准化状态:', this.githubConfigForm.tokenStatus);
          } else {
            this.courseSelectionLocked = false
            console.log('⚠️ Token状态无效或未知，课程选择未锁定，原始状态:', config.tokenStatus, '标准化状态:', this.githubConfigForm.tokenStatus);
          }
        } else {
          console.log('❌ 未找到该课程的GitHub配置');
          // 没有找到配置，重置表单但保留courseId
          const courseId = this.githubConfigForm.courseId
          this.resetGithubConfigForm()
          this.githubConfigForm.courseId = courseId
          this.courseSelectionLocked = false
        }
      } catch (error) {
        console.error('加载GitHub配置失败:', error)
        if (error.response && error.response.status !== 404) {
          this.$message.error('加载GitHub配置失败')
        }
        // 404错误表示没有配置，这是正常情况
        const courseId = this.githubConfigForm.courseId
        this.resetGithubConfigForm()
        this.githubConfigForm.courseId = courseId
        this.courseSelectionLocked = false
      }
    },

    async saveGithubConfig() {
      this.$refs.githubConfigForm.validate(async (valid) => {
        if (!valid) {
          return
        }
        
        this.savingConfig = true
        try {
          const requestData = {
            courseId: this.githubConfigForm.courseId,
            githubToken: this.githubConfigForm.githubToken
          }
          
          const response = await this.$http.post('/github-config/save', requestData)
          if (response.data.code === '200') {
            this.$message.success('GitHub配置保存成功')
            // 重新加载配置以获取最新状态
            await this.loadGithubConfig()
          } else {
            this.$message.error(response.data.message || '保存配置失败')
          }
        } catch (error) {
          console.error('保存GitHub配置失败:', error)
          this.$message.error('保存GitHub配置失败')
        } finally {
          this.savingConfig = false
        }
      })
    },

    async validateToken() {
      if (!this.githubConfigForm.githubToken) {
        this.$message.warning('请先输入GitHub Token')
        return
      }
      
      this.validatingToken = true
      try {
        const response = await this.$http.post('/github-config/validate', {
          githubToken: this.githubConfigForm.githubToken
        })
        
        if (response.data.code === '200') {
          this.githubConfigForm.tokenStatus = 'VALID'
          this.$message.success('Token验证成功')
        } else {
          this.githubConfigForm.tokenStatus = 'INVALID'
          this.$message.error('Token验证失败')
        }
      } catch (error) {
        console.error('验证Token失败:', error)
        this.githubConfigForm.tokenStatus = 'INVALID'
        this.$message.error('Token验证失败')
      } finally {
        this.validatingToken = false
      }
    },

    async deleteGithubConfig() {
      if (!this.githubConfigForm.id) {
        this.$message.warning('没有可删除的配置')
        return
      }
      
      this.$confirm('确定要删除这个GitHub配置吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await this.$http.delete(`/github-config/${this.githubConfigForm.id}`)
          this.$message.success('GitHub配置删除成功')
          this.resetGithubConfigForm()
        } catch (error) {
          console.error('删除GitHub配置失败:', error)
          this.$message.error('删除GitHub配置失败')
        }
      })
    },

    // 课程选择变化事件处理
    async onCourseChange() {
      console.log('=== 课程选择变化 ===');
      console.log('选择的课程ID:', this.githubConfigForm.courseId);
      
      // 查找选择的课程信息
      const selectedCourse = this.courses.find(course => course.courseId === this.githubConfigForm.courseId);
      console.log('选择的课程信息:', selectedCourse);
      
      await this.loadGithubConfig();
    },
    
    // 重新选择课程
    resetCourseSelection() {
      this.courseSelectionLocked = false
      this.resetGithubConfigForm()
      this.$message.info('已重置课程选择，可以重新选择其他课程')
    },

    resetGithubConfigForm() {
      this.githubConfigForm = {
        id: null,
        courseId: '',
        githubToken: '',
        tokenStatus: 'UNKNOWN'
      }
      this.courseSelectionLocked = false
      this.$refs.githubConfigForm && this.$refs.githubConfigForm.resetFields()
    },

    getTokenStatusType(status) {
      switch (status) {
        case 'VALID':
          return 'success'
        case 'INVALID':
          return 'danger'
        case 'EXPIRED':
          return 'warning'
        default:
          return 'info'
      }
    },

    getTokenStatusText(status) {
      switch (status) {
        case 'VALID':
          return '有效'
        case 'INVALID':
          return '无效'
        case 'EXPIRED':
          return '已过期'
        default:
          return '未知'
      }
    },

    // 标准化Token状态
    normalizeTokenStatus(status) {
      if (!status) return 'UNKNOWN';
      
      const statusLower = status.toString().toLowerCase();
      
      // 将各种有效状态标准化为 'VALID'
      if (statusLower === 'active' || statusLower === 'valid') {
        return 'VALID';
      }
      
      // 将各种无效状态标准化为 'INVALID'
      if (statusLower === 'inactive' || statusLower === 'invalid') {
        return 'INVALID';
      }
      
      // 过期状态
      if (statusLower === 'expired') {
        return 'EXPIRED';
      }
      
      // 其他情况保持原状态或返回未知
      return status.toUpperCase();
    },

    // 检查Token是否有效
    isTokenValid(status) {
      if (!status) return false;
      
      const statusLower = status.toString().toLowerCase();
      return statusLower === 'active' || statusLower === 'valid';
    },
    
    // 修改Token
    editToken() {
      this.$confirm('确定要修改GitHub Token吗？修改后需要重新验证。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 清空Token输入框，允许用户输入新Token
        this.githubConfigForm.githubToken = ''
        this.githubConfigForm.tokenStatus = 'UNKNOWN'
        this.$message.info('请输入新的GitHub Token')
      })
    }
  }
}
</script>

<style scoped>
.git-repository-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.filters {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
}

.repository-list {
  background: white;
  border-radius: 4px;
  overflow: hidden;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.dialog-footer {
  text-align: right;
}

.el-table {
  border: 1px solid #ebeef5;
}

.el-link {
  font-weight: 500;
}

/* GitHub配置区域样式 */
.github-config-section {
  margin-bottom: 20px;
}

.config-card {
  border: 1px solid #e4e7ed;
}

.config-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
}

.config-content {
  padding-top: 10px;
}

.config-content .el-form-item {
  margin-bottom: 18px;
}

.config-content .el-form-item:last-child {
  margin-bottom: 0;
}
</style>