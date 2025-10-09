<template>
  <div class="score-calculation">
    <NavMenu></NavMenu>
    <el-main>
      <div class="page-header">
        <h2 class="title">评分计算</h2>
        <div class="header-actions">
          <el-button type="primary" @click="startCalculation" :loading="calculating">
            <i class="el-icon-cpu"></i> {{ calculating ? '计算中...' : '开始计算' }}
          </el-button>
          <el-button @click="showScheduleDialog = true">
            <i class="el-icon-time"></i> 定时任务
          </el-button>
        </div>
      </div>

      <el-row :gutter="20">
        <!-- 左侧：计算配置 -->
        <el-col :span="8">
          <el-card class="config-card" shadow="hover">
            <div slot="header" class="card-header">
              <span>计算配置</span>
              <el-button type="text" @click="loadTemplate">
                <i class="el-icon-folder-opened"></i> 加载模板
              </el-button>
            </div>
            
            <el-form :model="calculationConfig" label-width="100px" size="small">
              <el-form-item label="选择课程">
                <el-select v-model="calculationConfig.selectedCourse" placeholder="请选择课程" style="width: 100%" @change="onCourseChange">
                  <el-option
                    v-for="course in courses"
                    :key="course.id"
                    :label="course.name"
                    :value="course.id">
                  </el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="计算范围">
                <el-select v-model="calculationConfig.scope" placeholder="选择计算范围">
                  <el-option label="全部课程" value="all"></el-option>
                  <el-option label="指定课程" value="specific"></el-option>
                  <el-option label="指定学生" value="students"></el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="课程选择" v-if="calculationConfig.scope === 'specific'">
                <el-select v-model="calculationConfig.courseIds" multiple placeholder="选择课程">
                  <el-option 
                    v-for="course in courses" 
                    :key="course.id" 
                    :label="course.name" 
                    :value="course.id"
                  ></el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="学生选择" v-if="calculationConfig.scope === 'students'">
                <el-select v-model="calculationConfig.studentIds" multiple placeholder="选择学生">
                  <el-option 
                    v-for="student in students" 
                    :key="student.id" 
                    :label="student.name" 
                    :value="student.id"
                  ></el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="算法模型">
                <el-select v-model="calculationConfig.algorithm" placeholder="选择算法">
                  <el-option label="加权平均算法" value="weighted_average"></el-option>
                  <el-option label="层次分析法" value="ahp"></el-option>
                  <el-option label="模糊综合评价" value="fuzzy_evaluation"></el-option>
                  <el-option label="神经网络" value="neural_network"></el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="时间范围">
                <el-date-picker
                  v-model="calculationConfig.dateRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  style="width: 100%"
                ></el-date-picker>
              </el-form-item>
              
              <el-form-item label="数据源">
                <el-checkbox-group v-model="calculationConfig.dataSources">
                  <el-checkbox label="git_commits">Git提交</el-checkbox>
                  <el-checkbox label="code_reviews">代码审查</el-checkbox>
                  <el-checkbox label="issue_tracking">问题跟踪</el-checkbox>
                  <el-checkbox label="peer_evaluation">同伴评价</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              
              <el-form-item label="异常处理">
                <el-radio-group v-model="calculationConfig.errorHandling">
                  <el-radio label="skip">跳过异常数据</el-radio>
                  <el-radio label="default">使用默认值</el-radio>
                  <el-radio label="stop">停止计算</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="saveConfig" size="small">
                  保存配置
                </el-button>
                <el-button @click="resetConfig" size="small">
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
          
          <!-- 算法参数 -->
          <el-card class="config-card" shadow="hover" style="margin-top: 20px;">
            <div slot="header" class="card-header">
              <span>算法参数</span>
            </div>
            
            <div v-if="calculationConfig.algorithm === 'weighted_average'">
              <el-form label-width="120px" size="small">
                <el-form-item label="代码贡献权重">
                  <el-slider v-model="algorithmParams.codeWeight" :max="100" show-input></el-slider>
                </el-form-item>
                <el-form-item label="协作贡献权重">
                  <el-slider v-model="algorithmParams.collaborationWeight" :max="100" show-input></el-slider>
                </el-form-item>
                <el-form-item label="质量贡献权重">
                  <el-slider v-model="algorithmParams.qualityWeight" :max="100" show-input></el-slider>
                </el-form-item>
              </el-form>
            </div>
            
            <div v-else-if="calculationConfig.algorithm === 'ahp'">
              <el-form label-width="120px" size="small">
                <el-form-item label="一致性比率">
                  <el-input-number v-model="algorithmParams.consistencyRatio" :precision="3" :step="0.001" :max="0.1"></el-input-number>
                </el-form-item>
                <el-form-item label="判断矩阵">
                  <el-button size="mini" @click="showMatrixDialog = true">编辑矩阵</el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <div v-else-if="calculationConfig.algorithm === 'neural_network'">
              <el-form label-width="120px" size="small">
                <el-form-item label="隐藏层数">
                  <el-input-number v-model="algorithmParams.hiddenLayers" :min="1" :max="5"></el-input-number>
                </el-form-item>
                <el-form-item label="学习率">
                  <el-input-number v-model="algorithmParams.learningRate" :precision="4" :step="0.0001" :max="1"></el-input-number>
                </el-form-item>
                <el-form-item label="训练轮数">
                  <el-input-number v-model="algorithmParams.epochs" :min="10" :max="1000" :step="10"></el-input-number>
                </el-form-item>
              </el-form>
            </div>
          </el-card>
        </el-col>
        
        <!-- 右侧：计算状态和结果 -->
        <el-col :span="16">
          <!-- 计算进度 -->
          <el-card class="progress-card" shadow="hover">
            <div slot="header" class="card-header">
              <span>计算进度</span>
              <el-button type="text" @click="refreshProgress">
                <i class="el-icon-refresh"></i> 刷新
              </el-button>
            </div>
            
            <div class="progress-overview">
              <el-row :gutter="20">
                <el-col :span="6">
                  <div class="progress-stat">
                    <div class="stat-number">{{ progressStats.totalTasks }}</div>
                    <div class="stat-label">总任务数</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="progress-stat">
                    <div class="stat-number">{{ progressStats.completedTasks }}</div>
                    <div class="stat-label">已完成</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="progress-stat">
                    <div class="stat-number">{{ progressStats.failedTasks }}</div>
                    <div class="stat-label">失败任务</div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="progress-stat">
                    <div class="stat-number">{{ Math.round(progressStats.overallProgress) }}%</div>
                    <div class="stat-label">总进度</div>
                  </div>
                </el-col>
              </el-row>
            </div>
            
            <div class="task-list">
              <div v-for="task in calculationTasks" :key="task.id" class="task-item">
                <div class="task-header">
                  <span class="task-name">{{ task.name }}</span>
                  <el-tag :type="getTaskStatusType(task.status)" size="mini">
                    {{ getTaskStatusText(task.status) }}
                  </el-tag>
                </div>
                <el-progress 
                  :percentage="task.progress" 
                  :status="task.status === 'failed' ? 'exception' : (task.progress === 100 ? 'success' : null)"
                  :show-text="false"
                ></el-progress>
                <div class="task-details">
                  <span class="task-time">{{ formatTime(task.startTime) }} - {{ formatTime(task.endTime) }}</span>
                  <span class="task-duration">{{ formatDuration(task.duration) }}</span>
                </div>
                <div v-if="task.error" class="task-error">
                  <i class="el-icon-warning"></i>
                  {{ task.error }}
                </div>
              </div>
            </div>
          </el-card>
          
          <!-- 计算结果预览 -->
          <el-card class="result-card" shadow="hover" style="margin-top: 20px;">
            <div slot="header" class="card-header">
              <span>计算结果</span>
              <div>
                <el-button type="text" @click="exportResults">
                  <i class="el-icon-download"></i> 导出
                </el-button>
                <el-button type="text" @click="viewDetailedResults">
                  <i class="el-icon-view"></i> 详细结果
                </el-button>
              </div>
            </div>
            
            <div v-if="calculationResults.length === 0" class="empty-results">
              <i class="el-icon-document-copy"></i>
              <p>暂无计算结果</p>
            </div>
            
            <div v-else>
              <el-table :data="calculationResults" style="width: 100%" size="small">
                <el-table-column prop="memberId" label="学生ID" width="80"></el-table-column>
                <el-table-column prop="memberName" label="学生姓名" width="120"></el-table-column>
                <el-table-column prop="totalScore" label="总分" width="80" sortable>
                  <template slot-scope="scope">
                    <span :class="getScoreClass(scope.row.totalScore)">{{ scope.row.totalScore ? Number(scope.row.totalScore).toFixed(1) : '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="dimensionScores" label="各维度得分" width="200">
                  <template slot-scope="scope">
                    <span v-if="scope.row.dimensionScores">{{ formatDimensionScores(scope.row.dimensionScores) }}</span>
                    <span v-else>-</span>
                  </template>
                </el-table-column>
                <el-table-column prop="ranking" label="排名" width="80"></el-table-column>
                <el-table-column prop="calculationTime" label="计算时间" width="150">
                  <template slot-scope="scope">
                    {{ formatDateTime(scope.row.calculationTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template slot-scope="scope">
                    <el-button type="text" size="mini" @click="viewStudentDetail(scope.row)">查看详情</el-button>
                  </template>
                </el-table-column>
              </el-table>
              
              <!-- 分页组件 -->
              <div style="margin-top: 20px; text-align: center;">
                <el-pagination
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                  :current-page="pagination.current"
                  :page-sizes="[10, 20, 50, 100]"
                  :page-size="pagination.pageSize"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="pagination.total">
                </el-pagination>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 定时任务对话框 -->
      <el-dialog title="定时计算任务" :visible.sync="showScheduleDialog" width="600px">
        <el-form :model="scheduleForm" label-width="100px">
          <el-form-item label="任务名称">
            <el-input v-model="scheduleForm.name" placeholder="请输入任务名称"></el-input>
          </el-form-item>
          <el-form-item label="执行频率">
            <el-select v-model="scheduleForm.frequency" placeholder="选择执行频率">
              <el-option label="每小时" value="hourly"></el-option>
              <el-option label="每天" value="daily"></el-option>
              <el-option label="每周" value="weekly"></el-option>
              <el-option label="每月" value="monthly"></el-option>
              <el-option label="自定义" value="custom"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="Cron表达式" v-if="scheduleForm.frequency === 'custom'">
            <el-input v-model="scheduleForm.cronExpression" placeholder="0 0 * * *"></el-input>
          </el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker
              v-model="scheduleForm.startTime"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 100%"
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker
              v-model="scheduleForm.endTime"
              type="datetime"
              placeholder="选择结束时间"
              style="width: 100%"
            ></el-date-picker>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="showScheduleDialog = false">取消</el-button>
          <el-button type="primary" @click="saveSchedule">保存</el-button>
        </div>
      </el-dialog>

      <!-- 判断矩阵编辑对话框 -->
      <el-dialog title="编辑判断矩阵" :visible.sync="showMatrixDialog" width="800px">
        <div class="matrix-editor">
          <p>请输入各评价维度之间的相对重要性（1-9标度）：</p>
          <el-table :data="matrixData" border>
            <el-table-column prop="dimension" label="维度" width="120"></el-table-column>
            <el-table-column 
              v-for="(dim, index) in dimensions" 
              :key="dim" 
              :label="dim" 
              width="100"
            >
              <template slot-scope="scope">
                <el-input-number 
                  v-model="scope.row.values[index]" 
                  :min="1" 
                  :max="9" 
                  :precision="0"
                  size="mini"
                ></el-input-number>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div slot="footer" class="dialog-footer">
          <el-button @click="showMatrixDialog = false">取消</el-button>
          <el-button type="primary" @click="saveMatrix">保存</el-button>
        </div>
      </el-dialog>
    </el-main>
  </div>
</template>

<script>
import NavMenu from '@/components/NavMenu.vue'

export default {
  name: 'ScoreCalculation',
  components: {
    NavMenu
  },
  data() {
    return {
      calculating: false,
      showScheduleDialog: false,
      showMatrixDialog: false,
      calculationConfig: {
        scope: 'all',
        courseIds: [],
        studentIds: [],
        algorithm: 'weighted_average',
        dateRange: [],
        dataSources: ['git_commits', 'code_reviews'],
        errorHandling: 'skip'
      },
      algorithmParams: {
        // 加权平均算法参数
        codeWeight: 40,
        collaborationWeight: 30,
        qualityWeight: 30,
        // AHP算法参数
        consistencyRatio: 0.05,
        judgmentMatrix: [],
        // 神经网络参数
        hiddenLayers: 2,
        learningRate: 0.001,
        epochs: 100
      },
      // 当前课程ID
      currentCourseId: null,
      courses: [],
      students: [],
      progressStats: {
        totalTasks: 0,
        completedTasks: 0,
        failedTasks: 0,
        overallProgress: 0
      },
      calculationTasks: [
        {
          id: 1,
          name: '数据预处理',
          status: 'completed',
          progress: 100,
          startTime: new Date(Date.now() - 300000),
          endTime: new Date(Date.now() - 240000),
          duration: 60000
        },
        {
          id: 2,
          name: '算法执行',
          status: 'running',
          progress: 65,
          startTime: new Date(Date.now() - 120000),
          endTime: null,
          duration: 120000
        }
      ],
      calculationResults: [],
      
      // 分页信息
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      scheduleForm: {
        name: '',
        frequency: 'daily',
        cronExpression: '',
        startTime: null,
        endTime: null
      },
      dimensions: ['代码贡献', '协作贡献', '质量贡献'],
      matrixData: [
        { dimension: '代码贡献', values: [1, 2, 3] },
        { dimension: '协作贡献', values: [0.5, 1, 2] },
        { dimension: '质量贡献', values: [0.33, 0.5, 1] }
      ]
    }
  },
  computed: {
    totalWeight() {
      return this.algorithmParams.codeWeight + 
             this.algorithmParams.collaborationWeight + 
             this.algorithmParams.qualityWeight
    }
  },
  watch: {
    'algorithmParams.codeWeight'() {
      this.normalizeWeights()
    },
    'algorithmParams.collaborationWeight'() {
      this.normalizeWeights()
    },
    'algorithmParams.qualityWeight'() {
      this.normalizeWeights()
    }
  },
  mounted() {
    this.initializeCourseId()
    this.loadCourses()
    this.loadStudents()
    this.refreshProgress()
    this.loadCalculationResults()
  },

    initializeCourseId() {
      // 从路由参数或本地存储获取当前课程ID
      this.currentCourseId = this.$route.params.courseId || localStorage.getItem('currentCourseId')
      if (this.currentCourseId) {
        this.calculationConfig.selectedCourse = this.currentCourseId
      }
    },
  methods: {
    async startCalculation() {
      if (!this.calculationConfig.selectedCourse) {
        this.$message.error('请选择课程');
        return;
      }
      
      this.$message.success('开始计算贡献度评分...');
      
      // 更新进度状态
      this.progressStats.isRunning = true;
      this.progressStats.completedTasks = 0;
      this.progressStats.failedTasks = 0;
      
      // 添加新任务
      const newTask = {
        id: Date.now(),
        name: `课程评分计算 - ${this.courses.find(c => c.id === this.calculationConfig.selectedCourse)?.name}`,
        status: 'running',
        progress: 0,
        startTime: new Date(),
        duration: null,
        error: null
      };
      
      this.calculationTasks.unshift(newTask);
      
      try {
        // 检查是否有选中的课程
        if (!this.calculationConfig.selectedCourse) {
          console.warn('未选择课程，跳过评分计算')
          this.$message.warning('请先选择课程')
          newTask.status = 'failed'
          newTask.error = '未选择课程'
          this.progressStats.isRunning = false
          this.progressStats.failedTasks = 1
          return
        }
        
        // 调用后端API计算课程评分
        const response = await this.$http.post(`/scores/course/${this.calculationConfig.selectedCourse}/calculate`);
        
        if (response.data.code === '200') {
          newTask.status = 'completed';
          newTask.progress = 100;
          newTask.duration = Date.now() - newTask.startTime.getTime();
          this.progressStats.isRunning = false;
          this.progressStats.completedTasks = this.progressStats.totalTasks;
          
          // 重新加载计算结果
          await this.loadCalculationResults();
          
          this.$message.success('计算完成！');
        } else {
          throw new Error(response.data.message || '计算失败');
        }
      } catch (error) {
        newTask.status = 'failed';
        newTask.error = error.message;
        this.progressStats.isRunning = false;
        this.progressStats.failedTasks = 1;
        this.$message.error('计算失败: ' + error.message);
      }
    },
    async loadCourses() {
      try {
        const response = await this.$http.get('/courses');
        if (response.data.code === '200') {
          this.courses = response.data.data;
        }
      } catch (error) {
        console.error('加载课程列表失败:', error);
        this.$message.error('加载课程列表失败');
      }
    },
    async loadStudents() {
      if (!this.calculationConfig.selectedCourse) return;
      
      try {
        const response = await this.$http.get(`/courses/${this.calculationConfig.selectedCourse}/members`);
        if (response.data.code === '200') {
          this.students = response.data.data;
        }
      } catch (error) {
        console.error('加载学生列表失败:', error);
        this.$message.error('加载学生列表失败');
      }
    },
    async loadCalculationResults() {
      if (!this.calculationConfig.selectedCourse) {
        console.warn('未选择课程，跳过加载计算结果')
        return
      }
      
      try {
        const response = await this.$http.get(`/scores/course/${this.calculationConfig.selectedCourse}/all`, {
          params: {
            pageNum: this.pagination.current,
            pageSize: this.pagination.pageSize
          }
        });
        
        if (response.data.code === '200') {
          this.calculationResults = response.data.data.records || [];
          this.pagination.total = response.data.data.total || 0;
          
          // 更新进度统计
          this.progressStats.totalTasks = this.pagination.total;
          this.progressStats.completedTasks = this.calculationResults.length;
        }
      } catch (error) {
        console.error('加载计算结果失败:', error);
        this.$message.error('加载计算结果失败');
      }
    },
    
    // 刷新进度
    async refreshProgress() {
      await this.loadCalculationResults();
    },
    
    // 监听课程选择变化
    onCourseChange() {
      this.loadStudents();
      this.loadCalculationResults();
    },
    
    async saveConfig() {
      try {
        await this.$http.post('/score-calculation/config', {
          config: this.calculationConfig,
          params: this.algorithmParams
        })
        this.$message.success('配置保存成功')
      } catch (error) {
        console.error('保存配置失败：', error)
        this.$message.error('保存配置失败')
      }
    },
    resetConfig() {
      this.calculationConfig = {
        scope: 'all',
        courseIds: [],
        studentIds: [],
        algorithm: 'weighted_average',
        dateRange: [],
        dataSources: ['git_commits', 'code_reviews'],
        errorHandling: 'skip'
      }
      this.algorithmParams = {
        codeWeight: 40,
        collaborationWeight: 30,
        qualityWeight: 30,
        consistencyRatio: 0.05,
        judgmentMatrix: [],
        hiddenLayers: 2,
        learningRate: 0.001,
        epochs: 100
      }
    },
    loadTemplate() {
      // 加载预设模板
      this.$message.info('模板加载功能开发中')
    },
    normalizeWeights() {
      // 权重归一化逻辑可以在这里实现
      if (this.totalWeight > 100) {
        this.$message.warning('权重总和不能超过100%')
      }
    },
    async exportResults() {
      if (!this.calculationConfig.selectedCourse) {
        console.warn('未选择课程，跳过导出结果')
        this.$message.error('请选择课程');
        return;
      }
      
      try {
        const response = await this.$http.get(`/scores/course/${this.calculationConfig.selectedCourse}/export`);
        if (response.data.code === '200') {
          // 处理导出数据，可以转换为CSV或Excel格式
          const data = response.data.data;
          this.downloadCSV(data);
          this.$message.success('导出成功！');
        }
      } catch (error) {
        console.error('导出失败:', error);
        this.$message.error('导出失败');
      }
    },
    
    // 下载CSV文件
    downloadCSV(data) {
      const csvContent = this.convertToCSV(data);
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      const url = URL.createObjectURL(blob);
      link.setAttribute('href', url);
      link.setAttribute('download', `course_scores_${this.calculationConfig.selectedCourse}.csv`);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },
    
    // 转换为CSV格式
    convertToCSV(data) {
      const headers = ['学生ID', '学生姓名', '总分', '各维度得分', '排名', '计算时间'];
      const csvRows = [headers.join(',')];
      
      data.forEach(item => {
        const row = [
          item.memberId,
          item.memberName || '',
          item.totalScore,
          item.dimensionScores,
          item.ranking,
          this.formatDateTime(item.calculationTime)
        ];
        csvRows.push(row.join(','));
      });
      
      return csvRows.join('\n');
    },
    
    viewDetailedResults() {
      this.$router.push({
        path: '/result-display',
        query: { courseId: this.calculationConfig.selectedCourse }
      });
    },
    async viewStudentDetail(student) {
      try {
        // 检查是否有选中的课程
        if (!this.calculationConfig.selectedCourse) {
          console.warn('未选择课程，跳过查看学生详情')
          this.$message.warning('请先选择课程')
          return
        }
        
        const response = await this.$http.get(`/scores/course/${this.calculationConfig.selectedCourse}/member/${student.memberId}`);
        if (response.data.code === '200') {
          // 可以打开一个对话框显示详细信息，或跳转到详情页面
          this.$router.push({
            path: '/member-profile',
            query: { 
              courseId: this.calculationConfig.selectedCourse,
              memberId: student.memberId 
            }
          });
        }
      } catch (error) {
        console.error('获取学生详情失败:', error);
        this.$message.error('获取学生详情失败');
      }
    },
    saveSchedule() {
      // 保存定时任务
      this.$message.success('定时任务保存成功')
      this.showScheduleDialog = false
    },
    saveMatrix() {
      // 保存判断矩阵
      this.algorithmParams.judgmentMatrix = this.matrixData
      this.$message.success('判断矩阵保存成功')
      this.showMatrixDialog = false
    },
    getTaskStatusType(status) {
      const statusMap = {
        'pending': 'info',
        'running': 'warning',
        'completed': 'success',
        'failed': 'danger'
      }
      return statusMap[status] || 'info'
    },
    getTaskStatusText(status) {
      const statusMap = {
        'pending': '等待中',
        'running': '运行中',
        'completed': '已完成',
        'failed': '失败'
      }
      return statusMap[status] || '未知'
    },
    getScoreClass(score) {
      if (score >= 90) return 'score-excellent'
      if (score >= 80) return 'score-good'
      if (score >= 70) return 'score-average'
      return 'score-poor'
    },
    formatTime(time) {
      return time ? new Date(time).toLocaleTimeString('zh-CN') : ''
    },
    formatDateTime(datetime) {
      return datetime ? new Date(datetime).toLocaleString('zh-CN') : ''
    },
    formatDuration(duration) {
      if (!duration) return ''
      const minutes = Math.floor(duration / 60000)
      const seconds = Math.floor((duration % 60000) / 1000)
      return `${minutes}分${seconds}秒`
    },
    formatDimensionScores(dimensionScores) {
       if (!dimensionScores) return '-'
       if (typeof dimensionScores === 'string') {
         try {
           const scores = JSON.parse(dimensionScores)
           return Object.entries(scores).map(([key, value]) => `${key}: ${Number(value).toFixed(1)}`).join(', ')
         } catch {
           return dimensionScores
         }
       }
       if (typeof dimensionScores === 'object') {
         return Object.entries(dimensionScores).map(([key, value]) => `${key}: ${Number(value).toFixed(1)}`).join(', ')
       }
       return dimensionScores.toString()
     },
     handleSizeChange(val) {
       this.pagination.pageSize = val
       this.pagination.current = 1
       this.loadCalculationResults()
     },
     handleCurrentChange(val) {
       this.pagination.current = val
       this.loadCalculationResults()
     }
  }
}
</script>

<style scoped>
.score-calculation {
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

.title {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.config-card,
.progress-card,
.result-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-overview {
  margin-bottom: 20px;
}

.progress-stat {
  text-align: center;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  color: #606266;
  font-size: 12px;
}

.task-list {
  max-height: 400px;
  overflow-y: auto;
}

.task-item {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 10px;
  background: #fafafa;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.task-name {
  font-weight: 500;
  color: #303133;
}

.task-details {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.task-error {
  margin-top: 8px;
  padding: 8px;
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  border-radius: 4px;
  color: #f56c6c;
  font-size: 12px;
}

.empty-results {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-results i {
  font-size: 48px;
  margin-bottom: 20px;
  display: block;
}

.result-pagination {
  text-align: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.score-excellent {
  color: #67c23a;
  font-weight: bold;
}

.score-good {
  color: #409eff;
  font-weight: bold;
}

.score-average {
  color: #e6a23c;
  font-weight: bold;
}

.score-poor {
  color: #f56c6c;
  font-weight: bold;
}

.matrix-editor {
  margin-bottom: 20px;
}

.matrix-editor p {
  margin-bottom: 15px;
  color: #606266;
}

.dialog-footer {
  text-align: right;
}

/* Element UI 样式覆盖 */
.score-calculation .el-button--primary {
  color: #fff;
  background-color: #131313;
  border-color: #131313;
}

.score-calculation .el-button--primary:focus,
.score-calculation .el-button--primary:hover {
  background: #404040;
  border-color: #404040;
  color: #fff;
}

.score-calculation .el-card__header {
  background-color: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

.score-calculation .el-table th {
  background-color: #fafafa;
  color: #303133;
}

.score-calculation .el-progress-bar__outer {
  background-color: #ebeef5;
}

.score-calculation .el-progress-bar__inner {
  background-color: #131313;
}

.score-calculation .el-slider__bar {
  background-color: #131313;
}

.score-calculation .el-slider__button {
  border-color: #131313;
}
</style>