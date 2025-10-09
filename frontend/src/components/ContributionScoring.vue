<template>
  <div class="contribution-scoring">
    <!-- 评分概览 -->
    <div class="score-overview">
      <div class="total-score">
        <div class="score-circle">
          <div class="score-value">{{ scoreResult.totalScore }}</div>
          <div class="score-grade">{{ scoreResult.grade }}</div>
        </div>
        <div class="score-info">
          <h3>综合贡献度得分</h3>
          <p class="score-description">{{ getScoreDescription(scoreResult.totalScore) }}</p>
        </div>
      </div>
      
      <div class="score-actions">
        <el-button type="primary" @click="refreshScore" :loading="loading">
          <i class="el-icon-refresh"></i> 刷新评分
        </el-button>
        <el-button @click="showWeightConfig = true">
          <i class="el-icon-setting"></i> 权重配置
        </el-button>
        <el-button @click="exportReport">
          <i class="el-icon-download"></i> 导出报告
        </el-button>
      </div>
    </div>
    
    <!-- 维度得分雷达图 -->
    <div class="dimension-radar">
      <h4>各维度得分分析</h4>
      <div ref="radarChart" class="radar-chart"></div>
    </div>
    
    <!-- 详细得分 -->
    <div class="detailed-scores">
      <h4>详细得分</h4>
      <div class="score-dimensions">
        <div 
          v-for="(score, dimension) in scoreResult.scores" 
          :key="dimension"
          class="dimension-item"
        >
          <div class="dimension-header">
            <span class="dimension-name">{{ getDimensionName(dimension) }}</span>
            <span class="dimension-score" :class="getScoreClass(score)">{{ score.toFixed(1) }}</span>
          </div>
          <div class="dimension-progress">
            <el-progress 
              :percentage="score" 
              :color="getProgressColor(score)"
              :stroke-width="8"
            ></el-progress>
          </div>
          <div class="dimension-weight">
            权重: {{ (scoreResult.weights[dimension] * 100).toFixed(0) }}%
          </div>
        </div>
      </div>
    </div>
    
    <!-- 改进建议 -->
    <div class="recommendations" v-if="scoreResult.recommendations && scoreResult.recommendations.length > 0">
      <h4>改进建议</h4>
      <div class="recommendation-list">
        <div 
          v-for="(rec, index) in scoreResult.recommendations"
          :key="index"
          class="recommendation-item"
          :class="`priority-${rec.priority}`"
        >
          <div class="rec-icon">
            <i :class="getRecommendationIcon(rec.type)"></i>
          </div>
          <div class="rec-content">
            <div class="rec-message">{{ rec.message }}</div>
            <div class="rec-priority">优先级: {{ getPriorityText(rec.priority) }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 历史趋势 -->
    <div class="score-trend">
      <h4>得分趋势</h4>
      <div ref="trendChart" class="trend-chart"></div>
    </div>
    
    <!-- 权重配置对话框 -->
    <el-dialog 
      title="权重配置" 
      :visible.sync="showWeightConfig" 
      width="600px"
      @close="resetWeightConfig"
    >
      <div class="weight-config">
        <div class="preset-weights">
          <h5>预设权重方案</h5>
          <el-radio-group v-model="selectedPreset" @change="applyPresetWeights">
            <el-radio label="default">默认方案</el-radio>
            <el-radio label="research">研究项目</el-radio>
            <el-radio label="enterprise">企业项目</el-radio>
            <el-radio label="opensource">开源项目</el-radio>
          </el-radio-group>
        </div>
        
        <div class="custom-weights">
          <h5>自定义权重</h5>
          <div class="weight-sliders">
            <div 
              v-for="(weight, dimension) in customWeights"
              :key="dimension"
              class="weight-item"
            >
              <label>{{ getDimensionName(dimension) }}</label>
              <el-slider 
                v-model="customWeights[dimension]"
                :min="0"
                :max="1"
                :step="0.05"
                :format-tooltip="formatWeightTooltip"
                @change="validateWeights"
              ></el-slider>
              <span class="weight-value">{{ (customWeights[dimension] * 100).toFixed(0) }}%</span>
            </div>
          </div>
          <div class="weight-total">
            总权重: {{ totalWeight.toFixed(2) }}
            <el-tag v-if="totalWeight !== 1" type="warning" size="mini">权重总和应为100%</el-tag>
          </div>
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="showWeightConfig = false">取消</el-button>
        <el-button @click="normalizeWeights">自动调整</el-button>
        <el-button type="primary" @click="applyCustomWeights" :disabled="totalWeight !== 1">应用</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { calculateContributionScore, adjustWeightsForProject, DIMENSION_WEIGHTS } from '@/utils/contributionScoring'

export default {
  name: 'ContributionScoring',
  props: {
    activities: {
      type: Array,
      default: () => []
    },
    userId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      loading: false,
      scoreResult: {
        totalScore: 0,
        grade: 'D',
        scores: {},
        weights: {},
        recommendations: []
      },
      showWeightConfig: false,
      selectedPreset: 'default',
      customWeights: { ...DIMENSION_WEIGHTS },
      radarChart: null,
      trendChart: null,
      historicalScores: []
    }
  },
  computed: {
    totalWeight() {
      return Object.values(this.customWeights).reduce((sum, weight) => sum + weight, 0)
    }
  },
  mounted() {
    this.calculateScore()
    this.initCharts()
    this.loadHistoricalScores()
  },
  beforeDestroy() {
    if (this.radarChart) {
      this.radarChart.dispose()
    }
    if (this.trendChart) {
      this.trendChart.dispose()
    }
  },
  methods: {
    async calculateScore() {
      this.loading = true
      try {
        // 模拟API调用延迟
        await new Promise(resolve => setTimeout(resolve, 500))
        
        this.scoreResult = calculateContributionScore(this.activities, this.customWeights)
        this.updateRadarChart()
        
        // 保存历史记录
        this.saveHistoricalScore()
      } catch (error) {
        console.error('计算评分失败:', error)
        this.$message.error('计算评分失败')
      } finally {
        this.loading = false
      }
    },
    
    refreshScore() {
      this.calculateScore()
    },
    
    initCharts() {
      this.initRadarChart()
      this.initTrendChart()
    },
    
    initRadarChart() {
      this.radarChart = echarts.init(this.$refs.radarChart)
      this.updateRadarChart()
    },
    
    updateRadarChart() {
      if (!this.radarChart) return
      
      const option = {
        title: {
          text: '能力雷达图',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'normal'
          }
        },
        tooltip: {
          trigger: 'item'
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
              color: ['rgba(114, 172, 209, 0.1)', 'rgba(114, 172, 209, 0.05)']
            }
          }
        },
        series: [{
          type: 'radar',
          data: [{
            value: [
              this.scoreResult.scores.codeContribution || 0,
              this.scoreResult.scores.collaboration || 0,
              this.scoreResult.scores.codeQuality || 0,
              this.scoreResult.scores.activity || 0,
              this.scoreResult.scores.innovation || 0
            ],
            name: '当前得分',
            areaStyle: {
              color: 'rgba(64, 158, 255, 0.3)'
            },
            lineStyle: {
              color: '#409EFF',
              width: 2
            },
            itemStyle: {
              color: '#409EFF'
            }
          }]
        }]
      }
      
      this.radarChart.setOption(option)
    },
    
    initTrendChart() {
      this.trendChart = echarts.init(this.$refs.trendChart)
      this.updateTrendChart()
    },
    
    updateTrendChart() {
      if (!this.trendChart || this.historicalScores.length === 0) return
      
      const dates = this.historicalScores.map(item => item.date)
      const scores = this.historicalScores.map(item => item.totalScore)
      
      const option = {
        title: {
          text: '得分趋势',
          left: 'center',
          textStyle: {
            fontSize: 16,
            fontWeight: 'normal'
          }
        },
        tooltip: {
          trigger: 'axis',
          formatter: function(params) {
            return `${params[0].axisValue}<br/>得分: ${params[0].value}`
          }
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: {
            formatter: function(value) {
              return new Date(value).toLocaleDateString()
            }
          }
        },
        yAxis: {
          type: 'value',
          min: 0,
          max: 100,
          axisLabel: {
            formatter: '{value}'
          }
        },
        series: [{
          data: scores,
          type: 'line',
          smooth: true,
          lineStyle: {
            color: '#409EFF',
            width: 3
          },
          itemStyle: {
            color: '#409EFF'
          },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
              offset: 0,
              color: 'rgba(64, 158, 255, 0.3)'
            }, {
              offset: 1,
              color: 'rgba(64, 158, 255, 0.1)'
            }])
          }
        }]
      }
      
      this.trendChart.setOption(option)
    },
    
    loadHistoricalScores() {
      // 模拟历史数据
      const now = new Date()
      this.historicalScores = Array.from({ length: 30 }, (_, i) => {
        const date = new Date(now.getTime() - (29 - i) * 24 * 60 * 60 * 1000)
        return {
          date: date.toISOString(),
          totalScore: 60 + Math.random() * 30 + i * 0.5
        }
      })
      
      this.updateTrendChart()
    },
    
    saveHistoricalScore() {
      const today = new Date().toISOString().split('T')[0]
      const existingIndex = this.historicalScores.findIndex(item => 
        item.date.startsWith(today)
      )
      
      if (existingIndex >= 0) {
        this.historicalScores[existingIndex].totalScore = this.scoreResult.totalScore
      } else {
        this.historicalScores.push({
          date: new Date().toISOString(),
          totalScore: this.scoreResult.totalScore
        })
      }
      
      this.updateTrendChart()
    },
    
    applyPresetWeights() {
      switch (this.selectedPreset) {
        case 'research':
          this.customWeights = adjustWeightsForProject('research')
          break
        case 'enterprise':
          this.customWeights = adjustWeightsForProject('enterprise')
          break
        case 'opensource':
          this.customWeights = adjustWeightsForProject('opensource')
          break
        default:
          this.customWeights = { ...DIMENSION_WEIGHTS }
      }
    },
    
    validateWeights() {
      // 权重验证逻辑
    },
    
    normalizeWeights() {
      const total = this.totalWeight
      if (total > 0) {
        Object.keys(this.customWeights).forEach(key => {
          this.customWeights[key] = this.customWeights[key] / total
        })
      }
    },
    
    applyCustomWeights() {
      this.calculateScore()
      this.showWeightConfig = false
      this.$message.success('权重配置已应用')
    },
    
    resetWeightConfig() {
      this.customWeights = { ...this.scoreResult.weights }
      this.selectedPreset = 'default'
    },
    
    exportReport() {
      // 导出报告逻辑
      const reportData = {
        userId: this.userId,
        timestamp: new Date().toISOString(),
        scoreResult: this.scoreResult,
        historicalScores: this.historicalScores
      }
      
      const blob = new Blob([JSON.stringify(reportData, null, 2)], {
        type: 'application/json'
      })
      
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `contribution-report-${this.userId}-${new Date().toISOString().split('T')[0]}.json`
      a.click()
      URL.revokeObjectURL(url)
      
      this.$message.success('报告已导出')
    },
    
    getDimensionName(dimension) {
      const names = {
        codeContribution: '代码贡献',
        collaboration: '协作能力',
        codeQuality: '代码质量',
        activity: '活跃度',
        innovation: '创新性'
      }
      return names[dimension] || dimension
    },
    
    getScoreDescription(score) {
      if (score >= 90) return '优秀的贡献者，在各个方面都表现出色'
      if (score >= 80) return '良好的贡献者，大部分方面表现良好'
      if (score >= 70) return '合格的贡献者，有一定的改进空间'
      if (score >= 60) return '需要改进的贡献者，建议关注薄弱环节'
      return '贡献度较低，需要大幅提升'
    },
    
    getScoreClass(score) {
      if (score >= 80) return 'score-excellent'
      if (score >= 70) return 'score-good'
      if (score >= 60) return 'score-average'
      return 'score-poor'
    },
    
    getProgressColor(score) {
      if (score >= 80) return '#67C23A'
      if (score >= 70) return '#409EFF'
      if (score >= 60) return '#E6A23C'
      return '#F56C6C'
    },
    
    getRecommendationIcon(type) {
      const icons = {
        codeContribution: 'el-icon-edit',
        collaboration: 'el-icon-user',
        codeQuality: 'el-icon-star-on',
        activity: 'el-icon-time',
        innovation: 'el-icon-magic-stick'
      }
      return icons[type] || 'el-icon-info'
    },
    
    getPriorityText(priority) {
      const texts = {
        high: '高',
        medium: '中',
        low: '低'
      }
      return texts[priority] || priority
    },
    
    formatWeightTooltip(value) {
      return `${(value * 100).toFixed(0)}%`
    }
  },
  watch: {
    activities: {
      handler() {
        this.calculateScore()
      },
      deep: true
    }
  }
}
</script>

<style scoped>
.contribution-scoring {
  padding: 20px;
}

.score-overview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  border-radius: 12px;
  margin-bottom: 30px;
}

.total-score {
  display: flex;
  align-items: center;
  gap: 20px;
}

.score-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.score-value {
  font-size: 36px;
  font-weight: bold;
  line-height: 1;
}

.score-grade {
  font-size: 18px;
  font-weight: 500;
  margin-top: 5px;
}

.score-info h3 {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: 600;
}

.score-description {
  margin: 0;
  font-size: 16px;
  opacity: 0.9;
}

.score-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dimension-radar,
.detailed-scores,
.recommendations,
.score-trend {
  background: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.dimension-radar h4,
.detailed-scores h4,
.recommendations h4,
.score-trend h4 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.radar-chart,
.trend-chart {
  height: 400px;
}

.score-dimensions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.dimension-item {
  padding: 20px;
  border: 1px solid #EBEEF5;
  border-radius: 8px;
  background: #FAFAFA;
}

.dimension-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.dimension-name {
  font-weight: 600;
  color: #303133;
}

.dimension-score {
  font-size: 24px;
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

.dimension-progress {
  margin-bottom: 10px;
}

.dimension-weight {
  font-size: 12px;
  color: #909399;
}

.recommendation-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.recommendation-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid;
}

.recommendation-item.priority-high {
  background: #FEF0F0;
  border-left-color: #F56C6C;
}

.recommendation-item.priority-medium {
  background: #FDF6EC;
  border-left-color: #E6A23C;
}

.recommendation-item.priority-low {
  background: #F0F9FF;
  border-left-color: #409EFF;
}

.rec-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.rec-content {
  flex: 1;
}

.rec-message {
  font-size: 14px;
  color: #303133;
  margin-bottom: 5px;
}

.rec-priority {
  font-size: 12px;
  color: #909399;
}

.weight-config {
  padding: 20px 0;
}

.preset-weights,
.custom-weights {
  margin-bottom: 30px;
}

.preset-weights h5,
.custom-weights h5 {
  margin: 0 0 15px 0;
  font-size: 16px;
  font-weight: 600;
}

.weight-sliders {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.weight-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.weight-item label {
  min-width: 80px;
  font-weight: 500;
}

.weight-item .el-slider {
  flex: 1;
}

.weight-value {
  min-width: 50px;
  text-align: right;
  font-weight: 600;
}

.weight-total {
  margin-top: 20px;
  padding: 15px;
  background: #F5F7FA;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .score-overview {
    flex-direction: column;
    gap: 20px;
    text-align: center;
  }
  
  .score-actions {
    flex-direction: row;
    flex-wrap: wrap;
  }
  
  .score-dimensions {
    grid-template-columns: 1fr;
  }
}
</style>