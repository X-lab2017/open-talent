<template>
  <div class="member-git-profile">
    <div class="page-header">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item :to="{ path: '/git-activity' }">Git活动监控</el-breadcrumb-item>
        <el-breadcrumb-item>成员详情</el-breadcrumb-item>
      </el-breadcrumb>
      
      <!-- 实时数据控制 -->
      <div class="header-actions">
        <el-button-group>
          <el-button 
            :type="autoRefresh ? 'success' : 'default'" 
            :icon="autoRefresh ? 'el-icon-video-pause' : 'el-icon-video-play'"
            @click="toggleAutoRefresh"
            size="small">
            {{ autoRefresh ? '停止刷新' : '自动刷新' }}
          </el-button>
          <el-button icon="el-icon-refresh" @click="refreshAllData" size="small">手动刷新</el-button>
          <el-button icon="el-icon-download" @click="exportProfile" size="small">导出档案</el-button>
        </el-button-group>
        
        <!-- 新增高级功能控制 -->
        <el-button-group style="margin-left: 12px;">
          <el-button 
            :type="profileEnhancement.showAdvancedMetrics ? 'primary' : 'default'"
            icon="el-icon-data-analysis"
            @click="toggleAdvancedMetrics"
            size="small">
            高级指标
          </el-button>
          <el-dropdown @command="handleVisualizationMode" size="small">
            <el-button size="small">
              {{ getVisualizationModeText() }}<i class="el-icon-arrow-down el-icon--right"></i>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="standard">标准模式</el-dropdown-item>
              <el-dropdown-item command="advanced">高级模式</el-dropdown-item>
              <el-dropdown-item command="minimal">简洁模式</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <el-button 
            :type="realTimeStatus.autoSyncEnabled ? 'success' : 'default'"
            icon="el-icon-connection"
            @click="toggleRealTimeSync"
            size="small">
            实时同步
          </el-button>
        </el-button-group>
        
        <!-- 实时状态显示 -->
        <div class="realtime-status" v-if="realTimeStatus.autoSyncEnabled">
          <el-tag 
            :type="realTimeStatus.connected ? 'success' : 'danger'" 
            size="mini"
            :class="{ 'connected-pulse': realTimeStatus.connected }">
            {{ realTimeStatus.connected ? '已连接' : '连接中' }}
          </el-tag>
          <span v-if="realTimeStatus.lastSync" class="last-sync">
            上次同步: {{ formatTime(realTimeStatus.lastSync) }}
          </span>
        </div>
        
        <span v-if="lastUpdateTime" class="last-update-time">
          最后更新: {{ formatTime(lastUpdateTime) }}
        </span>
      </div>
    </div>

    <!-- 成员基本信息 -->
    <div class="member-info" v-loading="loading.profile">
      <el-card>
        <div class="profile-header">
          <div class="avatar-section">
            <el-avatar :size="80" :src="memberInfo.avatar" icon="el-icon-user-solid"></el-avatar>
          </div>
          <div class="info-section">
            <h2>{{ memberInfo.name || '未知成员' }}</h2>
            <p class="member-id">ID: {{ memberInfo.id }}</p>
            <p class="github-info" v-if="memberInfo.githubUsername">
              <i class="el-icon-link"></i>
              <el-link :href="`https://github.com/${memberInfo.githubUsername}`" target="_blank">
                @{{ memberInfo.githubUsername }}
              </el-link>
            </p>
            <div class="member-stats">
              <div class="stat-item">
                <span class="stat-value">{{ profileStats.totalCommits }}</span>
                <span class="stat-label">总提交数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ profileStats.totalRepositories }}</span>
                <span class="stat-label">参与仓库</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ profileStats.totalLines }}</span>
                <span class="stat-label">代码行数</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ profileStats.activeDays }}</span>
                <span class="stat-label">活跃天数</span>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 贡献度概览 -->
    <div class="contribution-overview">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <div slot="header">
              <span>贡献度趋势</span>
              <el-select v-model="trendPeriod" size="mini" style="float: right; margin-top: -5px;" @change="loadContributionTrend">
                <el-option label="最近7天" value="7d"></el-option>
                <el-option label="最近30天" value="30d"></el-option>
                <el-option label="最近90天" value="90d"></el-option>
              </el-select>
            </div>
            <div id="contributionTrendChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div slot="header">
              <span>语言分布</span>
            </div>
            <div id="languageDistributionChart" style="height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 贡献热力图 -->
      <el-row style="margin-top: 20px;">
        <el-col :span="24">
          <el-card>
            <div slot="header">
              <span>年度贡献热力图</span>
              <el-button-group style="float: right;">
                <el-button size="mini" @click="switchToHeatmap">热力图</el-button>
                <el-button size="mini" @click="switchToTrend">趋势图</el-button>
              </el-button-group>
            </div>
            <div id="contributionCalendar" style="height: 200px;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 代码质量指标 -->
    <div class="code-quality-metrics">
      <el-card>
        <div slot="header">
          <span>代码质量指标</span>
          <el-button size="mini" icon="el-icon-refresh" @click="loadCodeMetrics" style="float: right; margin-top: -5px;">刷新</el-button>
        </div>
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="metric-item">
              <div class="metric-value">{{ codeMetrics.complexity }}%</div>
              <div class="metric-label">代码复杂度</div>
              <el-progress :percentage="codeMetrics.complexity" :color="getMetricColor(codeMetrics.complexity)"></el-progress>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-item">
              <div class="metric-value">{{ codeMetrics.coverage }}%</div>
              <div class="metric-label">测试覆盖率</div>
              <el-progress :percentage="codeMetrics.coverage" :color="getMetricColor(codeMetrics.coverage)"></el-progress>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-item">
              <div class="metric-value">{{ codeMetrics.maintainability }}%</div>
              <div class="metric-label">可维护性</div>
              <el-progress :percentage="codeMetrics.maintainability" :color="getMetricColor(codeMetrics.maintainability)"></el-progress>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="metric-item">
              <div class="metric-value">{{ codeMetrics.duplication }}%</div>
              <div class="metric-label">代码重复率</div>
              <el-progress :percentage="100 - codeMetrics.duplication" :color="getMetricColor(100 - codeMetrics.duplication)"></el-progress>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
    
    <!-- 成长轨迹与技能分析 -->
    <div class="growth-analysis">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card>
            <div slot="header">
              <span>成长轨迹</span>
              <el-radio-group v-model="growthViewType" size="mini" style="float: right; margin-top: -5px;" @change="renderGrowthChart">
                <el-radio-button label="score">综合得分</el-radio-button>
                <el-radio-button label="activity">活跃度</el-radio-button>
                <el-radio-button label="quality">代码质量</el-radio-button>
              </el-radio-group>
            </div>
            <div id="growthTrajectoryChart" style="height: 350px;"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <div slot="header">
              <span>技能雷达图</span>
              <el-button size="mini" style="float: right; margin-top: -5px;" @click="refreshSkillRadar">
                <i class="el-icon-refresh"></i>
              </el-button>
            </div>
            <div id="skillRadarChart" style="height: 350px;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 协作统计 -->
    <div class="collaboration-stats">
      <el-card>
        <div slot="header">
          <span>协作统计</span>
          <el-button size="mini" icon="el-icon-refresh" @click="loadCollaborationData" style="float: right; margin-top: -5px;">刷新</el-button>
        </div>
        <el-row :gutter="20">
          <el-col :span="8">
            <div class="collab-item">
              <div class="collab-icon">
                <i class="el-icon-s-promotion" style="color: #409EFF;"></i>
              </div>
              <div class="collab-content">
                <div class="collab-value">{{ collaborationData.pullRequests.length }}</div>
                <div class="collab-label">Pull Requests</div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="collab-item">
              <div class="collab-icon">
                <i class="el-icon-view" style="color: #67C23A;"></i>
              </div>
              <div class="collab-content">
                <div class="collab-value">{{ collaborationData.codeReviews.length }}</div>
                <div class="collab-label">Code Reviews</div>
              </div>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="collab-item">
              <div class="collab-icon">
                <i class="el-icon-warning-outline" style="color: #E6A23C;"></i>
              </div>
              <div class="collab-content">
                <div class="collab-value">{{ collaborationData.issues.length }}</div>
                <div class="collab-label">Issues</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>
    
    <!-- 能力评估与对比 -->
    <div class="capability-assessment">
      <el-card>
        <div slot="header">
          <span>能力评估</span>
          <el-select v-model="comparisonTarget" size="mini" style="float: right; margin-top: -5px; width: 150px;" @change="loadCapabilityComparison">
            <el-option label="团队平均" value="team"></el-option>
            <el-option label="课程平均" value="course"></el-option>
            <el-option label="历史最佳" value="best"></el-option>
          </el-select>
        </div>
        <div class="capability-grid">
          <div class="capability-item" v-for="capability in capabilities" :key="capability.name">
            <div class="capability-header">
              <span class="capability-name">{{ capability.name }}</span>
              <span class="capability-score" :class="getScoreClass(capability.score)">{{ capability.score }}</span>
            </div>
            <div class="capability-progress">
              <el-progress 
                :percentage="capability.score" 
                :color="getProgressColor(capability.score)"
                :show-text="false"
              ></el-progress>
            </div>
            <div class="capability-comparison">
              <span class="comparison-label">vs {{ getComparisonLabel() }}:</span>
              <span class="comparison-value" :class="getComparisonClass(capability.comparison)">
                {{ capability.comparison > 0 ? '+' : '' }}{{ capability.comparison.toFixed(1) }}
              </span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 性能分析 -->
    <div class="performance-analysis">
      <el-card>
        <div slot="header">
          <span>性能分析</span>
          <el-button size="mini" icon="el-icon-refresh" @click="loadPerformanceMetrics" style="float: right; margin-top: -5px;">刷新</el-button>
        </div>
        <div id="performanceChart" style="height: 300px;"></div>
      </el-card>
    </div>
    
    <!-- 高级指标分析 -->
    <div class="advanced-metrics" v-if="profileEnhancement.showAdvancedMetrics">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card>
            <div slot="header">
              <span>代码复杂度分析</span>
              <el-button size="mini" icon="el-icon-refresh" @click="loadAdvancedMetrics" style="float: right; margin-top: -5px;">刷新</el-button>
            </div>
            <div v-loading="advancedAnalytics.loading">
              <div class="metric-summary">
                <div class="metric-item">
                  <span class="metric-label">平均复杂度</span>
                  <span class="metric-value">{{ advancedAnalytics.metrics.codeComplexity?.average || 0 }}</span>
                </div>
                <div class="complexity-distribution">
                  <div class="distribution-item">
                    <span class="distribution-label">低复杂度</span>
                    <el-progress :percentage="advancedAnalytics.metrics.codeComplexity?.distribution?.low || 0" color="#67C23A" :show-text="false"></el-progress>
                    <span class="distribution-value">{{ advancedAnalytics.metrics.codeComplexity?.distribution?.low || 0 }}%</span>
                  </div>
                  <div class="distribution-item">
                    <span class="distribution-label">中复杂度</span>
                    <el-progress :percentage="advancedAnalytics.metrics.codeComplexity?.distribution?.medium || 0" color="#E6A23C" :show-text="false"></el-progress>
                    <span class="distribution-value">{{ advancedAnalytics.metrics.codeComplexity?.distribution?.medium || 0 }}%</span>
                  </div>
                  <div class="distribution-item">
                    <span class="distribution-label">高复杂度</span>
                    <el-progress :percentage="advancedAnalytics.metrics.codeComplexity?.distribution?.high || 0" color="#F56C6C" :show-text="false"></el-progress>
                    <span class="distribution-value">{{ advancedAnalytics.metrics.codeComplexity?.distribution?.high || 0 }}%</span>
                  </div>
                </div>
              </div>
              <div id="complexityChart" style="height: 200px; margin-top: 20px;"></div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <div slot="header">
              <span>测试覆盖率</span>
            </div>
            <div v-loading="advancedAnalytics.loading">
              <div class="coverage-summary">
                <div class="coverage-overall">
                  <div class="coverage-circle">
                    <el-progress type="circle" :percentage="advancedAnalytics.metrics.testCoverage?.overall || 0" :width="120"></el-progress>
                  </div>
                  <div class="coverage-label">整体覆盖率</div>
                </div>
                <div class="coverage-by-language">
                  <div class="language-coverage" v-for="(coverage, language) in advancedAnalytics.metrics.testCoverage?.byLanguage" :key="language">
                    <span class="language-name">{{ language }}</span>
                    <el-progress :percentage="coverage" :show-text="false" :stroke-width="8"></el-progress>
                    <span class="coverage-percent">{{ coverage }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <div slot="header">
              <span>性能指标</span>
            </div>
            <div v-loading="advancedAnalytics.loading">
              <div class="performance-metrics">
                <div class="metric-group">
                  <div class="metric-title">构建性能</div>
                  <div class="metric-chart">
                    <div id="buildTimeChart" style="height: 100px;"></div>
                  </div>
                </div>
                <div class="metric-group">
                  <div class="metric-title">资源使用</div>
                  <div class="resource-metrics">
                    <div class="resource-item">
                      <span class="resource-label">内存使用</span>
                      <el-progress :percentage="45" color="#409EFF" :show-text="false"></el-progress>
                      <span class="resource-value">45%</span>
                    </div>
                    <div class="resource-item">
                      <span class="resource-label">CPU使用</span>
                      <el-progress :percentage="25" color="#67C23A" :show-text="false"></el-progress>
                      <span class="resource-value">25%</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 仓库贡献详情 -->
    <div class="repository-contributions">
      <el-card>
        <div slot="header">
          <span>仓库贡献详情</span>
          <el-input
            v-model="repositorySearch"
            placeholder="搜索仓库"
            size="mini"
            style="width: 200px; float: right; margin-top: -5px;"
            @input="filterRepositories">
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
        </div>
        <el-table :data="filteredRepositories" v-loading="loading.repositories">
          <el-table-column prop="name" label="仓库名称" min-width="200">
            <template slot-scope="scope">
              <el-link :href="scope.row.url" target="_blank">
                <i class="el-icon-link"></i>
                {{ scope.row.name }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="commits" label="提交数" width="100" align="center"></el-table-column>
          <el-table-column prop="linesAdded" label="新增行" width="100" align="center">
            <template slot-scope="scope">
              <span class="lines-added">+{{ scope.row.linesAdded }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="linesDeleted" label="删除行" width="100" align="center">
            <template slot-scope="scope">
              <span class="lines-deleted">-{{ scope.row.linesDeleted }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="lastCommitDate" label="最后提交" width="150">
            <template slot-scope="scope">
              {{ formatDate(scope.row.lastCommitDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="primaryLanguage" label="主要语言" width="120">
            <template slot-scope="scope">
              <el-tag size="mini" :style="{ backgroundColor: getLanguageColor(scope.row.primaryLanguage) }">
                {{ scope.row.primaryLanguage }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center">
            <template slot-scope="scope">
              <el-button size="mini" @click="viewRepositoryDetail(scope.row)">详情</el-button>
              <el-button size="mini" type="primary" @click="viewCommitHistory(scope.row)">提交历史</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <!-- 最近活动 -->
    <div class="recent-activities">
      <el-card>
        <div slot="header">
          <span>最近活动</span>
          <el-button size="mini" style="float: right; margin-top: -5px;" @click="loadRecentActivities">
            <i class="el-icon-refresh"></i>
            刷新
          </el-button>
        </div>
        <el-timeline>
          <el-timeline-item
            v-for="activity in recentActivities"
            :key="activity.id"
            :timestamp="formatDate(activity.timestamp)"
            :type="getActivityTimelineType(activity.activityType)">
            <div class="activity-content">
              <div class="activity-header">
                <span class="activity-type">{{ getActivityTypeText(activity.activityType) }}</span>
                <span class="repository-name">{{ activity.repositoryName }}</span>
              </div>
              <div class="activity-message">{{ activity.message }}</div>
              <div class="activity-stats" v-if="activity.linesAdded || activity.linesDeleted">
                <span class="lines-added">+{{ activity.linesAdded || 0 }}</span>
                <span class="lines-deleted">-{{ activity.linesDeleted || 0 }}</span>
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
        <div class="load-more" v-if="hasMoreActivities">
          <el-button @click="loadMoreActivities" :loading="loading.activities">加载更多</el-button>
        </div>
      </el-card>
    </div>

    <!-- 仓库详情对话框 -->
    <el-dialog
      title="仓库详情"
      :visible.sync="showRepositoryDialog"
      width="800px">
      <div v-if="selectedRepository">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="仓库名称">{{ selectedRepository.name }}</el-descriptions-item>
          <el-descriptions-item label="URL">
            <el-link :href="selectedRepository.url" target="_blank">{{ selectedRepository.url }}</el-link>
          </el-descriptions-item>
          <el-descriptions-item label="主要语言">{{ selectedRepository.primaryLanguage }}</el-descriptions-item>
          <el-descriptions-item label="提交数">{{ selectedRepository.commits }}</el-descriptions-item>
          <el-descriptions-item label="新增行数">{{ selectedRepository.linesAdded }}</el-descriptions-item>
          <el-descriptions-item label="删除行数">{{ selectedRepository.linesDeleted }}</el-descriptions-item>
          <el-descriptions-item label="最后提交">{{ formatDate(selectedRepository.lastCommitDate) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(selectedRepository.createdAt) }}</el-descriptions-item>
        </el-descriptions>
        
        <div style="margin-top: 20px;">
          <h4>语言统计</h4>
          <div id="repoLanguageChart" style="height: 200px;"></div>
        </div>
      </div>
    </el-dialog>

    <!-- 提交历史对话框 -->
    <el-dialog
      title="提交历史"
      :visible.sync="showCommitHistoryDialog"
      width="1000px">
      <el-table :data="commitHistory" v-loading="loading.commits" max-height="400">
        <el-table-column prop="hash" label="提交哈希" width="120">
          <template slot-scope="scope">
            <el-link :href="getCommitUrl(scope.row)" target="_blank">
              {{ scope.row.hash.substring(0, 8) }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="提交信息" min-width="300" show-overflow-tooltip></el-table-column>
        <el-table-column prop="linesAdded" label="新增" width="80" align="center">
          <template slot-scope="scope">
            <span class="lines-added">+{{ scope.row.linesAdded }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="linesDeleted" label="删除" width="80" align="center">
          <template slot-scope="scope">
            <span class="lines-deleted">-{{ scope.row.linesDeleted }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="timestamp" label="提交时间" width="150">
          <template slot-scope="scope">
            {{ formatDate(scope.row.timestamp) }}
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  name: 'MemberGitProfile',
  data() {
    return {
      memberId: null,
      loading: {
        profile: false,
        repositories: false,
        activities: false,
        commits: false
      },
      memberInfo: {},
      profileStats: {
        totalCommits: 0,
        totalRepositories: 0,
        totalLines: 0,
        activeDays: 0
      },
      trendPeriod: '30d',
      repositorySearch: '',
      repositories: [],
      filteredRepositories: [],
      recentActivities: [],
      hasMoreActivities: false,
      activitiesPage: 0,
      showRepositoryDialog: false,
      showCommitHistoryDialog: false,
      selectedRepository: null,
      commitHistory: [],
      contributionTrendChart: null,
      languageDistributionChart: null,
      repoLanguageChart: null,
      // 新增字段
      growthViewType: 'score',
      comparisonTarget: 'team',
      growthTrajectoryChart: null,
      skillRadarChart: null,
      growthData: {
        score: [],
        activity: [],
        quality: []
      },
      skillData: {
        dimensions: ['代码能力', '协作能力', '学习能力', '创新能力', '问题解决', '项目管理'],
        values: []
      },
      capabilities: [
        { name: '代码贡献度', score: 0, comparison: 0 },
        { name: '协作能力', score: 0, comparison: 0 },
        { name: '代码质量', score: 0, comparison: 0 },
        { name: '活跃度', score: 0, comparison: 0 },
        { name: '创新性', score: 0, comparison: 0 },
        { name: '学习成长', score: 0, comparison: 0 }
      ],
      // 新增功能属性
      autoRefresh: false,
      refreshInterval: null,
      lastUpdateTime: null,
      heatmapData: [],
      contributionCalendar: null,
      codeMetrics: {
        complexity: 0,
        coverage: 0,
        maintainability: 0,
        duplication: 0
      },
      performanceMetrics: {
        responseTime: [],
        throughput: [],
        errorRate: []
      },
      collaborationData: {
        pullRequests: [],
        codeReviews: [],
        issues: []
      },
      // 新增个人档案增强功能
      profileEnhancement: {
        showAdvancedMetrics: false,
        enableRealTimeUpdate: false,
        dataVisualizationMode: 'standard', // standard, advanced, minimal
        customDateRange: {
          enabled: false,
          startDate: '',
          endDate: ''
        },
        displayOptions: {
          showTrendAnalysis: true,
          showSkillRadar: true,
          showContributionCalendar: true,
          showPerformanceMetrics: true
        }
      },
      advancedAnalytics: {
        contributionPatterns: [],
        productivityTrends: [],
        collaborationNetwork: [],
        codeQualityHistory: [],
        skillEvolution: [],
        activityHeatmap: []
      },
      interactiveFeatures: {
        chartInteraction: true,
        dataFiltering: true,
        exportOptions: {
          format: 'json', // json, csv, pdf
          includeCharts: true,
          dateRange: 'all'
        },
        comparisonMode: {
          enabled: false,
          compareWith: [], // 对比的其他成员
          metrics: ['commits', 'quality', 'collaboration']
        }
      },
      realTimeStatus: {
        connected: false,
        lastSync: null,
        updateInterval: 30000,
        autoSyncEnabled: false
      },
      dataCache: {
        enabled: true,
        ttl: 300000, // 5分钟缓存
        lastUpdate: {},
        cachedData: {}
      }
    }
  },
  mounted() {
    this.memberId = this.$route.params.id
    this.loadMemberProfile()
    this.loadRepositoryContributions()
    this.loadRecentActivities()
    this.initCharts()
    this.loadGrowthData()
    this.renderSkillRadar()
    this.loadCapabilityComparison()
    this.loadCodeMetrics()
    this.loadCollaborationData()
    this.loadPerformanceMetrics()
    this.renderContributionCalendar()
    this.lastUpdateTime = Date.now()
  },
  beforeDestroy() {
    this.disposeCharts()
    this.stopAutoRefresh()
    if (this.performanceChart) {
      this.performanceChart.dispose()
    }
    if (this.contributionCalendar) {
      this.contributionCalendar.dispose()
    }
  },
  methods: {
    async loadMemberProfile() {
      this.loading.profile = true
      try {
        // 使用后端MemberController的搜索接口获取成员信息
        const response = await this.$http.get('/member/search', {
          params: { memberId: this.memberId }
        })
        
        if (response.data && response.data.length > 0) {
          const memberData = response.data[0]
          this.memberInfo = {
            memberId: memberData.memberId,
            name: memberData.name,
            nationality: memberData.nationality,
            organizationId: memberData.organizationId,
            githubAccount: memberData.githubAccount,
            giteeAccount: memberData.giteeAccount,
            atomgitAccount: memberData.atomgitAccount,
            contactEmail: memberData.contactEmail,
            contactAddress: memberData.contactAddress,
            openrankValue: memberData.openrankValue || 0,
            lastOpenrank: memberData.lastOpenrank || 0,
            community: memberData.community,
            activeMonths: memberData.activeMonths || 0,
            // 扩展字段用于显示
            avatar: `https://github.com/${memberData.githubAccount}.png?size=80`,
            githubUsername: memberData.githubAccount,
            joinDate: memberData.createdAt,
            location: memberData.contactAddress,
            bio: '',
            company: '',
            blog: '',
            followers: 0,
            following: 0,
            publicRepos: 0
          }
        }
        
        // 加载Git活动统计数据
        await this.loadGitActivityStats()
        
      } catch (error) {
        console.error('加载成员信息失败:', error)
        this.$message.error('加载成员信息失败')
        // 使用模拟数据
        this.memberInfo = {
          memberId: this.memberId,
          name: '张三',
          githubAccount: 'zhangsan',
          githubUsername: 'zhangsan',
          avatar: 'https://github.com/zhangsan.png?size=80'
        }
        this.profileStats = {
          totalCommits: 156,
          totalRepositories: 8,
          totalLines: 12345,
          activeDays: 45
        }
      } finally {
        this.loading.profile = false
      }
    },
    
    async loadGitActivityStats() {
      try {
        // 获取成员的Git活动统计数据
        const statsResponse = await this.$http.get(`/git-data/activities/member/${this.memberId}`, {
          params: {
            startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            endDate: new Date().toISOString().split('T')[0]
          }
        })
        
        if (statsResponse.data && statsResponse.data.length > 0) {
          const activities = statsResponse.data
          
          // 计算统计数据
          const totalCommits = activities.filter(a => a.activityType === 'COMMIT').length
          const totalLines = activities.reduce((sum, a) => sum + (a.linesAdded || 0) + (a.linesDeleted || 0), 0)
          const uniqueRepos = new Set(activities.map(a => a.repositoryId)).size
          const activeDays = new Set(activities.map(a => a.activityTime?.split('T')[0])).size
          
          this.profileStats = {
            totalCommits,
            totalRepositories: uniqueRepos,
            totalLines,
            activeDays
          }
        } else {
          this.profileStats = {
            totalCommits: 0,
            totalRepositories: 0,
            totalLines: 0,
            activeDays: 0
          }
        }
      } catch (error) {
        console.error('加载Git活动统计失败:', error)
        // 使用默认值
        this.profileStats = {
          totalCommits: 0,
          totalRepositories: 0,
          totalLines: 0,
          activeDays: 0
        }
      }
    },
    
    async loadRepositoryContributions() {
      this.loading.repositories = true
      try {
        // 获取所有仓库列表
        const reposResponse = await this.$http.get('/git-data/repositories/all')
        
        if (reposResponse.data && reposResponse.data.length > 0) {
          const allRepositories = reposResponse.data
          
          // 为每个仓库获取成员的贡献统计
          const repositoryContributions = await Promise.all(
            allRepositories.map(async (repo) => {
              try {
                const statsResponse = await this.$http.get(
                  `/git-data/activities/member/${this.memberId}/repository/${repo.repositoryId}/stats`,
                  {
                    params: {
                      startDate: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
                      endDate: new Date().toISOString().split('T')[0]
                    }
                  }
                )
                
                const stats = statsResponse.data || {}
                return {
                  id: repo.repositoryId,
                  repositoryId: repo.repositoryId,
                  name: repo.repositoryName,
                  url: repo.repositoryUrl,
                  commits: stats.totalCommits || 0,
                  linesAdded: stats.totalLinesAdded || 0,
                  linesDeleted: stats.totalLinesDeleted || 0,
                  lastCommitDate: stats.lastCommitTime || repo.lastSyncTime,
                  primaryLanguage: this.detectLanguageFromRepo(repo.repositoryName),
                  createdAt: repo.createdAt,
                  platform: repo.platform,
                  branchName: repo.branchName
                }
              } catch (error) {
                console.error(`获取仓库 ${repo.repositoryName} 统计失败:`, error)
                return {
                  id: repo.repositoryId,
                  repositoryId: repo.repositoryId,
                  name: repo.repositoryName,
                  url: repo.repositoryUrl,
                  commits: 0,
                  linesAdded: 0,
                  linesDeleted: 0,
                  lastCommitDate: repo.lastSyncTime,
                  primaryLanguage: this.detectLanguageFromRepo(repo.repositoryName),
                  createdAt: repo.createdAt,
                  platform: repo.platform,
                  branchName: repo.branchName
                }
              }
            })
          )
          
          // 过滤出有贡献的仓库
          this.repositories = repositoryContributions.filter(repo => repo.commits > 0)
          this.filteredRepositories = [...this.repositories]
        }
      } catch (error) {
        console.error('加载仓库贡献失败:', error)
        // 使用模拟数据
        this.repositories = [
          {
            id: 1,
            name: 'project-frontend',
            url: 'https://github.com/example/project-frontend',
            commits: 45,
            linesAdded: 2345,
            linesDeleted: 567,
            lastCommitDate: '2024-01-15T10:30:00Z',
            primaryLanguage: 'JavaScript',
            createdAt: '2024-01-01T00:00:00Z'
          }
        ]
        this.filteredRepositories = [...this.repositories]
      } finally {
        this.loading.repositories = false
      }
    },
    
    detectLanguageFromRepo(repoName) {
      // 根据仓库名称推测主要编程语言
      const languageMap = {
        'frontend': 'JavaScript',
        'backend': 'Java',
        'web': 'JavaScript',
        'api': 'Java',
        'mobile': 'Java',
        'app': 'JavaScript',
        'ui': 'JavaScript',
        'service': 'Java'
      }
      
      const lowerName = repoName.toLowerCase()
      for (const [keyword, language] of Object.entries(languageMap)) {
        if (lowerName.includes(keyword)) {
          return language
        }
      }
      return 'Unknown'
    },
    
    async loadRecentActivities() {
      this.loading.activities = true
      try {
        // 使用后端GitDataController的活动查询接口
        const response = await this.$http.get(`/git-data/activities/member/${this.memberId}`, {
          params: {
            startDate: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
            endDate: new Date().toISOString().split('T')[0],
            page: this.activitiesPage,
            size: 10
          }
        })
        
        if (response.data && Array.isArray(response.data)) {
          const activities = response.data
          
          // 转换活动数据格式
          const formattedActivities = activities.map(activity => ({
            id: activity.activityId,
            type: this.formatActivityType(activity.activityType),
            message: this.formatActivityMessage(activity),
            timestamp: activity.activityTime || activity.createdAt,
            repository: activity.repositoryName || `Repository ${activity.repositoryId}`,
            linesAdded: activity.linesAdded || 0,
            linesDeleted: activity.linesDeleted || 0,
            commitHash: activity.commitHash,
            branch: activity.branchName,
            author: activity.authorName || activity.authorLogin
          }))
          
          if (this.activitiesPage === 0) {
            this.recentActivities = formattedActivities
          } else {
            this.recentActivities.push(...formattedActivities)
          }
          
          // 简单的分页判断
          this.hasMoreActivities = formattedActivities.length === 10
        }
      } catch (error) {
        console.error('加载最近活动失败:', error)
        // 使用模拟数据
        this.recentActivities = [
          {
            id: 1,
            activityType: 'COMMIT',
            repositoryName: 'project-frontend',
            message: 'feat: 添加用户登录功能',
            linesAdded: 45,
            linesDeleted: 12,
            timestamp: '2024-01-15T10:30:00Z'
          },
          {
            id: 2,
            activityType: 'PUSH',
            repositoryName: 'project-backend',
            message: 'fix: 修复API接口bug',
            linesAdded: 23,
            linesDeleted: 8,
            timestamp: '2024-01-14T16:45:00Z'
          }
        ]
        this.hasMoreActivities = false
      } finally {
        this.loading.activities = false
      }
    },
    
    formatActivityType(activityType) {
      const typeMap = {
        'COMMIT': '提交',
        'PUSH': '推送',
        'PULL_REQUEST': 'PR',
        'ISSUE': '问题',
        'RELEASE': '发布',
        'BRANCH': '分支',
        'TAG': '标签',
        'STATISTICS': '统计',
        'MEMBER': '成员'
      }
      return typeMap[activityType] || activityType
    },
    
    formatActivityMessage(activity) {
      switch (activity.activityType) {
        case 'COMMIT':
          return activity.commitMessage || `提交了代码到 ${activity.branchName || 'main'} 分支`
        case 'PULL_REQUEST':
          return activity.prTitle || `创建了 Pull Request #${activity.prNumber}`
        case 'ISSUE':
          return activity.issueTitle || `创建了 Issue #${activity.issueNumber}`
        case 'RELEASE':
          return activity.releaseName || `发布了版本 ${activity.releaseTag}`
        case 'BRANCH':
          return `创建了分支 ${activity.branchName}`
        case 'TAG':
          return `创建了标签 ${activity.tagName}`
        default:
          return `执行了 ${this.formatActivityType(activity.activityType)} 操作`
      }
    },
    
    loadMoreActivities() {
      this.activitiesPage++
      this.loadRecentActivities()
    },
    
    filterRepositories() {
      if (!this.repositorySearch) {
        this.filteredRepositories = [...this.repositories]
      } else {
        this.filteredRepositories = this.repositories.filter(repo =>
          repo.name.toLowerCase().includes(this.repositorySearch.toLowerCase())
        )
      }
    },
    
    viewRepositoryDetail(repository) {
      this.selectedRepository = repository
      this.showRepositoryDialog = true
      this.$nextTick(() => {
        this.renderRepoLanguageChart()
      })
    },
    
    async viewCommitHistory(repository) {
      this.selectedRepository = repository
      this.showCommitHistoryDialog = true
      this.loading.commits = true
      
      try {
        const response = await this.$http.get(`/git-activities/repository/${repository.id}/member/${this.memberId}`)
        if (response.data.code === '200') {
          this.commitHistory = response.data.data.content || []
        }
      } catch (error) {
        console.error('加载提交历史失败:', error)
        // 使用模拟数据
        this.commitHistory = [
          {
            hash: 'a1b2c3d4e5f6g7h8',
            message: 'feat: 添加用户登录功能',
            linesAdded: 45,
            linesDeleted: 12,
            timestamp: '2024-01-15T10:30:00Z'
          }
        ]
      } finally {
        this.loading.commits = false
      }
    },
    
    initCharts() {
      this.$nextTick(() => {
        this.contributionTrendChart = echarts.init(document.getElementById('contributionTrendChart'))
        this.languageDistributionChart = echarts.init(document.getElementById('languageDistributionChart'))
        this.growthTrajectoryChart = echarts.init(document.getElementById('growthTrajectoryChart'))
        this.skillRadarChart = echarts.init(document.getElementById('skillRadarChart'))
        this.loadContributionTrend()
        this.renderLanguageDistribution()
        this.loadGrowthData()
        this.renderSkillRadar()
        this.loadCapabilityComparison()
      })
    },
    
    disposeCharts() {
      if (this.contributionTrendChart) this.contributionTrendChart.dispose()
      if (this.languageDistributionChart) this.languageDistributionChart.dispose()
      if (this.repoLanguageChart) this.repoLanguageChart.dispose()
      if (this.growthTrajectoryChart) this.growthTrajectoryChart.dispose()
      if (this.skillRadarChart) this.skillRadarChart.dispose()
    },
    
    loadContributionTrend() {
      // 模拟贡献度趋势数据
      const data = {
        '7d': [12, 19, 3, 5, 2, 3, 8],
        '30d': [45, 52, 38, 67, 43, 29, 56, 78, 34, 23, 45, 67, 89, 34, 56, 78, 23, 45, 67, 34, 56, 78, 45, 67, 34, 56, 78, 45, 67, 34],
        '90d': Array.from({length: 90}, () => Math.floor(Math.random() * 20))
      }
      
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: Array.from({length: data[this.trendPeriod].length}, (_, i) => i + 1)
        },
        yAxis: {
          type: 'value',
          name: '提交数'
        },
        series: [{
          name: '提交数',
          type: 'line',
          data: data[this.trendPeriod],
          smooth: true,
          itemStyle: { color: '#409EFF' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [{
                offset: 0, color: 'rgba(64, 158, 255, 0.3)'
              }, {
                offset: 1, color: 'rgba(64, 158, 255, 0.1)'
              }]
            }
          }
        }]
      }
      
      this.contributionTrendChart.setOption(option)
    },
    
    renderLanguageDistribution() {
      const data = [
        { value: 45, name: 'JavaScript' },
        { value: 30, name: 'Java' },
        { value: 15, name: 'Python' },
        { value: 10, name: 'CSS' }
      ]
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        series: [{
          name: '语言分布',
          type: 'pie',
          radius: '70%',
          data: data,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      }
      
      this.languageDistributionChart.setOption(option)
    },
    
    renderRepoLanguageChart() {
      if (!this.repoLanguageChart) {
        this.repoLanguageChart = echarts.init(document.getElementById('repoLanguageChart'))
      }
      
      const data = [
        { value: 60, name: this.selectedRepository.primaryLanguage },
        { value: 25, name: 'HTML' },
        { value: 15, name: 'CSS' }
      ]
      
      const option = {
        tooltip: {
          trigger: 'item'
        },
        series: [{
          name: '语言占比',
          type: 'pie',
          radius: '60%',
          data: data
        }]
      }
      
      this.repoLanguageChart.setOption(option)
    },
    
    getActivityTypeText(type) {
      const typeMap = {
        'COMMIT': '提交',
        'PUSH': '推送',
        'MERGE': '合并',
        'BRANCH': '分支'
      }
      return typeMap[type] || type
    },
    
    getActivityTimelineType(type) {
      const typeMap = {
        'COMMIT': 'primary',
        'PUSH': 'success',
        'MERGE': 'warning',
        'BRANCH': 'info'
      }
      return typeMap[type] || 'primary'
    },
    
    getLanguageColor(language) {
      const colorMap = {
        'JavaScript': '#f1e05a',
        'Java': '#b07219',
        'Python': '#3572A5',
        'CSS': '#563d7c',
        'HTML': '#e34c26',
        'Vue': '#4fc08d'
      }
      return colorMap[language] || '#666'
    },
    
    getCommitUrl(commit) {
      if (this.selectedRepository && commit.hash) {
        return `${this.selectedRepository.url}/commit/${commit.hash}`
      }
      return '#'
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      return new Date(dateString).toLocaleString('zh-CN')
    },
    
    // 新增方法
    async loadGrowthData() {
      try {
        const response = await this.$http.get(`/members/${this.memberId}/growth-trajectory`)
        if (response.data.code === '200') {
          this.growthData = response.data.data
        }
      } catch (error) {
        console.error('加载成长轨迹数据失败:', error)
        // 使用模拟数据
        this.loadMockGrowthData()
      }
      this.renderGrowthChart()
    },
    
    loadMockGrowthData() {
      const dates = []
      const scoreData = []
      const activityData = []
      const qualityData = []
      
      for (let i = 29; i >= 0; i--) {
        const date = new Date()
        date.setDate(date.getDate() - i)
        dates.push(date.toISOString().split('T')[0])
        
        scoreData.push(Math.floor(Math.random() * 30) + 70)
        activityData.push(Math.floor(Math.random() * 50) + 20)
        qualityData.push(Math.floor(Math.random() * 25) + 75)
      }
      
      this.growthData = {
        dates,
        score: scoreData,
        activity: activityData,
        quality: qualityData
      }
    },
    
    renderGrowthChart() {
      if (!this.growthTrajectoryChart || !this.growthData.dates) return
      
      const dataMap = {
        score: { name: '综合得分', data: this.growthData.score, color: '#409EFF' },
        activity: { name: '活跃度', data: this.growthData.activity, color: '#67C23A' },
        quality: { name: '代码质量', data: this.growthData.quality, color: '#E6A23C' }
      }
      
      const currentData = dataMap[this.growthViewType]
      
      const option = {
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            return `${params[0].axisValue}<br/>${params[0].seriesName}: ${params[0].value}`
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
          data: this.growthData.dates,
          axisLabel: {
            formatter: function(value) {
              return value.split('-').slice(1).join('/')
            }
          }
        },
        yAxis: {
          type: 'value',
          name: currentData.name,
          min: 0,
          max: 100
        },
        series: [{
          name: currentData.name,
          type: 'line',
          data: currentData.data,
          smooth: true,
          itemStyle: { color: currentData.color },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [{
                offset: 0, color: currentData.color + '40'
              }, {
                offset: 1, color: currentData.color + '10'
              }]
            }
          }
        }]
      }
      
      this.growthTrajectoryChart.setOption(option)
    },
    
    async renderSkillRadar() {
      try {
        const response = await this.$http.get(`/members/${this.memberId}/skill-assessment`)
        if (response.data.code === '200') {
          this.skillData.values = response.data.data.values
        }
      } catch (error) {
        console.error('加载技能数据失败:', error)
        // 使用模拟数据
        this.skillData.values = [85, 78, 92, 73, 88, 81]
      }
      
      if (!this.skillRadarChart) return
      
      const option = {
        tooltip: {
          trigger: 'item'
        },
        radar: {
          indicator: this.skillData.dimensions.map(dim => ({
            name: dim,
            max: 100
          })),
          radius: '70%',
          splitNumber: 4,
          axisName: {
            color: '#666',
            fontSize: 12
          },
          splitLine: {
            lineStyle: {
              color: '#e6e6e6'
            }
          },
          splitArea: {
            areaStyle: {
              color: ['rgba(64, 158, 255, 0.1)', 'rgba(64, 158, 255, 0.05)']
            }
          }
        },
        series: [{
          name: '技能评估',
          type: 'radar',
          data: [{
            value: this.skillData.values,
            name: '当前能力',
            itemStyle: {
              color: '#409EFF'
            },
            areaStyle: {
              color: 'rgba(64, 158, 255, 0.3)'
            }
          }]
        }]
      }
      
      this.skillRadarChart.setOption(option)
    },
    
    refreshSkillRadar() {
      this.renderSkillRadar()
    },
    
    async loadCapabilityComparison() {
      try {
        const response = await this.$http.get(`/members/${this.memberId}/capability-comparison?target=${this.comparisonTarget}`)
        if (response.data.code === '200') {
          this.capabilities = response.data.data
        }
      } catch (error) {
        console.error('加载能力对比数据失败:', error)
        // 使用模拟数据
        this.loadMockCapabilityData()
      }
    },
    
    loadMockCapabilityData() {
      this.capabilities = [
        { name: '代码贡献度', score: 85, comparison: 12.5 },
        { name: '协作能力', score: 78, comparison: -3.2 },
        { name: '代码质量', score: 92, comparison: 18.7 },
        { name: '活跃度', score: 73, comparison: -8.1 },
        { name: '创新性', score: 88, comparison: 25.3 },
        { name: '学习成长', score: 81, comparison: 6.8 }
      ]
    },
    
    getScoreClass(score) {
      if (score >= 90) return 'score-excellent'
      if (score >= 80) return 'score-good'
      if (score >= 70) return 'score-average'
      return 'score-poor'
    },
    
    getProgressColor(score) {
      if (score >= 90) return '#67C23A'
      if (score >= 80) return '#409EFF'
      if (score >= 70) return '#E6A23C'
      return '#F56C6C'
    },
    
    getComparisonClass(comparison) {
      return comparison > 0 ? 'comparison-positive' : 'comparison-negative'
    },
    
    getComparisonLabel() {
      const labelMap = {
        team: '团队平均',
        course: '课程平均',
        best: '历史最佳'
      }
      return labelMap[this.comparisonTarget] || '对比目标'
    },
    
    // 新增方法
    toggleAutoRefresh() {
      this.autoRefresh = !this.autoRefresh
      if (this.autoRefresh) {
        this.startAutoRefresh()
      } else {
        this.stopAutoRefresh()
      }
    },
    
    startAutoRefresh() {
      this.refreshInterval = setInterval(() => {
        this.refreshAllData()
      }, 30000) // 30秒刷新一次
    },
    
    stopAutoRefresh() {
      if (this.refreshInterval) {
        clearInterval(this.refreshInterval)
        this.refreshInterval = null
      }
    },
    
    async refreshAllData() {
      this.lastUpdateTime = Date.now()
      await Promise.all([
        this.loadMemberProfile(),
        this.loadRepositoryContributions(),
        this.loadRecentActivities(),
        this.loadGrowthData(),
        this.renderSkillRadar(),
        this.loadCapabilityComparison(),
        this.loadCodeMetrics(),
        this.loadCollaborationData(),
        this.loadPerformanceMetrics(),
        this.renderContributionCalendar()
      ])
    },
    
    async exportProfile() {
      try {
        const profileData = {
          memberInfo: this.memberInfo,
          profileStats: this.profileStats,
          repositories: this.repositories,
          capabilities: this.capabilities,
          codeMetrics: this.codeMetrics,
          collaborationData: this.collaborationData,
          exportTime: new Date().toISOString()
        }
        
        const blob = new Blob([JSON.stringify(profileData, null, 2)], {
          type: 'application/json'
        })
        const url = URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = `${this.memberInfo.name || 'member'}_profile_${Date.now()}.json`
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        URL.revokeObjectURL(url)
        
        this.$message.success('档案导出成功')
      } catch (error) {
        console.error('导出档案失败:', error)
        this.$message.error('导出档案失败')
      }
    },
    
    formatTime(timestamp) {
      if (!timestamp) return ''
      const now = new Date()
      const time = new Date(timestamp)
      const diff = now.getTime() - time.getTime()
      const oneDay = 24 * 60 * 60 * 1000
      
      if (diff < oneDay && now.getDate() === time.getDate()) {
        return '今天 ' + time.toTimeString().slice(0, 5)
      } else if (diff < 2 * oneDay && now.getDate() - time.getDate() === 1) {
        return '昨天 ' + time.toTimeString().slice(0, 5)
      } else {
        return (time.getMonth() + 1).toString().padStart(2, '0') + '-' + 
               time.getDate().toString().padStart(2, '0') + ' ' + 
               time.toTimeString().slice(0, 5)
      }
    },
    
    async loadCodeMetrics() {
      try {
        const response = await this.$http.get(`/members/${this.memberId}/code-metrics`)
        if (response.data.code === '200') {
          this.codeMetrics = response.data.data
        }
      } catch (error) {
        console.error('加载代码质量指标失败:', error)
        // 使用模拟数据
        this.codeMetrics = {
          complexity: Math.floor(Math.random() * 30) + 70,
          coverage: Math.floor(Math.random() * 40) + 60,
          maintainability: Math.floor(Math.random() * 25) + 75,
          duplication: Math.floor(Math.random() * 15) + 5
        }
      }
    },
    
    getMetricColor(value) {
      if (value >= 90) return '#67C23A'
      if (value >= 80) return '#409EFF'
      if (value >= 70) return '#E6A23C'
      return '#F56C6C'
    },
    
    async loadCollaborationData() {
      try {
        const response = await this.$http.get(`/members/${this.memberId}/collaboration`)
        if (response.data.code === '200') {
          this.collaborationData = response.data.data
        }
      } catch (error) {
        console.error('加载协作数据失败:', error)
        // 使用模拟数据
        this.collaborationData = {
          pullRequests: Array.from({length: Math.floor(Math.random() * 20) + 5}, (_, i) => ({ id: i })),
          codeReviews: Array.from({length: Math.floor(Math.random() * 30) + 10}, (_, i) => ({ id: i })),
          issues: Array.from({length: Math.floor(Math.random() * 15) + 3}, (_, i) => ({ id: i }))
        }
      }
    },
    
    async loadPerformanceMetrics() {
      try {
        const response = await this.$http.get(`/members/${this.memberId}/performance`)
        if (response.data.code === '200') {
          this.performanceMetrics = response.data.data
        }
      } catch (error) {
        console.error('加载性能指标失败:', error)
        // 使用模拟数据
        this.generateMockPerformanceData()
      }
      this.renderPerformanceChart()
    },
    
    generateMockPerformanceData() {
      const dates = []
      const responseTime = []
      const throughput = []
      const errorRate = []
      
      for (let i = 29; i >= 0; i--) {
        const date = new Date()
        date.setDate(date.getDate() - i)
        dates.push(date.toISOString().split('T')[0])
        
        responseTime.push(Math.floor(Math.random() * 200) + 100)
        throughput.push(Math.floor(Math.random() * 1000) + 500)
        errorRate.push(Math.random() * 5)
      }
      
      this.performanceMetrics = {
        dates,
        responseTime,
        throughput,
        errorRate
      }
    },
    
    renderPerformanceChart() {
      if (!this.performanceChart) {
        this.performanceChart = echarts.init(document.getElementById('performanceChart'))
      }
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        legend: {
          data: ['响应时间(ms)', '吞吐量(req/s)', '错误率(%)']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.performanceMetrics.dates || [],
          axisLabel: {
            formatter: function(value) {
              return value.split('-').slice(1).join('/')
            }
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '响应时间(ms)',
            position: 'left'
          },
          {
            type: 'value',
            name: '吞吐量(req/s)',
            position: 'right'
          }
        ],
        series: [
          {
            name: '响应时间(ms)',
            type: 'line',
            data: this.performanceMetrics.responseTime || [],
            smooth: true,
            itemStyle: { color: '#409EFF' }
          },
          {
            name: '吞吐量(req/s)',
            type: 'line',
            yAxisIndex: 1,
            data: this.performanceMetrics.throughput || [],
            smooth: true,
            itemStyle: { color: '#67C23A' }
          },
          {
            name: '错误率(%)',
            type: 'bar',
            data: this.performanceMetrics.errorRate || [],
            itemStyle: { color: '#F56C6C' }
          }
        ]
      }
      
      this.performanceChart.setOption(option)
    },
    
    renderContributionCalendar() {
      if (!this.contributionCalendar) {
        this.contributionCalendar = echarts.init(document.getElementById('contributionCalendar'))
      }
      
      // 生成一年的贡献数据
      const startDate = new Date()
      startDate.setFullYear(startDate.getFullYear() - 1)
      const data = []
      
      for (let i = 0; i < 365; i++) {
        const date = new Date(startDate)
        date.setDate(date.getDate() + i)
        const value = Math.floor(Math.random() * 10)
        data.push([
          date.toISOString().split('T')[0],
          value
        ])
      }
      
      const option = {
        tooltip: {
          formatter: function(params) {
            return params.value[0] + ': ' + params.value[1] + ' 次提交'
          }
        },
        visualMap: {
          min: 0,
          max: 10,
          type: 'piecewise',
          orient: 'horizontal',
          left: 'center',
          top: 10,
          pieces: [
            {min: 0, max: 0, color: '#ebedf0'},
            {min: 1, max: 2, color: '#c6e48b'},
            {min: 3, max: 5, color: '#7bc96f'},
            {min: 6, max: 8, color: '#239a3b'},
            {min: 9, max: 10, color: '#196127'}
          ]
        },
        calendar: {
          top: 60,
          left: 30,
          right: 30,
          cellSize: ['auto', 13],
          range: [startDate.toISOString().split('T')[0], new Date().toISOString().split('T')[0]],
          itemStyle: {
            borderWidth: 0.5
          },
          yearLabel: { show: false }
        },
        series: {
          type: 'heatmap',
          coordinateSystem: 'calendar',
          data: data
        }
      }
      
      this.contributionCalendar.setOption(option)
    },
    
    switchToHeatmap() {
      this.renderContributionCalendar()
    },
    
    switchToTrend() {
      this.loadContributionTrend()
    },
    
    // 新增高级功能控制方法
    toggleAdvancedMetrics() {
      this.profileEnhancement.showAdvancedMetrics = !this.profileEnhancement.showAdvancedMetrics;
      this.$nextTick(() => {
        if (this.profileEnhancement.showAdvancedMetrics) {
          this.loadAdvancedMetrics();
        }
      });
    },
    
    handleVisualizationMode(mode) {
      this.profileEnhancement.visualizationMode = mode;
      this.applyVisualizationMode(mode);
    },
    
    getVisualizationModeText() {
      const modes = {
        standard: '标准模式',
        advanced: '高级模式',
        minimal: '简洁模式'
      };
      return modes[this.profileEnhancement.visualizationMode] || '标准模式';
    },
    
    applyVisualizationMode(mode) {
      // 根据模式调整图表和界面显示
      switch(mode) {
        case 'advanced':
          this.profileEnhancement.showDetailedCharts = true;
          this.profileEnhancement.showAdvancedMetrics = true;
          break;
        case 'minimal':
          this.profileEnhancement.showDetailedCharts = false;
          this.profileEnhancement.showAdvancedMetrics = false;
          break;
        default: // standard
          this.profileEnhancement.showDetailedCharts = true;
          this.profileEnhancement.showAdvancedMetrics = false;
      }
      
      // 重新渲染图表
      this.$nextTick(() => {
        this.refreshCharts();
      });
    },
    
    toggleRealTimeSync() {
      this.realTimeStatus.autoSyncEnabled = !this.realTimeStatus.autoSyncEnabled;
      
      if (this.realTimeStatus.autoSyncEnabled) {
        this.startRealTimeSync();
      } else {
        this.stopRealTimeSync();
      }
    },
    
    startRealTimeSync() {
      this.realTimeStatus.connected = true;
      this.realTimeStatus.lastSync = new Date();
      
      // 启动实时同步定时器
      this.realTimeStatus.syncInterval = setInterval(() => {
        this.syncRealTimeData();
      }, 30000); // 30秒同步一次
    },
    
    stopRealTimeSync() {
      this.realTimeStatus.connected = false;
      if (this.realTimeStatus.syncInterval) {
        clearInterval(this.realTimeStatus.syncInterval);
        this.realTimeStatus.syncInterval = null;
      }
    },
    
    async syncRealTimeData() {
      try {
        // 实时同步数据
        await this.loadMemberProfile();
        await this.loadRepositoryContributions();
        await this.loadRecentActivities();
        
        this.realTimeStatus.lastSync = new Date();
        this.realTimeStatus.connected = true;
      } catch (error) {
        console.error('实时同步失败:', error);
        this.realTimeStatus.connected = false;
      }
    },
    
    async loadAdvancedMetrics() {
      try {
        this.advancedAnalytics.loading = true;
        
        // 加载高级指标数据
        const response = await this.$http.get(`/members/${this.memberId}/advanced-metrics`);
        this.advancedAnalytics.metrics = response.data;
        
        // 渲染高级图表
        this.renderAdvancedCharts();
      } catch (error) {
        console.error('加载高级指标失败:', error);
        // 使用模拟数据
        this.loadMockAdvancedMetrics();
      } finally {
        this.advancedAnalytics.loading = false;
      }
    },
    
    loadMockAdvancedMetrics() {
      this.advancedAnalytics.metrics = {
        codeComplexity: {
          average: 3.2,
          trend: [2.8, 3.1, 3.2, 3.0, 3.2],
          distribution: {
            low: 65,
            medium: 28,
            high: 7
          }
        },
        testCoverage: {
          overall: 78,
          byLanguage: {
            'JavaScript': 82,
            'Python': 75,
            'Java': 80
          }
        },
        performanceMetrics: {
          buildTime: [120, 115, 108, 112, 105],
          memoryUsage: [45, 48, 42, 46, 44],
          cpuUsage: [25, 28, 23, 26, 24]
        }
      };
      
      this.renderAdvancedCharts();
    },
    
    renderAdvancedCharts() {
      // 渲染高级图表的实现
      this.$nextTick(() => {
        if (this.profileEnhancement.showAdvancedMetrics) {
          this.renderComplexityChart();
          this.renderCoverageChart();
          this.renderPerformanceMetricsChart();
        }
      });
    },
    
    refreshCharts() {
      // 刷新所有图表
      const charts = [
        'contributionTrendChart',
        'languageDistributionChart', 
        'repoLanguageChart',
        'growthTrajectoryChart',
        'skillRadarChart',
        'contributionCalendar',
        'performanceChart'
      ];
      
      charts.forEach(chartName => {
        if (this[chartName]) {
          this[chartName].resize();
        }
      });
    }
  }
}
</script>

<style scoped>
.member-git-profile {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.member-info {
  margin-bottom: 20px;
}

.profile-header {
  display: flex;
  align-items: center;
}

.avatar-section {
  margin-right: 20px;
}

.info-section {
  flex: 1;
}

.info-section h2 {
  margin: 0 0 10px 0;
  color: #303133;
}

.member-id {
  color: #909399;
  margin: 5px 0;
}

.github-info {
  margin: 10px 0;
}

.member-stats {
  display: flex;
  gap: 30px;
  margin-top: 15px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 5px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.contribution-overview {
  margin-bottom: 20px;
}

.repository-contributions {
  margin-bottom: 20px;
}

.recent-activities {
  margin-bottom: 20px;
}

.activity-content {
  padding: 10px 0;
}

.activity-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}

.activity-type {
  background: #409EFF;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.repository-name {
  font-weight: bold;
  color: #303133;
}

.activity-message {
  color: #606266;
  margin-bottom: 5px;
}

.activity-stats {
  display: flex;
  gap: 10px;
}

.lines-added {
  color: #67C23A;
  font-weight: bold;
}

.lines-deleted {
  color: #F56C6C;
  font-weight: bold;
}

.load-more {
  text-align: center;
  margin-top: 20px;
}

.el-card {
  margin-bottom: 20px;
}

/* 新增样式 */
.growth-analysis {
  margin-bottom: 20px;
}

.capability-assessment {
  margin-bottom: 20px;
}

.capability-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-top: 15px;
}

.capability-item {
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fafafa;
}

.capability-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.capability-name {
  font-weight: bold;
  color: #303133;
}

.capability-score {
  font-size: 18px;
  font-weight: bold;
}

.score-excellent {
  color: #67C23A;
}

.score-good {
  color: #409EFF;
}

.score-average {
  color: #E6A23C;
}

.score-poor {
  color: #F56C6C;
}

.capability-progress {
  margin-bottom: 10px;
}

.capability-comparison {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.comparison-label {
  color: #909399;
}

.comparison-value {
  font-weight: bold;
}

.comparison-positive {
  color: #67C23A;
}

.comparison-negative {
  color: #F56C6C;
}

.el-radio-group .el-radio-button__inner {
  padding: 8px 12px;
}

/* 新增样式 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.last-update-time {
  font-size: 12px;
  color: #909399;
  margin-left: 12px;
}

/* 实时状态样式 */
.realtime-status {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 12px;
  padding: 4px 8px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
}

.last-sync {
  color: #909399;
  font-size: 11px;
}

.connected-pulse {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0.7);
  }
  70% {
    box-shadow: 0 0 0 4px rgba(103, 194, 58, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(103, 194, 58, 0);
  }
}

/* 高级功能按钮样式 */
.el-button-group .el-button {
  border-radius: 4px;
}

.el-button-group .el-button:not(:first-child) {
  margin-left: -1px;
}

.el-dropdown .el-button {
  padding: 7px 15px;
  white-space: nowrap;
}

.view-toggle {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.metric-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.metric-item:last-child {
  border-bottom: none;
}

.metric-label {
  font-weight: 500;
  color: #303133;
}

.metric-value {
  display: flex;
  align-items: center;
  gap: 8px;
}

.metric-score {
  font-weight: bold;
  font-size: 16px;
}

.metric-progress {
  width: 60px;
}

.collaboration-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  text-align: center;
}

.stat-item {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.chart-container {
  height: 300px;
  margin-top: 16px;
}

.contribution-calendar {
  height: 200px;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .capability-grid {
    grid-template-columns: 1fr;
  }
  
  .member-stats {
    flex-direction: column;
    gap: 15px;
  }
  
  .profile-header {
    flex-direction: column;
    text-align: center;
  }
  
  .avatar-section {
    margin-right: 0;
    margin-bottom: 15px;
  }
  
  .header-actions {
    flex-direction: column;
    align-items: stretch;
    gap: 8px;
  }
  
  .view-toggle {
    justify-content: center;
  }
  
  .collaboration-stats {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .metric-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>