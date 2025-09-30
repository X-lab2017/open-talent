<template>
  <div class="evaluation-config">
    <NavMenu></NavMenu>
    <el-main>
      <div class="page-header">
        <div class="header-left">
          <h2 class="title">评价配置</h2>
          <div class="course-selector">
            <el-select 
              v-model="currentCourseId"   
              placeholder="请选择课程"
              @change="onCourseChange"
              @clear="onCourseClear"
              clearable
              style="width: 300px;"
            >
              <el-option
                v-for="course in courseList"
                :key="course.courseId"
                :label="course.courseName"
                :value="course.courseId"
              >
              </el-option>
            </el-select>
          </div>
        </div>
        <el-button type="primary" @click="saveAllConfig" :disabled="!currentCourseId">
          <i class="el-icon-check"></i> 保存配置
        </el-button>
      </div>

      <!-- 课程选择提示 -->
      <el-alert
        v-if="!currentCourseId"
        title="请先选择课程"
        description="请在上方选择要配置评价维度的课程，然后进行相关配置。"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 20px;"
      ></el-alert>

      <!-- 只有选择了课程才显示配置tabs -->
      <el-tabs v-if="currentCourseId" v-model="activeTab" type="card">
        <!-- 评价维度配置 -->
        <el-tab-pane label="评价维度" name="dimensions">
          <div class="config-section">
            <div class="section-header">
              <h3>评价维度设置</h3>
            </div>
            
            <!-- 无配置提示区域 -->
            <div v-if="showNoConfigPrompt" class="no-config-prompt">
              <el-alert
                title="该课程暂无评价配置"
                type="info"
                description="您可以选择应用默认配置，或者手动添加评价维度"
                show-icon
                :closable="false"
              >
              </el-alert>
              <div class="prompt-actions">
                <el-button type="primary" @click="applyDefaultConfig">
                  <i class="el-icon-setting"></i> 应用默认配置
                </el-button>
                <el-button type="default" @click="hidePrompt">
                  <i class="el-icon-plus"></i> 手动添加维度
                </el-button>
              </div>
            </div>
            
            <div class="dimensions-list">
              <el-card 
                v-for="(dimension, index) in dimensions" 
                :key="dimension.id" 
                class="dimension-card"
                shadow="hover"
              >
                <div class="dimension-header">
                  <el-input 
                    v-model="dimension.name" 
                    placeholder="维度名称"
                    class="dimension-name"
                  ></el-input>
                  <div class="dimension-actions">
                    <el-button 
                      type="text" 
                      size="small" 
                      @click="removeDimension(index)"
                      class="danger"
                    >
                      <i class="el-icon-delete"></i>
                    </el-button>
                  </div>
                </div>
                
                <el-form :model="dimension" label-width="100px" size="small">
                  <el-form-item label="权重">
                    <el-slider 
                      v-model="dimension.weight" 
                      :min="0" 
                      :max="100" 
                      show-input
                      :show-input-controls="false"
                    ></el-slider>
                  </el-form-item>
                  
                  <el-form-item label="描述">
                    <el-input 
                      type="textarea" 
                      v-model="dimension.description" 
                      placeholder="请输入维度描述"
                      :rows="2"
                    ></el-input>
                  </el-form-item>
                  
                  <el-form-item label="计算方式">
                    <el-select v-model="dimension.calculationType" placeholder="选择计算方式">
                      <el-option label="代码提交" value="code_commits"></el-option>
                      <el-option label="问题参与" value="issue_participation"></el-option>
                      <el-option label="PR审查" value="pr_reviews"></el-option>
                      <el-option label="文档贡献" value="documentation"></el-option>
                    </el-select>
                  </el-form-item>
                </el-form>
              </el-card>
            </div>
            
            <!-- 维度列表底部的添加按钮，只在有维度时显示 -->
            <div v-if="dimensions.length > 0" class="add-dimension-bottom">
              <el-button type="primary" size="small" @click="addDimension" plain>
                <i class="el-icon-plus"></i> 添加维度
              </el-button>
            </div>
            
            <div class="weight-summary">
              <el-alert 
                :title="`总权重: ${totalWeight}%`" 
                :type="totalWeight === 100 ? 'success' : 'warning'"
                :description="totalWeight !== 100 ? '权重总和应为100%' : '权重配置正确'"
                show-icon
              ></el-alert>
            </div>
          </div>
        </el-tab-pane>

        <!-- 评分规则配置 -->
        <el-tab-pane label="评分规则" name="rules">
          <div class="config-section">
            <div class="section-header">
              <h3>评分规则设置</h3>
            </div>
            
            <el-form :model="scoringRules" label-width="120px">
              <el-form-item label="评分方式">
                <el-radio-group v-model="scoringRules.method">
                  <el-radio label="relative">相对评分</el-radio>
                  <el-radio label="absolute">绝对评分</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="计算方法">
                <el-radio-group v-model="scoringRules.calculationMethod">
                  <el-radio label="weighted_average">加权求和</el-radio>
                  <el-radio label="normalized_score">标准化评分</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="分数范围">
                <div class="score-range">
                  <el-input-number 
                    v-model="scoringRules.minScore" 
                    :min="0" 
                    :max="scoringRules.maxScore - 1"
                    label="最低分"
                  ></el-input-number>
                  <span class="range-separator">-</span>
                  <el-input-number 
                    v-model="scoringRules.maxScore" 
                    :min="scoringRules.minScore + 1" 
                    :max="1000"
                    label="最高分"
                  ></el-input-number>
                </div>
              </el-form-item>
              
              <el-form-item label="等级划分">
                <div class="grade-config">
                  <div v-for="(grade, index) in scoringRules.grades" :key="index" class="grade-item">
                    <el-input v-model="grade.name" placeholder="等级名称" style="width: 100px;"></el-input>
                    <el-input-number 
                      v-model="grade.minScore" 
                      :min="0" 
                      :max="scoringRules.maxScore"
                      style="width: 120px; margin: 0 10px;"
                    ></el-input-number>
                    <span>-</span>
                    <el-input-number 
                      v-model="grade.maxScore" 
                      :min="grade.minScore" 
                      :max="scoringRules.maxScore"
                      style="width: 120px; margin: 0 10px;"
                    ></el-input-number>
                    <el-button 
                      type="text" 
                      @click="removeGrade(index)"
                      class="danger"
                    >
                      <i class="el-icon-delete"></i>
                    </el-button>
                  </div>
                  <el-button type="text" @click="addGrade">
                    <i class="el-icon-plus"></i> 添加等级
                  </el-button>
                </div>
              </el-form-item>
              
              <el-form-item label="异常处理">
                <el-checkbox-group v-model="scoringRules.exceptionHandling">
                  <el-checkbox label="ignore_inactive">忽略不活跃成员</el-checkbox>
                  <el-checkbox label="normalize_outliers">标准化异常值</el-checkbox>
                  <el-checkbox label="manual_review">人工审核异常</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 算法参数配置 -->
        <el-tab-pane label="算法参数" name="algorithm">
          <div class="config-section">
            <div class="section-header">
              <h3>算法参数设置</h3>
            </div>
            
            <el-form :model="algorithmConfig" label-width="150px">
              <el-form-item label="时间窗口(天)">
                <el-input-number 
                  v-model="algorithmConfig.timeWindow" 
                  :min="1" 
                  :max="365"
                ></el-input-number>
              </el-form-item>
              
              <el-form-item label="最小提交数">
                <el-input-number 
                  v-model="algorithmConfig.minCommits" 
                  :min="0" 
                  :max="1000"
                ></el-input-number>
              </el-form-item>
              
              <el-form-item label="协作权重">
                <el-slider 
                  v-model="algorithmConfig.collaborationWeight" 
                  :min="0" 
                  :max="100" 
                  show-input
                ></el-slider>
              </el-form-item>
              
              <el-form-item label="质量权重">
                <el-slider 
                  v-model="algorithmConfig.qualityWeight" 
                  :min="0" 
                  :max="100" 
                  show-input
                ></el-slider>
              </el-form-item>
              
              <el-form-item label="活跃度阈值">
                <el-input-number 
                  v-model="algorithmConfig.activityThreshold" 
                  :min="0" 
                  :max="1" 
                  :step="0.1"
                  :precision="1"
                ></el-input-number>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>


      </el-tabs>
    </el-main>
  </div>
</template>

<script>
import NavMenu from '@/components/NavMenu.vue'

export default {
  name: 'EvaluationConfig',
  components: {
    NavMenu
  },
  data() {
    return {
      activeTab: 'dimensions',
      currentCourseId: null,
      configId: null,
      courseList: [],
      showNoConfigPrompt: false,
      dimensions: [
        {
          id: 1,
          name: '代码提交',
          weight: 40,
          description: '基于代码提交量的贡献度评估',
          calculationType: 'code_commits'
        },
        {
          id: 2,
          name: '问题参与',
          weight: 20,
          description: '基于Issue参与度的评估',
          calculationType: 'issue_participation'
        },
        {
          id: 3,
          name: 'PR审查',
          weight: 20,
          description: '基于Pull Request审查的评估',
          calculationType: 'pr_reviews'
        },
        {
          id: 4,
          name: '文档贡献',
          weight: 20,
          description: '基于文档贡献的评估',
          calculationType: 'documentation'
        }
      ],
      scoringRules: {
        method: 'relative',
        calculationMethod: 'weighted_average',
        minScore: 0,
        maxScore: 100,
        grades: [
          { name: '优秀', minScore: 90, maxScore: 100 },
          { name: '良好', minScore: 80, maxScore: 89 },
          { name: '中等', minScore: 70, maxScore: 79 },
          { name: '及格', minScore: 60, maxScore: 69 },
          { name: '不及格', minScore: 0, maxScore: 59 }
        ],
        exceptionHandling: ['normalize_outliers']
      },
      algorithmConfig: {
        timeWindow: 30,
        minCommits: 5,
        collaborationWeight: 30,
        qualityWeight: 40,
        activityThreshold: 0.3
      },

      nextDimensionId: 4
    }
  },
  computed: {
    totalWeight() {
      return this.dimensions.reduce((sum, dim) => sum + dim.weight, 0)
    }
  },
  async mounted() {
    // 只获取课程列表，不自动初始化courseId
    await this.fetchCourseList()
  },
  methods: {
    async fetchCourseList() {
      try {
        const organizationId = this.getOrganizationId()
        const requestParams = {
          organizationId: organizationId,
          pageNum: 1,
          pageSize: 100
        }
        
        const response = await this.$http.get('/courses', {
          params: requestParams
        })
        
        // 修正响应数据判断逻辑：后端返回的是code字段，200表示成功
        if (response.data.code === '200' || response.data.code === 200) {
          this.courseList = response.data.data?.records || response.data.data || []
          console.log('获取到的课程列表:', this.courseList)
        } else {
          this.$message.error('获取课程列表失败: ' + (response.data.msg || '未知错误'))
        }
      } catch (error) {
        console.error('获取课程列表失败：', error)
        this.$message.error('获取课程列表失败: ' + (error.response?.data?.msg || error.message || '网络错误'))
      }
    },
    onCourseChange(courseId) {
      this.currentCourseId = courseId
      this.configId = null // 重置配置ID
      
      if (courseId) {
        // 保存选择的课程ID到localStorage
        localStorage.setItem('currentCourseId', courseId)
        this.loadConfig() // 重新加载配置
      } else {
        // 清除localStorage中的课程ID
        localStorage.removeItem('currentCourseId')
        // 重置为默认配置状态
        this.resetToDefaultConfig()
      }
    },
    onCourseClear() {
      // 清除课程选择
      this.currentCourseId = null
      this.configId = null
      // 清除localStorage中的课程ID
      localStorage.removeItem('currentCourseId')
      // 重置为默认配置状态
      this.resetToDefaultConfig()
      this.$message.info('已清除课程选择')
    },
    initializeCourseId() {
      // 确保进入界面时不会默认绑定任何课程
      this.currentCourseId = null
      // 清理可能存在的localStorage中的courseId
      localStorage.removeItem('currentCourseId')
      console.log('已重置课程ID为null，确保不会默认选择任何课程')
    },
    validateCurrentCourseId() {
      if (!this.currentCourseId) {
        console.log('当前课程ID为空，无需验证')
        return
      }
      
      // 检查当前课程ID是否在课程列表中存在
      const courseExists = this.courseList.some(course => 
        course.courseId === this.currentCourseId || 
        course.id === this.currentCourseId
      )
      
      console.log('验证课程ID存在性:', {
        currentCourseId: this.currentCourseId,
        courseExists: courseExists,
        courseList: this.courseList.map(c => ({ id: c.id || c.courseId, name: c.courseName }))
      })
      
      if (!courseExists) {
        console.warn('当前课程ID不存在于课程列表中，重置为null')
        this.currentCourseId = null
        localStorage.removeItem('currentCourseId')
        this.$message.warning('之前选择的课程不存在，请重新选择课程')
      }
    },
    async loadConfig() {
      if (!this.currentCourseId) {
        console.warn('未选择课程，重置为默认配置')
        this.resetToDefaultConfig()
        return
      }
      
      try {
        const response = await this.$http.get(`/evaluation/config/course/${this.currentCourseId}`)
        // 修正响应判断逻辑：使用code字段判断成功
        if ((response.data.code === '200' || response.data.code === 200) && response.data.data) {
          const config = response.data.data
          this.configId = config.configId
          console.log('加载到现有配置:', config)
          
          // 解析维度权重配置
          if (config.dimensionWeights) {
            const weights = typeof config.dimensionWeights === 'string' 
              ? JSON.parse(config.dimensionWeights) 
              : config.dimensionWeights
            this.updateDimensionsFromWeights(weights)
          }
          
          // 设置计算方法
          if (config.calculationMethod) {
            this.scoringRules.calculationMethod = config.calculationMethod
          }
          
          this.showNoConfigPrompt = false
          this.$message.success('已加载该课程的现有配置')
        } else {
          // 配置不存在，显示提示让用户选择
          console.log('该课程暂无配置')
          this.dimensions = []
          this.scoringRules = this.getDefaultScoringRules()
          this.algorithmConfig = this.getDefaultAlgorithmConfig()
          this.configId = null
          this.showNoConfigPrompt = true
          this.$message.info('该课程暂无配置')
        }
      } catch (error) {
        console.error('加载配置失败：', error)
        // 出错时显示提示让用户选择
        this.dimensions = []
        this.scoringRules = this.getDefaultScoringRules()
        this.algorithmConfig = this.getDefaultAlgorithmConfig()
        this.configId = null
        this.showNoConfigPrompt = true
        this.$message.error('加载配置失败')
      }
    },
    async loadDefaultConfig() {
      try {
        const response = await this.$http.get('/evaluation/config/default-weights')
        if (response.data.code === '200' && response.data.data) {
          this.updateDimensionsFromWeights(response.data.data)
        }
      } catch (error) {
        console.error('加载默认配置失败：', error)
      }
    },
    resetToDefaultConfig() {
      // 重置为默认配置状态
      this.configId = null
      this.dimensions = [
        {
          id: 1,
          name: '代码提交',
          weight: 40,
          description: '基于代码提交量的贡献度评估',
          calculationType: 'code_commits'
        },
        {
          id: 2,
          name: '问题参与',
          weight: 20,
          description: '基于Issue参与度的评估',
          calculationType: 'issue_participation'
        },
        {
          id: 3,
          name: 'PR审查',
          weight: 20,
          description: '基于Pull Request审查的评估',
          calculationType: 'pr_reviews'
        },
        {
          id: 4,
          name: '文档贡献',
          weight: 20,
          description: '基于文档贡献的评估',
          calculationType: 'documentation'
        }
      ]
      this.scoringRules.calculationMethod = 'weighted_average'
    },
    updateDimensionsFromWeights(weights) {
      // 根据权重配置更新维度
      this.dimensions.forEach(dimension => {
        const weight = weights[dimension.calculationType]
        if (weight !== undefined) {
          dimension.weight = Math.round(weight * 100) // 转换为百分比
        }
      })
    },
    async saveAllConfig() {
      if (this.totalWeight !== 100) {
        this.$message.error('权重总和必须为100%')
        return
      }
      
      if (!this.currentCourseId) {
        this.$message.error('请先选择要配置的课程')
        return
      }
      
      try {
        // 构建维度权重配置
        const dimensionWeights = {}
        let totalDecimalWeight = 0
        
        this.dimensions.forEach(dimension => {
          const decimalWeight = parseFloat((dimension.weight / 100).toFixed(4)) // 保留4位小数精度
          dimensionWeights[dimension.calculationType] = decimalWeight
          totalDecimalWeight += decimalWeight
        })
        
        // 修正浮点数精度问题，确保总和精确等于1.0
        totalDecimalWeight = parseFloat(totalDecimalWeight.toFixed(4))
        if (Math.abs(totalDecimalWeight - 1.0) > 0.0001) {
          // 如果总和不等于1.0，调整最后一个维度的权重
          const lastDimension = this.dimensions[this.dimensions.length - 1]
          const adjustment = parseFloat((1.0 - (totalDecimalWeight - dimensionWeights[lastDimension.calculationType])).toFixed(4))
          dimensionWeights[lastDimension.calculationType] = adjustment
        }
        
        // 添加调试信息
        console.log('权重配置调试信息:', {
          originalWeights: this.dimensions.map(d => ({ type: d.calculationType, weight: d.weight })),
          decimalWeights: dimensionWeights,
          totalDecimalWeight: Object.values(dimensionWeights).reduce((sum, w) => sum + w, 0)
        })
        
        // 验证权重配置
        const validateResponse = await this.$http.post('/evaluation/config/validate-weights', dimensionWeights)
        
        // 添加详细的验证响应调试信息
        console.log('权重验证响应调试信息:', {
          fullResponse: validateResponse,
          responseData: validateResponse.data,
          code: validateResponse.data.code,
          data: validateResponse.data.data,
          msg: validateResponse.data.msg
        })
        
        if (validateResponse.data.code !== '200' || validateResponse.data.data !== true) {
          console.error('权重验证失败，响应详情:', validateResponse.data)
          this.$message.error(validateResponse.data.msg || '权重配置验证失败，权重总和必须等于1.0')
          return
        }
        
        console.log('权重验证通过，继续保存配置')
        
        // 获取当前选中课程的名称
        const currentCourse = this.courseList.find(course => 
          course.courseId === this.currentCourseId || course.id === this.currentCourseId
        )
        const courseName = currentCourse ? currentCourse.courseName : ''
        
        const requestData = {
          courseId: this.currentCourseId,
          courseName: courseName,
          organizationId: this.getOrganizationId(),
          dimensionWeights: dimensionWeights,
          calculationMethod: this.scoringRules.calculationMethod
        }
        
        // 添加详细的调试信息
        console.log('发送给后端的完整请求数据:', {
          requestData: requestData,
          dimensionWeightsDetail: Object.entries(dimensionWeights).map(([key, value]) => ({
            key: key,
            value: value,
            type: typeof value,
            precision: value.toString().split('.')[1]?.length || 0
          })),
          totalWeight: Object.values(dimensionWeights).reduce((sum, weight) => sum + weight, 0),
          totalWeightPrecision: Object.values(dimensionWeights).reduce((sum, weight) => sum + weight, 0).toString().split('.')[1]?.length || 0
        })
        
        let response
        console.log('开始保存配置，configId:', this.configId)
        
        if (this.configId) {
          // 更新现有配置
          console.log('更新现有配置，URL:', `/evaluation/config/${this.configId}`)
          response = await this.$http.put(`/evaluation/config/${this.configId}`, requestData)
        } else {
          // 创建新配置
          console.log('创建新配置，URL:', '/evaluation/config')
          response = await this.$http.post('/evaluation/config', requestData)
        }
        
        // 添加详细的响应调试信息
        console.log('保存配置API响应详情:', {
          fullResponse: response,
          status: response.status,
          statusText: response.statusText,
          data: response.data,
          headers: response.headers
        })
        
        if (response.data && (response.data.code === '200' || response.data.code === 200)) {
          console.log('配置保存成功')
          this.$message.success('配置保存成功')
          // 重新加载配置以获取最新的configId
          await this.loadConfig()
        } else {
          console.error('配置保存失败，响应数据:', response.data)
          const errorMessage = response.data?.message || response.data?.msg || '配置保存失败'
          console.error('错误消息:', errorMessage)
          this.$message.error(errorMessage)
        }
      } catch (error) {
        console.error('保存配置失败，详细错误信息:', {
          error: error,
          message: error.message,
          stack: error.stack,
          response: error.response,
          responseData: error.response?.data,
          responseStatus: error.response?.status,
          responseStatusText: error.response?.statusText,
          config: error.config
        })
        
        let errorMessage = '保存配置失败'
        if (error.response?.data?.message) {
          errorMessage = error.response.data.message
        } else if (error.response?.data?.msg) {
          errorMessage = error.response.data.msg
        } else if (error.message) {
          errorMessage = `保存配置失败: ${error.message}`
        }
        
        console.error('最终错误消息:', errorMessage)
        this.$message.error(errorMessage)
      }
    },
    addDimension() {
      this.dimensions.push({
        id: this.nextDimensionId++,
        name: '新维度',
        weight: 0,
        description: '',
        calculationType: 'code_commits'
      })
    },
    removeDimension(index) {
      this.dimensions.splice(index, 1)
    },
    addGrade() {
      this.scoringRules.grades.push({
        name: '新等级',
        minScore: 0,
        maxScore: 100
      })
    },
    removeGrade(index) {
      this.scoringRules.grades.splice(index, 1)
    },

    getOrganizationId() {
      // 从Vuex store获取组织ID
      if (this.$store.getters.currentOrganizationId) {
        return this.$store.getters.currentOrganizationId
      }
      
      // 从localStorage获取组织ID
      const organizationId = localStorage.getItem('organizationId')
      if (organizationId) {
        return organizationId
      }
      
      // 默认返回1（如果没有找到组织ID）
      return 1
    },
    
    applyDefaultConfig() {
      this.resetToDefaultConfig();
      this.showNoConfigPrompt = false;
      this.$message.success('已应用默认配置');
    },
    
    hidePrompt() {
      this.showNoConfigPrompt = false;
      this.addDimension(); // 自动添加第一个维度
      this.$message.success('已为您添加第一个评价维度，请完善维度信息');
    },
    
    getDefaultScoringRules() {
      return {
        method: 'relative',
        calculationMethod: 'weighted_average',
        minScore: 0,
        maxScore: 100,
        grades: [
          { name: '优秀', minScore: 90, maxScore: 100 },
          { name: '良好', minScore: 80, maxScore: 89 },
          { name: '中等', minScore: 70, maxScore: 79 },
          { name: '及格', minScore: 60, maxScore: 69 },
          { name: '不及格', minScore: 0, maxScore: 59 }
        ],
        exceptionHandling: ['normalize_outliers']
      }
    },
    
    getDefaultAlgorithmConfig() {
      return {
        timeWindow: 30,
        minCommits: 5,
        collaborationWeight: 30,
        qualityWeight: 40,
        activityThreshold: 0.3
      }
    }
  }
}
</script>

<style scoped>
.evaluation-config {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.el-main {
  padding: 30px;
  background-color: #f5f5f5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.course-selector {
  display: flex;
  align-items: center;
}

.course-selector::before {
  content: '选择课程：';
  margin-right: 10px;
  color: #606266;
  font-size: 14px;
}

.title {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.config-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.section-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.dimensions-list {
  display: grid;
  gap: 20px;
  margin-bottom: 20px;
}

.dimension-card {
  border: 1px solid #ebeef5;
}

.no-config-prompt {
  margin-bottom: 20px;
}

.prompt-actions {
  margin-top: 15px;
  text-align: center;
}

.prompt-actions .el-button {
  margin: 0 10px;
}

.add-dimension-bottom {
  text-align: center;
  margin: 20px 0;
  padding: 15px;
  border: 2px dashed #e4e7ed;
  border-radius: 6px;
  background-color: #fafafa;
}

.add-dimension-bottom:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.dimension-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.dimension-name {
  flex: 1;
  margin-right: 15px;
}

.dimension-actions .danger {
  color: #f56c6c;
}

.weight-summary {
  margin-top: 20px;
}

.score-range {
  display: flex;
  align-items: center;
  gap: 10px;
}

.range-separator {
  color: #909399;
  font-weight: bold;
}

.grade-config {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
}

.grade-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.grade-item:last-child {
  margin-bottom: 0;
}

.templates-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.template-card {
  cursor: pointer;
}

.template-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.template-header h4 {
  margin: 0;
  color: #303133;
}

.template-description {
  color: #606266;
  margin: 10px 0;
  line-height: 1.5;
}

.template-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.danger {
  color: #f56c6c;
}

.danger:hover {
  color: #f56c6c;
  background-color: #fef0f0;
}

/* Element UI 样式覆盖 */
.evaluation-config .el-button--primary {
  color: #fff;
  background-color: #131313;
  border-color: #131313;
}

.evaluation-config .el-button--primary:focus,
.evaluation-config .el-button--primary:hover {
  background: #404040;
  border-color: #404040;
  color: #fff;
}

.evaluation-config .el-tabs--card > .el-tabs__header .el-tabs__nav {
  border: 1px solid #e4e7ed;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
}

.evaluation-config .el-tabs--card > .el-tabs__header .el-tabs__item {
  border-bottom: 1px solid #e4e7ed;
  border-right: 1px solid #e4e7ed;
}

.evaluation-config .el-tabs--card > .el-tabs__header .el-tabs__item.is-active {
  background-color: #fff;
  border-bottom-color: #fff;
}
</style>