<template>
  <div class="result-display">
    <NavMenu />
    <div class="main-content">
      <div class="page-header">
        <h1>结果展示</h1>
        <p>查看学生贡献度评估结果和分析报告</p>
      </div>

      <el-tabs v-model="activeTab" type="card" class="result-tabs">
        <!-- 个人报告 -->
        <el-tab-pane label="个人报告" name="personal">
          <div class="personal-report" v-loading="loading.personal">
            <div class="report-filters">
              <el-row :gutter="20">
                <el-col :span="6">
                  <el-select v-model="selectedCourse" placeholder="选择课程" @change="onCourseChange">
                    <el-option
                      v-for="course in courses"
                      :key="course.courseId || course.id"
                      :label="course.courseName || course.name"
                      :value="course.courseId || course.id">
                    </el-option>
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-select v-model="selectedStudent" placeholder="选择学生" @change="loadPersonalData">
                    <el-option
                      v-for="student in students"
                      :key="student.memberId || student.id"
                      :label="student.memberName || student.name"
                      :value="student.memberId || student.id">
                    </el-option>
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-date-picker
                    v-model="dateRange"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    @change="loadPersonalData">
                  </el-date-picker>
                </el-col>
                <el-col :span="6">
                  <el-button type="primary" @click="exportPersonalReport" :loading="loading.personal">导出报告</el-button>
                </el-col>
              </el-row>
            </div>

            <div class="report-content" v-if="personalData">
              <el-row :gutter="20">
                <el-col :span="8">
                  <el-card class="score-card">
                    <div class="score-display">
                      <div class="score-value">{{ personalData.totalScore }}</div>
                      <div class="score-label">总体贡献度</div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card class="rank-card">
                    <div class="rank-display">
                      <div class="rank-value">{{ personalData.rank }}</div>
                      <div class="rank-label">班级排名</div>
                    </div>
                  </el-card>
                </el-col>
                <el-col :span="8">
                  <el-card class="improvement-card">
                    <div class="improvement-display">
                      <div class="improvement-value" :class="personalData.improvement >= 0 ? 'positive' : 'negative'">
                        {{ personalData.improvement >= 0 ? '+' : '' }}{{ personalData.improvement }}%
                      </div>
                      <div class="improvement-label">较上期变化</div>
                    </div>
                  </el-card>
                </el-col>
              </el-row>

              <el-row :gutter="20" class="chart-row">
                <el-col :span="12">
                  <el-card>
                    <div slot="header">维度得分雷达图</div>
                    <div id="radarChart" style="height: 300px;"></div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card>
                    <div slot="header">贡献度趋势</div>
                    <div id="trendChart" style="height: 300px;"></div>
                  </el-card>
                </el-col>
              </el-row>

              <el-card class="detail-table">
                <div slot="header">详细评分</div>
                <el-table :data="personalData.details" stripe style="width: 100%" table-layout="fixed">
                  <el-table-column prop="dimension" label="评价维度" min-width="120" show-overflow-tooltip></el-table-column>
                  <el-table-column prop="score" label="得分" min-width="80" align="center">
                    <template slot-scope="scope">
                      <el-tag :type="getScoreType(scope.row.score)">{{ scope.row.score }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="weight" label="权重" min-width="80" align="center">
                    <template slot-scope="scope">
                      {{ (scope.row.weight * 100).toFixed(0) }}%
                    </template>
                  </el-table-column>
                  <el-table-column prop="weightedScore" label="加权得分" min-width="100" align="center">
                    <template slot-scope="scope">
                      <el-tag type="info">{{ scope.row.weightedScore }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="description" label="评价说明" min-width="150" show-overflow-tooltip></el-table-column>
                </el-table>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <!-- 团队对比 -->
        <el-tab-pane label="团队对比" name="team">
          <div class="team-comparison" v-loading="loading.team">
            <div class="comparison-filters">
              <el-row :gutter="20">
                <el-col :span="6">
                  <el-select v-model="selectedTeamCourse" placeholder="选择课程" @change="onTeamCourseChange">
                    <el-option
                      v-for="course in courses"
                      :key="course.courseId || course.id"
                      :label="course.courseName || course.name"
                      :value="course.courseId || course.id">
                    </el-option>
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-select v-model="selectedTeam" placeholder="选择团队" @change="onTeamChange">
                    <el-option
                      v-for="team in teams"
                      :key="team.id"
                      :label="team.name"
                      :value="team.id">
                    </el-option>
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-select v-model="comparisonType" placeholder="对比类型" @change="loadTeamData">
                    <el-option label="团队内对比" value="internal"></el-option>
                    <el-option label="团队间对比" value="external"></el-option>
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-button type="primary" @click="exportTeamReport" :loading="loading.team">导出对比报告</el-button>
                </el-col>
              </el-row>
            </div>

            <div class="comparison-content" v-if="teamData">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-card>
                    <div slot="header">团队贡献度对比</div>
                    <div id="teamComparisonChart" style="height: 400px;"></div>
                  </el-card>
                </el-col>
              </el-row>

              <el-row :gutter="20" class="chart-row">
                <el-col :span="12">
                  <el-card>
                    <div slot="header">维度分布对比</div>
                    <div id="dimensionChart" style="height: 300px;"></div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card>
                    <div slot="header">团队协作指数</div>
                    <div id="collaborationChart" style="height: 300px;"></div>
                  </el-card>
                </el-col>
              </el-row>

              <el-card class="team-table">
                <div slot="header">团队成员详情</div>
                <el-table :data="teamData.members" stripe style="width: 100%" table-layout="fixed">
                  <el-table-column prop="name" label="姓名" min-width="100" show-overflow-tooltip></el-table-column>
                  <el-table-column prop="totalScore" label="总分" min-width="90" sortable align="center">
                    <template slot-scope="scope">
                      <el-tag :type="getScoreType(scope.row.totalScore)">{{ scope.row.totalScore }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="codeContribution" label="代码贡献" min-width="110" sortable align="center">
                    <template slot-scope="scope">
                      <span>{{ scope.row.codeContribution }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="documentContribution" label="文档贡献" min-width="110" sortable align="center">
                    <template slot-scope="scope">
                      <span>{{ scope.row.documentContribution }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="communicationScore" label="沟通协作" min-width="110" sortable align="center">
                    <template slot-scope="scope">
                      <span>{{ scope.row.communicationScore }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="innovationScore" label="创新能力" min-width="110" sortable align="center">
                    <template slot-scope="scope">
                      <span>{{ scope.row.innovationScore }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="rank" label="排名" min-width="80" align="center">
                    <template slot-scope="scope">
                      <el-tag :type="getRankType(scope.row.rank)" size="small">第{{ scope.row.rank }}名</el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <!-- 趋势分析 -->
        <el-tab-pane label="趋势分析" name="trend">
          <div class="trend-analysis" v-loading="loading.trend">
            <div class="trend-filters">
              <el-row :gutter="20">
                <el-col :span="6">
                  <el-select v-model="selectedTrendCourse" placeholder="选择课程" @change="onTrendCourseChange">
                    <el-option
                      v-for="course in courses"
                      :key="course.courseId || course.id"
                      :label="course.courseName || course.name"
                      :value="course.courseId || course.id">
                    </el-option>
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-select v-model="trendPeriod" placeholder="时间周期" @change="loadTrendData">
                    <el-option label="最近一周" value="week"></el-option>
                    <el-option label="最近一月" value="month"></el-option>
                    <el-option label="最近三月" value="quarter"></el-option>
                    <el-option label="整个学期" value="semester"></el-option>
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-select v-model="trendMetric" placeholder="分析指标" @change="loadTrendData">
                    <el-option label="总体贡献度" value="total"></el-option>
                    <el-option label="代码贡献" value="code"></el-option>
                    <el-option label="文档贡献" value="document"></el-option>
                    <el-option label="沟通协作" value="communication"></el-option>
                  </el-select>
                </el-col>
                <el-col :span="6">
                  <el-button type="primary" @click="exportTrendReport" :loading="loading.trend">导出趋势报告</el-button>
                </el-col>
              </el-row>
            </div>

            <div class="trend-content" v-if="trendData">
              <el-row :gutter="20">
                <el-col :span="24">
                  <el-card>
                    <div slot="header">贡献度趋势变化</div>
                    <div id="overallTrendChart" style="height: 400px;"></div>
                  </el-card>
                </el-col>
              </el-row>

              <el-row :gutter="20" class="chart-row">
                <el-col :span="12">
                  <el-card>
                    <div slot="header">维度趋势对比</div>
                    <div id="dimensionTrendChart" style="height: 300px;"></div>
                  </el-card>
                </el-col>
                <el-col :span="12">
                  <el-card>
                    <div slot="header">活跃度分析</div>
                    <div id="activityChart" style="height: 300px;"></div>
                  </el-card>
                </el-col>
              </el-row>

              <el-card class="trend-summary">
                <div slot="header">趋势总结</div>
                <el-row :gutter="20">
                  <el-col :span="8">
                    <div class="summary-item">
                      <div class="summary-title">平均增长率</div>
                      <div class="summary-value positive">+{{ trendData.avgGrowthRate }}%</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="summary-item">
                      <div class="summary-title">最佳表现维度</div>
                      <div class="summary-value">{{ trendData.bestDimension }}</div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="summary-item">
                      <div class="summary-title">需改进维度</div>
                      <div class="summary-value warning">{{ trendData.improvementNeeded }}</div>
                    </div>
                  </el-col>
                </el-row>
              </el-card>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import NavMenu from '@/components/NavMenu.vue'
import * as echarts from 'echarts'

export default {
  name: 'ResultDisplay',
  components: {
    NavMenu
  },
  data() {
    return {
      activeTab: 'personal',
      
      // 基础数据
      courses: [],
      students: [],
      teams: [],
      total: 0,
      organizationId: null,
      
      // 加载状态
      loading: {
        personal: false,
        team: false,
        trend: false
      },
      
      // 个人报告相关
      selectedCourse: null,
      selectedStudent: null,
      dateRange: null,
      personalData: null,
      
      // 团队对比相关
      selectedTeamCourse: null,
      selectedTeam: null,
      comparisonType: 'internal',
      teamData: null,
      
      // 趋势分析相关
      selectedTrendCourse: null,
      trendPeriod: 'month',
      trendMetric: 'total',
      trendData: null,
      
      // 图表实例
      charts: {}
    }
  },
  async mounted() {
    this.initializeCharts()
    this.organizationId = this.getOrganizationId()
    await this.loadCourses()
    
    // 添加窗口resize监听器
    this.handleResize = () => {
      Object.values(this.charts).forEach(chart => {
        if (chart && typeof chart.resize === 'function') {
          chart.resize()
        }
      })
    }
    window.addEventListener('resize', this.handleResize)
    
    // 从路由参数获取初始值
    const { courseId, memberId } = this.$route.query
    if (courseId) {
      const parsedCourseId = parseInt(courseId)
      this.selectedCourse = parsedCourseId
      this.selectedTeamCourse = parsedCourseId
      this.selectedTrendCourse = parsedCourseId
      
      // 加载初始课程的相关数据
      await this.loadStudents(parsedCourseId)
      await this.loadTeams(parsedCourseId)
    }
    if (memberId) {
      this.selectedStudent = parseInt(memberId)
    }
  },
  watch: {
    // 监听activeTab变化，切换标签页时重新渲染图表
    activeTab(newTab, oldTab) {
      console.log('标签页切换:', oldTab, '->', newTab)
      this.$nextTick(() => {
        if (newTab === 'team') {
          console.log('切换到团队对比标签页，当前团队数据:', this.teamData)
          // 切换到团队对比页面时，重新初始化和渲染图表
          setTimeout(() => {
            this.initializeTeamCharts()
            // 延迟渲染，确保图表实例创建完成
            setTimeout(() => {
              if (this.teamData && this.teamData.members && this.teamData.members.length > 0) {
                console.log('开始渲染团队图表，数据:', this.teamData)
                this.renderTeamComparisonChart()
                this.renderDimensionChart()
                this.renderCollaborationChart()
              } else {
                console.log('无团队数据，跳过图表渲染')
              }
            }, 800)
          }, 300)
        } else if (newTab === 'trend') {
          // 切换到趋势分析页面时，重新初始化和渲染图表
          setTimeout(() => {
            this.initializeTrendCharts()
            setTimeout(() => {
              if (this.trendData) {
                this.renderOverallTrendChart()
                this.renderDimensionTrendChart()
                this.renderActivityChart()
              }
            }, 200)
          }, 300)
        } else if (newTab === 'personal') {
          // 切换到个人报告页面时，重新渲染图表
          setTimeout(() => {
            if (this.personalData) {
              this.renderRadarChart()
              this.renderTrendChart()
            }
          }, 300)
        }
      })
    },
    
    // 监听团队选择变化
    selectedTeam(newTeam, oldTeam) {
      console.log('团队选择变化:', oldTeam, '->', newTeam)
      if (newTeam && this.activeTab === 'team') {
        console.log('团队选择变化，重新加载团队数据')
        this.loadTeamData()
      }
    },
    
    // 监听团队课程选择变化
    selectedTeamCourse(newCourse, oldCourse) {
      console.log('团队课程选择变化:', oldCourse, '->', newCourse)
      if (newCourse && this.activeTab === 'team') {
        console.log('团队课程选择变化，重新加载团队列表和数据')
        this.loadTeams()
      }
    },

    // 监听趋势分析课程选择变化
    selectedTrendCourse(newCourse, oldCourse) {
      console.log('趋势分析课程选择变化:', oldCourse, '->', newCourse)
      if (newCourse && this.activeTab === 'trend') {
        console.log('趋势分析课程选择变化，重新加载趋势数据')
        this.loadTrendData()
      }
    },

    // 监听趋势数据变化
    trendData: {
      handler(newData, oldData) {
        console.log('趋势数据变化:', oldData, '->', newData)
        if (newData && this.activeTab === 'trend') {
          console.log('趋势数据变化，重新渲染图表')
          this.$nextTick(() => {
            setTimeout(() => {
              this.renderOverallTrendChart()
              this.renderDimensionTrendChart()
              this.renderActivityChart()
            }, 100)
          })
        }
      },
      deep: true
    },

    // 监听趋势分析时间范围变化
    trendTimeRange(newRange, oldRange) {
      console.log('趋势分析时间范围变化:', oldRange, '->', newRange)
      if (newRange && this.activeTab === 'trend') {
        console.log('趋势分析时间范围变化，重新加载数据')
        this.loadTrendData()
      }
    },

    // 监听趋势分析指标变化
    trendMetric(newMetric, oldMetric) {
      console.log('趋势分析指标变化:', oldMetric, '->', newMetric)
      if (newMetric && this.activeTab === 'trend') {
        console.log('趋势分析指标变化，重新加载数据')
        this.loadTrendData()
      }
    }
  },
  beforeDestroy() {
    // 移除resize监听器
    if (this.handleResize) {
      window.removeEventListener('resize', this.handleResize)
    }
    
    // 销毁图表实例
    Object.values(this.charts).forEach(chart => {
      if (chart) {
        chart.dispose()
      }
    })
  },
  methods: {
    initializeCharts() {
      // 不在这里初始化所有图表，而是根据当前activeTab来初始化
      this.$nextTick(() => {
        if (this.activeTab === 'team') {
          this.initializeTeamCharts()
        } else if (this.activeTab === 'trend') {
          this.initializeTrendCharts()
        }
        // 个人报告页面的雷达图和趋势图将在数据加载时动态创建
      })
    },
    
    clearTeamCharts() {
      console.log('清除团队图表实例')
      if (this.charts.teamComparison) {
        this.charts.teamComparison.dispose()
        this.charts.teamComparison = null
      }
      if (this.charts.dimension) {
        this.charts.dimension.dispose()
        this.charts.dimension = null
      }
      if (this.charts.collaboration) {
        this.charts.collaboration.dispose()
        this.charts.collaboration = null
      }
    },

    clearTrendCharts() {
      console.log('清除趋势分析图表实例')
      if (this.charts.overallTrend) {
        this.charts.overallTrend.dispose()
        this.charts.overallTrend = null
      }
      if (this.charts.dimensionTrend) {
        this.charts.dimensionTrend.dispose()
        this.charts.dimensionTrend = null
      }
      if (this.charts.activity) {
        this.charts.activity.dispose()
        this.charts.activity = null
      }
    },

    initializeTeamCharts() {
      console.log('初始化团队对比图表，当前activeTab:', this.activeTab)
      
      // 使用更长的延迟确保DOM完全渲染和标签页切换完成
      setTimeout(() => {
        console.log('开始检查团队图表DOM元素')
        
        const teamComparisonElement = document.getElementById('teamComparisonChart')
        console.log('团队贡献度对比图表元素:', teamComparisonElement)
        if (teamComparisonElement) {
          const rect = teamComparisonElement.getBoundingClientRect()
          console.log('团队贡献度对比图表元素尺寸:', rect)
          
          if (rect.width > 0 && rect.height > 0) {
            try {
              if (this.charts.teamComparison) {
                this.charts.teamComparison.dispose()
              }
              this.charts.teamComparison = echarts.init(teamComparisonElement)
              console.log('团队贡献度对比图表初始化成功，实例:', !!this.charts.teamComparison)
            } catch (error) {
              console.error('团队贡献度对比图表初始化失败:', error)
            }
          } else {
            console.warn('团队贡献度对比图表容器不可见，尺寸:', rect)
          }
        } else {
          console.error('团队贡献度对比图表容器不存在')
        }
        
        const dimensionElement = document.getElementById('dimensionChart')
        console.log('维度分布对比图表元素:', dimensionElement)
        if (dimensionElement) {
          const rect = dimensionElement.getBoundingClientRect()
          console.log('维度分布对比图表元素尺寸:', rect)
          
          if (rect.width > 0 && rect.height > 0) {
            try {
              if (this.charts.dimension) {
                this.charts.dimension.dispose()
              }
              this.charts.dimension = echarts.init(dimensionElement)
              console.log('维度分布对比图表初始化成功，实例:', !!this.charts.dimension)
            } catch (error) {
              console.error('维度分布对比图表初始化失败:', error)
            }
          } else {
            console.warn('维度分布对比图表容器不可见，尺寸:', rect)
          }
        } else {
          console.error('维度分布对比图表容器不存在')
        }
        
        const collaborationElement = document.getElementById('collaborationChart')
        console.log('团队协作指数图表元素:', collaborationElement)
        if (collaborationElement) {
          const rect = collaborationElement.getBoundingClientRect()
          console.log('团队协作指数图表元素尺寸:', rect)
          
          if (rect.width > 0 && rect.height > 0) {
            try {
              if (this.charts.collaboration) {
                this.charts.collaboration.dispose()
              }
              this.charts.collaboration = echarts.init(collaborationElement)
              console.log('团队协作指数图表初始化成功，实例:', !!this.charts.collaboration)
            } catch (error) {
              console.error('团队协作指数图表初始化失败:', error)
            }
          } else {
            console.warn('团队协作指数图表容器不可见，尺寸:', rect)
          }
        } else {
          console.error('团队协作指数图表容器不存在')
        }
        
        console.log('团队图表初始化完成，实例状态:', {
          teamComparison: !!this.charts.teamComparison,
          dimension: !!this.charts.dimension,
          collaboration: !!this.charts.collaboration
        })
      }, 600) // 增加延迟时间到600ms
    },
    
    initializeTrendCharts() {
      console.log('初始化趋势分析图表')
      
      const overallTrendElement = document.getElementById('overallTrendChart')
      if (overallTrendElement) {
        if (this.charts.overallTrend) {
          this.charts.overallTrend.dispose()
        }
        this.charts.overallTrend = echarts.init(overallTrendElement)
        console.log('整体趋势图表初始化成功')
      } else {
        console.warn('整体趋势图表容器不存在')
      }
      
      const dimensionTrendElement = document.getElementById('dimensionTrendChart')
      if (dimensionTrendElement) {
        if (this.charts.dimensionTrend) {
          this.charts.dimensionTrend.dispose()
        }
        this.charts.dimensionTrend = echarts.init(dimensionTrendElement)
        console.log('维度趋势图表初始化成功')
      } else {
        console.warn('维度趋势图表容器不存在')
      }
      
      const activityElement = document.getElementById('activityChart')
      if (activityElement) {
        if (this.charts.activity) {
          this.charts.activity.dispose()
        }
        this.charts.activity = echarts.init(activityElement)
        console.log('活跃度图表初始化成功')
      } else {
        console.warn('活跃度图表容器不存在')
      }
    },
    
  // 获取组织ID（从store或localStorage获取）
    getOrganizationId() {
      return this.$store?.state?.user?.organizationId || localStorage.getItem('organizationId') || 1
    },

    async loadCourses() {
      // 注释掉接口调用，使用模拟数据
      /*
      try {
        const response = await this.$http.get(`/courses?organizationId=${this.organizationId}`)
        if (response.data.code === '200') {
          this.total = response.data.data?.total || 0
          this.courses = response.data.data?.records || response.data.data || []
        }
      } catch (error) {
        console.error('加载课程列表失败:', error)
        this.$message.error('加载课程列表失败')
      }
      */
      
      // 使用模拟数据
      this.courses = [
        { id: 1, courseId: 1, name: '软件工程', courseName: '软件工程', description: '软件开发基础课程' },
        { id: 2, courseId: 2, name: '数据结构', courseName: '数据结构', description: '计算机基础课程' },
        { id: 3, courseId: 3, name: '算法设计', courseName: '算法设计', description: '算法分析与设计' },
        { id: 4, courseId: 4, name: '数据库系统', courseName: '数据库系统', description: '数据库原理与应用' }
      ]
      this.total = this.courses.length
    },
    
    // 课程选择变化时的处理方法
    async onCourseChange(courseId) {
      if (courseId) {
        // 清空当前选择的学生
        this.selectedStudent = null
        this.personalData = null
        
        // 加载该课程的学生列表
        await this.loadStudents(courseId)
      } else {
        // 如果没有选择课程，清空学生列表
        this.students = []
        this.selectedStudent = null
        this.personalData = null
      }
    },
     
     // 团队对比课程选择变化时的处理方法
     async onTeamCourseChange(courseId) {
       console.log('团队课程选择变化:', courseId)
       if (courseId) {
         this.selectedTeam = null
         this.teamData = null
         
         // 清除现有图表实例
         this.clearTeamCharts()
         
         await this.loadTeams(courseId)
         
         // 自动加载第一个团队的数据
         if (this.teams && this.teams.length > 0) {
           this.selectedTeam = this.teams[0].id
           console.log('自动选择第一个团队:', this.selectedTeam)
           await this.loadTeamData()
         }
       } else {
         this.teams = []
         this.selectedTeam = null
         this.teamData = null
         this.clearTeamCharts()
       }
     },
     
     // 趋势分析课程选择变化时的处理方法
     async onTrendCourseChange(courseId) {
       console.log('趋势分析课程选择变化:', courseId)
       if (courseId) {
         this.trendData = null
         // 清除旧的图表实例
         this.clearTrendCharts()
         
         await this.loadTrendData()
         
         // 确保在数据加载完成后重新初始化和渲染图表
         this.$nextTick(() => {
           this.initializeTrendCharts()
           setTimeout(() => {
             if (this.trendData) {
               this.renderOverallTrendChart()
               this.renderDimensionTrendChart()
               this.renderActivityChart()
             }
           }, 100)
         })
       } else {
         this.trendData = null
         this.clearTrendCharts()
       }
     },
     
     // 团队选择变化时的处理方法
     async onTeamChange(teamId) {
       console.log('团队选择变化:', teamId)
       if (teamId) {
         this.selectedTeam = teamId
         // 清除现有图表实例，确保重新渲染
         this.clearTeamCharts()
         await this.loadTeamData()
       } else {
         this.selectedTeam = null
         this.teamData = null
         this.clearTeamCharts()
       }
     },
     
     async loadStudents(courseId) {
      if (!courseId) return
      // 注释掉接口调用，使用模拟数据
      /*
      try {
        const response = await this.$http.get(`/courses/${courseId}/students`)
        if (response.data.code === '200') {
          this.students = response.data.data
        }
      } catch (error) {
        console.error('加载学生列表失败:', error)
        this.$message.error('加载学生列表失败')
      }
      */
      
      // 使用模拟数据
      this.students = [
        { id: 1, memberId: 1, name: '张三', memberName: '张三', studentId: '2021001', email: 'zhangsan@example.com' },
        { id: 2, memberId: 2, name: '李四', memberName: '李四', studentId: '2021002', email: 'lisi@example.com' },
        { id: 3, memberId: 3, name: '王五', memberName: '王五', studentId: '2021003', email: 'wangwu@example.com' },
        { id: 4, memberId: 4, name: '赵六', memberName: '赵六', studentId: '2021004', email: 'zhaoliu@example.com' },
        { id: 5, memberId: 5, name: '钱七', memberName: '钱七', studentId: '2021005', email: 'qianqi@example.com' }
      ]
    },
     
     async loadTeams(courseId) {
       if (!courseId) return
       // 注释掉接口调用，使用模拟数据
       /*
       try {
         const response = await this.$http.get(`/courses/${courseId}/teams`)
         if (response.data.code === '200') {
           this.teams = response.data.data || []
         }
       } catch (error) {
         console.error('加载团队列表失败:', error)
         this.$message.error('加载团队列表失败')
       }
       */
       
       // 使用模拟数据
       this.teams = [
         { id: 1, name: '开发小组A', description: '负责前端开发', memberCount: 4 },
         { id: 2, name: '开发小组B', description: '负责后端开发', memberCount: 3 },
         { id: 3, name: '测试小组', description: '负责系统测试', memberCount: 2 },
         { id: 4, name: '设计小组', description: '负责UI设计', memberCount: 3 }
       ]
     },
     
     renderCharts() {
      // 使用nextTick确保DOM已渲染完成
      this.$nextTick(() => {
        // 添加延迟确保容器元素完全可见
        setTimeout(() => {
          this.renderRadarChart()
          this.renderTrendChart()
          this.renderTeamComparisonChart()
          this.renderDimensionChart()
          this.renderCollaborationChart()
          this.renderOverallTrendChart()
          this.renderDimensionTrendChart()
          this.renderActivityChart()
        }, 200)
      })
    },
    
    renderRadarChart() {
      // 确保在下一个tick中执行，等待DOM完全渲染
      this.$nextTick(() => {
        const chartDom = document.getElementById('radarChart')
        if (!chartDom) {
          console.warn('雷达图容器不存在')
          return
        }
        
        // 如果图表实例已存在，先销毁
        if (this.charts.radar) {
          this.charts.radar.dispose()
        }
        
        const myChart = echarts.init(chartDom)
        this.charts.radar = myChart
      
      // 使用完整的模拟数据
      const radarData = {
        dimensions: ['代码贡献', '文档贡献', '沟通协作', '创新能力', '团队合作', '问题解决'],
        personalScores: [88, 82, 90, 75, 85, 78],
        classAverage: [75, 78, 80, 70, 82, 73]
      }
      
      const option = {
        title: {
          text: '维度得分雷达图',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: function(params) {
            return `${params.seriesName}<br/>${params.name}: ${params.value}`
          }
        },
        legend: {
          data: ['个人得分', '班级平均'],
          bottom: 10
        },
        radar: {
          indicator: radarData.dimensions.map(dim => ({
            name: dim,
            max: 100,
            min: 0
          })),
          radius: '60%',
          splitNumber: 5,
          axisLine: {
            lineStyle: {
              color: '#e6e6e6'
            }
          },
          splitLine: {
            lineStyle: {
              color: '#e6e6e6'
            }
          },
          splitArea: {
            show: true,
            areaStyle: {
              color: ['rgba(114, 172, 209, 0.1)', 'rgba(255, 255, 255, 0.1)']
            }
          }
        },
        series: [{
          name: '维度得分对比',
          type: 'radar',
          data: [
            {
              value: radarData.personalScores,
              name: '个人得分',
              itemStyle: {
                color: '#409EFF'
              },
              areaStyle: {
                color: 'rgba(64, 158, 255, 0.3)'
              }
            },
            {
              value: radarData.classAverage,
              name: '班级平均',
              itemStyle: {
                color: '#67C23A'
              },
              areaStyle: {
                color: 'rgba(103, 194, 58, 0.2)'
              }
            }
          ]
        }]
      }
      
        myChart.setOption(option)
        
        // 响应式处理
        window.addEventListener('resize', () => {
          if (this.charts.radar) {
            this.charts.radar.resize()
          }
        })
      })
    },
    
    renderTrendChart() {
      // 确保在下一个tick中执行，等待DOM完全渲染
      this.$nextTick(() => {
        const chartDom = document.getElementById('trendChart')
        if (!chartDom) {
          console.warn('趋势图容器不存在')
          return
        }
        
        // 如果图表实例已存在，先销毁
        if (this.charts.trend) {
          this.charts.trend.dispose()
        }
        
        const myChart = echarts.init(chartDom)
        this.charts.trend = myChart
      
      // 使用完整的模拟数据
      const trendData = {
        weeks: ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周', '第7周', '第8周'],
        personalTrend: [72, 75, 78, 82, 85, 88, 90, 92],
        classAverage: [70, 73, 76, 78, 80, 82, 84, 85],
        targetLine: [85, 85, 85, 85, 85, 85, 85, 85]
      }
      
      const option = {
        title: {
          text: '贡献度趋势分析',
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
              result += `${param.seriesName}: ${param.value}<br/>`
            })
            return result
          }
        },
        legend: {
          data: ['个人贡献度', '班级平均', '目标线'],
          bottom: 10
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: trendData.weeks,
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value',
          min: 60,
          max: 100,
          axisLabel: {
            formatter: '{value}分'
          }
        },
        series: [
          {
            name: '个人贡献度',
            type: 'line',
            smooth: true,
            data: trendData.personalTrend,
            itemStyle: {
              color: '#409EFF'
            },
            lineStyle: {
              width: 3
            },
            areaStyle: {
              color: 'rgba(64, 158, 255, 0.2)'
            },
            markPoint: {
              data: [
                {type: 'max', name: '最高值'},
                {type: 'min', name: '最低值'}
              ]
            }
          },
          {
            name: '班级平均',
            type: 'line',
            smooth: true,
            data: trendData.classAverage,
            itemStyle: {
              color: '#67C23A'
            },
            lineStyle: {
              width: 2,
              type: 'dashed'
            }
          },
          {
            name: '目标线',
            type: 'line',
            data: trendData.targetLine,
            itemStyle: {
              color: '#E6A23C'
            },
            lineStyle: {
              width: 2,
              type: 'dotted'
            },
            markLine: {
              data: [
                {type: 'average', name: '平均值'}
              ]
            }
          }
        ]
      }
      
        myChart.setOption(option)
        
        // 响应式处理
        window.addEventListener('resize', () => {
          if (this.charts.trend) {
            this.charts.trend.resize()
          }
        })
      })
    },
    
    renderTeamComparisonChart() {
      console.log('开始渲染团队贡献度对比图表')
      console.log('图表实例:', this.charts.teamComparison)
      console.log('团队数据:', this.teamData)
      
      if (!this.charts.teamComparison) {
        console.error('团队贡献度对比图表实例不存在')
        return
      }
      
      if (!this.teamData || !this.teamData.members || this.teamData.members.length === 0) {
        console.error('团队数据不存在或为空')
        return
      }
      
      // 从团队数据中提取成员姓名和总分
      const memberNames = this.teamData.members.map(member => member.name)
      const memberScores = this.teamData.members.map(member => parseFloat(member.totalScore))
      
      const option = {
        title: {
          text: '团队贡献度对比',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            return `${params[0].name}<br/>总分: ${params[0].value}分`
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: memberNames,
          axisLabel: {
            interval: 0,
            rotate: 0
          }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 100,
          axisLabel: {
            formatter: '{value}分'
          }
        },
        series: [{
          name: '总分',
          data: memberScores.map((score, index) => ({
            value: score,
            itemStyle: {
              color: this.getScoreColor(score)
            }
          })),
          type: 'bar',
          barWidth: '60%',
          label: {
            show: true,
            position: 'top',
            formatter: '{c}分'
          }
        }]
      }
      
      this.charts.teamComparison.setOption(option)
    },
    
    renderDimensionChart() {
      console.log('开始渲染维度分布对比图表')
      console.log('图表实例:', this.charts.dimension)
      console.log('团队数据:', this.teamData)
      
      if (!this.charts.dimension) {
        console.error('维度分布对比图表实例不存在')
        return
      }
      
      if (!this.teamData || !this.teamData.members || this.teamData.members.length === 0) {
        console.error('团队数据不存在或为空')
        return
      }
      
      // 计算各维度的平均分
      const dimensions = ['codeContribution', 'documentContribution', 'communicationScore', 'innovationScore']
      const dimensionNames = ['代码贡献', '文档贡献', '沟通协作', '创新能力']
      const dimensionColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C']
      
      const dimensionData = dimensions.map((dim, index) => {
        const total = this.teamData.members.reduce((sum, member) => {
          return sum + parseFloat(member[dim] || 0)
        }, 0)
        const average = total / this.teamData.members.length
        
        return {
          value: average.toFixed(1),
          name: dimensionNames[index],
          itemStyle: {
            color: dimensionColors[index]
          }
        }
      })
      
      const option = {
        title: {
          text: '维度分布对比',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}分 ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: dimensionNames
        },
        series: [{
          name: '维度分布',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['60%', '50%'],
          avoidLabelOverlap: false,
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
          data: dimensionData
        }]
      }
      
      this.charts.dimension.setOption(option)
    },
    
    renderCollaborationChart() {
      console.log('开始渲染团队协作指数图表')
      console.log('图表实例:', this.charts.collaboration)
      console.log('团队数据:', this.teamData)
      
      if (!this.charts.collaboration) {
        console.error('团队协作指数图表实例不存在')
        return
      }
      
      if (!this.teamData || !this.teamData.members || this.teamData.members.length === 0) {
        console.error('团队数据不存在或为空')
        return
      }
      
      // 计算团队协作指数（基于沟通协作分数的平均值）
      const collaborationScores = this.teamData.members.map(member => parseFloat(member.communicationScore || 0))
      const averageCollaboration = collaborationScores.reduce((sum, score) => sum + score, 0) / collaborationScores.length
      
      const option = {
        title: {
          text: '团队协作指数',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          formatter: '{a} <br/>{b} : {c}分'
        },
        series: [{
          name: '协作指数',
          type: 'gauge',
          min: 0,
          max: 100,
          splitNumber: 10,
          radius: '80%',
          axisLine: {
            lineStyle: {
              width: 10,
              color: [
                [0.3, '#FF6E76'],
                [0.7, '#FDDD60'],
                [1, '#58D9F9']
              ]
            }
          },
          axisLabel: {
            fontSize: 12,
            distance: -40,
            color: '#999',
            formatter: function(value) {
              if (value === 0) return '0'
              if (value === 50) return '50'
              if (value === 100) return '100'
              return ''
            }
          },
          axisTick: {
            distance: -30,
            length: 8,
            lineStyle: {
              color: '#eee',
              width: 2
            }
          },
          splitLine: {
            distance: -30,
            length: 30,
            lineStyle: {
              color: '#eee',
              width: 4
            }
          },
          pointer: {
            itemStyle: {
              color: 'auto'
            }
          },
          detail: {
            valueAnimation: true,
            formatter: '{value}分',
            color: 'auto',
            fontSize: 20,
            offsetCenter: [0, '70%']
          },
          data: [{
            value: averageCollaboration.toFixed(1),
            name: '协作指数'
          }]
        }]
      }
      
      this.charts.collaboration.setOption(option)
    },
    
    renderOverallTrendChart() {
      console.log('开始渲染贡献度趋势变化图表')
      console.log('图表实例:', this.charts.overallTrend)
      console.log('趋势数据:', this.trendData)
      
      if (!this.charts.overallTrend) {
        console.error('贡献度趋势变化图表实例不存在')
        return
      }
      
      if (!this.trendData || !this.trendData.overallTrend) {
        console.error('趋势数据不存在或为空')
        return
      }
      
      const option = {
        title: {
          text: '贡献度趋势变化',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['班级平均', '个人得分'],
          top: 30
        },
        xAxis: {
          type: 'category',
          data: this.trendData.overallTrend.dates || ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周']
        },
        yAxis: {
          type: 'value',
          name: '得分',
          min: 0,
          max: 100
        },
        series: [
          {
            name: '班级平均',
            data: this.trendData.overallTrend.classAverage || [72, 75, 78, 80, 82, 85],
            type: 'line',
            smooth: true,
            itemStyle: {
              color: '#409EFF'
            }
          },
          {
            name: '个人得分',
            data: this.trendData.overallTrend.personalScore || [70, 73, 80, 83, 85, 88],
            type: 'line',
            smooth: true,
            itemStyle: {
              color: '#67C23A'
            }
          }
        ]
      }
      
      this.charts.overallTrend.setOption(option)
    },
    
    renderDimensionTrendChart() {
      console.log('开始渲染维度趋势变化图表')
      console.log('图表实例:', this.charts.dimensionTrend)
      console.log('趋势数据:', this.trendData)
      
      if (!this.charts.dimensionTrend) {
        console.error('维度趋势变化图表实例不存在')
        return
      }
      
      if (!this.trendData || !this.trendData.dimensionTrends) {
        console.error('维度趋势数据不存在或为空')
        return
      }
      
      const dimensionColors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C']
      const dimensionNames = Object.keys(this.trendData.dimensionTrends)
      
      const option = {
        title: {
          text: '维度趋势变化',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: dimensionNames,
          top: 30
        },
        xAxis: {
          type: 'category',
          data: this.trendData.overallTrend?.dates || ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周']
        },
        yAxis: {
          type: 'value',
          name: '得分',
          min: 0,
          max: 100
        },
        series: dimensionNames.map((name, index) => ({
          name: name,
          data: this.trendData.dimensionTrends[name] || [],
          type: 'line',
          smooth: true,
          itemStyle: {
            color: dimensionColors[index % dimensionColors.length]
          }
        }))
      }
      
      this.charts.dimensionTrend.setOption(option)
    },
    
    renderActivityChart() {
      console.log('开始渲染活跃度分析图表')
      console.log('图表实例:', this.charts.activity)
      console.log('趋势数据:', this.trendData)
      
      if (!this.charts.activity) {
        console.error('活跃度分析图表实例不存在')
        return
      }
      
      if (!this.trendData || !this.trendData.activityData) {
        console.error('活跃度数据不存在或为空')
        return
      }
      
      const option = {
        title: {
          text: '活跃度分析',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: '{b}: {c}次活动'
        },
        xAxis: {
          type: 'category',
          data: this.trendData.activityData.labels || ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        },
        yAxis: {
          type: 'value',
          name: '活动次数'
        },
        series: [{
          name: '活跃度',
          data: this.trendData.activityData.weekly || [12, 15, 18, 22, 25, 8, 5],
          type: 'bar',
          barWidth: '60%',
          itemStyle: {
            color: '#409EFF'
          },
          label: {
            show: true,
            position: 'top',
            formatter: '{c}'
          }
        }]
      }
      
      this.charts.activity.setOption(option)
    },
    
    async loadPersonalData() {
      if (!this.selectedCourse || !this.selectedStudent) {
        console.warn('未选择课程或学生，跳过加载个人数据')
        return
      }
      
      this.loading.personal = true
      
      // 注释掉接口调用，使用模拟数据
      /*
      try {
        // 获取个人评分详情
        const scoreResponse = await this.$http.get(`/scores/course/${this.selectedCourse}/member/${this.selectedStudent}`)
        if (scoreResponse.data.code === '200') {
          const scoreData = scoreResponse.data.data
          
          // 获取维度得分详情
          const dimensionResponse = await this.$http.get(`/scores/course/${this.selectedCourse}/member/${this.selectedStudent}/dimensions`)
          
          // 获取课程排行榜来计算排名
          const leaderboardResponse = await this.$http.get(`/scores/course/${this.selectedCourse}/leaderboard`)
          
          let rank = 1
          if (leaderboardResponse.data.code === '200') {
            const leaderboard = leaderboardResponse.data.data
            const memberIndex = leaderboard.findIndex(item => item.memberId === this.selectedStudent)
            rank = memberIndex >= 0 ? memberIndex + 1 : 1
          }
          
          // 构建个人数据
          this.personalData = {
            totalScore: scoreData.totalScore || 0,
            rank: rank,
            improvement: 0, // 暂时设为0，需要历史数据计算
            details: this.buildDimensionDetails(scoreData.dimensionScores)
          }
          
          this.renderRadarChart()
          this.renderTrendChart()
        }
      } catch (error) {
        console.error('加载个人数据失败:', error)
        this.$message.error('加载个人数据失败')
      } finally {
        this.loading.personal = false
      }
      */
      
      // 使用模拟数据
      try {
        // 模拟异步加载延迟
        await new Promise(resolve => setTimeout(resolve, 500))
        
        // 根据选择的学生生成不同的模拟数据
        const studentScores = {
          1: { total: 88.5, rank: 2, improvement: 5.2, dimensions: { '代码贡献': 90, '文档贡献': 85, '沟通协作': 92, '创新能力': 87 } },
          2: { total: 92.3, rank: 1, improvement: 8.1, dimensions: { '代码贡献': 95, '文档贡献': 88, '沟通协作': 94, '创新能力': 92 } },
          3: { total: 76.8, rank: 4, improvement: -2.3, dimensions: { '代码贡献': 78, '文档贡献': 82, '沟通协作': 75, '创新能力': 72 } },
          4: { total: 84.2, rank: 3, improvement: 3.7, dimensions: { '代码贡献': 86, '文档贡献': 80, '沟通协作': 88, '创新能力': 83 } },
          5: { total: 71.5, rank: 5, improvement: 1.2, dimensions: { '代码贡献': 70, '文档贡献': 75, '沟通协作': 72, '创新能力': 69 } }
        }
        
        const selectedScore = studentScores[this.selectedStudent] || studentScores[1]
        
        this.personalData = {
          totalScore: selectedScore.total,
          rank: selectedScore.rank,
          improvement: selectedScore.improvement,
          details: this.buildDimensionDetails(selectedScore.dimensions)
        }
        
        // 确保DOM更新后再渲染图表
        this.$nextTick(() => {
          this.renderRadarChart()
          this.renderTrendChart()
        })
      } catch (error) {
        console.error('加载个人数据失败:', error)
        this.$message.error('加载个人数据失败')
      } finally {
        this.loading.personal = false
      }
    },
    
    buildDimensionDetails(dimensionScores) {
      if (!dimensionScores) return []
      
      let scores = dimensionScores
      if (typeof dimensionScores === 'string') {
        try {
          scores = JSON.parse(dimensionScores)
        } catch {
          return []
        }
      }
      
      return Object.entries(scores).map(([dimension, score]) => ({
        dimension,
        score: Number(score).toFixed(1),
        weight: 0.25, // 默认权重，实际应从配置获取
        weightedScore: (Number(score) * 0.25).toFixed(1),
        description: this.getDimensionDescription(dimension, score)
      }))
    },
    
    getDimensionDescription(dimension, score) {
      const scoreNum = Number(score)
      if (scoreNum >= 90) return `${dimension}表现优秀`
      if (scoreNum >= 80) return `${dimension}表现良好`
      if (scoreNum >= 70) return `${dimension}表现一般`
      return `${dimension}需要改进`
    },
    
    async loadTeamData() {
      if (!this.selectedTeamCourse) {
        console.warn('未选择团队课程，跳过加载团队数据')
        return
      }
      
      this.loading.team = true
      
      // 注释掉接口调用，使用模拟数据
      /*
      try {
        // 获取课程所有成员评分
        const response = await this.$http.get(`/scores/course/${this.selectedTeamCourse}/all`)
        if (response.data.code === '200') {
          const allScores = response.data.data
          
          // 构建团队数据
          this.teamData = {
            members: allScores.map(score => ({
              name: score.memberName || `成员${score.memberId}`,
              memberId: score.memberId,
              totalScore: Number(score.totalScore || 0).toFixed(1),
              dimensionScores: score.dimensionScores,
              rank: score.ranking || 0
            }))
          }
          
          this.renderTeamComparisonChart()
          this.renderDimensionChart()
          this.renderCollaborationChart()
        }
      } catch (error) {
        console.error('加载团队数据失败:', error)
        this.$message.error('加载团队数据失败')
      } finally {
        this.loading.team = false
      }
      */
      
      // 使用模拟数据
      try {
        // 模拟异步加载延迟
        await new Promise(resolve => setTimeout(resolve, 300))
        
        // 构建团队数据
        this.teamData = {
          members: [
            {
              name: '张三',
              memberId: 1,
              totalScore: '88.5',
              codeContribution: '90.2',
              documentContribution: '85.3',
              communicationScore: '92.1',
              innovationScore: '87.4',
              rank: 2
            },
            {
              name: '李四',
              memberId: 2,
              totalScore: '92.3',
              codeContribution: '95.1',
              documentContribution: '88.7',
              communicationScore: '94.2',
              innovationScore: '91.8',
              rank: 1
            },
            {
              name: '王五',
              memberId: 3,
              totalScore: '76.8',
              codeContribution: '78.3',
              documentContribution: '82.1',
              communicationScore: '75.2',
              innovationScore: '71.9',
              rank: 4
            },
            {
              name: '赵六',
              memberId: 4,
              totalScore: '84.2',
              codeContribution: '86.5',
              documentContribution: '79.8',
              communicationScore: '87.9',
              innovationScore: '82.7',
              rank: 3
            }
          ]
        }
        
        // 确保在团队对比标签页时才初始化和渲染图表
        if (this.activeTab === 'team') {
          console.log('当前在团队对比标签页，开始初始化图表')
          this.$nextTick(() => {
            // 先初始化图表实例
            this.initializeTeamCharts()
            
            // 使用多层$nextTick确保DOM完全更新
            this.$nextTick(() => {
              setTimeout(() => {
                console.log('开始渲染团队图表，数据:', this.teamData)
                // 检查图表实例是否存在
                if (this.charts.teamComparison && this.charts.dimension && this.charts.collaboration) {
                  console.log('所有图表实例已就绪，开始渲染')
                  this.renderTeamComparisonChart()
                  this.renderDimensionChart()
                  this.renderCollaborationChart()
                } else {
                  console.warn('图表实例未完全初始化，延迟重试')
                  setTimeout(() => {
                    if (this.charts.teamComparison && this.charts.dimension && this.charts.collaboration) {
                      this.renderTeamComparisonChart()
                      this.renderDimensionChart()
                      this.renderCollaborationChart()
                    }
                  }, 500)
                }
              }, 1000) // 增加延迟时间确保初始化完成
            })
          })
        } else {
          console.log('当前不在团队对比标签页，跳过图表渲染')
        }
      } catch (error) {
        console.error('加载团队数据失败:', error)
        this.$message.error('加载团队数据失败')
      } finally {
        this.loading.team = false
      }
    },
    
    async loadTrendData() {
      if (!this.selectedTrendCourse) {
        console.warn('未选择趋势课程，跳过加载趋势数据')
        return
      }
      
      this.loading.trend = true
      
      // 注释掉接口调用，使用模拟数据
      /*
      try {
        // 获取课程历史评分数据（模拟趋势数据）
        const response = await this.$http.get(`/scores/course/${this.selectedTrendCourse}/all`)
        if (response.data.code === '200') {
          const currentScores = response.data.data
          
          // 构建趋势数据（由于缺少历史数据，这里模拟趋势）
          this.trendData = {
            overallTrend: this.buildOverallTrend(currentScores),
            dimensionTrends: this.buildDimensionTrends(currentScores),
            activityData: this.buildActivityData(currentScores),
            summary: this.buildTrendSummary(currentScores)
          }
          
          this.renderOverallTrendChart()
          this.renderDimensionTrendChart()
          this.renderActivityChart()
        }
      } catch (error) {
        console.error('加载趋势数据失败:', error)
        this.$message.error('加载趋势数据失败')
      } finally {
        this.loading.trend = false
      }
      */
      
      // 使用模拟数据
      try {
        // 模拟异步加载延迟
        await new Promise(resolve => setTimeout(resolve, 400))
        
        // 构建趋势数据
        this.trendData = {
          avgGrowthRate: 12.5,
          bestDimension: '代码贡献',
          improvementNeeded: '沟通协作',
          overallTrend: {
            dates: ['第1周', '第2周', '第3周', '第4周', '第5周', '第6周'],
            classAverage: [72, 75, 78, 80, 82, 85],
            personalScore: [70, 73, 80, 83, 85, 88]
          },
          dimensionTrends: {
            '代码贡献': [75, 78, 82, 85, 87, 88],
            '文档贡献': [70, 72, 75, 78, 80, 82],
            '沟通协作': [80, 82, 85, 87, 89, 90],
            '创新能力': [65, 68, 70, 72, 74, 75]
          },
          activityData: {
            weekly: [12, 15, 18, 22, 25, 8, 5],
            labels: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
          }
        }
        
        this.renderOverallTrendChart()
        this.renderDimensionTrendChart()
        this.renderActivityChart()
      } catch (error) {
        console.error('加载趋势数据失败:', error)
        this.$message.error('加载趋势数据失败')
      } finally {
        this.loading.trend = false
      }
    },
    
    buildOverallTrend(scores) {
      // 模拟构建总体趋势数据
      const avgScore = scores.reduce((sum, score) => sum + Number(score.totalScore || 0), 0) / scores.length
      return {
        dates: ['第1周', '第2周', '第3周', '第4周'],
        values: [avgScore * 0.7, avgScore * 0.8, avgScore * 0.9, avgScore]
      }
    },
    
    buildDimensionTrends(scores) {
      // 模拟构建维度趋势数据
      const dimensions = ['代码贡献', '协作能力', '代码质量', '活跃度']
      return dimensions.map(dim => ({
        name: dim,
        data: [60, 70, 80, 85] // 模拟数据
      }))
    },
    
    buildActivityData(scores) {
      // 模拟构建活跃度数据
      return scores.map(score => ({
        name: score.memberName || `成员${score.memberId}`,
        value: Math.floor(Math.random() * 100) + 50
      }))
    },
    
    buildTrendSummary(scores) {
      const avgScore = scores.reduce((sum, score) => sum + Number(score.totalScore || 0), 0) / scores.length
      return {
        avgGrowthRate: '12.5%',
        bestDimension: '代码质量',
        improvementArea: '协作能力'
      }
    },
    
    getScoreType(score) {
      if (score >= 90) return 'success'
      if (score >= 80) return 'warning'
      if (score >= 70) return 'info'
      return 'danger'
    },
    
    getScoreColor(score) {
      if (score >= 90) return '#67C23A' // 绿色 - 优秀
      if (score >= 80) return '#E6A23C' // 橙色 - 良好
      if (score >= 70) return '#409EFF' // 蓝色 - 一般
      return '#F56C6C' // 红色 - 需要改进
    },
    
    getRankType(rank) {
      if (rank === 1) return 'success'
      if (rank <= 3) return 'warning'
      if (rank <= 5) return 'info'
      return 'danger'
    },
    
    async exportPersonalReport() {
      if (!this.selectedCourse || !this.selectedStudent) {
        console.warn('未选择课程或学生，跳过导出个人报告')
        this.$message.warning('请先选择课程和学生')
        return
      }
      
      // 注释掉接口调用，使用模拟导出
      /*
      try {
        const response = await this.$http.get(`/scores/course/${this.selectedCourse}/member/${this.selectedStudent}/export`, {
          responseType: 'blob'
        })
        
        // 创建下载链接
        const blob = new Blob([response.data], { type: 'text/csv' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `个人报告_${this.selectedStudent}_${new Date().toISOString().split('T')[0]}.csv`
        link.click()
        window.URL.revokeObjectURL(url)
        
        this.$message.success('个人报告导出成功')
      } catch (error) {
        console.error('导出个人报告失败:', error)
        this.$message.error('导出个人报告失败')
      }
      */
      
      // 模拟导出功能
      try {
        const studentName = this.students.find(s => (s.memberId || s.id) === this.selectedStudent)?.memberName || this.students.find(s => (s.memberId || s.id) === this.selectedStudent)?.name || this.selectedStudent
        
        // 模拟导出延迟
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // 创建模拟的CSV内容
        const csvContent = `个人报告,${studentName}\n总分,${this.personalData?.totalScore || 0}\n排名,${this.personalData?.rank || 0}\n提升度,${this.personalData?.improvement || 0}%\n\n维度,得分,权重,加权得分,说明\n${this.personalData?.details?.map(d => `${d.dimension},${d.score},${d.weight},${d.weightedScore},${d.description}`).join('\n') || ''}`
        
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `个人报告_${studentName}_${new Date().toISOString().split('T')[0]}.csv`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        this.$message.success('个人报告导出成功（模拟数据）')
      } catch (error) {
        console.error('导出个人报告失败:', error)
        this.$message.error('导出个人报告失败')
      }
    },
    
    async exportTeamReport() {
      if (!this.selectedTeamCourse) {
        console.warn('未选择团队课程，跳过导出团队报告')
        this.$message.warning('请先选择课程')
        return
      }
      
      // 注释掉接口调用，使用模拟导出
      /*
      try {
        const response = await this.$http.get(`/scores/course/${this.selectedTeamCourse}/export`, {
          responseType: 'blob'
        })
        
        // 创建下载链接
        const blob = new Blob([response.data], { type: 'text/csv' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `团队报告_${this.selectedTeamCourse}_${new Date().toISOString().split('T')[0]}.csv`
        link.click()
        window.URL.revokeObjectURL(url)
        
        this.$message.success('团队报告导出成功')
      } catch (error) {
        console.error('导出团队报告失败:', error)
        this.$message.error('导出团队报告失败')
      }
      */
      
      // 模拟导出功能
      try {
        const courseName = this.courses.find(c => (c.courseId || c.id) === this.selectedTeamCourse)?.courseName || this.courses.find(c => (c.courseId || c.id) === this.selectedTeamCourse)?.name || this.selectedTeamCourse
        
        // 模拟导出延迟
        await new Promise(resolve => setTimeout(resolve, 1200))
        
        // 创建模拟的团队报告CSV内容
        let csvContent = `团队报告,${courseName}\n\n成员,总分,排名,代码贡献,文档贡献,沟通协作,创新能力\n`
        
        if (this.teamData && this.teamData.members) {
          this.teamData.members.forEach(member => {
            csvContent += `${member.name},${member.totalScore},${member.rank},${member.codeContribution || 0},${member.documentContribution || 0},${member.communicationScore || 0},${member.innovationScore || 0}\n`
          })
        }
        
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `团队报告_${courseName}_${new Date().toISOString().split('T')[0]}.csv`
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        this.$message.success('团队报告导出成功（模拟数据）')
      } catch (error) {
        console.error('导出团队报告失败:', error)
        this.$message.error('导出团队报告失败')
      }
    },
    
    async exportTrendReport() {
      if (!this.selectedTrendCourse) {
        console.warn('未选择趋势课程，跳过导出趋势报告')
        this.$message.warning('请先选择课程')
        return
      }
      
      try {
        // 由于后端可能没有专门的趋势报告导出接口，使用课程导出接口
        const response = await this.$http.get(`/scores/course/${this.selectedTrendCourse}/export`, {
          responseType: 'blob'
        })
        
        // 创建下载链接
        const blob = new Blob([response.data], { type: 'text/csv' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `趋势报告_${this.selectedTrendCourse}_${new Date().toISOString().split('T')[0]}.csv`
        link.click()
        window.URL.revokeObjectURL(url)
        
        this.$message.success('趋势报告导出成功')
      } catch (error) {
        console.error('导出趋势报告失败:', error)
        this.$message.error('导出趋势报告失败')
      }
    }
  }
}
</script>

<style scoped>
.result-display {
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

.result-tabs {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.report-filters,
.comparison-filters,
.trend-filters {
  margin-bottom: 20px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 6px;
}

.score-card,
.rank-card,
.improvement-card {
  text-align: center;
  margin-bottom: 20px;
}

.score-display,
.rank-display,
.improvement-display {
  padding: 20px;
}

.score-value,
.rank-value,
.improvement-value {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
}

.improvement-value.positive {
  color: #67c23a;
}

.improvement-value.negative {
  color: #f56c6c;
}

.score-label,
.rank-label,
.improvement-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.chart-row {
  margin-top: 20px;
}

.detail-table,
.team-table {
  margin-top: 20px;
}

.detail-table .el-table {
  width: 100%;
}

.detail-table .el-table th,
.detail-table .el-table td {
  text-align: center;
  padding: 12px 8px;
}

.detail-table .el-table th:first-child,
.detail-table .el-table td:first-child,
.detail-table .el-table th:last-child,
.detail-table .el-table td:last-child {
  text-align: left;
}

.detail-table .el-table .el-tag {
  font-weight: bold;
}

.trend-summary {
  margin-top: 20px;
}

.summary-item {
  text-align: center;
  padding: 20px;
}

.summary-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.summary-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.summary-value.positive {
  color: #67c23a;
}

.summary-value.warning {
  color: #e6a23c;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    padding: 10px;
  }
  
  .page-header h1 {
    font-size: 24px;
  }
  
  .score-value,
  .rank-value,
  .improvement-value {
    font-size: 28px;
  }
  
  .summary-value {
    font-size: 20px;
  }
}
</style>