<template>
  <div>
    <NavMenu />
    <div class="git-activity-monitor">
    <div class="page-header">
      <h2>Git活动监控</h2>
      <el-button type="primary" @click="refreshData" :loading="loading">
        <i class="el-icon-refresh"></i>
        刷新数据
      </el-button>
      <el-button @click="exportData" :loading="exportLoading">
        <i class="el-icon-download"></i>
        导出数据
      </el-button>
      <el-button @click="resetFilters">
        <i class="el-icon-refresh-left"></i>
        重置筛选
      </el-button>
    </div>

    <!-- 筛选器 -->
    <div class="filters">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="selectedCourse" placeholder="选择课程" @change="loadActivities">
            <el-option label="全部课程" value=""></el-option>
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="course.name"
              :value="course.id">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="selectedRepository" placeholder="选择仓库" @change="loadActivities">
            <el-option label="全部仓库" value=""></el-option>
            <el-option
              v-for="repo in repositories"
              :key="repo.id"
              :label="repo.name"
              :value="repo.id">
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="activityType" placeholder="活动类型" @change="loadActivities">
            <el-option label="全部类型" value=""></el-option>
            <el-option label="提交" value="COMMIT"></el-option>
            <el-option label="推送" value="PUSH"></el-option>
            <el-option label="Pull Request" value="PULL_REQUEST"></el-option>
            <el-option label="Issue" value="ISSUE"></el-option>
            <el-option label="代码审查" value="CODE_REVIEW"></el-option>
            <el-option label="分支操作" value="BRANCH"></el-option>
            <el-option label="标签操作" value="TAG"></el-option>
            <el-option label="合并操作" value="MERGE"></el-option>
            <el-option label="发布操作" value="RELEASE"></el-option>
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="loadActivities">
          </el-date-picker>
        </el-col>
      </el-row>
      <el-row :gutter="20" style="margin-top: 15px;">
        <el-col :span="6">
          <el-switch 
            v-model="autoRefresh" 
            @change="toggleAutoRefresh"
            active-text="实时更新"
            inactive-text="手动刷新">
          </el-switch>
          <span v-if="autoRefresh" class="refresh-info" style="margin-left: 10px; color: #67C23A; font-size: 12px;">每30秒自动刷新</span>
        </el-col>
        <el-col :span="6">
          <el-button-group>
            <el-button 
              size="small" 
              :type="realTimeData.enabled ? 'success' : 'default'"
              @click="realTimeData.enabled ? stopRealTimeUpdate() : startRealTimeUpdate()"
              :loading="realTimeData.connectionStatus === 'connecting'">
              <i :class="realTimeData.enabled ? 'el-icon-video-pause' : 'el-icon-video-play'"></i>
              {{ realTimeData.enabled ? '停止实时' : '启动实时' }}
            </el-button>
            <el-button 
              size="small" 
              :type="interactiveMode.enabled ? 'primary' : 'default'"
              @click="interactiveMode.enabled ? (interactiveMode.enabled = false) : enableInteractiveMode()">
              <i class="el-icon-mouse"></i>
              {{ interactiveMode.enabled ? '退出交互' : '交互模式' }}
            </el-button>
          </el-button-group>
        </el-col>
        <el-col :span="6" v-if="realTimeData.enabled">
          <div class="realtime-status">
            <el-tag 
              :type="realTimeData.connectionStatus === 'connected' ? 'success' : 
                     realTimeData.connectionStatus === 'error' ? 'danger' : 'warning'"
              size="mini">
              <i :class="realTimeData.connectionStatus === 'connected' ? 'el-icon-success' : 
                        realTimeData.connectionStatus === 'error' ? 'el-icon-error' : 'el-icon-loading'"></i>
              {{ realTimeData.connectionStatus === 'connected' ? '已连接' : 
                 realTimeData.connectionStatus === 'error' ? '连接错误' : '连接中' }}
            </el-tag>
            <span v-if="realTimeData.lastUpdate" class="last-update">
              最后更新: {{ formatDate(realTimeData.lastUpdate) }}
            </span>
          </div>
        </el-col>
        <el-col :span="6" v-if="interactiveMode.enabled && interactiveMode.selectedTimeRange">
          <div class="selected-range">
            <el-tag type="info" size="mini">
              已选择: {{ interactiveMode.selectedTimeRange.date }} - {{ interactiveMode.selectedTimeRange.type }}
            </el-tag>
            <el-button size="mini" type="text" @click="clearSelection()">清除选择</el-button>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon commit">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.totalCommits }}</div>
              <div class="stat-label">总提交数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon active">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.activeMembers }}</div>
              <div class="stat-label">活跃成员</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon lines">
              <i class="el-icon-edit"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.totalLines }}</div>
              <div class="stat-label">代码行数</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon repos">
              <i class="el-icon-folder"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ stats.activeRepos }}</div>
              <div class="stat-label">活跃仓库</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 活动时间线图表 -->
    <div class="chart-section">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-card>
              <div slot="header">
                <span>活动时间线</span>
                <el-button-group style="float: right;">
                  <el-button size="mini" @click="changeChartTimeRange('7d')" :type="chartTimeRange === '7d' ? 'primary' : ''">7天</el-button>
                  <el-button size="mini" @click="changeChartTimeRange('30d')" :type="chartTimeRange === '30d' ? 'primary' : ''">30天</el-button>
                  <el-button size="mini" @click="changeChartTimeRange('90d')" :type="chartTimeRange === '90d' ? 'primary' : ''">90天</el-button>
                </el-button-group>
              </div>
              <div id="activityTimelineChart" style="height: 400px;"></div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card>
              <div slot="header">
                <span>活动类型分布</span>
              </div>
              <div id="activityDistributionChart" style="height: 400px;"></div>
            </el-card>
          </el-col>
        </el-row>
      </div>

    <!-- 活动列表 -->
    <div class="activity-list" v-loading="loading">
      <el-card>
        <div slot="header">
          <span>最近活动</span>
        </div>
        <el-table :data="activities" stripe>
          <el-table-column prop="memberName" label="成员" width="120"></el-table-column>
          <el-table-column prop="repositoryName" label="仓库" width="150"></el-table-column>
          <el-table-column prop="activityType" label="活动类型" width="100">
            <template slot-scope="scope">
              <el-tag :type="getActivityTypeColor(scope.row.activityType)" size="mini">
                {{ getActivityTypeText(scope.row.activityType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="描述" min-width="300" show-overflow-tooltip></el-table-column>
          <el-table-column prop="linesAdded" label="新增行" width="80" align="center">
            <template slot-scope="scope">
              <span class="lines-added">+{{ scope.row.linesAdded || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="linesDeleted" label="删除行" width="80" align="center">
            <template slot-scope="scope">
              <span class="lines-deleted">-{{ scope.row.linesDeleted || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="timestamp" label="时间" width="180">
            <template slot-scope="scope">
              {{ formatDate(scope.row.timestamp) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template slot-scope="scope">
              <el-button size="mini" @click="viewActivityDetail(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
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
      </el-card>
    </div>

    <!-- 活动详情对话框 -->
    <el-dialog
      title="活动详情"
      :visible.sync="showDetailDialog"
      width="800px">
      <div v-if="selectedActivity">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="成员">{{ selectedActivity.memberName }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ selectedActivity.repositoryName }}</el-descriptions-item>
          <el-descriptions-item label="活动类型">
            <el-tag :type="getActivityTypeColor(selectedActivity.activityType)">
              {{ getActivityTypeText(selectedActivity.activityType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="时间">{{ formatDate(selectedActivity.timestamp) }}</el-descriptions-item>
          <el-descriptions-item label="新增行数">{{ selectedActivity.linesAdded || 0 }}</el-descriptions-item>
          <el-descriptions-item label="删除行数">{{ selectedActivity.linesDeleted || 0 }}</el-descriptions-item>
          <el-descriptions-item label="提交哈希" v-if="selectedActivity.commitHash">
            <el-link :href="getCommitUrl(selectedActivity)" target="_blank">
              {{ selectedActivity.commitHash.substring(0, 8) }}
            </el-link>
          </el-descriptions-item>
          <el-descriptions-item label="分支" v-if="selectedActivity.branch">{{ selectedActivity.branch }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 20px;">
          <h4>描述信息</h4>
          <p>{{ selectedActivity.message || '无描述信息' }}</p>
        </div>
        <div v-if="selectedActivity.fileChanges" style="margin-top: 20px;">
          <h4>文件变更</h4>
          <el-table :data="selectedActivity.fileChanges" size="mini">
            <el-table-column prop="filename" label="文件名"></el-table-column>
            <el-table-column prop="additions" label="新增" width="80" align="center">
              <template slot-scope="scope">
                <span class="lines-added">+{{ scope.row.additions }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="deletions" label="删除" width="80" align="center">
              <template slot-scope="scope">
                <span class="lines-deleted">-{{ scope.row.deletions }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import NavMenu from '@/components/NavMenu.vue'

export default {
  name: 'GitActivityMonitor',
  components: {
    NavMenu
  },
  data() {
    return {
      loading: false,
      showDetailDialog: false,
      selectedActivity: null,
      selectedCourse: '',
      selectedRepository: '',
      activityType: '',
      selectedMember: '',
      fileType: '',
      dateRange: [],
      autoRefresh: false,
      refreshInterval: null,
      realTimeData: {
        enabled: false,
        interval: 30000, // 30秒
        lastUpdate: null,
        connectionStatus: 'disconnected'
      },
      interactiveMode: {
        enabled: true,
        selectedTimeRange: null,
        drillDownData: null,
        brushSelection: null
      },
      exportLoading: false,
      courses: [],
      repositories: [],
      members: [],
      activities: [],
      stats: {
        totalCommits: 0,
        activeMembers: 0,
        totalLines: 0,
        activeRepos: 0
      },
      pagination: {
        current: 1,
        pageSize: 20,
        total: 0
      },
      timelineChart: null,
      distributionChart: null,
      chartTimeRange: '30d'
    }
  },
  mounted() {
    this.loadCourses()
    this.loadMembers()
    this.loadRepositories()
    this.loadActivities()
    this.loadStats()
    this.initTimelineChart()
    this.initDistributionChart()
  },
  beforeDestroy() {
    if (this.timelineChart) {
      this.timelineChart.dispose()
    }
    if (this.distributionChart) {
      this.distributionChart.dispose()
    }
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval)
    }
  },
  methods: {
    async loadCourses() {
      try {
        const response = await this.$http.get('/courses')
        if (response.data.code === '200') {
          this.courses = response.data.data
        }
      } catch (error) {
        console.error('加载课程失败:', error)
        // 使用模拟数据
        this.courses = [
          { id: 1, name: '软件工程实践', code: 'SE2024' },
          { id: 2, name: '数据结构与算法', code: 'DS2024' },
          { id: 3, name: 'Web开发技术', code: 'WEB2024' }
        ]
      }
    },
    
    async loadMembers() {
      try {
        const params = {}
        if (this.selectedCourse) params.courseId = this.selectedCourse
        
        const response = await this.$http.get('/members', { params })
        if (response.data.code === '200') {
          this.members = response.data.data
        }
      } catch (error) {
        console.error('加载成员失败:', error)
        // 使用模拟数据
        this.members = [
          { id: 1, name: '张三', githubUsername: 'zhangsan' },
          { id: 2, name: '李四', githubUsername: 'lisi' },
          { id: 3, name: '王五', githubUsername: 'wangwu' },
          { id: 4, name: '赵六', githubUsername: 'zhaoliu' }
        ]
      }
    },
    
    async loadRepositories() {
      try {
        const response = await this.$http.get('/git-repositories')
        if (response.data.code === '200') {
          this.repositories = response.data.data.content || []
        }
      } catch (error) {
        console.error('加载仓库列表失败:', error)
      }
    },
    
    async loadActivities() {
      this.loading = true
      try {
        let activities = []
        let total = 0
        
        if (this.selectedRepository) {
          // 按仓库查询活动
          const params = {
            pageNum: this.pagination.current,
            pageSize: this.pagination.pageSize
          }
          
          const response = await this.$http.get(`/git-data/activities/repository/${this.selectedRepository}`, { params })
          if (response.data.code === 200) {
            activities = response.data.data.records || []
            total = response.data.data.total || 0
          }
        } else if (this.selectedMember) {
          // 按成员查询活动
          const params = {
            page: this.pagination.current - 1,
            size: this.pagination.pageSize
          }
          
          if (this.dateRange && this.dateRange.length === 2) {
            params.startDate = this.dateRange[0].toISOString().split('T')[0]
            params.endDate = this.dateRange[1].toISOString().split('T')[0]
          }
          
          const response = await this.$http.get(`/git-data/activities/member/${this.selectedMember}`, { params })
          if (response.data.code === 200) {
            activities = response.data.data || []
            total = activities.length
          }
        } else {
          // 查询所有活动（分页）
          const params = {
            pageNum: this.pagination.current,
            pageSize: this.pagination.pageSize
          }
          
          if (this.activityType) params.activityType = this.activityType
          if (this.dateRange && this.dateRange.length === 2) {
            params.startDate = this.dateRange[0].toISOString().split('T')[0]
            params.endDate = this.dateRange[1].toISOString().split('T')[0]
          }
          
          const response = await this.$http.get('/git-data/activities', { params })
          if (response.data.code === 200) {
            activities = response.data.data.records || []
            total = response.data.data.total || 0
          }
        }
        
        this.activities = activities
        this.pagination.total = total
        
        // 丰富活动数据
        await this.enrichActivitiesData()
        
      } catch (error) {
        console.error('加载活动数据失败:', error)
        this.$message.error('加载活动数据失败')
        // 使用模拟数据
        this.loadMockActivities()
      } finally {
        this.loading = false
      }
    },
    
    async enrichActivitiesData() {
      for (let activity of this.activities) {
        // 获取仓库名称
        const repo = this.repositories.find(r => r.id === activity.repositoryId)
        activity.repositoryName = repo ? repo.name : '未知仓库'
        
        // 模拟成员名称（实际应从后端获取）
        activity.memberName = activity.memberName || `成员${activity.memberId}`
      }
    },
    
    loadMockActivities() {
      // 生成模拟活动数据
      const mockActivities = []
      const activityTypes = ['COMMIT', 'PUSH', 'PULL_REQUEST', 'ISSUE', 'CODE_REVIEW']
      const repositories = ['项目A', '项目B', '项目C']
      
      for (let i = 0; i < this.pagination.pageSize; i++) {
        const activity = {
          id: i + 1,
          activityType: activityTypes[Math.floor(Math.random() * activityTypes.length)],
          message: `模拟活动消息 ${i + 1}`,
          repositoryName: repositories[Math.floor(Math.random() * repositories.length)],
          memberName: `成员${Math.floor(Math.random() * 10) + 1}`,
          linesAdded: Math.floor(Math.random() * 100),
          linesDeleted: Math.floor(Math.random() * 50),
          filesChanged: Math.floor(Math.random() * 10) + 1,
          timestamp: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString()
        }
        mockActivities.push(activity)
      }
      
      this.activities = mockActivities
      this.pagination.total = 100
    },
    
    async loadStats() {
      try {
        // 使用后端GitDataController的统计接口
        const response = await this.$http.get('/git-data/activities/statistics')
        if (response.data.code === 200) {
          const data = response.data.data
          this.stats = {
            totalCommits: data.totalCommits || 0,
            activeMembers: data.activeMembers || 0,
            totalLines: data.totalLines || 0,
            activeRepos: data.activeRepositories || 0
          }
        } else {
          throw new Error('统计数据获取失败')
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
        // 使用模拟数据
        this.stats = {
          totalCommits: 1234,
          activeMembers: 45,
          totalLines: 98765,
          activeRepos: 12
        }
      }
    },
    
    initTimelineChart() {
      const chartDom = document.getElementById('activityTimelineChart')
      if (chartDom) {
        this.timelineChart = echarts.init(chartDom)
        this.renderTimelineChart()
        
        // 添加窗口大小变化监听
        window.addEventListener('resize', () => {
          if (this.timelineChart) {
            this.timelineChart.resize()
          }
        })
      }
    },
    
    initDistributionChart() {
      const chartDom = document.getElementById('activityDistributionChart')
      if (chartDom) {
        this.distributionChart = echarts.init(chartDom)
        this.renderDistributionChart()
        
        // 添加窗口大小变化监听
        window.addEventListener('resize', () => {
          if (this.distributionChart) {
            this.distributionChart.resize()
          }
        })
      }
    },
    
    async renderTimelineChart() {
      try {
        // 获取实际的活动趋势数据
        const trendData = await this.loadActivityTrendData()
        
        const option = {
          title: {
            text: '活动趋势分析',
            left: 'center',
            textStyle: {
              fontSize: 16,
              fontWeight: 'bold'
            }
          },
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'cross',
              label: {
                backgroundColor: '#6a7985'
              }
            },
            formatter: function(params) {
              let result = params[0].name + '<br/>'
              params.forEach(param => {
                result += `${param.marker}${param.seriesName}: ${param.value}<br/>`
              })
              return result
            }
          },
          legend: {
            data: ['提交', '推送', 'Pull Request', 'Issue', '代码审查'],
            top: 30
          },
          grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
          },
          toolbox: {
            feature: {
              saveAsImage: {
                title: '保存为图片',
                pixelRatio: 2
              },
              dataZoom: {
                title: {
                  zoom: '区域缩放',
                  back: '区域缩放还原'
                }
              },
              restore: {
                title: '还原'
              },
              magicType: {
                title: {
                  line: '切换为折线图',
                  bar: '切换为柱状图',
                  stack: '切换为堆叠',
                  tiled: '切换为平铺'
                },
                type: ['line', 'bar', 'stack', 'tiled']
              }
            }
          },
          xAxis: {
            type: 'category',
            boundaryGap: false,
            data: trendData.dates,
            axisLabel: {
              formatter: function(value) {
                return new Date(value).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
              }
            }
          },
          yAxis: {
            type: 'value',
            name: '活动次数',
            axisLabel: {
              formatter: '{value}'
            }
          },
          dataZoom: [{
            type: 'inside',
            start: 0,
            end: 100
          }, {
            start: 0,
            end: 100,
            handleIcon: 'M10.7,11.9v-1.3H9.3v1.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4v1.3h1.3v-1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23.1h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
            handleSize: '80%',
            handleStyle: {
              color: '#fff',
              shadowBlur: 3,
              shadowColor: 'rgba(0, 0, 0, 0.6)',
              shadowOffsetX: 2,
              shadowOffsetY: 2
            }
          }],
          series: [{
            name: '提交',
            type: 'line',
            stack: 'Total',
            data: trendData.commits,
            smooth: true,
            itemStyle: { color: '#409EFF' },
            areaStyle: {
              opacity: 0.3
            }
          }, {
            name: '推送',
            type: 'line',
            stack: 'Total',
            data: trendData.pushes,
            smooth: true,
            itemStyle: { color: '#67C23A' },
            areaStyle: {
              opacity: 0.3
            }
          }, {
            name: 'Pull Request',
            type: 'line',
            stack: 'Total',
            data: trendData.pullRequests,
            smooth: true,
            itemStyle: { color: '#E6A23C' },
            areaStyle: {
              opacity: 0.3
            }
          }, {
            name: 'Issue',
            type: 'line',
            stack: 'Total',
            data: trendData.issues,
            smooth: true,
            itemStyle: { color: '#F56C6C' },
            areaStyle: {
              opacity: 0.3
            }
          }, {
            name: '代码审查',
            type: 'line',
            stack: 'Total',
            data: trendData.reviews,
            smooth: true,
            itemStyle: { color: '#909399' },
            areaStyle: {
              opacity: 0.3
            }
          }]
        }
        
        this.timelineChart.setOption(option)
        
        // 添加图表交互事件
        this.timelineChart.on('click', (params) => {
          this.onChartClick(params)
        })
        
        // 添加图表悬停事件
        this.timelineChart.on('mouseover', (params) => {
          this.onChartHover(params)
        })
        
        // 添加双击事件进行数据钻取
        this.timelineChart.on('dblclick', (params) => {
          this.onChartDrillDown(params)
        })
        
      } catch (error) {
        console.error('渲染活动趋势图表失败:', error)
        // 使用默认数据
        this.renderDefaultTimelineChart()
      }
    },
    
    async loadActivityTrendData() {
      try {
        const params = {
          days: this.getTimeRangeDays() // 根据选择的时间范围获取数据
        }
        
        // 根据选择的仓库或成员调用不同接口
        let url = '/git-data/activities/trend'
        if (this.selectedRepository) {
          url = `/git-data/activities/repository/${this.selectedRepository}/trend`
        } else if (this.selectedMember) {
          url = `/git-data/activities/member/${this.selectedMember}/trend`
        }
        
        if (this.dateRange && this.dateRange.length === 2) {
          params.startDate = this.dateRange[0].toISOString().split('T')[0]
          params.endDate = this.dateRange[1].toISOString().split('T')[0]
        }
        
        const response = await this.$http.get(url, { params })
        if (response.data.code === 200) {
          return response.data.data
        }
      } catch (error) {
        console.error('加载活动趋势数据失败:', error)
      }
      
      // 返回模拟数据
      return this.generateMockTrendData()
    },
    
    generateMockTrendData() {
      const dates = []
      const commits = []
      const pushes = []
      const pullRequests = []
      const issues = []
      const reviews = []
      
      const days = this.getTimeRangeDays()
      
      // 生成指定天数的数据
      for (let i = days - 1; i >= 0; i--) {
        const date = new Date()
        date.setDate(date.getDate() - i)
        dates.push(date.toISOString().split('T')[0])
        
        // 生成随机数据，工作日活动更多
        const isWeekend = date.getDay() === 0 || date.getDay() === 6
        const factor = isWeekend ? 0.3 : 1
        
        commits.push(Math.floor(Math.random() * 20 * factor))
        pushes.push(Math.floor(Math.random() * 15 * factor))
        pullRequests.push(Math.floor(Math.random() * 8 * factor))
        issues.push(Math.floor(Math.random() * 5 * factor))
        reviews.push(Math.floor(Math.random() * 10 * factor))
      }
      
      return {
        dates,
        commits,
        pushes,
        pullRequests,
        issues,
        reviews
      }
    },
    
    getTimeRangeDays() {
      const rangeMap = {
        '7d': 7,
        '30d': 30,
        '90d': 90
      }
      return rangeMap[this.chartTimeRange] || 30
    },
    
    changeChartTimeRange(range) {
      this.chartTimeRange = range
      this.renderTimelineChart()
      this.$message.success(`已切换到${range === '7d' ? '7天' : range === '30d' ? '30天' : '90天'}视图`)
    },
    
    renderDefaultTimelineChart() {
      const option = {
        title: {
          text: '活动趋势分析',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: {
          type: 'value',
          name: '活动次数'
        },
        series: [{
          name: '提交',
          type: 'line',
          data: [23, 45, 56, 78, 32, 17, 28],
          smooth: true,
          itemStyle: { color: '#409EFF' }
        }, {
          name: '推送',
          type: 'line',
          data: [12, 23, 34, 45, 21, 8, 15],
          smooth: true,
          itemStyle: { color: '#67C23A' }
        }]
      }
      
      this.timelineChart.setOption(option)
    },
    
    async renderDistributionChart() {
      try {
        // 获取活动类型分布数据
        const distributionData = await this.loadActivityDistributionData()
        
        const option = {
          title: {
            text: '活动类型分布',
            left: 'center',
            textStyle: {
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c} ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left',
            top: 'middle',
            textStyle: {
              fontSize: 12
            }
          },
          series: [{
            name: '活动类型',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['60%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '18',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: distributionData
          }]
        }
        
        this.distributionChart.setOption(option)
        
        // 添加点击事件
        this.distributionChart.on('click', (params) => {
          this.onDistributionChartClick(params)
        })
        
      } catch (error) {
        console.error('渲染活动分布图表失败:', error)
        this.renderDefaultDistributionChart()
      }
    },
    
    async loadActivityDistributionData() {
      try {
        const params = {}
        
        // 根据选择的仓库或成员调用不同接口
        let url = '/git-data/activities/distribution'
        if (this.selectedRepository) {
          url = `/git-data/activities/repository/${this.selectedRepository}/distribution`
        } else if (this.selectedMember) {
          url = `/git-data/activities/member/${this.selectedMember}/distribution`
        }
        
        if (this.dateRange && this.dateRange.length === 2) {
          params.startDate = this.dateRange[0].toISOString().split('T')[0]
          params.endDate = this.dateRange[1].toISOString().split('T')[0]
        }
        
        const response = await this.$http.get(url, { params })
        if (response.data.code === 200) {
          return response.data.data.map(item => ({
            name: this.getActivityTypeText(item.type),
            value: item.count,
            itemStyle: {
              color: this.getActivityTypeChartColor(item.type)
            }
          }))
        }
      } catch (error) {
        console.error('加载活动分布数据失败:', error)
      }
      
      // 返回模拟数据
      return this.generateMockDistributionData()
    },
    
    generateMockDistributionData() {
      return [
        { name: '提交', value: 45, itemStyle: { color: '#409EFF' } },
        { name: '推送', value: 32, itemStyle: { color: '#67C23A' } },
        { name: 'Pull Request', value: 18, itemStyle: { color: '#E6A23C' } },
        { name: 'Issue', value: 12, itemStyle: { color: '#F56C6C' } },
        { name: '代码审查', value: 25, itemStyle: { color: '#909399' } }
      ]
    },
    
    renderDefaultDistributionChart() {
      const option = {
        title: {
          text: '活动类型分布',
          left: 'center'
        },
        tooltip: {
          trigger: 'item'
        },
        series: [{
          name: '活动类型',
          type: 'pie',
          radius: '50%',
          data: this.generateMockDistributionData()
        }]
      }
      
      this.distributionChart.setOption(option)
    },
    
    onDistributionChartClick(params) {
      const activityType = params.name
      this.activityType = this.getActivityTypeCode(activityType)
      this.loadActivities()
      this.$message.info(`已筛选 ${activityType} 类型的活动`)
    },
    
    getActivityTypeChartColor(type) {
      const colorMap = {
        'COMMIT': '#409EFF',
        'PUSH': '#67C23A',
        'PULL_REQUEST': '#E6A23C',
        'ISSUE': '#F56C6C',
        'CODE_REVIEW': '#909399',
        'MERGE': '#F78989',
        'BRANCH': '#85CE61'
      }
      return colorMap[type] || '#C0C4CC'
    },
    
    onChartClick(params) {
      // 处理图表点击事件，可以跳转到具体日期的活动详情
      const clickedDate = params.name
      const activityType = params.seriesName
      
      console.log(`点击了 ${clickedDate} 的 ${activityType} 数据`)
      
      // 可以在这里添加更多交互逻辑，比如筛选特定日期的活动
      this.filterActivitiesByDate(clickedDate, activityType)
    },
    
    onChartHover(params) {
      // 处理图表悬停事件，显示详细信息
      const date = params.name
      const value = params.value
      const seriesName = params.seriesName
      
      // 可以在这里添加悬停提示逻辑
      console.log(`悬停在 ${date} 的 ${seriesName}: ${value}`)
    },
    
    onChartDrillDown(params) {
      // 处理双击事件，进行数据钻取
      const clickedDate = params.name
      const activityType = params.seriesName
      
      // 打开详细分析对话框或跳转到详细页面
      this.$message.info(`正在加载 ${clickedDate} ${activityType} 的详细数据...`)
      this.loadDetailedActivityData(clickedDate, activityType)
    },
    
    async loadDetailedActivityData(date, activityType) {
      try {
        const params = {
          date: date,
          activityType: this.getActivityTypeCode(activityType),
          detailed: true
        }
        
        const response = await this.$http.get('/git-activities/detailed', { params })
        if (response.data.code === '200') {
          // 显示详细数据对话框或处理详细数据
          this.showDetailedAnalysis(response.data.data, date, activityType)
        }
      } catch (error) {
        console.error('加载详细活动数据失败:', error)
        this.$message.error('加载详细数据失败')
      }
    },
    
    showDetailedAnalysis(data, date, activityType) {
      // 显示详细分析结果
      this.$alert(
        `${date} 的 ${activityType} 活动详情：\n` +
        `总计: ${data.total || 0} 次\n` +
        `参与成员: ${data.memberCount || 0} 人\n` +
        `涉及仓库: ${data.repoCount || 0} 个`,
        '详细分析',
        {
          confirmButtonText: '确定',
          type: 'info'
        }
      )
    },
    
    filterActivitiesByDate(date, activityType) {
      // 根据日期和活动类型筛选活动列表
      const targetDate = new Date(date)
      const startOfDay = new Date(targetDate.setHours(0, 0, 0, 0))
      const endOfDay = new Date(targetDate.setHours(23, 59, 59, 999))
      
      this.dateRange = [startOfDay, endOfDay]
      if (activityType !== '全部') {
        this.activityType = this.getActivityTypeCode(activityType)
      }
      
      this.loadActivities()
      this.$message.info(`已筛选 ${date} 的 ${activityType} 活动`)
    },
    
    getActivityTypeCode(typeName) {
      const typeMap = {
        '提交': 'COMMIT',
        '推送': 'PUSH',
        'Pull Request': 'PULL_REQUEST',
        'Issue': 'ISSUE',
        '代码审查': 'CODE_REVIEW',
        '合并': 'MERGE',
        '分支': 'BRANCH'
      }
      return typeMap[typeName] || ''
    },
    
    toggleAutoRefresh(enabled) {
      if (enabled) {
        this.refreshInterval = setInterval(() => {
          this.refreshData()
        }, 30000) // 30秒刷新一次
        this.$message.success('已开启实时更新，每30秒自动刷新数据')
      } else {
        if (this.refreshInterval) {
          clearInterval(this.refreshInterval)
          this.refreshInterval = null
        }
        this.$message.info('已关闭实时更新')
      }
    },
    
    // 启动实时数据更新
    startRealTimeUpdate() {
      this.realTimeData.enabled = true
      this.realTimeData.connectionStatus = 'connecting'
      
      // 模拟WebSocket连接
      setTimeout(() => {
        this.realTimeData.connectionStatus = 'connected'
        this.realTimeData.lastUpdate = new Date()
        this.$message.success('实时数据连接已建立')
        
        // 启动定时更新 (每30秒)
         this.realTimeData.interval = setInterval(() => {
           this.updateRealTimeData()
         }, 30000)
      }, 1000)
    },
    
    // 停止实时数据更新
    stopRealTimeUpdate() {
      this.realTimeData.enabled = false
      this.realTimeData.connectionStatus = 'disconnected'
      
      if (this.realTimeData.interval) {
        clearInterval(this.realTimeData.interval)
        this.realTimeData.interval = null
      }
      
      this.$message.info('实时数据连接已断开')
    },
    
    // 更新实时数据
    async updateRealTimeData() {
      try {
        // 获取最新的活动数据
        const response = await this.$http.get('/git-activities/realtime')
        if (response.data.code === '200' && response.data.data.length > 0) {
          // 更新活动列表
          const newActivities = response.data.data
          this.activities = [...newActivities, ...this.activities].slice(0, this.pagination.pageSize)
          
          // 更新统计数据
          await this.loadStats()
          
          // 更新图表
          this.renderTimelineChart()
          this.renderDistributionChart()
          
          // 更新最后更新时间
          this.realTimeData.lastUpdate = new Date()
          
          // 显示新活动通知
          if (newActivities.length > 0) {
            this.$notify({
              title: '新活动',
              message: `检测到 ${newActivities.length} 个新的Git活动`,
              type: 'info',
              duration: 3000
            })
          }
        }
      } catch (error) {
        console.error('更新实时数据失败:', error)
        this.realTimeData.connectionStatus = 'error'
      }
    },
    
    // 启用交互式模式
    enableInteractiveMode() {
      this.interactiveMode.enabled = true
      
      // 为图表添加交互事件
      if (this.timelineChart) {
        this.addTimelineChartInteractions()
      }
      
      if (this.distributionChart) {
        this.addDistributionChartInteractions()
      }
    },
    
    // 添加时间线图表交互
    addTimelineChartInteractions() {
      // 添加点击事件
      this.timelineChart.on('click', (params) => {
        this.onTimelineChartClick(params)
      })
      
      // 添加双击事件（数据钻取）
      this.timelineChart.on('dblclick', (params) => {
        this.onChartDrillDown(params)
      })
      
      // 添加框选事件
      this.timelineChart.on('brush', (params) => {
        this.onTimelineChartBrush(params)
      })
      
      // 添加悬停事件
      this.timelineChart.on('mouseover', (params) => {
        this.onChartHover(params)
      })
    },
    
    // 添加分布图表交互
    addDistributionChartInteractions() {
      // 添加点击事件
      this.distributionChart.on('click', (params) => {
        this.onDistributionChartClick(params)
      })
      
      // 添加悬停高亮
      this.distributionChart.on('mouseover', (params) => {
        this.highlightRelatedData(params)
      })
    },
    
    // 时间线图表点击事件
    onTimelineChartClick(params) {
      const clickedDate = params.name
      const activityType = params.seriesName
      
      // 设置选中的时间范围
      this.interactiveMode.selectedTimeRange = {
        date: clickedDate,
        type: activityType
      }
      
      // 筛选对应的活动数据
      this.filterActivitiesByDate(clickedDate, activityType)
      
      // 高亮显示选中的数据点
      this.highlightDataPoint(params)
    },
    
    // 时间线图表框选事件
    onTimelineChartBrush(params) {
      if (params.areas && params.areas.length > 0) {
        const brushArea = params.areas[0]
        const startIndex = brushArea.coordRange[0]
        const endIndex = brushArea.coordRange[1]
        
        // 获取框选范围的数据
        this.interactiveMode.brushSelection = {
          startIndex,
          endIndex,
          data: this.getDataInRange(startIndex, endIndex)
        }
        
        // 显示框选数据的统计信息
        this.showBrushStatistics(this.interactiveMode.brushSelection)
      }
    },
    
    // 高亮相关数据
    highlightRelatedData(params) {
      const activityType = params.name
      
      // 在时间线图表中高亮对应的系列
      if (this.timelineChart) {
        this.timelineChart.dispatchAction({
          type: 'highlight',
          seriesName: activityType
        })
      }
    },
    
    // 高亮数据点
    highlightDataPoint(params) {
      const option = {
        series: [{
          markPoint: {
            data: [{
              name: '选中点',
              coord: [params.dataIndex, params.value],
              itemStyle: {
                color: '#FF6B6B',
                borderColor: '#fff',
                borderWidth: 2
              }
            }]
          }
        }]
      }
      
      this.timelineChart.setOption(option)
    },
    
    // 获取范围内的数据
    getDataInRange(startIndex, endIndex) {
      // 根据索引范围获取对应的数据
      const rangeData = {
        totalActivities: 0,
        activityTypes: {},
        dateRange: []
      }
      
      // 这里应该根据实际的图表数据来计算
      // 暂时返回模拟数据
      return rangeData
    },
    
    // 显示框选统计信息
     showBrushStatistics(brushData) {
       this.$notify({
         title: '框选区域统计',
         message: `选中区域包含 ${brushData.data.totalActivities || 0} 个活动`,
         type: 'info',
         duration: 5000
       })
     },
     
     // 清除选择
     clearSelection() {
       this.interactiveMode.selectedTimeRange = null
       this.interactiveMode.brushSelection = null
       
       // 清除图表中的高亮和标记
       if (this.timelineChart) {
         this.timelineChart.setOption({
           series: [{
             markPoint: { data: [] }
           }]
         })
         
         this.timelineChart.dispatchAction({
           type: 'downplay'
         })
       }
       
       // 重新加载所有活动数据
       this.loadActivities()
     },
    
    async exportData() {
      this.exportLoading = true
      try {
        const params = {
          courseId: this.selectedCourse,
          repositoryId: this.selectedRepository,
          activityType: this.activityType,
          memberId: this.selectedMember,
          fileType: this.fileType
        }
        
        if (this.dateRange && this.dateRange.length === 2) {
          params.startDate = this.dateRange[0].toISOString().split('T')[0]
          params.endDate = this.dateRange[1].toISOString().split('T')[0]
        }
        
        const response = await this.$http.get('/git-activities/export', {
          params,
          responseType: 'blob'
        })
        
        // 创建下载链接
        const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `git-activities-${new Date().toISOString().split('T')[0]}.xlsx`
        link.click()
        window.URL.revokeObjectURL(url)
        
        this.$message.success('数据导出成功')
      } catch (error) {
        console.error('导出数据失败:', error)
        this.$message.error('导出数据失败，请稍后重试')
      } finally {
        this.exportLoading = false
      }
    },
    
    resetFilters() {
      this.selectedCourse = ''
      this.selectedRepository = ''
      this.activityType = ''
      this.selectedMember = ''
      this.fileType = ''
      this.dateRange = []
      
      // 重新加载数据
      this.loadRepositories()
      this.loadMembers()
      this.loadActivities()
      this.loadStats()
      
      this.$message.success('筛选条件已重置')
    },
    
    refreshData() {
      this.loadActivities()
      this.loadStats()
      this.renderTimelineChart()
      this.renderDistributionChart()
    },
    
    viewActivityDetail(activity) {
      this.selectedActivity = activity
      this.showDetailDialog = true
    },
    
    getActivityTypeText(type) {
      const typeMap = {
        'COMMIT': '提交',
        'PUSH': '推送',
        'PULL_REQUEST': 'Pull Request',
        'ISSUE': 'Issue',
        'CODE_REVIEW': '代码审查',
        'MERGE': '合并',
        'BRANCH': '分支'
      }
      return typeMap[type] || type
    },
    
    getActivityTypeColor(type) {
      const colorMap = {
        'COMMIT': 'primary',
        'PUSH': 'success',
        'MERGE': 'warning',
        'BRANCH': 'info'
      }
      return colorMap[type] || ''
    },
    
    getCommitUrl(activity) {
      // 构建提交URL（需要根据实际Git平台调整）
      if (activity.repositoryUrl && activity.commitHash) {
        return `${activity.repositoryUrl}/commit/${activity.commitHash}`
      }
      return '#'
    },
    
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.pagination.current = 1
      this.loadActivities()
    },
    
    handleCurrentChange(page) {
      this.pagination.current = page
      this.loadActivities()
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      return new Date(dateString).toLocaleString('zh-CN')
    }
  }
}
</script>

<style scoped>
.git-activity-monitor {
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

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.stat-icon.commit {
  background: #409EFF;
}

.stat-icon.active {
  background: #67C23A;
}

.stat-icon.lines {
  background: #E6A23C;
}

.stat-icon.repos {
  background: #F56C6C;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.chart-section {
  margin-bottom: 20px;
}

.activity-list {
  background: white;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.lines-added {
  color: #67C23A;
  font-weight: bold;
}

.lines-deleted {
  color: #F56C6C;
  font-weight: bold;
}

.el-card {
  margin-bottom: 20px;
}

.realtime-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.last-update {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.selected-range {
  display: flex;
  align-items: center;
  gap: 8px;
}

.refresh-info {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.5; }
  100% { opacity: 1; }
}

.realtime-status .el-tag {
  animation: none;
}

.realtime-status .el-tag.el-tag--success {
  animation: connected-pulse 3s infinite;
}

@keyframes connected-pulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.4); }
  50% { box-shadow: 0 0 0 4px rgba(103, 194, 58, 0); }
}
</style>