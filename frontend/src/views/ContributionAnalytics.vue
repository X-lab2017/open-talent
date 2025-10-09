<template>
  <div>
    <NavMenu />
    <div class="contribution-analytics">
    <div class="page-header">
      <h2>贡献度分析</h2>
      <div class="header-actions">
        <el-button 
          :type="autoRefreshEnabled ? 'success' : 'info'" 
          size="small" 
          @click="toggleAutoRefresh"
          :loading="loading">
          <i :class="autoRefreshEnabled ? 'el-icon-video-pause' : 'el-icon-video-play'"></i>
          {{ autoRefreshEnabled ? '停止自动刷新' : '开启自动刷新' }}
        </el-button>
        <el-button type="primary" @click="exportAnalytics">
          <i class="el-icon-download"></i>
          导出报告
        </el-button>
        <el-button @click="refreshData">
          <i class="el-icon-refresh"></i>
          刷新数据
        </el-button>
        <span v-if="lastUpdateTime" class="last-update-time">
          最后更新: {{ formatTime(lastUpdateTime) }}
        </span>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filters">
      <el-card>
        <el-row :gutter="20">
          <el-col :span="6">
            <el-select v-model="selectedCourse" placeholder="选择课程" @change="loadAnalytics">
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
            <el-select v-model="analysisType" placeholder="分析类型" @change="loadAnalytics">
              <el-option label="个人分析" value="individual"></el-option>
              <el-option label="团队分析" value="team"></el-option>
              <el-option label="课程分析" value="course"></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select v-model="timePeriod" placeholder="时间周期" @change="loadAnalytics">
              <el-option label="最近7天" value="7d"></el-option>
              <el-option label="最近30天" value="30d"></el-option>
              <el-option label="最近90天" value="90d"></el-option>
              <el-option label="本学期" value="semester"></el-option>
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-date-picker
              v-model="customDateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              @change="loadAnalytics">
            </el-date-picker>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 15px;">
          <el-col :span="24">
            <div style="text-align: right;">
              <el-button size="small" @click="showIntelligentConfig = true" icon="el-icon-setting">
                智能评分配置
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </div>

    <!-- 总体统计 -->
    <div class="overview-stats">
      <el-row :gutter="20">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon total">
              <i class="el-icon-user"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ overviewStats.totalMembers }}</div>
              <div class="stat-label">参与成员</div>
              <div class="stat-change" :class="{ positive: overviewStats.memberChange > 0 }">
                {{ overviewStats.memberChange > 0 ? '+' : '' }}{{ overviewStats.memberChange }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon commits">
              <i class="el-icon-document"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ overviewStats.totalCommits }}</div>
              <div class="stat-label">总提交数</div>
              <div class="stat-change" :class="{ positive: overviewStats.commitChange > 0 }">
                {{ overviewStats.commitChange > 0 ? '+' : '' }}{{ overviewStats.commitChange }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon score">
              <i class="el-icon-trophy"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ overviewStats.avgScore.toFixed(1) }}</div>
              <div class="stat-label">平均得分</div>
              <div class="stat-change" :class="{ positive: overviewStats.scoreChange > 0 }">
                {{ overviewStats.scoreChange > 0 ? '+' : '' }}{{ overviewStats.scoreChange }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon active">
              <i class="el-icon-time"></i>
            </div>
            <div class="stat-content">
              <div class="stat-number">{{ overviewStats.activeDays }}</div>
              <div class="stat-label">活跃天数</div>
              <div class="stat-change" :class="{ positive: overviewStats.activeChange > 0 }">
                {{ overviewStats.activeChange > 0 ? '+' : '' }}{{ overviewStats.activeChange }}%
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表分析 -->
    <div class="charts-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-card>
            <div slot="header">
              <span>贡献度分布</span>
              <el-button-group size="mini" style="float: right; margin-top: -5px;">
                <el-button @click="changeDistributionView('pie')" :type="distributionView === 'pie' ? 'primary' : ''">饼图</el-button>
                <el-button @click="changeDistributionView('bar')" :type="distributionView === 'bar' ? 'primary' : ''">柱图</el-button>
              </el-button-group>
            </div>
            <div id="contributionDistributionChart" style="height: 350px;"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <div slot="header">
              <span>趋势分析</span>
              <el-button-group size="mini" style="float: right; margin-top: -5px;">
                <el-button @click="changeTrendPeriod('7d')" :type="trendPeriod === '7d' ? 'primary' : ''">7天</el-button>
                <el-button @click="changeTrendPeriod('30d')" :type="trendPeriod === '30d' ? 'primary' : ''">30天</el-button>
                <el-button @click="changeTrendPeriod('90d')" :type="trendPeriod === '90d' ? 'primary' : ''">90天</el-button>
              </el-button-group>
            </div>
            <div id="trendAnalysisChart" style="height: 350px;"></div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <div slot="header">
              <span>活动热力图</span>
              <el-tooltip content="显示成员在不同时间段的活跃程度" placement="top">
                <i class="el-icon-info" style="float: right; margin-top: 2px; color: #909399;"></i>
              </el-tooltip>
            </div>
            <div id="activityHeatmapChart" style="height: 350px;"></div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 第二行图表 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <div slot="header">
              <span>技能雷达图</span>
              <el-select v-model="selectedRadarMember" size="mini" style="width: 150px; float: right; margin-top: -5px;" @change="renderSkillRadar">
                <el-option label="团队平均" value="average"></el-option>
                <el-option 
                  v-for="member in topContributors.slice(0, 5)" 
                  :key="member.id" 
                  :label="member.name" 
                  :value="member.id">
                </el-option>
              </el-select>
            </div>
            <div id="skillRadarChart" style="height: 350px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div slot="header">
              <span>代码质量分析</span>
              <el-button size="mini" style="float: right; margin-top: -5px;" @click="refreshQualityData">
                <i class="el-icon-refresh"></i>
              </el-button>
            </div>
            <div id="codeQualityChart" style="height: 350px;"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 智能贡献度评分 -->
    <div class="contribution-scoring-section" v-if="selectedMember">
      <el-card>
        <div slot="header">
          <span>{{ selectedMember.name }} - 智能贡献度评分</span>
          <el-button size="mini" style="float: right;" @click="selectedMember = null">关闭</el-button>
        </div>
        <ContributionScoring 
          :activities="selectedMemberActivities" 
          :userId="selectedMember.id"
        />
      </el-card>
    </div>

    <!-- 维度分析 -->
    <div class="dimension-analysis">
      <el-card>
        <div slot="header">
          <span>维度分析</span>
          <div style="float: right; margin-top: -5px;">
            <el-button size="mini" @click="showWeightConfig = true">权重配置</el-button>
            <el-button size="mini" @click="showBatchScoring = true" style="margin-left: 5px;">批量评分</el-button>
            <el-radio-group v-model="dimensionView" size="mini" style="margin-left: 10px;">
              <el-radio-button label="radar">雷达图</el-radio-button>
              <el-radio-button label="bar">柱状图</el-radio-button>
            </el-radio-group>
          </div>
        </div>
        <div id="dimensionAnalysisChart" style="height: 400px;"></div>
      </el-card>
    </div>
    
    <!-- 对比分析卡片 -->
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card class="comparison-card">
          <div slot="header">
            <span>当前周期 vs 上一周期</span>
          </div>
          <div class="comparison-content">
            <div class="comparison-item" v-for="(value, key) in comparisonData.currentPeriod" :key="key">
              <span class="dimension-name">{{ getDimensionName(key) }}</span>
              <div class="comparison-values">
                <span class="current-value">{{ value.toFixed(1) }}</span>
                <span class="vs-text">vs</span>
                <span class="previous-value">{{ comparisonData.previousPeriod[key].toFixed(1) }}</span>
                <span :class="['change-indicator', getChangeClass(value - comparisonData.previousPeriod[key])]">
                  {{ getChangeText(value - comparisonData.previousPeriod[key]) }}
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="comparison-card">
          <div slot="header">
            <span>当前周期 vs 基准水平</span>
          </div>
          <div class="comparison-content">
            <div class="comparison-item" v-for="(value, key) in comparisonData.currentPeriod" :key="key">
              <span class="dimension-name">{{ getDimensionName(key) }}</span>
              <div class="comparison-values">
                <span class="current-value">{{ value.toFixed(1) }}</span>
                <span class="vs-text">vs</span>
                <span class="benchmark-value">{{ comparisonData.benchmark[key].toFixed(1) }}</span>
                <span :class="['change-indicator', getChangeClass(value - comparisonData.benchmark[key])]">
                  {{ getChangeText(value - comparisonData.benchmark[key]) }}
                </span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="comparison-card">
          <div slot="header">
            <span>评分权重配置</span>
          </div>
          <div class="weight-display">
            <div class="weight-item" v-for="(weight, key) in scoreWeights" :key="key">
              <span class="weight-name">{{ getDimensionName(key) }}</span>
              <el-progress 
                :percentage="weight * 100" 
                :show-text="false" 
                :stroke-width="8"
                :color="getWeightColor(weight)"
              ></el-progress>
              <span class="weight-value">{{ (weight * 100).toFixed(0) }}%</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 排行榜 -->
    <div class="leaderboard">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <div slot="header">
              <span>贡献度排行榜</span>
            </div>
            <div class="leaderboard-list">
              <div
                v-for="(member, index) in topContributors"
                :key="member.id"
                class="leaderboard-item"
                :class="{ 'top-three': index < 3 }">
                <div class="rank">
                  <span class="rank-number" :class="getRankClass(index)">{{ index + 1 }}</span>
                </div>
                <div class="member-info">
                  <div class="member-name">{{ member.name }}</div>
                  <div class="member-score">{{ member.totalScore.toFixed(1) }}分</div>
                </div>
                <div class="member-stats">
                  <div class="stat-item">
                    <span class="stat-value">{{ member.commits }}</span>
                    <span class="stat-label">提交</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ member.linesAdded }}</span>
                    <span class="stat-label">新增行</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div slot="header">
              <span>活跃度排行榜</span>
            </div>
            <div class="leaderboard-list">
              <div
                v-for="(member, index) in mostActiveMembers"
                :key="member.id"
                class="leaderboard-item"
                :class="{ 'top-three': index < 3 }">
                <div class="rank">
                  <span class="rank-number" :class="getRankClass(index)">{{ index + 1 }}</span>
                </div>
                <div class="member-info">
                  <div class="member-name">{{ member.name }}</div>
                  <div class="member-score">{{ member.activeDays }}天</div>
                </div>
                <div class="member-stats">
                  <div class="stat-item">
                    <span class="stat-value">{{ member.avgCommitsPerDay.toFixed(1) }}</span>
                    <span class="stat-label">日均提交</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-value">{{ member.repositories }}</span>
                    <span class="stat-label">仓库数</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 详细数据表格 -->
    <div class="detailed-table">
      <el-card>
        <div slot="header" class="table-header">
          <div class="header-left">
            <span class="table-title">详细数据</span>
            <el-tag size="mini" type="info">{{ filteredTableData.length }} 条记录</el-tag>
          </div>
          <div class="header-right">
            <el-input
              v-model="tableSearch"
              placeholder="搜索成员姓名"
              size="small"
              clearable
              class="search-input"
              @input="filterTableData">
              <i slot="prefix" class="el-input__icon el-icon-search"></i>
            </el-input>
          </div>
        </div>
        
        <div class="table-container">
          <el-table 
            :data="filteredTableData" 
            v-loading="loading" 
            stripe 
            border
            :max-height="600"
            show-header
            class="detailed-data-table">
            <el-table-column prop="memberName" label="成员" width="160" fixed="left" show-overflow-tooltip>
              <template slot-scope="scope">
                <div class="member-cell">
                  <el-avatar :size="32" :src="scope.row.avatar" class="member-avatar">
                    {{ scope.row.memberName.charAt(0) }}
                  </el-avatar>
                  <span class="member-name">{{ scope.row.memberName }}</span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="totalScore" label="总分" width="180" align="center" sortable>
              <template slot-scope="scope">
                <el-tag :type="getScoreType(scope.row.totalScore)" class="score-tag">
                  {{ scope.row.totalScore.toFixed(1) }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="codeContribution" label="代码贡献" width="120" align="center" sortable>
              <template slot-scope="scope">
                <span class="score-value">{{ scope.row.codeContribution.toFixed(1) }}</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="collaborationScore" label="协作得分" width="120" align="center" sortable>
              <template slot-scope="scope">
                <span class="score-value">{{ scope.row.collaborationScore.toFixed(1) }}</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="qualityScore" label="质量得分" width="120" align="center" sortable>
              <template slot-scope="scope">
                <span class="score-value">{{ scope.row.qualityScore.toFixed(1) }}</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="commits" label="提交数" width="180" align="center" sortable>
              <template slot-scope="scope">
                <el-tag size="mini" type="primary">{{ scope.row.commits }}</el-tag>
              </template>
            </el-table-column>
            
            <el-table-column prop="linesAdded" label="新增行" width="100" align="center" sortable>
              <template slot-scope="scope">
                <span class="lines-added">+{{ scope.row.linesAdded.toLocaleString() }}</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="linesDeleted" label="删除行" width="100" align="center" sortable>
              <template slot-scope="scope">
                <span class="lines-deleted">-{{ scope.row.linesDeleted.toLocaleString() }}</span>
              </template>
            </el-table-column>
            
            <el-table-column prop="activeDays" label="活跃天数" width="120" align="center" sortable>
              <template slot-scope="scope">
                <div class="activity-cell">
                  <span class="activity-days">{{ scope.row.activeDays }}</span>
                  <span class="activity-unit">天</span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column prop="lastActivityDate" label="最后活动" width="140" align="center" sortable>
              <template slot-scope="scope">
                <div class="date-cell">
                  <i class="el-icon-time"></i>
                  <span>{{ formatDate(scope.row.lastActivityDate) }}</span>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="280" align="center" fixed="right">
              <template slot-scope="scope">
                <div class="action-buttons">
                  <el-button size="small" icon="el-icon-view" @click="viewMemberDetail(scope.row)">详情</el-button>
                  <el-button size="small" type="primary" icon="el-icon-user" @click="viewMemberProfile(scope.row)">档案</el-button>
                  <el-button size="small" type="success" icon="el-icon-star-on" @click="showMemberScoring(scope.row)">评分</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
        
        <!-- 优化后的分页组件 -->
        <div class="table-footer">
          <div class="pagination-wrapper">
            <div class="pagination-info">
              <span>共 {{ pagination.total }} 条记录，每页显示 {{ pagination.pageSize }} 条</span>
            </div>
            <el-pagination
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="pagination.current"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="pagination.pageSize"
              layout="sizes, prev, pager, next, jumper"
              :total="pagination.total"
              class="table-pagination">
            </el-pagination>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 权重配置对话框 -->
    <el-dialog
      title="评分权重配置"
      :visible.sync="showWeightConfig"
      width="600px"
      @open="openWeightConfig"
    >
      <div class="weight-config">
        <div class="config-description">
          <p>调整各维度在总分计算中的权重比例，所有权重之和应为100%</p>
        </div>
        
        <div class="weight-sliders">
          <div class="weight-slider" v-for="(weight, key) in tempWeights" :key="key">
            <div class="slider-header">
              <span class="dimension-label">{{ getDimensionName(key) }}</span>
              <span class="weight-percentage">{{ (weight * 100).toFixed(0) }}%</span>
            </div>
            <el-slider
              v-model="tempWeights[key]"
              :min="0"
              :max="1"
              :step="0.01"
              :show-tooltip="false"
              @change="validateWeights"
            ></el-slider>
          </div>
        </div>
        
        <div class="weight-summary">
          <span>总权重: </span>
          <span :class="['total-weight', getTotalWeightClass()]">
            {{ getTotalWeight().toFixed(0) }}%
          </span>
          <el-button 
            size="small" 
            type="text" 
            @click="normalizeWeights"
            v-if="getTotalWeight() !== 100"
          >
            自动调整为100%
          </el-button>
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="showWeightConfig = false">取消</el-button>
        <el-button @click="resetWeights">重置</el-button>
        <el-button @click="applyRecommendedWeights" v-if="intelligentScoring.recommendedWeights">应用推荐权重</el-button>
        <el-button type="primary" @click="saveWeights" :disabled="getTotalWeight() !== 100">保存</el-button>
      </div>
    </el-dialog>
    
    <!-- 智能评分配置对话框 -->
    <el-dialog
      title="智能评分配置"
      :visible.sync="showIntelligentConfig"
      width="700px"
    >
      <div class="intelligent-config">
        <!-- 项目类型配置 -->
        <el-card class="config-section">
          <div slot="header">
            <span>项目类型配置</span>
          </div>
          <el-form label-width="120px">
            <el-form-item label="项目类型">
              <el-select v-model="intelligentScoring.projectType" @change="adjustProjectType">
                <el-option label="标准项目" value="standard"></el-option>
                <el-option label="研究项目" value="research"></el-option>
                <el-option label="企业项目" value="enterprise"></el-option>
                <el-option label="开源项目" value="opensource"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="自动权重调整">
              <el-switch v-model="intelligentScoring.autoWeightAdjustment"></el-switch>
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 推荐权重 -->
        <el-card class="config-section" v-if="intelligentScoring.recommendedWeights">
          <div slot="header">
            <span>推荐权重配置</span>
            <el-button size="mini" type="primary" style="float: right" @click="applyRecommendedWeights">应用推荐权重</el-button>
          </div>
          <div class="recommended-weights">
            <div class="weight-comparison" v-for="(weight, key) in intelligentScoring.recommendedWeights" :key="key">
              <div class="dimension-name">{{ getDimensionName(key) }}</div>
              <div class="weight-bars">
                <div class="current-weight">
                  <span class="label">当前:</span>
                  <div class="weight-bar">
                    <div class="bar-fill" :style="{ width: (scoreWeights[key] * 100) + '%' }"></div>
                    <span class="percentage">{{ (scoreWeights[key] * 100).toFixed(0) }}%</span>
                  </div>
                </div>
                <div class="recommended-weight">
                  <span class="label">推荐:</span>
                  <div class="weight-bar recommended">
                    <div class="bar-fill" :style="{ width: (weight * 100) + '%' }"></div>
                    <span class="percentage">{{ (weight * 100).toFixed(0) }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
        
        <!-- 趋势分析 -->
        <el-card class="config-section" v-if="intelligentScoring.trendAnalysis.direction !== 'stable'">
          <div slot="header">
            <span>趋势分析</span>
          </div>
          <div class="trend-analysis">
            <div class="trend-indicator">
              <i :class="getTrendIcon()" :style="{ color: getTrendColor() }"></i>
              <span class="trend-text">{{ getTrendText() }}</span>
              <el-tag :type="getTrendTagType()" size="mini">置信度: {{ intelligentScoring.trendAnalysis.confidence }}%</el-tag>
            </div>
            <div class="trend-factors" v-if="intelligentScoring.trendAnalysis.factors.length > 0">
              <div class="factors-title">影响因素:</div>
              <el-tag v-for="factor in intelligentScoring.trendAnalysis.factors" :key="factor" size="small" style="margin-right: 8px; margin-bottom: 4px;">{{ factor }}</el-tag>
            </div>
          </div>
        </el-card>
        
        <!-- 评分预测 -->
        <el-card class="config-section" v-if="intelligentScoring.scorePredictions.length > 0">
          <div slot="header">
            <span>评分预测 (未来7天)</span>
          </div>
          <div class="score-predictions">
            <div class="prediction-summary">
              <div class="summary-item">
                <span class="label">预期提升成员:</span>
                <span class="value positive">{{ getPredictionSummary().improving }}</span>
              </div>
              <div class="summary-item">
                <span class="label">预期下降成员:</span>
                <span class="value negative">{{ getPredictionSummary().declining }}</span>
              </div>
              <div class="summary-item">
                <span class="label">稳定成员:</span>
                <span class="value stable">{{ getPredictionSummary().stable }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="showIntelligentConfig = false">关闭</el-button>
      </div>
    </el-dialog>

    <!-- 批量评分对话框 -->
    <el-dialog
      title="批量贡献度评分"
      :visible.sync="showBatchScoring"
      width="800px"
      @open="initBatchScoring"
    >
      <div class="batch-scoring">
        <div class="scoring-options">
          <el-form label-width="100px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="评分范围">
                  <el-radio-group v-model="batchScoringOptions.scope">
                    <el-radio label="all">全部成员</el-radio>
                    <el-radio label="selected">选中成员</el-radio>
                    <el-radio label="course">当前课程</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="评分模式">
                  <el-radio-group v-model="batchScoringOptions.mode">
                    <el-radio label="standard">标准评分</el-radio>
                    <el-radio label="relative">相对评分</el-radio>
                    <el-radio label="custom">自定义权重</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <div class="scoring-progress" v-if="batchScoringInProgress">
          <el-progress 
            :percentage="batchScoringProgress" 
            :status="batchScoringStatus"
            :stroke-width="8"
          ></el-progress>
          <p class="progress-text">{{ batchScoringText }}</p>
        </div>

        <div class="scoring-results" v-if="batchScoringResults.length > 0">
          <h4>评分结果预览</h4>
          <el-table :data="batchScoringResults" max-height="300" stripe>
            <el-table-column prop="memberName" label="成员" width="120"></el-table-column>
            <el-table-column prop="previousScore" label="原得分" width="80" align="center">
              <template slot-scope="scope">
                {{ scope.row.previousScore ? scope.row.previousScore.toFixed(1) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="newScore" label="新得分" width="80" align="center">
              <template slot-scope="scope">
                <span :class="getScoreChangeClass(scope.row.newScore - scope.row.previousScore)">
                  {{ scope.row.newScore.toFixed(1) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="scoreChange" label="变化" width="80" align="center">
              <template slot-scope="scope">
                <span :class="getScoreChangeClass(scope.row.scoreChange)">
                  {{ scope.row.scoreChange > 0 ? '+' : '' }}{{ scope.row.scoreChange.toFixed(1) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="grade" label="等级" width="60" align="center">
              <template slot-scope="scope">
                <el-tag :type="getGradeType(scope.row.grade)">{{ scope.row.grade }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="recommendations" label="建议数" width="80" align="center">
              <template slot-scope="scope">
                {{ scope.row.recommendations ? scope.row.recommendations.length : 0 }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="showBatchScoring = false">取消</el-button>
        <el-button 
          type="primary" 
          @click="startBatchScoring" 
          :loading="batchScoringInProgress"
          :disabled="batchScoringInProgress"
        >
          {{ batchScoringInProgress ? '评分中...' : '开始评分' }}
        </el-button>
        <el-button 
          v-if="batchScoringResults.length > 0" 
          type="success" 
          @click="applyBatchScoring"
        >
          应用结果
        </el-button>
        <el-button 
          v-if="batchScoringResults.length > 0" 
          @click="exportBatchResults"
        >
          导出结果
        </el-button>
      </div>
    </el-dialog>
  </div>
</div>
</template>

<script>
import * as echarts from 'echarts'
import NavMenu from '@/components/NavMenu.vue'
import ContributionScoring from '@/components/ContributionScoring.vue'
import { 
  calculateContributionScore, 
  batchCalculateScores, 
  updateScoreRealtime,
  recommendWeights,
  predictScoreTrend,
  adjustWeightsForProject
} from '@/utils/contributionScoring.js'

export default {
  name: 'ContributionAnalytics',
  components: {
    NavMenu,
    ContributionScoring
  },
  data() {
    return {
      loading: false,
      selectedCourse: '',
      analysisType: 'individual',
      timePeriod: '30d',
      customDateRange: [],
      dimensionView: 'radar',
      tableSearch: '',
      courses: [],
      organizationId: null,
      overviewStats: {
        totalMembers: 0,
        memberChange: 0,
        totalCommits: 0,
        commitChange: 0,
        avgScore: 0,
        scoreChange: 0,
        activeDays: 0,
        activeChange: 0
      },
      topContributors: [],
      mostActiveMembers: [],
      tableData: [],
      filteredTableData: [],
      pagination: {
        current: 1,
        pageSize: 20,
        total: 0
      },
      charts: {
        contributionDistribution: null,
        trendAnalysis: null,
        dimensionAnalysis: null,
        activityHeatmap: null,
        skillRadar: null,
        codeQuality: null
      },
      
      // 图表控制变量
      distributionView: 'pie',
      trendPeriod: '30d',
      selectedRadarMember: 'average',
      
      // 多维度评分数据
      scoreWeights: {
        codeContribution: 0.3,
        collaboration: 0.25,
        codeQuality: 0.25,
        activity: 0.15,
        innovation: 0.05
      },
      
      // 对比分析数据
      comparisonData: {
        currentPeriod: null,
        previousPeriod: null,
        benchmark: null
      },
      
      // 评分算法配置
      scoringConfig: {
        activityWeights: {
          commit: 1.0,
          push: 0.8,
          pullRequest: 1.5,
          issue: 1.2,
          codeReview: 1.3,
          branch: 0.6,
          tag: 0.7,
          merge: 1.1,
          release: 2.0
        },
        qualityFactors: {
          testCoverage: 0.3,
          codeComplexity: 0.25,
          documentationRatio: 0.2,
          bugFixRatio: 0.25
        }
      },
      
      // UI控制
      showWeightConfig: false,
      showIntelligentConfig: false,
      tempWeights: {},
      
      // 贡献度评分相关
      selectedMember: null,
      selectedMemberActivities: [],
      showBatchScoring: false,
      batchScoringOptions: {
        scope: 'all',
        mode: 'standard'
      },
      batchScoringInProgress: false,
      batchScoringProgress: 0,
      batchScoringStatus: '',
      batchScoringText: '',
      batchScoringResults: [],
      
      // 新增数据结构
      activityHeatmapData: [],
      skillRadarData: {},
      codeQualityData: {},
      
      // 实时数据更新
      autoRefreshEnabled: false,
      refreshInterval: null,
      lastUpdateTime: null,
      
      // 智能评分功能
      intelligentScoring: {
        enabled: true,
        autoWeightAdjustment: false,
        projectType: 'standard', // standard, research, enterprise, opensource
        recommendedWeights: null,
        scorePredictions: [],
        trendAnalysis: {
          direction: 'stable', // up, down, stable
          confidence: 0,
          factors: []
        }
      },
      
      // 实时评分缓存
      scoreCache: new Map(),
      pendingUpdates: [],
      
      // 高级筛选
      advancedFilters: {
        scoreRange: [0, 100],
        dimensions: [],
        activityTypes: [],
        timeGranularity: 'day' // day, week, month
      },
      
      // 数据导出配置
      exportConfig: {
        format: 'excel', // excel, csv, pdf
        includeCharts: true,
        dateRange: 'current',
        selectedMembers: []
      }
    }
  },
  mounted() {
    this.organizationId = this.getOrganizationId()
    this.loadCourses()
    this.loadAnalytics()
    this.initCharts()
    
    // 初始化智能评分
    this.initIntelligentScoring()
  },
  beforeDestroy() {
    this.disposeCharts()
    
    // 清理实时评分定时器
    if (this.realtimeScoringInterval) {
      clearInterval(this.realtimeScoringInterval)
    }
  },
  watch: {
    dimensionView(newVal) {
      console.log('维度分析视图切换:', newVal)
      this.$nextTick(() => {
        if (this.charts.dimensionAnalysis) {
          this.charts.dimensionAnalysis.clear()
          this.renderDimensionAnalysis()
        }
      })
    }
  },
  methods: {
    // 获取组织ID（从store或localStorage获取）
    getOrganizationId() {
      return this.$store?.state?.user?.organizationId || localStorage.getItem('organizationId') || 1
    },
    
    async loadCourses() {
      console.log('开始加载课程列表，organizationId:', this.organizationId)
      
      if (!this.organizationId) {
        console.warn('组织ID未设置，无法加载课程列表')
        return
      }
      
      try {
        // 注释掉原有的API调用
        // const apiUrl = `/courses?organizationId=${this.organizationId}`
        // console.log('API请求URL:', apiUrl)
        // 
        // const response = await this.$http.get(apiUrl)
        // console.log('API响应原始数据:', response)
        // console.log('响应数据结构:', response.data)
        // 
        // this.courses = response.data.data?.records || response.data.data || []
        
        // 使用模拟数据
        await new Promise(resolve => setTimeout(resolve, 500)) // 模拟网络延迟
        
        this.courses = [
          {
            courseId: 'course-001',
            courseName: 'Java高级编程',
            description: 'Java高级特性与框架应用',
            status: 'active',
            memberCount: 45,
            createTime: '2024-01-15T08:00:00Z'
          },
          {
            courseId: 'course-002',
            courseName: 'Vue.js前端开发',
            description: 'Vue.js框架与现代前端开发技术',
            status: 'active',
            memberCount: 38,
            createTime: '2024-02-01T09:00:00Z'
          },
          {
            courseId: 'course-003',
            courseName: 'Spring Boot微服务',
            description: 'Spring Boot微服务架构设计与实践',
            status: 'active',
            memberCount: 52,
            createTime: '2024-01-20T10:00:00Z'
          },
          {
            courseId: 'course-004',
            courseName: 'Python数据分析',
            description: 'Python在数据科学中的应用',
            status: 'active',
            memberCount: 29,
            createTime: '2024-02-10T11:00:00Z'
          },
          {
            courseId: 'course-005',
            courseName: 'React Native移动开发',
            description: 'React Native跨平台移动应用开发',
            status: 'active',
            memberCount: 33,
            createTime: '2024-01-25T14:00:00Z'
          }
        ]
        
        console.log('解析后的课程列表:', this.courses)
        console.log('课程数量:', this.courses.length)
        
        if (this.courses.length > 0) {
          console.log('第一个课程示例:', this.courses[0])
        } else {
          console.warn('课程列表为空')
        }
      } catch (error) {
        console.error('获取课程列表失败：', error)
        console.error('错误详情:', error.response?.data || error.message)
        this.$message.error('获取课程列表失败')
      }
    },
    
    async loadAnalytics() {
      this.loading = true
      try {
        if (!this.selectedCourse) {
          // 允许显示全部课程的数据，不显示警告
          console.log('显示全部课程的分析数据')
        }
        
        const params = {
          page: this.pagination.current - 1,
          size: this.pagination.pageSize
        }
        
        if (this.customDateRange && this.customDateRange.length === 2) {
          params.startDate = this.customDateRange[0].toISOString().split('T')[0]
          params.endDate = this.customDateRange[1].toISOString().split('T')[0]
        }
        
        // 注释掉原有的API调用
        // const leaderboardResponse = await this.$http.get(`/scores/course/${this.selectedCourse}/leaderboard`, { params })
        // if (leaderboardResponse.data && Array.isArray(leaderboardResponse.data)) {
        //   this.topContributors = leaderboardResponse.data.slice(0, 10).map(score => ({
        //     id: score.memberId,
        //     name: score.memberName || `成员${score.memberId}`,
        //     totalScore: score.totalScore,
        //     commits: score.commitCount || 0,
        //     linesAdded: score.linesAdded || 0,
        //     codeContribution: score.codeContributionScore,
        //     collaborationScore: score.collaborationScore,
        //     qualityScore: score.codeQualityScore,
        //     activityScore: score.activityScore,
        //     innovationScore: score.innovationScore
        //   }))
        //   
        //   // 提取最活跃成员（基于活跃度评分）
        //   this.mostActiveMembers = [...this.topContributors]
        //     .sort((a, b) => b.activityScore - a.activityScore)
        //     .slice(0, 5)
        //     .map(member => ({
        //       id: member.id,
        //       name: member.name,
        //       activeDays: Math.round(member.activityScore * 30 / 100),
        //       avgCommitsPerDay: (member.commits / 30).toFixed(1),
        //       repositories: Math.round(member.activityScore / 10)
        //     }))
        // }
        
        // 使用模拟排行榜数据
        await new Promise(resolve => setTimeout(resolve, 300)) // 模拟网络延迟
        
        this.topContributors = [
          {
            id: 'member-001',
            name: '张三',
            totalScore: 95.8,
            commits: 156,
            linesAdded: 8520,
            codeContribution: 92.5,
            collaborationScore: 88.3,
            qualityScore: 94.2,
            activityScore: 91.7,
            innovationScore: 87.4
          },
          {
            id: 'member-002',
            name: '李四',
            totalScore: 89.3,
            commits: 142,
            linesAdded: 7230,
            codeContribution: 85.6,
            collaborationScore: 92.1,
            qualityScore: 88.9,
            activityScore: 86.5,
            innovationScore: 93.2
          },
          {
            id: 'member-003',
            name: '王五',
            totalScore: 87.1,
            commits: 128,
            linesAdded: 6840,
            codeContribution: 89.3,
            collaborationScore: 84.7,
            qualityScore: 91.5,
            activityScore: 88.2,
            innovationScore: 81.8
          },
          {
            id: 'member-004',
            name: '赵六',
            totalScore: 84.7,
            commits: 119,
            linesAdded: 6150,
            codeContribution: 82.4,
            collaborationScore: 87.9,
            qualityScore: 86.3,
            activityScore: 83.1,
            innovationScore: 84.8
          },
          {
            id: 'member-005',
            name: '孙七',
            totalScore: 82.5,
            commits: 105,
            linesAdded: 5680,
            codeContribution: 80.1,
            collaborationScore: 85.3,
            qualityScore: 83.7,
            activityScore: 81.9,
            innovationScore: 80.5
          }
        ]
        
        // 提取最活跃成员（基于活跃度评分）
        this.mostActiveMembers = [...this.topContributors]
          .sort((a, b) => b.activityScore - a.activityScore)
          .slice(0, 5)
          .map(member => ({
            id: member.id,
            name: member.name,
            activeDays: Math.round(member.activityScore * 30 / 100),
            avgCommitsPerDay: member.commits / 30,
            repositories: Math.round(member.activityScore / 10)
          }))
        
        // 注释掉原有的API调用
        // const allScoresResponse = await this.$http.get(`/scores/course/${this.selectedCourse}/all`, { params })
        // if (allScoresResponse.data && allScoresResponse.data.content) {
        //   this.tableData = allScoresResponse.data.content.map(score => ({
        //     id: score.memberId,
        //     memberName: score.memberName || `成员${score.memberId}`,
        //     totalScore: score.totalScore,
        //     codeContribution: score.codeContributionScore,
        //     collaborationScore: score.collaborationScore,
        //     qualityScore: score.codeQualityScore,
        //     activityScore: score.activityScore,
        //     innovationScore: score.innovationScore,
        //     commits: score.commitCount || 0,
        //     linesAdded: score.linesAdded || 0,
        //     linesDeleted: score.linesDeleted || 0,
        //     activeDays: Math.round(score.activityScore * 30 / 100),
        //     lastActivityDate: score.lastActivityTime || new Date().toISOString()
        //   }))
        //   this.filteredTableData = [...this.tableData]
        //   this.pagination.total = allScoresResponse.data.totalElements || 0
        // }
        
        // 使用模拟表格数据
        this.tableData = [
          {
            id: 'member-001',
            memberName: '张三',
            totalScore: 95.8,
            codeContribution: 92.5,
            collaborationScore: 88.3,
            qualityScore: 94.2,
            activityScore: 91.7,
            innovationScore: 87.4,
            commits: 156,
            linesAdded: 8520,
            linesDeleted: 1240,
            activeDays: 27,
            lastActivityDate: '2024-03-15T14:30:00Z'
          },
          {
            id: 'member-002',
            memberName: '李四',
            totalScore: 89.3,
            codeContribution: 85.6,
            collaborationScore: 92.1,
            qualityScore: 88.9,
            activityScore: 86.5,
            innovationScore: 93.2,
            commits: 142,
            linesAdded: 7230,
            linesDeleted: 980,
            activeDays: 26,
            lastActivityDate: '2024-03-15T16:45:00Z'
          },
          {
            id: 'member-003',
            memberName: '王五',
            totalScore: 87.1,
            codeContribution: 89.3,
            collaborationScore: 84.7,
            qualityScore: 91.5,
            activityScore: 88.2,
            innovationScore: 81.8,
            commits: 128,
            linesAdded: 6840,
            linesDeleted: 1150,
            activeDays: 26,
            lastActivityDate: '2024-03-15T10:20:00Z'
          },
          {
            id: 'member-004',
            memberName: '赵六',
            totalScore: 84.7,
            codeContribution: 82.4,
            collaborationScore: 87.9,
            qualityScore: 86.3,
            activityScore: 83.1,
            innovationScore: 84.8,
            commits: 119,
            linesAdded: 6150,
            linesDeleted: 890,
            activeDays: 25,
            lastActivityDate: '2024-03-14T18:15:00Z'
          },
          {
            id: 'member-005',
            memberName: '孙七',
            totalScore: 82.5,
            codeContribution: 80.1,
            collaborationScore: 85.3,
            qualityScore: 83.7,
            activityScore: 81.9,
            innovationScore: 80.5,
            commits: 105,
            linesAdded: 5680,
            linesDeleted: 720,
            activeDays: 25,
            lastActivityDate: '2024-03-14T15:30:00Z'
          },
          {
            id: 'member-006',
            memberName: '周八',
            totalScore: 79.2,
            codeContribution: 77.8,
            collaborationScore: 81.5,
            qualityScore: 80.9,
            activityScore: 78.3,
            innovationScore: 77.5,
            commits: 98,
            linesAdded: 5120,
            linesDeleted: 650,
            activeDays: 23,
            lastActivityDate: '2024-03-13T12:45:00Z'
          },
          {
            id: 'member-007',
            memberName: '吴九',
            totalScore: 76.8,
            codeContribution: 75.2,
            collaborationScore: 78.9,
            qualityScore: 79.1,
            activityScore: 75.6,
            innovationScore: 75.2,
            commits: 89,
            linesAdded: 4680,
            linesDeleted: 580,
            activeDays: 22,
            lastActivityDate: '2024-03-13T09:20:00Z'
          },
          {
            id: 'member-008',
            memberName: '郑十',
            totalScore: 74.5,
            codeContribution: 72.6,
            collaborationScore: 76.8,
            qualityScore: 76.3,
            activityScore: 73.2,
            innovationScore: 73.6,
            commits: 82,
            linesAdded: 4250,
            linesDeleted: 520,
            activeDays: 22,
            lastActivityDate: '2024-03-12T16:10:00Z'
          }
        ]
        this.filteredTableData = [...this.tableData]
        this.pagination.total = this.tableData.length
        
        // 加载课程统计信息
        await this.loadCourseStatistics()
        
        // 加载多维度评分数据
        await this.loadScoreAnalysis()
        
        // 渲染图表
        this.renderCharts()
        
      } catch (error) {
        console.error('加载分析数据失败:', error)
        this.loadMockData()
      } finally {
        this.loading = false
      }
    },
    
    async loadCourseStatistics() {
      try {
        // 检查是否有选中的课程
        if (!this.selectedCourse) {
          console.warn('未选择课程，跳过统计信息加载')
          return
        }
        
        // 模拟网络延迟
        await new Promise(resolve => setTimeout(resolve, 300))
        
        // 注释掉原有的API调用
        // const statsResponse = await this.$http.get(`/scores/course/${this.selectedCourse}/statistics`)
        // if (statsResponse.data) {
        //   const stats = statsResponse.data
        //   this.overviewStats = {
        //     totalMembers: stats.totalMembers || 0,
        //     memberChange: stats.memberGrowthRate || 0,
        //     totalCommits: stats.totalCommits || 0,
        //     commitChange: stats.commitGrowthRate || 0,
        //     avgScore: stats.averageScore || 0,
        //     scoreChange: stats.scoreGrowthRate || 0,
        //     activeDays: stats.activeDays || 0,
        //     activeChange: stats.activityGrowthRate || 0
        //   }
        // }
        
        // 使用模拟统计数据
        this.overviewStats = {
          totalMembers: 28,
          memberChange: 12.5,
          totalCommits: 1456,
          commitChange: 8.3,
          avgScore: 78.6,
          scoreChange: 5.2,
          activeDays: 25,
          activeChange: -2.1
        }
        
        console.log('课程统计数据加载完成:', this.overviewStats)
        
      } catch (error) {
        console.error('加载课程统计失败:', error)
        // 使用默认统计数据
        this.overviewStats = {
          totalMembers: this.tableData.length,
          memberChange: 0,
          totalCommits: this.tableData.reduce((sum, member) => sum + member.commits, 0),
          commitChange: 0,
          avgScore: this.tableData.length > 0 ? this.tableData.reduce((sum, member) => sum + member.totalScore, 0) / this.tableData.length : 0,
          scoreChange: 0,
          activeDays: 30,
          activeChange: 0
        }
      }
    },
    
    async loadScoreAnalysis() {
      try {
        // 基于已加载的评分数据进行分析
        if (this.tableData.length > 0) {
          this.comparisonData = this.calculateComparisonData()
        } else {
          this.loadMockScoreData()
        }
        
      } catch (error) {
        console.error('加载评分分析数据失败:', error)
        this.loadMockScoreData()
      }
    },
    
    calculateComparisonData() {
      // 基于已加载的评分数据计算对比分析
      if (this.tableData.length === 0) {
        return this.loadMockScoreData()
      }
      
      const dimensions = ['codeContribution', 'collaboration', 'codeQuality', 'activity', 'innovation']
      const currentPeriod = {}
      const previousPeriod = {}
      const benchmark = {
        avgCodeContribution: 80.0,
        avgCollaboration: 75.0,
        avgCodeQuality: 85.0,
        avgActivity: 70.0,
        avgInnovation: 65.0
      }
      
      dimensions.forEach(dim => {
        const scores = this.tableData.map(member => member[dim] || 0)
        const avgScore = scores.reduce((sum, score) => sum + score, 0) / scores.length
        
        currentPeriod[`avg${dim.charAt(0).toUpperCase() + dim.slice(1)}`] = Math.round(avgScore * 10) / 10
        // 模拟历史数据（当前分数的90%-110%）
        previousPeriod[`avg${dim.charAt(0).toUpperCase() + dim.slice(1)}`] = Math.round(avgScore * (0.9 + Math.random() * 0.2) * 10) / 10
      })
      
      this.comparisonData = {
        currentPeriod,
        previousPeriod,
        benchmark
      }
      
      return this.comparisonData
    },
    
    loadMockScoreData() {
      // 模拟对比分析数据
      this.comparisonData = {
        currentPeriod: {
          avgCodeContribution: 78.5,
          avgCollaboration: 72.3,
          avgCodeQuality: 85.2,
          avgActivity: 68.9,
          avgInnovation: 65.4
        },
        previousPeriod: {
          avgCodeContribution: 75.2,
          avgCollaboration: 69.8,
          avgCodeQuality: 82.1,
          avgActivity: 71.2,
          avgInnovation: 62.3
        },
        benchmark: {
          avgCodeContribution: 80.0,
          avgCollaboration: 75.0,
          avgCodeQuality: 85.0,
          avgActivity: 70.0,
          avgInnovation: 65.0
        }
      }
      return this.comparisonData
    },
    
    calculateMemberScore(memberData) {
      // 基于多维度评分算法计算成员总分
      const scores = {
        codeContribution: this.calculateCodeContributionScore(memberData),
        collaboration: this.calculateCollaborationScore(memberData),
        codeQuality: this.calculateCodeQualityScore(memberData),
        activity: this.calculateActivityScore(memberData),
        innovation: this.calculateInnovationScore(memberData)
      }
      
      // 加权计算总分
      let totalScore = 0
      Object.keys(scores).forEach(dimension => {
        totalScore += scores[dimension] * this.scoreWeights[dimension]
      })
      
      return {
        totalScore: Math.round(totalScore * 100) / 100,
        dimensionScores: scores
      }
    },
    
    calculateCodeContributionScore(memberData) {
      // 代码贡献度评分：基于提交数、代码行数、文件修改数等
      const commitScore = Math.min(memberData.commits / 100 * 100, 100)
      const linesScore = Math.min((memberData.linesAdded + memberData.linesDeleted) / 5000 * 100, 100)
      const filesScore = Math.min(memberData.filesChanged / 200 * 100, 100)
      
      return (commitScore * 0.4 + linesScore * 0.4 + filesScore * 0.2)
    },
    
    calculateCollaborationScore(memberData) {
      // 协作能力评分：基于PR、代码审查、Issue参与等
      const prScore = Math.min(memberData.pullRequests / 20 * 100, 100)
      const reviewScore = Math.min(memberData.codeReviews / 30 * 100, 100)
      const issueScore = Math.min(memberData.issues / 15 * 100, 100)
      
      return (prScore * 0.4 + reviewScore * 0.4 + issueScore * 0.2)
    },
    
    calculateCodeQualityScore(memberData) {
      // 代码质量评分：基于测试覆盖率、复杂度、文档等
      const testScore = memberData.testCoverage || 70
      const complexityScore = Math.max(100 - (memberData.codeComplexity || 10), 0)
      const docScore = memberData.documentationRatio || 60
      const bugScore = Math.max(100 - (memberData.bugCount || 5) * 10, 0)
      
      return (testScore * 0.3 + complexityScore * 0.25 + docScore * 0.2 + bugScore * 0.25)
    },
    
    calculateActivityScore(memberData) {
      // 活跃度评分：基于活跃天数、提交频率等
      const activeDaysScore = Math.min(memberData.activeDays / 30 * 100, 100)
      const frequencyScore = Math.min(memberData.avgCommitsPerDay / 5 * 100, 100)
      const repoScore = Math.min(memberData.repositories / 10 * 100, 100)
      
      return (activeDaysScore * 0.5 + frequencyScore * 0.3 + repoScore * 0.2)
    },
    
    calculateInnovationScore(memberData) {
      // 创新性评分：基于新功能、技术栈使用等
      const newFeaturesScore = Math.min((memberData.newFeatures || 0) / 5 * 100, 100)
      const techStackScore = Math.min((memberData.techStackDiversity || 0) / 10 * 100, 100)
      const experimentScore = Math.min((memberData.experimentalCommits || 0) / 20 * 100, 100)
      
      return (newFeaturesScore * 0.4 + techStackScore * 0.3 + experimentScore * 0.3)
    },
    
    // 智能评分相关方法
    async initIntelligentScoring() {
      try {
        // 获取推荐权重
        await this.loadRecommendedWeights()
        
        // 初始化实时评分
        if (this.intelligentScoring.enabled) {
          this.startRealtimeScoring()
        }
        
        // 加载评分趋势预测
        await this.loadScorePredictions()
        
      } catch (error) {
        console.error('初始化智能评分失败:', error)
      }
    },
    
    async loadRecommendedWeights() {
      try {
        // 验证课程是否已选择
        if (!this.selectedCourse) {
          console.warn('未选择课程，跳过推荐权重加载')
          return
        }
        
        const projectData = {
          type: this.intelligentScoring.projectType,
          memberCount: this.overviewStats.totalMembers,
          avgActivity: this.overviewStats.avgScore,
          timeRange: this.timePeriod
        }
        
        // 注释掉原有的API调用
        // const response = await this.$http.post(`/scores/course/${this.selectedCourse}/recommend-weights`, projectData)
        // if (response.data) {
        //   this.intelligentScoring.recommendedWeights = response.data
        //   
        //   // 如果启用自动权重调整，应用推荐权重
        //   if (this.intelligentScoring.autoWeightAdjustment) {
        //     this.scoreWeights = { ...this.scoreWeights, ...response.data }
        //     this.$message.success('已自动应用推荐权重配置')
        //   }
        // } else {
        //   // 降级到本地算法
        //   const recommendedWeights = recommendWeights(projectData)
        //   this.intelligentScoring.recommendedWeights = recommendedWeights
        //   
        //   if (this.intelligentScoring.autoWeightAdjustment) {
        //     this.scoreWeights = { ...this.scoreWeights, ...recommendedWeights }
        //     this.$message.success('已自动应用推荐权重配置')
        //   }
        // }
        
        // 使用模拟推荐权重数据
        const mockRecommendedWeights = {
          codeContribution: 0.3,
          collaboration: 0.25,
          codeQuality: 0.2,
          activity: 0.15,
          innovation: 0.1
        }
        
        this.intelligentScoring.recommendedWeights = mockRecommendedWeights
        
        if (this.intelligentScoring.autoWeightAdjustment) {
          this.scoreWeights = { ...this.scoreWeights, ...mockRecommendedWeights }
          this.$message.success('已自动应用推荐权重配置')
        }
        
      } catch (error) {
        console.error('加载推荐权重失败:', error)
        // 降级到本地算法
        const projectData = {
          type: this.intelligentScoring.projectType,
          memberCount: this.overviewStats.totalMembers,
          avgActivity: this.overviewStats.avgScore,
          timeRange: this.timePeriod
        }
        const recommendedWeights = recommendWeights(projectData)
        this.intelligentScoring.recommendedWeights = recommendedWeights
      }
    },
    
    startRealtimeScoring() {
      // 启动实时评分更新
      this.realtimeScoringInterval = setInterval(() => {
        this.updateRealtimeScores()
      }, 30000) // 每30秒更新一次
    },
    
    async updateRealtimeScores() {
      try {
        if (this.pendingUpdates.length === 0) return
        
        // 检查是否有选中的课程
        if (!this.selectedCourse) {
          console.warn('未选择课程，跳过实时评分更新')
          return
        }
        
        const updates = [...this.pendingUpdates]
        this.pendingUpdates = []
        
        // 注释掉原有的API调用
        // const updateRequests = updates.map(update => ({
        //   memberId: update.memberId,
        //   activities: update.activities,
        //   weights: this.scoreWeights
        // }))
        // 
        // const response = await this.$http.post(`/scores/course/${this.selectedCourse}/realtime-update`, {
        //   updates: updateRequests
        // })
        
        // 模拟网络延迟
        await new Promise(resolve => setTimeout(resolve, 200))
        
        // 使用模拟响应数据
        const response = {
          data: updates.map(update => ({
            memberId: update.memberId,
            totalScore: 75 + Math.random() * 25,
            codeContributionScore: 70 + Math.random() * 30,
            collaborationScore: 65 + Math.random() * 35,
            codeQualityScore: 80 + Math.random() * 20,
            activityScore: 60 + Math.random() * 40,
            innovationScore: 55 + Math.random() * 45
          }))
        }
        
        if (response.data && Array.isArray(response.data)) {
          // 处理后端返回的评分结果
          response.data.forEach(scoreResult => {
            // 更新缓存
            this.scoreCache.set(scoreResult.memberId, {
              score: scoreResult,
              timestamp: Date.now()
            })
            
            // 更新表格数据
            const memberIndex = this.tableData.findIndex(m => m.id === scoreResult.memberId)
            if (memberIndex !== -1) {
              this.$set(this.tableData, memberIndex, {
                ...this.tableData[memberIndex],
                totalScore: scoreResult.totalScore,
                codeContribution: scoreResult.codeContributionScore,
                collaborationScore: scoreResult.collaborationScore,
                qualityScore: scoreResult.codeQualityScore,
                activityScore: scoreResult.activityScore,
                innovationScore: scoreResult.innovationScore
              })
            }
          })
        } else {
          // 降级到本地计算
          for (const update of updates) {
            const newScore = await updateScoreRealtime(
              update.memberId,
              update.activities,
              this.scoreWeights
            )
            
            // 更新缓存
            this.scoreCache.set(update.memberId, {
              score: newScore,
              timestamp: Date.now()
            })
            
            // 更新表格数据
            const memberIndex = this.tableData.findIndex(m => m.id === update.memberId)
            if (memberIndex !== -1) {
              this.$set(this.tableData, memberIndex, {
                ...this.tableData[memberIndex],
                totalScore: newScore.totalScore,
                ...newScore.dimensionScores
              })
            }
          }
        }
        
        // 重新渲染相关图表
        this.renderDistributionChart()
        this.renderDimensionAnalysis()
        
      } catch (error) {
        console.error('实时评分更新失败:', error)
        // 降级到本地计算
        const updates = [...this.pendingUpdates]
        this.pendingUpdates = []
        
        for (const update of updates) {
          const newScore = await updateScoreRealtime(
            update.memberId,
            update.activities,
            this.scoreWeights
          )
          
          // 更新缓存
          this.scoreCache.set(update.memberId, {
            score: newScore,
            timestamp: Date.now()
          })
          
          // 更新表格数据
          const memberIndex = this.tableData.findIndex(m => m.id === update.memberId)
          if (memberIndex !== -1) {
            this.$set(this.tableData, memberIndex, {
              ...this.tableData[memberIndex],
              totalScore: newScore.totalScore,
              ...newScore.dimensionScores
            })
          }
        }
      }
    },
    
    async loadScorePredictions() {
      try {
        // 检查是否有选中的课程
        if (!this.selectedCourse) {
          console.warn('未选择课程，跳过评分预测加载')
          return
        }
        
        // 注释掉原有的API调用
        // const response = await this.$http.get(`/scores/course/${this.selectedCourse}/predictions`, {
        //   params: {
        //     days: 7,
        //     includeFactors: true
        //   }
        // })
        // 
        // if (response.data && Array.isArray(response.data)) {
        //   this.intelligentScoring.scorePredictions = response.data
        //   
        //   // 分析趋势
        //   this.analyzeTrends(response.data)
        // } else {
        //   // 降级到本地预测算法
        //   const historicalData = this.tableData.map(member => ({
        //     memberId: member.id,
        //     scores: [member.totalScore], // 简化，实际应包含历史评分
        //     activities: member.activities || []
        //   }))
        //   
        //   const predictions = predictScoreTrend(historicalData, 7) // 预测7天
        //   this.intelligentScoring.scorePredictions = predictions
        
        // 模拟网络延迟
        await new Promise(resolve => setTimeout(resolve, 300))
        
        // 使用模拟预测数据
        const mockPredictions = [
          {
            memberId: 1,
            predictedScores: [78.5, 79.2, 80.1, 81.0, 82.3, 83.1, 84.0],
            trend: 'increasing',
            confidence: 0.85
          },
          {
            memberId: 2,
            predictedScores: [85.2, 84.8, 84.5, 84.0, 83.8, 83.5, 83.2],
            trend: 'decreasing',
            confidence: 0.78
          },
          {
            memberId: 3,
            predictedScores: [72.1, 72.5, 72.8, 73.2, 73.6, 74.0, 74.5],
            trend: 'stable',
            confidence: 0.92
          }
        ]
        
        this.intelligentScoring.scorePredictions = mockPredictions
        
        // 分析趋势
        this.analyzeTrends(mockPredictions)
          
          // 分析趋势
          this.analyzeTrends(predictions)
        
      } catch (error) {
        console.error('加载评分预测失败:', error)
        // 降级到本地预测算法
        const historicalData = this.tableData.map(member => ({
          memberId: member.id,
          scores: [member.totalScore],
          activities: member.activities || []
        }))
        
        const predictions = predictScoreTrend(historicalData, 7)
        this.intelligentScoring.scorePredictions = predictions
        this.analyzeTrends(predictions)
      }
    },
    
    analyzeTrends(predictions) {
      if (!predictions || predictions.length === 0) return
      
      const avgTrend = predictions.reduce((sum, p) => sum + p.trend, 0) / predictions.length
      const confidence = predictions.reduce((sum, p) => sum + p.confidence, 0) / predictions.length
      
      let direction = 'stable'
      if (avgTrend > 0.1) direction = 'up'
      else if (avgTrend < -0.1) direction = 'down'
      
      this.intelligentScoring.trendAnalysis = {
        direction,
        confidence: Math.round(confidence * 100),
        factors: this.identifyTrendFactors(predictions)
      }
    },
    
    identifyTrendFactors(predictions) {
      const factors = []
      
      // 分析影响因素
      const improvingMembers = predictions.filter(p => p.trend > 0).length
      const decliningMembers = predictions.filter(p => p.trend < 0).length
      
      if (improvingMembers > decliningMembers) {
        factors.push('团队整体表现提升')
      } else if (decliningMembers > improvingMembers) {
        factors.push('部分成员活跃度下降')
      }
      
      // 可以添加更多因素分析逻辑
      
      return factors
    },
    
    // 权重配置相关方法
    applyRecommendedWeights() {
      if (this.intelligentScoring.recommendedWeights) {
        this.scoreWeights = { ...this.scoreWeights, ...this.intelligentScoring.recommendedWeights }
        this.tempWeights = { ...this.scoreWeights }
        this.$message.success('已应用推荐权重配置')
        
        // 重新计算评分
        this.recalculateAllScores()
      }
    },
    
    async recalculateAllScores() {
      try {
        // 检查是否有选中的课程
        if (!this.selectedCourse) {
          console.warn('未选择课程，跳过评分重新计算')
          this.$message.warning('请先选择课程')
          return
        }
        
        this.loading = true
        
        // 调用后端批量重新计算评分接口
        const response = await this.$http.post(`/scores/course/${this.selectedCourse}/recalculate`, {
          weights: this.scoreWeights,
          memberIds: this.tableData.map(member => member.id)
        })
        
        if (response.data && Array.isArray(response.data)) {
          // 更新表格数据
          response.data.forEach(scoreResult => {
            const memberIndex = this.tableData.findIndex(m => m.id === scoreResult.memberId)
            if (memberIndex !== -1) {
              this.$set(this.tableData, memberIndex, {
                ...this.tableData[memberIndex],
                totalScore: scoreResult.totalScore,
                codeContribution: scoreResult.codeContributionScore,
                collaborationScore: scoreResult.collaborationScore,
                qualityScore: scoreResult.codeQualityScore,
                activityScore: scoreResult.activityScore,
                innovationScore: scoreResult.innovationScore
              })
            }
          })
        } else {
          // 降级到本地计算
          for (let i = 0; i < this.tableData.length; i++) {
            const member = this.tableData[i]
            const newScore = this.calculateMemberScore(member)
            
            this.$set(this.tableData, i, {
              ...member,
              totalScore: newScore.totalScore,
              codeContribution: newScore.dimensionScores.codeContribution,
              collaborationScore: newScore.dimensionScores.collaboration,
              qualityScore: newScore.dimensionScores.codeQuality,
              activityScore: newScore.dimensionScores.activity,
              innovationScore: newScore.dimensionScores.innovation
            })
          }
        }
        
        // 更新排序
        this.tableData.sort((a, b) => b.totalScore - a.totalScore)
        this.filteredTableData = [...this.tableData]
        
        // 重新渲染图表
        this.renderCharts()
        
        this.$message.success('评分重新计算完成')
        
      } catch (error) {
        console.error('重新计算评分失败:', error)
        this.$message.error('评分计算失败')
        
        // 降级到本地计算
        for (let i = 0; i < this.tableData.length; i++) {
          const member = this.tableData[i]
          const newScore = this.calculateMemberScore(member)
          
          this.$set(this.tableData, i, {
            ...member,
            totalScore: newScore.totalScore,
            codeContribution: newScore.dimensionScores.codeContribution,
            collaborationScore: newScore.dimensionScores.collaboration,
            qualityScore: newScore.dimensionScores.codeQuality,
            activityScore: newScore.dimensionScores.activity,
            innovationScore: newScore.dimensionScores.innovation
          })
        }
        
        this.tableData.sort((a, b) => b.totalScore - a.totalScore)
        this.filteredTableData = [...this.tableData]
        this.renderCharts()
      } finally {
        this.loading = false
      }
    },
    
    // 项目类型调整
    async adjustProjectType(projectType) {
      try {
        // 检查是否有选中的课程
        if (!this.selectedCourse) {
          console.warn('未选择课程，跳过项目类型调整')
          this.$message.warning('请先选择课程')
          return
        }
        
        this.intelligentScoring.projectType = projectType
        
        // 调用后端项目类型调整接口
        const response = await this.$http.post(`/scores/course/${this.selectedCourse}/adjust-project-type`, {
          projectType: projectType,
          currentWeights: this.scoreWeights
        })
        
        if (response.data) {
          this.scoreWeights = response.data
          this.tempWeights = { ...response.data }
        } else {
          // 降级到本地调整
          const adjustedWeights = adjustWeightsForProject(this.scoreWeights, projectType)
          this.scoreWeights = adjustedWeights
          this.tempWeights = { ...adjustedWeights }
        }
        
        // 重新加载推荐权重
        await this.loadRecommendedWeights()
        
        this.$message.success(`已切换到${projectType}项目模式`)
        
      } catch (error) {
        console.error('调整项目类型失败:', error)
        // 降级到本地调整
        const adjustedWeights = adjustWeightsForProject(this.scoreWeights, projectType)
        this.scoreWeights = adjustedWeights
        this.tempWeights = { ...adjustedWeights }
        
        await this.loadRecommendedWeights()
        this.$message.success(`已切换到${projectType}项目模式`)
      }
    },
    
    // 获取维度名称
    getDimensionName(key) {
      const names = {
        codeContribution: '代码贡献',
        collaboration: '协作能力',
        codeQuality: '代码质量',
        activity: '活跃度',
        innovation: '创新性'
      }
      return names[key] || key
    },
    
    // 获取趋势图标
    getTrendIcon() {
      const direction = this.intelligentScoring.trendAnalysis.direction
      switch (direction) {
        case 'up': return 'el-icon-top'
        case 'down': return 'el-icon-bottom'
        default: return 'el-icon-minus'
      }
    },
    
    // 获取趋势颜色
    getTrendColor() {
      const direction = this.intelligentScoring.trendAnalysis.direction
      switch (direction) {
        case 'up': return '#67C23A'
        case 'down': return '#F56C6C'
        default: return '#909399'
      }
    },
    
    // 获取趋势文本
    getTrendText() {
      const direction = this.intelligentScoring.trendAnalysis.direction
      switch (direction) {
        case 'up': return '整体评分呈上升趋势'
        case 'down': return '整体评分呈下降趋势'
        default: return '整体评分保持稳定'
      }
    },
    
    // 获取趋势标签类型
    getTrendTagType() {
      const confidence = this.intelligentScoring.trendAnalysis.confidence
      if (confidence >= 80) return 'success'
      if (confidence >= 60) return 'warning'
      return 'info'
    },
    
    // 获取预测摘要
    getPredictionSummary() {
      const predictions = this.intelligentScoring.scorePredictions
      const summary = {
        improving: 0,
        declining: 0,
        stable: 0
      }
      
      predictions.forEach(prediction => {
        if (prediction.trend > 0.1) {
          summary.improving++
        } else if (prediction.trend < -0.1) {
          summary.declining++
        } else {
          summary.stable++
        }
      })
      
      return summary
    },
    
    // 打开智能评分配置
    openIntelligentConfig() {
      this.showIntelligentConfig = true
    },
    
    // 关闭智能评分配置
    closeIntelligentConfig() {
      this.showIntelligentConfig = false
    },
    
    loadMockData() {
      // 模拟数据
      this.overviewStats = {
        totalMembers: 45,
        memberChange: 12.5,
        totalCommits: 1234,
        commitChange: 8.3,
        avgScore: 78.5,
        scoreChange: 5.2,
        activeDays: 28,
        activeChange: -2.1
      }
      
      this.topContributors = [
        { id: 1, name: '张三', totalScore: 95.5, commits: 156, linesAdded: 3245 },
        { id: 2, name: '李四', totalScore: 89.2, commits: 134, linesAdded: 2876 },
        { id: 3, name: '王五', totalScore: 85.7, commits: 128, linesAdded: 2654 }
      ]
      
      this.mostActiveMembers = [
        { id: 1, name: '张三', activeDays: 28, avgCommitsPerDay: 5.6, repositories: 8 },
        { id: 2, name: '李四', activeDays: 26, avgCommitsPerDay: 5.2, repositories: 6 },
        { id: 3, name: '王五', activeDays: 25, avgCommitsPerDay: 5.1, repositories: 7 }
      ]
      
      this.tableData = [
        {
          memberId: 1,
          memberName: '张三',
          totalScore: 95.5,
          codeContribution: 32.5,
          collaborationScore: 28.0,
          qualityScore: 35.0,
          commits: 156,
          linesAdded: 3245,
          linesDeleted: 567,
          activeDays: 28,
          lastActivityDate: '2024-01-15T10:30:00Z'
        }
      ]
      this.filteredTableData = [...this.tableData]
      this.pagination.total = this.tableData.length
      
      this.loadMockScoreData()
      this.renderCharts()
    },
    
    initCharts() {
      this.$nextTick(() => {
        this.charts.contributionDistribution = echarts.init(document.getElementById('contributionDistributionChart'))
        this.charts.trendAnalysis = echarts.init(document.getElementById('trendAnalysisChart'))
        this.charts.dimensionAnalysis = echarts.init(document.getElementById('dimensionAnalysisChart'))
        this.charts.activityHeatmap = echarts.init(document.getElementById('activityHeatmapChart'))
        this.charts.skillRadar = echarts.init(document.getElementById('skillRadarChart'))
        this.charts.codeQuality = echarts.init(document.getElementById('codeQualityChart'))
        
        // 监听窗口大小变化
        window.addEventListener('resize', this.handleResize)
        
        // 添加图表交互事件
        this.addChartInteractions()
      })
    },
    
    disposeCharts() {
      Object.values(this.charts).forEach(chart => {
        if (chart) chart.dispose()
      })
      window.removeEventListener('resize', this.handleResize)
      
      // 清除自动刷新
      if (this.refreshInterval) {
        clearInterval(this.refreshInterval)
      }
    },
    
    renderCharts() {
      this.renderContributionDistribution()
      this.renderTrendAnalysis()
      this.renderDimensionAnalysis()
      this.renderActivityHeatmap()
      this.renderSkillRadar()
      this.renderCodeQuality()
    },
    
    // 新增图表渲染方法
    renderActivityHeatmap() {
      if (!this.charts.activityHeatmap) return
      
      const option = {
        title: {
          text: '活动热力图',
          left: 'center'
        },
        tooltip: {
          position: 'top',
          formatter: function (params) {
            return `${params.value[0]}日 ${params.value[1]}时: ${params.value[2]}次活动`
          }
        },
        visualMap: {
          min: 0,
          max: 20,
          calculable: true,
          orient: 'horizontal',
          left: 'center',
          bottom: '10%'
        },
        calendar: {
          top: 120,
          left: 30,
          right: 30,
          cellSize: ['auto', 13],
          range: '2024',
          itemStyle: {
            borderWidth: 0.5
          },
          yearLabel: { show: false }
        },
        series: [{
          type: 'heatmap',
          coordinateSystem: 'calendar',
          data: this.generateHeatmapData()
        }]
      }
      
      this.charts.activityHeatmap.setOption(option)
    },
    
    renderSkillRadar() {
      if (!this.charts.skillRadar) return
      
      const selectedMember = this.tableData.find(m => m.memberId === this.selectedRadarMember) || this.tableData[0]
      
      const option = {
        title: {
          text: `技能雷达图 - ${selectedMember?.memberName || '未选择'}`,
          left: 'center'
        },
        tooltip: {},
        legend: {
          data: ['当前成员', '团队平均'],
          bottom: 10
        },
        radar: {
          indicator: [
            { name: '代码贡献', max: 100 },
            { name: '协作能力', max: 100 },
            { name: '代码质量', max: 100 },
            { name: '活跃度', max: 100 },
            { name: '创新性', max: 100 }
          ]
        },
        series: [{
          name: '技能评分',
          type: 'radar',
          data: [
            {
              value: [
                selectedMember?.codeContribution || 0,
                selectedMember?.collaborationScore || 0,
                selectedMember?.qualityScore || 0,
                selectedMember?.activityScore || 0,
                selectedMember?.innovationScore || 0
              ],
              name: '当前成员'
            },
            {
              value: [75, 70, 80, 65, 60],
              name: '团队平均'
            }
          ]
        }]
      }
      
      this.charts.skillRadar.setOption(option)
    },
    
    renderCodeQuality() {
      if (!this.charts.codeQuality) return
      
      const option = {
        title: {
          text: '代码质量分析',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        legend: {
          data: ['测试覆盖率', '代码复杂度', '文档完整度', 'Bug密度'],
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
          data: this.tableData.slice(0, 10).map(item => item.memberName)
        },
        yAxis: {
          type: 'value',
          max: 100
        },
        series: [
          {
            name: '测试覆盖率',
            type: 'bar',
            data: this.tableData.slice(0, 10).map(() => Math.floor(Math.random() * 40) + 60)
          },
          {
            name: '代码复杂度',
            type: 'bar',
            data: this.tableData.slice(0, 10).map(() => Math.floor(Math.random() * 30) + 70)
          },
          {
            name: '文档完整度',
            type: 'bar',
            data: this.tableData.slice(0, 10).map(() => Math.floor(Math.random() * 35) + 55)
          },
          {
            name: 'Bug密度',
            type: 'bar',
            data: this.tableData.slice(0, 10).map(() => Math.floor(Math.random() * 25) + 75)
          }
        ]
      }
      
      this.charts.codeQuality.setOption(option)
     },
     
     // 辅助方法
     generateHeatmapData() {
       const data = []
       const startDate = new Date('2024-01-01')
       const endDate = new Date('2024-12-31')
       
       for (let d = new Date(startDate); d <= endDate; d.setDate(d.getDate() + 1)) {
         const dateStr = d.toISOString().split('T')[0]
         const activity = Math.floor(Math.random() * 20)
         data.push([dateStr, activity])
       }
       
       return data
     },
     
     addChartInteractions() {
       // 为图表添加交互事件
       if (this.charts.contributionDistribution) {
         this.charts.contributionDistribution.on('click', (params) => {
           this.filterByScoreRange(params.name)
         })
       }
       
       if (this.charts.skillRadar) {
         this.charts.skillRadar.on('click', (params) => {
           this.showMemberDetail(this.selectedRadarMember)
         })
       }
     },
     
     handleResize() {
       Object.values(this.charts).forEach(chart => {
         if (chart) chart.resize()
       })
     },
     
     // 图表交互方法
     changeDistributionView(view) {
       this.distributionView = view
       this.renderContributionDistribution()
     },
     
     changeTrendPeriod(period) {
       this.trendPeriod = period
       this.renderTrendAnalysis()
     },
     
     changeRadarMember(memberId) {
       this.selectedRadarMember = memberId
       this.renderSkillRadar()
     },
     
     filterByScoreRange(rangeName) {
       // 根据分数范围筛选数据
       let minScore = 0, maxScore = 100
       
       if (rangeName.includes('90-100')) {
         minScore = 90
         maxScore = 100
       } else if (rangeName.includes('80-89')) {
         minScore = 80
         maxScore = 89
       } else if (rangeName.includes('70-79')) {
         minScore = 70
         maxScore = 79
       } else if (rangeName.includes('60-69')) {
         minScore = 60
         maxScore = 69
       }
       
       this.filteredTableData = this.tableData.filter(item => 
         item.totalScore >= minScore && item.totalScore <= maxScore
       )
       this.pagination.total = this.filteredTableData.length
       this.pagination.currentPage = 1
     },
     
     showMemberDetail(memberId) {
       const member = this.tableData.find(m => m.memberId === memberId)
       if (member) {
         this.$message.info(`查看 ${member.memberName} 的详细信息`)
         // 这里可以跳转到成员详情页面或打开详情对话框
       }
     },
     
     // 实时数据更新
     startAutoRefresh() {
       if (this.autoRefreshEnabled && !this.refreshInterval) {
         this.refreshInterval = setInterval(() => {
           this.refreshData()
         }, 30000) // 30秒刷新一次
       }
     },
     
     stopAutoRefresh() {
       if (this.refreshInterval) {
         clearInterval(this.refreshInterval)
         this.refreshInterval = null
       }
     },
     
     toggleAutoRefresh() {
       this.autoRefreshEnabled = !this.autoRefreshEnabled
       if (this.autoRefreshEnabled) {
         this.startAutoRefresh()
       } else {
         this.stopAutoRefresh()
       }
     },
    
    renderContributionDistribution() {
      const data = [
        { value: 35, name: '优秀 (90-100分)' },
        { value: 40, name: '良好 (80-89分)' },
        { value: 20, name: '中等 (70-79分)' },
        { value: 5, name: '待改进 (60-69分)' }
      ]
      
      let option
      
      if (this.distributionView === 'pie') {
        option = {
          tooltip: {
            trigger: 'item',
            formatter: '{a} <br/>{b}: {c}% ({d}%)'
          },
          legend: {
            orient: 'vertical',
            left: 'left'
          },
          series: [{
            name: '贡献度分布',
            type: 'pie',
            radius: '70%',
            center: ['60%', '50%'],
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
      } else {
        option = {
          tooltip: {
            trigger: 'axis',
            axisPointer: {
              type: 'shadow'
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
            data: data.map(item => item.name)
          },
          yAxis: {
            type: 'value',
            name: '人数占比(%)'
          },
          series: [{
            name: '贡献度分布',
            type: 'bar',
            data: data.map(item => item.value),
            itemStyle: {
              color: function(params) {
                const colors = ['#5470c6', '#91cc75', '#fac858', '#ee6666']
                return colors[params.dataIndex]
              }
            },
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }]
        }
      }
      
      this.charts.contributionDistribution.setOption(option)
    },
    
    renderTrendAnalysis() {
      const days = this.trendPeriod === '7d' ? 7 : this.trendPeriod === '30d' ? 30 : 90
      
      const dates = Array.from({length: days}, (_, i) => {
        const date = new Date()
        date.setDate(date.getDate() - days + 1 + i)
        return date.toISOString().split('T')[0]
      })
      
      const option = {
        title: {
          text: `趋势分析 (${days}天)`,
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross',
            label: {
              backgroundColor: '#6a7985'
            }
          }
        },
        legend: {
          data: ['平均得分', '提交数', '活跃成员'],
          bottom: 10
        },
        toolbox: {
          feature: {
            saveAsImage: {}
          }
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
          data: dates
        },
        yAxis: [{
          type: 'value',
          name: '得分',
          position: 'left',
          axisLabel: {
            formatter: '{value}'
          }
        }, {
          type: 'value',
          name: '数量',
          position: 'right',
          axisLabel: {
            formatter: '{value}'
          }
        }],
        series: [{
          name: '平均得分',
          type: 'line',
          data: Array.from({length: days}, () => Math.floor(Math.random() * 20) + 70),
          smooth: true,
          itemStyle: { color: '#409EFF' },
          areaStyle: {
            color: 'rgba(64, 158, 255, 0.1)'
          }
        }, {
          name: '提交数',
          type: 'bar',
          yAxisIndex: 1,
          data: Array.from({length: days}, () => Math.floor(Math.random() * 50) + 10),
          itemStyle: { color: '#67C23A' }
        }, {
          name: '活跃成员',
          type: 'line',
          yAxisIndex: 1,
          data: Array.from({length: days}, () => Math.floor(Math.random() * 10) + 20),
          smooth: true,
          itemStyle: { color: '#E6A23C' },
          lineStyle: {
            type: 'dashed'
          }
        }]
      }
      
      this.charts.trendAnalysis.setOption(option)
    },
    
    renderDimensionAnalysis() {
      if (this.dimensionView === 'radar') {
        this.renderRadarChart()
      } else {
        this.renderBarChart()
      }
    },
    
    renderRadarChart() {
      const currentData = this.comparisonData?.currentPeriod
      const benchmarkData = this.comparisonData?.benchmark
      const previousData = this.comparisonData?.previousPeriod
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: function(params) {
            if (Array.isArray(params.value)) {
              // 雷达图数据是数组
              const indicators = ['代码贡献', '协作能力', '代码质量', '活跃度', '创新性']
              let result = `${params.seriesName}<br/>`
              params.value.forEach((value, index) => {
                result += `${indicators[index]}: ${value.toFixed(1)}<br/>`
              })
              return result
            } else {
              // 柱状图数据是数字
              return `${params.seriesName}<br/>${params.name}: ${params.value.toFixed(1)}`
            }
          }
        },
        legend: {
          data: ['当前周期', '基准水平', '上一周期'],
          bottom: 10
        },
        radar: {
          indicator: [
            { name: '代码贡献', max: 100 },
            { name: '协作能力', max: 100 },
            { name: '代码质量', max: 100 },
            { name: '活跃度', max: 100 },
            { name: '创新性', max: 100 }
          ],
          radius: '70%',
          splitNumber: 5,
          axisLine: {
            lineStyle: {
              color: 'rgba(211, 253, 250, 0.8)'
            }
          },
          splitLine: {
            lineStyle: {
              color: 'rgba(211, 253, 250, 0.8)'
            }
          },
          splitArea: {
            show: false
          }
        },
        series: [{
          name: '维度分析',
          type: 'radar',
          data: [
            {
              value: currentData ? [
                currentData.avgCodeContribution,
                currentData.avgCollaboration,
                currentData.avgCodeQuality,
                currentData.avgActivity,
                currentData.avgInnovation
              ] : [78.5, 72.3, 85.2, 68.9, 65.4],
              name: '当前周期',
              itemStyle: {
                color: '#409EFF'
              },
              areaStyle: {
                color: 'rgba(64, 158, 255, 0.1)'
              }
            },
            {
              value: benchmarkData ? [
                benchmarkData.avgCodeContribution,
                benchmarkData.avgCollaboration,
                benchmarkData.avgCodeQuality,
                benchmarkData.avgActivity,
                benchmarkData.avgInnovation
              ] : [80.0, 75.0, 85.0, 70.0, 65.0],
              name: '基准水平',
              itemStyle: {
                color: '#67C23A'
              },
              areaStyle: {
                color: 'rgba(103, 194, 58, 0.1)'
              }
            },
            {
              value: previousData ? [
                previousData.avgCodeContribution,
                previousData.avgCollaboration,
                previousData.avgCodeQuality,
                previousData.avgActivity,
                previousData.avgInnovation
              ] : [75.2, 69.8, 82.1, 71.2, 62.3],
              name: '上一周期',
              itemStyle: {
                color: '#E6A23C'
              },
              areaStyle: {
                color: 'rgba(230, 162, 60, 0.1)'
              }
            }
          ]
        }]
      }
      
      this.charts.dimensionAnalysis.setOption(option)
    },
    
    renderBarChart() {
      const currentData = this.comparisonData?.currentPeriod
      const benchmarkData = this.comparisonData?.benchmark
      const previousData = this.comparisonData?.previousPeriod
      
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
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
          data: ['当前周期', '基准水平', '上一周期'],
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
          data: ['代码贡献', '协作能力', '代码质量', '活跃度', '创新性'],
          axisLabel: {
            interval: 0,
            rotate: 0
          }
        },
        yAxis: {
          type: 'value',
          max: 100,
          axisLabel: {
            formatter: '{value}分'
          }
        },
        series: [
          {
            name: '当前周期',
            type: 'bar',
            data: currentData ? [
              currentData.avgCodeContribution,
              currentData.avgCollaboration,
              currentData.avgCodeQuality,
              currentData.avgActivity,
              currentData.avgInnovation
            ] : [78.5, 72.3, 85.2, 68.9, 65.4],
            itemStyle: { 
              color: '#409EFF',
              borderRadius: [4, 4, 0, 0]
            },
            emphasis: {
              itemStyle: {
                color: '#66b1ff'
              }
            }
          },
          {
            name: '基准水平',
            type: 'bar',
            data: benchmarkData ? [
              benchmarkData.avgCodeContribution,
              benchmarkData.avgCollaboration,
              benchmarkData.avgCodeQuality,
              benchmarkData.avgActivity,
              benchmarkData.avgInnovation
            ] : [80.0, 75.0, 85.0, 70.0, 65.0],
            itemStyle: { 
              color: '#67C23A',
              borderRadius: [4, 4, 0, 0]
            },
            emphasis: {
              itemStyle: {
                color: '#85ce61'
              }
            }
          },
          {
            name: '上一周期',
            type: 'bar',
            data: previousData ? [
              previousData.avgCodeContribution,
              previousData.avgCollaboration,
              previousData.avgCodeQuality,
              previousData.avgActivity,
              previousData.avgInnovation
            ] : [75.2, 69.8, 82.1, 71.2, 62.3],
            itemStyle: { 
              color: '#E6A23C',
              borderRadius: [4, 4, 0, 0]
            },
            emphasis: {
              itemStyle: {
                color: '#ebb563'
              }
            }
          }
        ]
      }
      
      this.charts.dimensionAnalysis.setOption(option)
    },
    
    refreshData() {
      this.loadAnalytics()
    },
    
    async exportAnalytics() {
      try {
        const params = {
          courseId: this.selectedCourse,
          analysisType: this.analysisType,
          timePeriod: this.timePeriod
        }
        
        const response = await this.$http.get('/analytics/export', {
          params,
          responseType: 'blob'
        })
        
        const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `贡献度分析报告_${new Date().toISOString().split('T')[0]}.xlsx`
        link.click()
        window.URL.revokeObjectURL(url)
        
        this.$message.success('报告导出成功')
      } catch (error) {
        console.error('导出报告失败:', error)
        this.$message.error('导出报告失败')
      }
    },
    
    filterTableData() {
      if (!this.tableSearch) {
        this.filteredTableData = [...this.tableData]
      } else {
        this.filteredTableData = this.tableData.filter(item =>
          item.memberName.toLowerCase().includes(this.tableSearch.toLowerCase())
        )
      }
    },
    
    viewMemberDetail(member) {
      this.$router.push({
        path: '/result-display',
        query: {
          courseId: this.selectedCourse,
          memberId: member.memberId
        }
      })
    },
    
    viewMemberProfile(member) {
      this.$router.push({
        path: `/member-git-profile/${member.memberId}`
      })
    },
    
    getRankClass(index) {
      if (index === 0) return 'gold'
      if (index === 1) return 'silver'
      if (index === 2) return 'bronze'
      return ''
    },
    
    getScoreType(score) {
      if (score >= 90) return 'success'
      if (score >= 80) return 'primary'
      if (score >= 70) return 'warning'
      return 'danger'
    },
    
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.pagination.current = 1
      this.loadAnalytics()
    },
    
    handleCurrentChange(page) {
      this.pagination.current = page
      this.loadAnalytics()
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      return new Date(dateString).toLocaleDateString('zh-CN')
    },
    
    // 权重配置相关方法
    openWeightConfig() {
      this.tempWeights = { ...this.scoreWeights }
    },
    
    validateWeights() {
      // 实时验证权重总和
      const total = this.getTotalWeight()
      if (total > 100) {
        this.$message.warning('权重总和不能超过100%')
      }
    },
    
    getTotalWeight() {
      return Object.values(this.tempWeights).reduce((sum, weight) => sum + weight, 0) * 100
    },
    
    getTotalWeightClass() {
      const total = this.getTotalWeight()
      if (total === 100) return 'valid'
      if (total > 100) return 'invalid'
      return 'warning'
    },
    
    normalizeWeights() {
      const total = Object.values(this.tempWeights).reduce((sum, weight) => sum + weight, 0)
      if (total > 0) {
        Object.keys(this.tempWeights).forEach(key => {
          this.tempWeights[key] = this.tempWeights[key] / total
        })
      }
    },
    
    resetWeights() {
      this.tempWeights = {
        codeContribution: 0.3,
        collaboration: 0.25,
        codeQuality: 0.25,
        activity: 0.15,
        innovation: 0.05
      }
    },
    
    async saveWeights() {
      try {
        const response = await this.$http.post('/analytics/score-weights', {
          courseId: this.selectedCourse,
          weights: this.tempWeights
        })
        
        if (response.data.code === '200') {
          this.scoreWeights = { ...this.tempWeights }
          this.showWeightConfig = false
          this.$message.success('权重配置保存成功')
          
          // 重新计算分数并刷新图表
          this.refreshData()
        }
      } catch (error) {
        console.error('保存权重配置失败:', error)
        this.$message.error('保存权重配置失败')
      }
    },
    
    // 对比分析相关方法
    getDimensionName(key) {
      const nameMap = {
        avgCodeContribution: '代码贡献',
        avgCollaboration: '协作能力',
        avgCodeQuality: '代码质量',
        avgActivity: '活跃度',
        avgInnovation: '创新性',
        codeContribution: '代码贡献',
        collaboration: '协作能力',
        codeQuality: '代码质量',
        activity: '活跃度',
        innovation: '创新性'
      }
      return nameMap[key] || key
    },
    
    getChangeClass(change) {
      if (change > 0) return 'positive'
      if (change < 0) return 'negative'
      return 'neutral'
    },
    
    getChangeText(change) {
      if (change > 0) return `+${change.toFixed(1)}`
      if (change < 0) return change.toFixed(1)
      return '0.0'
    },
    
    getWeightColor(weight) {
      if (weight >= 0.25) return '#409EFF'
      if (weight >= 0.15) return '#67C23A'
      if (weight >= 0.1) return '#E6A23C'
      return '#F56C6C'
    },
    
    // 贡献度评分相关方法
    async viewMemberScoring(member) {
      try {
        this.selectedMember = member
        
        // 获取成员的Git活动数据
        const response = await this.$http.get(`/git-activities/${member.memberId}`, {
          params: {
            courseId: this.selectedCourse,
            timePeriod: this.timePeriod
          }
        })
        
        if (response.data.code === '200') {
          this.selectedMemberActivities = response.data.data
        } else {
          // 使用模拟数据
          this.selectedMemberActivities = this.generateMockActivities(member)
        }
      } catch (error) {
        console.error('获取成员活动数据失败:', error)
        this.selectedMemberActivities = this.generateMockActivities(member)
      }
    },
    
    generateMockActivities(member) {
      return {
        memberId: member.memberId,
        memberName: member.memberName,
        activities: [
          { type: 'commit', count: member.commits || 50, weight: 1.0 },
          { type: 'pullRequest', count: Math.floor((member.commits || 50) * 0.2), weight: 1.5 },
          { type: 'codeReview', count: Math.floor((member.commits || 50) * 0.3), weight: 1.2 },
          { type: 'issue', count: Math.floor((member.commits || 50) * 0.15), weight: 1.1 },
          { type: 'branch', count: Math.floor((member.commits || 50) * 0.1), weight: 0.8 },
          { type: 'merge', count: Math.floor((member.commits || 50) * 0.18), weight: 1.3 },
          { type: 'tag', count: Math.floor((member.commits || 50) * 0.05), weight: 0.6 },
          { type: 'fork', count: Math.floor((member.commits || 50) * 0.02), weight: 0.5 },
          { type: 'star', count: Math.floor((member.commits || 50) * 0.08), weight: 0.3 }
        ],
        codeMetrics: {
          linesAdded: member.linesAdded || 2500,
          linesDeleted: member.linesDeleted || 800,
          filesChanged: Math.floor((member.commits || 50) * 3),
          complexity: Math.random() * 10 + 5,
          testCoverage: Math.random() * 30 + 70,
          documentationRatio: Math.random() * 40 + 60
        },
        timeMetrics: {
          activeDays: member.activeDays || 25,
          avgCommitsPerDay: (member.commits || 50) / (member.activeDays || 25),
          continuousActiveDays: Math.floor(Math.random() * 15) + 10,
          peakActivityHour: Math.floor(Math.random() * 24)
        },
        collaborationMetrics: {
          pullRequests: Math.floor((member.commits || 50) * 0.2),
          codeReviews: Math.floor((member.commits || 50) * 0.3),
          issues: Math.floor((member.commits || 50) * 0.15),
          mentionedByOthers: Math.floor(Math.random() * 20) + 5,
          helpedOthers: Math.floor(Math.random() * 15) + 3
        }
      }
    },
    
    closeMemberScoring() {
      this.selectedMember = null
      this.selectedMemberActivities = []
    },
    
    openBatchScoring() {
      this.showBatchScoring = true
      this.batchScoringResults = []
      this.batchScoringProgress = 0
      this.batchScoringInProgress = false
    },
    
    closeBatchScoring() {
      this.showBatchScoring = false
      this.batchScoringInProgress = false
      this.batchScoringProgress = 0
      this.batchScoringResults = []
    },
    
    async startBatchScoring() {
      try {
        this.batchScoringInProgress = true
        this.batchScoringProgress = 0
        this.batchScoringStatus = 'running'
        this.batchScoringText = '正在准备评分数据...'
        
        // 获取需要评分的成员列表
        let membersToScore = []
        if (this.batchScoringOptions.scope === 'all') {
          membersToScore = [...this.tableData]
        } else if (this.batchScoringOptions.scope === 'selected') {
          // 这里应该从表格选中的成员中获取
          membersToScore = this.tableData.slice(0, 5) // 模拟选中前5个
        } else if (this.batchScoringOptions.scope === 'course') {
          membersToScore = this.tableData.filter(member => 
            member.courseId === this.selectedCourse
          )
        }
        
        this.batchScoringText = `开始评分 ${membersToScore.length} 名成员...`
        
        // 批量计算评分
        const results = []
        for (let i = 0; i < membersToScore.length; i++) {
          const member = membersToScore[i]
          this.batchScoringText = `正在评分: ${member.memberName} (${i + 1}/${membersToScore.length})`
          
          // 获取成员活动数据
          const activities = this.generateMockActivities(member)
          
          // 计算评分
          const oldScore = member.totalScore || 0
          const scoringResult = calculateContributionScore(activities.activities, this.scoreWeights)
          
          const newScore = scoringResult.totalScore
          const change = newScore - oldScore
          const grade = scoringResult.grade
          const suggestions = scoringResult.recommendations.length
          
          results.push({
            memberId: member.memberId,
            memberName: member.memberName,
            oldScore: oldScore,
            newScore: newScore,
            change: change,
            grade: grade,
            suggestions: suggestions,
            dimensionScores: scoringResult.dimensionScores
          })
          
          this.batchScoringProgress = Math.round(((i + 1) / membersToScore.length) * 100)
          
          // 模拟处理时间
          await new Promise(resolve => setTimeout(resolve, 200))
        }
        
        this.batchScoringResults = results
        this.batchScoringStatus = 'completed'
        this.batchScoringText = `评分完成！共处理 ${results.length} 名成员`
        this.$message.success('批量评分完成')
        
      } catch (error) {
        console.error('批量评分失败:', error)
        this.batchScoringStatus = 'error'
        this.batchScoringText = '评分过程中出现错误'
        this.$message.error('批量评分失败')
      } finally {
        this.batchScoringInProgress = false
      }
    },
    
    async applyBatchResults() {
      try {
        // 应用批量评分结果到数据表格
        this.batchScoringResults.forEach(result => {
          const memberIndex = this.tableData.findIndex(m => m.memberId === result.memberId)
          if (memberIndex !== -1) {
            this.tableData[memberIndex].totalScore = result.newScore
            // 更新维度得分
            if (result.dimensionScores) {
              this.tableData[memberIndex].codeContribution = result.dimensionScores.codeContribution
              this.tableData[memberIndex].collaborationScore = result.dimensionScores.collaboration
              this.tableData[memberIndex].qualityScore = result.dimensionScores.codeQuality
            }
          }
        })
        
        // 更新过滤后的数据
        this.filterTableData()
        
        // 重新渲染图表
        this.renderCharts()
        
        this.$message.success('评分结果已应用到数据表格')
        this.closeBatchScoring()
        
      } catch (error) {
        console.error('应用评分结果失败:', error)
        this.$message.error('应用评分结果失败')
      }
    },
    
    exportBatchResults() {
      try {
        // 准备导出数据
        const exportData = this.batchScoringResults.map(result => ({
          '成员姓名': result.memberName,
          '原得分': result.oldScore.toFixed(1),
          '新得分': result.newScore.toFixed(1),
          '得分变化': result.change > 0 ? `+${result.change.toFixed(1)}` : result.change.toFixed(1),
          '等级': result.grade,
          '改进建议数': result.suggestions,
          '代码贡献': result.dimensionScores?.codeContribution?.toFixed(1) || 'N/A',
          '协作能力': result.dimensionScores?.collaboration?.toFixed(1) || 'N/A',
          '代码质量': result.dimensionScores?.codeQuality?.toFixed(1) || 'N/A',
          '活跃度': result.dimensionScores?.activity?.toFixed(1) || 'N/A',
          '创新性': result.dimensionScores?.innovation?.toFixed(1) || 'N/A'
        }))
        
        // 转换为CSV格式
        const headers = Object.keys(exportData[0])
        const csvContent = [
          headers.join(','),
          ...exportData.map(row => headers.map(header => row[header]).join(','))
        ].join('\n')
        
        // 创建下载链接
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `批量评分结果_${new Date().toISOString().split('T')[0]}.csv`
        link.click()
        window.URL.revokeObjectURL(url)
        
        this.$message.success('评分结果已导出')
        
      } catch (error) {
        console.error('导出评分结果失败:', error)
        this.$message.error('导出评分结果失败')
      }
    },
    
    getBatchScoringModeText(mode) {
      const modeMap = {
        'standard': '标准评分',
        'relative': '相对评分',
        'custom': '自定义权重'
      }
      return modeMap[mode] || mode
    },
    
    getBatchScopingText(scope) {
      const scopeMap = {
        'all': '全部成员',
        'selected': '选中成员',
        'course': '当前课程'
      }
      return scopeMap[scope] || scope
    },
    
    getScoreChangeClass(change) {
      if (change > 5) return 'score-increase-high'
      if (change > 0) return 'score-increase'
      if (change < -5) return 'score-decrease-high'
      if (change < 0) return 'score-decrease'
      return 'score-no-change'
    },

    // 刷新代码质量数据
    async refreshQualityData() {
      try {
        this.loading = true
        
        // 重新渲染代码质量图表
        if (this.charts.codeQuality) {
          this.renderCodeQualityChart()
        }
        
        // 如果有选中的课程，重新加载质量数据
        if (this.selectedCourse) {
          await this.loadAnalytics()
        }
        
        this.$message.success('代码质量数据已刷新')
      } catch (error) {
        console.error('刷新代码质量数据失败:', error)
        this.$message.error('刷新代码质量数据失败')
      } finally {
        this.loading = false
      }
    },

    // 初始化批量评分
    initBatchScoring() {
      try {
        // 重置批量评分状态
        this.batchScoringInProgress = false
        this.batchScoringProgress = 0
        this.batchScoringResults = []
        this.batchScoringStatus = 'success'
        this.batchScoringText = '准备开始批量评分...'
        
        // 设置默认评分选项
        this.batchScoringOptions = {
          scope: 'all',
          mode: 'standard'
        }
        
        // 如果有课程数据，预加载成员信息
        if (this.selectedCourse && this.tableData.length > 0) {
          this.batchScoringText = `准备为 ${this.tableData.length} 名成员进行批量评分`
        }
        
      } catch (error) {
        console.error('初始化批量评分失败:', error)
        this.$message.error('初始化批量评分失败')
      }
    },

    // 格式化时间显示
    formatTime(timestamp) {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      const now = new Date();
      const diff = now - date;
      
      // 如果是今天
      if (diff < 24 * 60 * 60 * 1000) {
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        return `今天 ${hours}:${minutes}`;
      }
      
      // 如果是昨天
      if (diff < 48 * 60 * 60 * 1000) {
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        return `昨天 ${hours}:${minutes}`;
      }
      
      // 其他情况显示完整日期
      const month = (date.getMonth() + 1).toString().padStart(2, '0');
      const day = date.getDate().toString().padStart(2, '0');
      const hours = date.getHours().toString().padStart(2, '0');
      const minutes = date.getMinutes().toString().padStart(2, '0');
      return `${month}-${day} ${hours}:${minutes}`;
    },
    
    // 刷新质量数据
    async refreshQualityData() {
      if (!this.selectedCourse) {
        console.warn('未选择课程，跳过刷新质量数据')
        this.$message.warning('请先选择课程')
        return
      }
      
      this.loading = true
      try {
        // 重新加载代码质量相关数据
        await this.loadAnalytics()
        await this.loadScoreAnalysis()
        
        // 重新渲染代码质量图表
        this.renderCodeQuality()
        
        this.$message.success('质量数据已刷新')
      } catch (error) {
        console.error('刷新质量数据失败:', error)
        this.$message.error('刷新质量数据失败')
      } finally {
        this.loading = false
      }
    },
    
    // 初始化批量评分
    initBatchScoring() {
      if (!this.selectedCourse) {
        console.warn('未选择课程，无法初始化批量评分')
        this.$message.warning('请先选择课程')
        return
      }
      
      // 重置批量评分状态
      this.batchScoring = {
        isActive: false,
        progress: 0,
        currentMember: '',
        results: [],
        errors: []
      }
      
      // 显示批量评分对话框
      this.showBatchScoringDialog = true
      
      this.$message.info('批量评分已初始化，请配置评分参数')
    }
  }
}
</script>

<style scoped>
.contribution-analytics {
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

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.last-update-time {
  font-size: 12px;
  color: #909399;
  margin-left: 15px;
  white-space: nowrap;
}

.filters {
  margin-bottom: 20px;
}

.overview-stats {
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

.stat-icon.total {
  background: #409EFF;
}

.stat-icon.commits {
  background: #67C23A;
}

.stat-icon.score {
  background: #E6A23C;
}

.stat-icon.active {
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
  margin-bottom: 5px;
}

.stat-change {
  font-size: 12px;
  color: #F56C6C;
}

.stat-change.positive {
  color: #67C23A;
}

.charts-section {
  margin-bottom: 20px;
}

.dimension-analysis {
  margin-bottom: 20px;
}

/* 对比分析卡片样式 */
.comparison-card {
  margin-bottom: 20px;
}

.comparison-content {
  padding: 10px 0;
}

.comparison-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comparison-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.dimension-name {
  font-weight: 500;
  color: #606266;
  min-width: 80px;
}

.comparison-values {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-value {
  font-weight: bold;
  color: #409EFF;
  min-width: 40px;
  text-align: right;
}

.vs-text {
  color: #909399;
  font-size: 12px;
}

.previous-value,
.benchmark-value {
  font-weight: 500;
  color: #606266;
  min-width: 40px;
  text-align: right;
}

.change-indicator {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
  min-width: 50px;
  text-align: center;
}

.change-indicator.positive {
  background-color: #f0f9ff;
  color: #67c23a;
}

.change-indicator.negative {
  background-color: #fef0f0;
  color: #f56c6c;
}

.change-indicator.neutral {
  background-color: #f5f7fa;
  color: #909399;
}

/* 权重显示样式 */
.weight-display {
  padding: 10px 0;
}

.weight-item {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  gap: 15px;
}

.weight-item:last-child {
  margin-bottom: 0;
}

.weight-name {
  font-weight: 500;
  color: #606266;
  min-width: 80px;
  flex-shrink: 0;
}

.weight-value {
  font-weight: bold;
  color: #303133;
  min-width: 40px;
  text-align: right;
  flex-shrink: 0;
}

/* 权重配置对话框样式 */
.weight-config {
  padding: 10px 0;
}

.config-description {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border-left: 4px solid #409eff;
}

.config-description p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.weight-sliders {
  margin-bottom: 20px;
}

.weight-slider {
  margin-bottom: 25px;
}

.slider-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.dimension-label {
  font-weight: 500;
  color: #303133;
}

.weight-percentage {
  font-weight: bold;
  color: #409eff;
  font-size: 16px;
}

.weight-summary {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
  font-size: 16px;
}

.total-weight {
  font-weight: bold;
  font-size: 18px;
}

.total-weight.valid {
  color: #67c23a;
}

.total-weight.warning {
  color: #e6a23c;
}

.total-weight.invalid {
  color: #f56c6c;
}

.leaderboard {
  margin-bottom: 20px;
}

.leaderboard-list {
  max-height: 400px;
  overflow-y: auto;
}

.leaderboard-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #EBEEF5;
  transition: background-color 0.3s;
}

.leaderboard-item:hover {
  background-color: #F5F7FA;
}

.leaderboard-item.top-three {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.rank {
  margin-right: 15px;
}

.rank-number {
  display: inline-block;
  width: 30px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-radius: 50%;
  font-weight: bold;
  color: white;
  background: #909399;
}

.rank-number.gold {
  background: #FFD700;
}

.rank-number.silver {
  background: #C0C0C0;
}

.rank-number.bronze {
  background: #CD7F32;
}

.member-info {
  flex: 1;
  margin-right: 15px;
}

.member-name {
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.member-score {
  color: #409EFF;
  font-size: 14px;
}

.member-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  display: block;
  font-weight: bold;
  color: #303133;
  margin-bottom: 2px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.detailed-table {
  margin-bottom: 20px;
}

/* 表格头部样式 */
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.search-input {
  width: 240px;
}

/* 表格容器样式 */
.table-container {
  margin: 0 -20px;
  padding-top: 10px;
}

/* 确保表头显示 */
.detailed-data-table .el-table__header-wrapper {
  position: relative;
  z-index: 1;
}

.detailed-data-table .el-table__header {
  background-color: #fafafa;
  font-weight: 600;
}

.detailed-data-table {
  font-size: 14px;
}

/* 成员单元格样式 */
.member-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.member-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

.member-name {
  font-weight: 500;
  color: #303133;
}

/* 分数样式 */
.score-tag {
  font-weight: 600;
  border-radius: 6px;
}

.score-value {
  font-weight: 600;
  color: #409EFF;
}

/* 代码行数样式 */
.lines-added {
  color: #67C23A;
  font-weight: 500;
}

.lines-deleted {
  color: #F56C6C;
  font-weight: 500;
}

/* 活跃度单元格样式 */
.activity-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.activity-days {
  font-weight: 600;
  color: #303133;
}

.activity-unit {
  font-size: 12px;
  color: #909399;
}

/* 日期单元格样式 */
.date-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.date-cell i {
  color: #909399;
}

/* 操作按钮样式 */
.action-buttons {
  display: flex;
  gap: 12px;
  justify-content: center;
  align-items: center;
  flex-wrap: nowrap;
}

.action-buttons .el-button {
  padding: 8px 16px;
  font-size: 12px;
  min-width: 70px;
  white-space: nowrap;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.action-buttons .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.action-buttons .el-button + .el-button {
  margin-left: 0;
}

/* 表格底部样式 */
.table-footer {
  margin-top: 20px;
  padding: 16px 0;
  border-top: 1px solid #EBEEF5;
}

.pagination-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-info {
  color: #606266;
  font-size: 14px;
}

.table-pagination {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .table-container {
    overflow-x: auto;
  }
  
  .detailed-data-table {
    min-width: 1000px;
  }
  
  .action-buttons {
    gap: 8px;
    flex-wrap: wrap;
  }
  
  .action-buttons .el-button {
    padding: 6px 12px;
    font-size: 11px;
    min-width: 60px;
  }
}

@media (max-width: 768px) {
  .table-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }
  
  .header-left,
  .header-right {
    justify-content: center;
  }
  
  .search-input {
    width: 100%;
  }
  
  .pagination-wrapper {
    flex-direction: column;
    gap: 12px;
    align-items: center;
  }
  
  .pagination-info {
    text-align: center;
  }
}

.el-card {
  margin-bottom: 20px;
}

/* 智能贡献度评分样式 */
.scoring-section {
  margin-bottom: 20px;
}

.scoring-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.scoring-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.close-scoring {
  color: #909399;
  cursor: pointer;
  font-size: 18px;
  transition: color 0.3s;
}

.close-scoring:hover {
  color: #f56c6c;
}

/* 批量评分对话框样式 */
.batch-scoring-content {
  padding: 20px 0;
}

.scoring-options {
  margin-bottom: 25px;
}

.option-group {
  margin-bottom: 20px;
}

.option-label {
  font-weight: 500;
  color: #303133;
  margin-bottom: 10px;
  display: block;
}

.option-description {
  color: #909399;
  font-size: 13px;
  margin-top: 5px;
}

.scoring-progress {
  margin: 25px 0;
}

.progress-text {
  text-align: center;
  color: #606266;
  margin-bottom: 10px;
  font-size: 14px;
}

.scoring-results {
  margin-top: 25px;
}

.results-table {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
}

.results-header {
  background-color: #F5F7FA;
  padding: 12px;
  border-bottom: 1px solid #EBEEF5;
  font-weight: bold;
  color: #303133;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr;
  gap: 10px;
  align-items: center;
}

.results-row {
  padding: 12px;
  border-bottom: 1px solid #F5F7FA;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1fr 1fr 1fr;
  gap: 10px;
  align-items: center;
  transition: background-color 0.3s;
}

.results-row:hover {
  background-color: #F5F7FA;
}

.results-row:last-child {
  border-bottom: none;
}

.member-name-cell {
  font-weight: 500;
  color: #303133;
}

.score-cell {
  text-align: center;
  font-weight: bold;
}

.old-score {
  color: #909399;
}

.new-score {
  color: #409EFF;
}

.score-change {
  text-align: center;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.score-increase {
  background-color: #f0f9ff;
  color: #67c23a;
}

.score-increase-high {
  background-color: #e8f5e8;
  color: #52c41a;
}

.score-decrease {
  background-color: #fef0f0;
  color: #f56c6c;
}

.score-decrease-high {
  background-color: #ffebee;
  color: #f5222d;
}

.score-no-change {
  background-color: #f5f7fa;
  color: #909399;
}

.level-cell {
  text-align: center;
}

.level-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.level-excellent {
  background-color: #e8f5e8;
  color: #52c41a;
}

.level-good {
  background-color: #e6f7ff;
  color: #1890ff;
}

.level-average {
  background-color: #fff7e6;
  color: #fa8c16;
}

.level-needs-improvement {
  background-color: #fff2f0;
  color: #f5222d;
}

.suggestions-cell {
  text-align: center;
  color: #606266;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #EBEEF5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .results-header,
  .results-row {
    grid-template-columns: 1fr;
    gap: 5px;
  }
  
  .results-header {
    display: none;
  }
  
  .results-row {
    display: block;
    padding: 15px;
  }
  
  .member-name-cell {
    font-size: 16px;
    margin-bottom: 10px;
  }
  
  .score-cell,
  .level-cell,
  .suggestions-cell {
    display: inline-block;
    margin-right: 15px;
    margin-bottom: 5px;
  }
}

/* 动画效果 */
.scoring-fade-enter-active,
.scoring-fade-leave-active {
  transition: opacity 0.3s;
}

.scoring-fade-enter,
.scoring-fade-leave-to {
  opacity: 0;
}

.batch-progress-enter-active {
  transition: all 0.3s ease;
}

.batch-progress-enter {
  opacity: 0;
  transform: translateY(-10px);
}
</style>