<template>
  <div class="data-collection">
    <NavMenu></NavMenu>
    <el-main>
      <div class="page-header">
        <h2 class="title">数据采集</h2>
        <el-button type="primary" @click="startDataCollection" :loading="collecting">
          <i class="el-icon-refresh"></i> {{ collecting ? '采集中...' : '开始采集' }}
        </el-button>
      </div>

      <el-tabs v-model="activeTab" type="card">
        <!-- 仓库选择 -->
        <el-tab-pane label="仓库选择" name="repositories">
          <div class="config-section">
            <div class="section-header">
              <h3>选择数据采集仓库</h3>
              <div class="header-actions">
                <el-button size="small" @click="loadRepositories" :loading="repositoriesLoading">
                  <i class="el-icon-refresh"></i> 刷新仓库列表
                </el-button>
                <el-button size="small" type="primary" @click="showBatchOperations = !showBatchOperations">
                  <i class="el-icon-setting"></i> 批量操作
                </el-button>
                <el-button size="small" @click="showAdvancedFilter = !showAdvancedFilter">
                  <i class="el-icon-search"></i> 高级筛选
                </el-button>
              </div>
            </div>
            
            <!-- 高级筛选 -->
            <el-collapse-transition>
              <div v-show="showAdvancedFilter" class="advanced-filter">
                <el-form :model="repositoryFilter" inline size="small">
                  <el-form-item label="平台">
                    <el-select v-model="repositoryFilter.platform" placeholder="全部平台" clearable>
                      <el-option label="GitHub" value="github"></el-option>
                      <el-option label="GitLab" value="gitlab"></el-option>
                      <el-option label="Gitee" value="gitee"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="状态">
                    <el-select v-model="repositoryFilter.status" placeholder="全部状态" clearable>
                      <el-option label="已同步" value="synced"></el-option>
                      <el-option label="未同步" value="not_synced"></el-option>
                      <el-option label="同步中" value="syncing"></el-option>
                      <el-option label="同步失败" value="failed"></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="课程">
                    <el-select v-model="repositoryFilter.courseId" placeholder="全部课程" clearable>
                      <el-option 
                        v-for="course in courses" 
                        :key="course.courseId" 
                        :label="course.courseName" 
                        :value="course.courseId"
                      ></el-option>
                    </el-select>
                  </el-form-item>
                  <el-form-item label="关键词">
                    <el-input v-model="repositoryFilter.keyword" placeholder="搜索仓库名称" clearable></el-input>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="applyRepositoryFilter">筛选</el-button>
                    <el-button @click="resetRepositoryFilter">重置</el-button>
                  </el-form-item>
                </el-form>
              </div>
            </el-collapse-transition>
            
            <!-- 批量操作 -->
            <el-collapse-transition>
              <div v-show="showBatchOperations" class="batch-operations">
                <div class="batch-header">
                  <el-checkbox 
                    v-model="selectAll" 
                    @change="handleSelectAll"
                    :indeterminate="isIndeterminate"
                  >
                    全选 ({{ selectedRepositories.length }}/{{ filteredRepositories.length }})
                  </el-checkbox>
                  <div class="batch-actions">
                    <el-button 
                      size="small" 
                      type="primary" 
                      @click="batchSyncRepositories" 
                      :disabled="selectedRepositories.length === 0"
                      :loading="batchSyncing"
                    >
                      <i class="el-icon-refresh"></i> 批量同步 ({{ selectedRepositories.length }})
                    </el-button>
                    <el-button 
                      size="small" 
                      @click="batchConfigureTokens" 
                      :disabled="selectedRepositories.length === 0"
                    >
                      <i class="el-icon-key"></i> 批量配置Token
                    </el-button>
                    <el-button 
                      size="small" 
                      type="warning" 
                      @click="batchExportData" 
                      :disabled="selectedRepositories.length === 0"
                    >
                      <i class="el-icon-download"></i> 批量导出
                    </el-button>
                  </div>
                </div>
                
                <!-- 仓库列表 -->
                <div class="repository-list">
                  <el-checkbox-group v-model="selectedRepositories">
                    <div 
                      v-for="repo in filteredRepositories" 
                      :key="repo.repositoryId" 
                      class="repository-item"
                      :class="{ 'selected': selectedRepositories.includes(repo.repositoryId) }"
                    >
                      <el-checkbox :label="repo.repositoryId">
                        <div class="repo-info-card">
                          <div class="repo-header">
                            <span class="repo-name">{{ repo.repositoryName }}</span>
                            <div class="repo-badges">
                              <el-tag size="mini" :type="getPlatformType(repo.platform)">{{ repo.platform }}</el-tag>
                              <el-tag size="mini" :type="getRepoStatusType(repo.status)">{{ getRepoStatusText(repo.status) }}</el-tag>
                            </div>
                          </div>
                          <div class="repo-details">
                            <p><i class="el-icon-link"></i> {{ repo.repositoryUrl }}</p>
                            <p><i class="el-icon-school"></i> {{ repo.courseName || '未知课程' }}</p>
                            <p v-if="repo.lastSync"><i class="el-icon-time"></i> 最后同步: {{ formatDateTime(repo.lastSync) }}</p>
                          </div>
                          <div class="repo-actions">
                            <el-button size="mini" @click.stop="syncRepository(repo)" :loading="repo.syncing">
                              <i class="el-icon-refresh"></i> 同步
                            </el-button>
                            <el-button size="mini" type="text" @click.stop="viewRepositoryDetails(repo)">
                              <i class="el-icon-view"></i> 详情
                            </el-button>
                          </div>
                        </div>
                      </el-checkbox>
                    </div>
                  </el-checkbox-group>
                </div>
              </div>
            </el-collapse-transition>
            
            <el-form :model="collectionForm" label-width="120px">
              <el-form-item label="选择仓库">
                <el-select 
                  v-model="collectionForm.selectedRepositoryId" 
                  placeholder="请选择要采集数据的仓库"
                  style="width: 100%"
                  @change="onRepositoryChange"
                >
                  <el-option
                    v-for="repo in repositories"
                    :key="repo.repositoryId"
                    :label="`${repo.repositoryName} (${repo.platform})`"
                    :value="repo.repositoryId"
                  >
                    <span style="float: left">{{ repo.repositoryName }}</span>
                    <span style="float: right; color: #8492a6; font-size: 13px">{{ repo.platform }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item v-if="selectedRepository" label="仓库信息">
                <div class="repo-info">
                  <p><strong>仓库地址：</strong>{{ selectedRepository.repositoryUrl }}</p>
                  <p><strong>平台：</strong>{{ selectedRepository.platform }}</p>
                  <p><strong>所属课程：</strong>{{ selectedRepository.courseName || selectedRepository.courseId || '未知课程' }}</p>
                  <p><strong>状态：</strong>
                    <el-tag :type="getRepoStatusType(selectedRepository.status)">
                      {{ getRepoStatusText(selectedRepository.status) }}
                    </el-tag>
                  </p>
                  <p v-if="selectedRepository.lastSync"><strong>最后同步：</strong>{{ formatDateTime(selectedRepository.lastSync) }}</p>
                  
                  <!-- GitHub Token状态 -->
                  <p><strong>GitHub Token：</strong>
                    <span v-if="tokenStatus.loading" class="token-loading">
                      <i class="el-icon-loading"></i> 检查中...
                    </span>
                    <span v-else-if="tokenStatus.status === 'valid'" class="token-valid">
                      <i class="el-icon-success"></i> 
                      <el-tag type="success" size="small">已配置且有效</el-tag>
                      <span class="token-tip">可正常进行数据采集</span>
                    </span>
                    <span v-else-if="tokenStatus.status === 'invalid'" class="token-invalid">
                      <i class="el-icon-warning"></i> 
                      <el-tag type="warning" size="small">已配置但无效</el-tag>
                      <span class="token-tip">Token已过期或无效，请重新配置</span>
                    </span>
                    <span v-else class="token-not-configured">
                      <i class="el-icon-info"></i> 
                      <el-tag type="info" size="small">未配置</el-tag>
                      <span class="token-tip">需要配置Token才能进行数据采集</span>
                    </span>
                    <el-button 
                      type="text" 
                      size="mini" 
                      @click="goToTokenConfig"
                      class="config-token-btn"
                    >
                      {{ tokenStatus.status === 'not_configured' ? '配置Token' : '管理Token' }}
                    </el-button>
                  </p>
                </div>
              </el-form-item>
              
              <el-form-item v-if="selectedRepository">
                <el-button 
                  type="primary" 
                  @click="syncRepository(selectedRepository)"
                  :loading="selectedRepository.syncing"
                >
                  <i class="el-icon-refresh"></i> 同步仓库数据
                </el-button>
              </el-form-item>
            </el-form>
            
            <!-- 空状态 -->
            <div v-if="repositories.length === 0" class="empty-state">
              <i class="el-icon-folder-opened"></i>
              <p>暂无可用仓库，请先在Git仓库管理中添加仓库</p>
              <el-button type="text" @click="goToRepoManagement">前往仓库管理</el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- 数据同步 -->
        <el-tab-pane label="数据同步" name="sync">
          <div class="config-section">
            <div class="section-header">
              <h3>同步配置</h3>
            </div>
            
            <el-form :model="syncConfig" label-width="120px">
              <el-form-item label="同步频率">
                <el-select v-model="syncConfig.frequency" placeholder="选择同步频率">
                  <el-option label="实时同步" value="realtime"></el-option>
                  <el-option label="每小时" value="hourly"></el-option>
                  <el-option label="每天" value="daily"></el-option>
                  <el-option label="每周" value="weekly"></el-option>
                  <el-option label="手动同步" value="manual"></el-option>
                </el-select>
              </el-form-item>
              
              <el-form-item label="数据范围">
                <el-checkbox-group v-model="syncConfig.dataTypes">
                  <el-checkbox label="commits">提交记录</el-checkbox>
                  <el-checkbox label="pull_requests">Pull Request</el-checkbox>
                  <el-checkbox label="issues">Issues</el-checkbox>
                  <el-checkbox label="code_reviews">代码审查</el-checkbox>
                  <el-checkbox label="file_changes">文件变更</el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              
              <el-form-item label="时间范围">
                <el-date-picker
                  v-model="syncConfig.dateRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 100%"
                ></el-date-picker>
              </el-form-item>
              
              <el-form-item label="过滤条件">
                <el-input 
                  type="textarea" 
                  v-model="syncConfig.filters" 
                  placeholder="输入过滤条件，如：author:username, file:*.js"
                  :rows="3"
                ></el-input>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="saveSyncConfig">
                  保存配置
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <!-- 数据预处理 -->
        <el-tab-pane label="数据预处理" name="preprocessing">
          <div class="config-section">
            <div class="section-header">
              <h3>预处理规则</h3>
              <el-button type="primary" size="small" @click="addPreprocessRule">
                <i class="el-icon-plus"></i> 添加规则
              </el-button>
            </div>
            
            <div class="rules-list">
              <el-card 
                v-for="(rule, index) in preprocessRules" 
                :key="rule.id" 
                class="rule-card"
                shadow="hover"
              >
                <div class="rule-header">
                  <el-input 
                    v-model="rule.name" 
                    placeholder="规则名称"
                    class="rule-name"
                  ></el-input>
                  <el-switch 
                    v-model="rule.enabled" 
                    active-text="启用"
                    inactive-text="禁用"
                  ></el-switch>
                  <el-button 
                    type="text" 
                    size="small" 
                    @click="removePreprocessRule(index)"
                    class="danger"
                  >
                    <i class="el-icon-delete"></i>
                  </el-button>
                </div>
                
                <el-form :model="rule" label-width="100px" size="small">
                  <el-form-item label="规则类型">
                    <el-select v-model="rule.type" placeholder="选择规则类型">
                      <el-option label="数据清洗" value="cleaning"></el-option>
                      <el-option label="数据转换" value="transformation"></el-option>
                      <el-option label="数据验证" value="validation"></el-option>
                      <el-option label="数据聚合" value="aggregation"></el-option>
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="应用条件">
                    <el-input 
                      v-model="rule.condition" 
                      placeholder="如：file_type == 'js' && lines > 10"
                    ></el-input>
                  </el-form-item>
                  
                  <el-form-item label="处理动作">
                    <el-select v-model="rule.action" placeholder="选择处理动作">
                      <el-option label="移除空提交" value="remove_empty_commits"></el-option>
                      <el-option label="合并重复记录" value="merge_duplicates"></el-option>
                      <el-option label="标准化时间" value="normalize_time"></el-option>
                      <el-option label="计算代码行数" value="calculate_lines"></el-option>
                      <el-option label="提取文件类型" value="extract_file_type"></el-option>
                    </el-select>
                  </el-form-item>
                  
                  <el-form-item label="参数配置">
                    <el-input 
                      type="textarea" 
                      v-model="rule.parameters" 
                      placeholder="JSON格式的参数配置"
                      :rows="2"
                    ></el-input>
                  </el-form-item>
                </el-form>
              </el-card>
            </div>
          </div>
        </el-tab-pane>

        <!-- 采集状态 -->
        <el-tab-pane label="采集状态" name="status">
          <div class="config-section">
            <div class="section-header">
              <h3>采集进度</h3>
              <div class="status-controls">
                <el-switch
                  v-model="realTimeMonitoring"
                  active-text="实时监控"
                  inactive-text="手动刷新"
                  @change="toggleRealTimeMonitoring"
                ></el-switch>
                <el-button size="small" @click="refreshStatus" :loading="statusLoading">
                  <i class="el-icon-refresh"></i> 刷新
                </el-button>
                <el-button size="small" type="primary" @click="exportCollectionReport">
                  <i class="el-icon-download"></i> 导出报告
                </el-button>
              </div>
            </div>
            
            <!-- 连接状态指示器 -->
            <div class="connection-status">
              <el-alert
                v-if="realTimeMonitoring"
                :title="connectionStatus.connected ? '实时监控已启用' : '连接已断开'"
                :type="connectionStatus.connected ? 'success' : 'warning'"
                :closable="false"
                show-icon
              >
                <template slot="default">
                  {{ connectionStatus.message }}
                  <span v-if="connectionStatus.connected" class="last-update">
                    最后更新: {{ formatTime(connectionStatus.lastUpdate) }}
                  </span>
                </template>
              </el-alert>
            </div>
            
            <div class="status-overview">
              <el-row :gutter="20">
                <el-col :span="6">
                  <div class="status-card" :class="{ 'status-loading': statusLoading }">
                    <div class="status-number">{{ collectionStats.totalRepos }}</div>
                    <div class="status-label">总仓库数</div>
                    <div class="status-trend" v-if="collectionStats.reposTrend">
                      <i :class="getTrendIcon(collectionStats.reposTrend)"></i>
                      {{ Math.abs(collectionStats.reposTrend) }}
                    </div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="status-card" :class="{ 'status-loading': statusLoading }">
                    <div class="status-number">{{ collectionStats.syncedRepos }}</div>
                    <div class="status-label">已同步</div>
                    <div class="status-progress">
                      <el-progress 
                        :percentage="getSyncProgress()" 
                        :stroke-width="4" 
                        :show-text="false"
                      ></el-progress>
                    </div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="status-card" :class="{ 'status-loading': statusLoading }">
                    <div class="status-number">{{ formatNumber(collectionStats.totalCommits) }}</div>
                    <div class="status-label">提交记录</div>
                    <div class="status-trend" v-if="collectionStats.commitsTrend">
                      <i :class="getTrendIcon(collectionStats.commitsTrend)"></i>
                      {{ formatNumber(Math.abs(collectionStats.commitsTrend)) }}
                    </div>
                  </div>
                </el-col>
                <el-col :span="6">
                  <div class="status-card" :class="{ 'status-loading': statusLoading }">
                    <div class="status-number">{{ formatNumber(collectionStats.processedData) }}</div>
                    <div class="status-label">已处理数据</div>
                    <div class="status-rate" v-if="collectionStats.processingRate">
                      {{ collectionStats.processingRate }}/min
                    </div>
                  </div>
                </el-col>
              </el-row>
              
              <!-- 错误统计 -->
              <el-row :gutter="20" class="error-stats" v-if="collectionStats.errors && collectionStats.errors.length > 0">
                <el-col :span="24">
                  <el-alert
                    title="检测到采集错误"
                    type="warning"
                    :closable="false"
                    show-icon
                  >
                    <template slot="default">
                      <div class="error-summary">
                        <span>共 {{ collectionStats.errors.length }} 个错误</span>
                        <el-button type="text" size="mini" @click="showErrorDetails = !showErrorDetails">
                          {{ showErrorDetails ? '隐藏详情' : '查看详情' }}
                        </el-button>
                      </div>
                      <div v-if="showErrorDetails" class="error-details">
                        <div v-for="error in collectionStats.errors" :key="error.id" class="error-item">
                          <div class="error-info">
                            <span class="error-repo">{{ error.repository }}</span>
                            <span class="error-message">{{ error.message }}</span>
                            <span class="error-time">{{ formatTime(error.timestamp) }}</span>
                          </div>
                          <el-button 
                            size="mini" 
                            type="primary" 
                            @click="retryFailedTask(error)"
                            :loading="error.retrying"
                          >
                            重试
                          </el-button>
                        </div>
                      </div>
                    </template>
                  </el-alert>
                </el-col>
              </el-row>
            </div>
            
            <div class="progress-section">
              <div class="progress-header">
                <h4>当前任务进度</h4>
                <div class="progress-controls">
                  <el-button size="mini" @click="pauseAllTasks" :disabled="!hasRunningTasks">
                    <i class="el-icon-video-pause"></i> 暂停全部
                  </el-button>
                  <el-button size="mini" @click="resumeAllTasks" :disabled="!hasPausedTasks">
                    <i class="el-icon-video-play"></i> 继续全部
                  </el-button>
                  <el-button size="mini" type="danger" @click="cancelAllTasks" :disabled="!hasActiveTasks">
                    <i class="el-icon-close"></i> 取消全部
                  </el-button>
                </div>
              </div>
              
              <div v-if="activeTasks.length === 0" class="no-tasks">
                <i class="el-icon-success"></i>
                <p>当前没有进行中的任务</p>
              </div>
              
              <div v-for="task in activeTasks" :key="task.id" class="task-progress enhanced">
                <div class="task-header">
                  <div class="task-info">
                    <span class="task-name">{{ task.name }}</span>
                    <el-tag :type="getTaskStatusType(task.status)" size="mini">{{ task.status }}</el-tag>
                  </div>
                  <div class="task-actions">
                    <el-button 
                      size="mini" 
                      :type="task.status === '暂停' ? 'primary' : 'default'"
                      @click="toggleTaskStatus(task)"
                      :disabled="task.status === '已完成' || task.status === '已取消'"
                    >
                      <i :class="task.status === '暂停' ? 'el-icon-video-play' : 'el-icon-video-pause'"></i>
                      {{ task.status === '暂停' ? '继续' : '暂停' }}
                    </el-button>
                    <el-button 
                      size="mini" 
                      type="danger" 
                      @click="cancelTask(task)"
                      :disabled="task.status === '已完成' || task.status === '已取消'"
                    >
                      <i class="el-icon-close"></i> 取消
                    </el-button>
                  </div>
                </div>
                
                <div class="task-details">
                  <div class="task-meta">
                    <span class="task-repository" v-if="task.repository">仓库: {{ task.repository }}</span>
                    <span class="task-duration">耗时: {{ formatDuration(task.duration) }}</span>
                    <span class="task-eta" v-if="task.eta">预计剩余: {{ formatDuration(task.eta) }}</span>
                  </div>
                  
                  <el-progress 
                    :percentage="task.progress" 
                    :status="getProgressStatus(task)"
                    :stroke-width="8"
                  >
                    <template slot="default" slot-scope="{ percentage }">
                      <span class="progress-text">{{ percentage }}%</span>
                      <span class="progress-detail" v-if="task.currentStep">({{ task.currentStep }})</span>
                    </template>
                  </el-progress>
                  
                  <div class="task-logs" v-if="task.logs && task.logs.length > 0">
                    <div class="task-log" v-for="log in task.logs.slice(-3)" :key="log.id">
                      <span class="log-time">{{ formatTime(log.timestamp) }}</span>
                      <span class="log-message">{{ log.message }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <div class="logs-section">
              <h4>采集日志</h4>
              <div class="logs-container">
                <div 
                  v-for="log in collectionLogs" 
                  :key="log.id" 
                  class="log-item"
                  :class="log.level"
                >
                  <span class="log-time">{{ formatTime(log.timestamp) }}</span>
                  <span class="log-level">{{ log.level.toUpperCase() }}</span>
                  <span class="log-message">{{ log.message }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>


    </el-main>
  </div>
</template>

<style scoped>
.data-collection {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.batch-operations {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid #e9ecef;
}

.batch-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.advanced-filter {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid #e9ecef;
}

.filter-row {
  display: flex;
  gap: 16px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.filter-actions {
  display: flex;
  gap: 12px;
}

.repository-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.repository-card {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  background: white;
  transition: all 0.3s ease;
}

.repository-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #409eff;
}

.repository-card.selected {
  border-color: #409eff;
  background: #f0f8ff;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.repo-info {
  flex: 1;
}

.repo-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.repo-url {
  font-size: 12px;
  color: #909399;
  word-break: break-all;
}

.card-content {
  margin-bottom: 12px;
}

.repo-meta {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

.card-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sync-info {
  font-size: 12px;
  color: #909399;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.stat-trend.positive {
  color: #67c23a;
}

.stat-trend.negative {
  color: #f56c6c;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
}

.connection-status.connected {
  background: #f0f9ff;
  border: 1px solid #67c23a;
  color: #67c23a;
}

.connection-status.disconnected {
  background: #fef0f0;
  border: 1px solid #f56c6c;
  color: #f56c6c;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: currentColor;
}

.error-list {
  margin-top: 16px;
}

.error-item {
  background: #fef0f0;
  border: 1px solid #fbc4c4;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 8px;
}

.error-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.error-message {
  color: #f56c6c;
  font-weight: 500;
}

.error-time {
  font-size: 12px;
  color: #909399;
}

.error-details {
  font-size: 12px;
  color: #606266;
  background: white;
  padding: 8px;
  border-radius: 4px;
  margin-top: 8px;
}

.task-list {
  margin-top: 16px;
}

.task-item {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.task-info {
  flex: 1;
}

.task-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.task-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #909399;
}

.task-controls {
  display: flex;
  gap: 8px;
}

.task-progress {
  margin-bottom: 12px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 12px;
  color: #606266;
}

.task-logs {
  max-height: 200px;
  overflow-y: auto;
  background: #f8f9fa;
  border-radius: 4px;
  padding: 8px;
}

.log-item {
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
  padding: 2px 0;
  border-bottom: 1px solid #e9ecef;
}

.log-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.log-time {
  color: #909399;
  margin-right: 8px;
}

.collection-logs {
  background: white;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  margin-top: 16px;
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e9ecef;
}

.logs-content {
  max-height: 400px;
  overflow-y: auto;
}

.log-entry {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.log-entry:last-child {
  border-bottom: none;
}

.log-level {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
  text-transform: uppercase;
  min-width: 50px;
  text-align: center;
}

.log-level.info {
  background: #e1f5fe;
  color: #0277bd;
}

.log-level.success {
  background: #e8f5e8;
  color: #2e7d32;
}

.log-level.warning {
  background: #fff3e0;
  color: #f57c00;
}

.log-level.error {
  background: #ffebee;
  color: #c62828;
}

.log-timestamp {
  font-size: 11px;
  color: #909399;
  min-width: 80px;
}

.log-message {
  flex: 1;
  font-size: 13px;
  color: #303133;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .repository-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .batch-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .task-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .task-controls {
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .header-actions {
    justify-content: center;
  }
}
</style>

<script>
import NavMenu from '@/components/NavMenu.vue'

export default {
  name: 'DataCollection',
  components: {
    NavMenu
  },
  data() {
    return {
      activeTab: 'repositories',
      collecting: false,
      currentCourseId: 1, // 从路由参数或store获取当前课程ID
      repositories: [],
      filteredRepositories: [],
      repositoriesLoading: false,
      selectedRepository: null,
      collectionForm: {
        selectedRepositoryId: null
      },
      // 批量操作相关
      showBatchOperations: false,
      selectedRepositories: [],
      selectAll: false,
      isIndeterminate: false,
      batchSyncing: false,
      // 高级筛选相关
      showAdvancedFilter: false,
      repositoryFilter: {
        platform: '',
        status: '',
        courseId: '',
        keyword: ''
      },
      courses: [],
      syncConfig: {
        frequency: 'daily',
        dataTypes: ['commits', 'pull_requests', 'file_changes'],
        dateRange: [],
        filters: ''
      },
      preprocessRules: [
        {
          id: 1,
          name: '移除空提交',
          type: 'cleaning',
          condition: 'lines_added == 0 && lines_deleted == 0',
          action: 'remove_empty_commits',
          parameters: '{}',
          enabled: true
        },
        {
          id: 2,
          name: '计算代码行数',
          type: 'transformation',
          condition: 'file_type in ["js", "vue", "py"]',
          action: 'calculate_lines',
          parameters: '{"exclude_comments": true}',
          enabled: true
        }
      ],
      collectionStats: {
        totalRepos: 5,
        syncedRepos: 3,
        totalCommits: 1247,
        processedData: 856,
        reposTrend: 2,
        commitsTrend: 156,
        processingRate: 45,
        errors: []
      },
      statusLoading: false,
      realTimeMonitoring: false,
      connectionStatus: {
        connected: false,
        message: '实时监控已关闭',
        lastUpdate: null
      },
      showErrorDetails: false,
      activeTasks: [
        {
          id: 1,
          name: '同步仓库数据',
          status: '进行中',
          progress: 65,
          repository: 'student/project-a',
          duration: 120,
          eta: 60,
          currentStep: '获取提交记录',
          logs: [
            {
              id: 1,
              timestamp: new Date(),
              message: '开始获取提交记录'
            }
          ]
        },
        {
          id: 2,
          name: '数据预处理',
          status: '等待中',
          progress: 0,
          repository: 'student/project-b',
          duration: 0,
          eta: null,
          currentStep: null,
          logs: []
        }
      ],
      collectionLogs: [
        {
          id: 1,
          timestamp: new Date(),
          level: 'info',
          message: '开始同步仓库 student/project-a'
        },
        {
          id: 2,
          timestamp: new Date(Date.now() - 60000),
          level: 'success',
          message: '成功获取 125 条提交记录'
        },
        {
          id: 3,
          timestamp: new Date(Date.now() - 120000),
          level: 'warning',
          message: '检测到重复提交记录，已自动合并'
        }
      ],
      nextRuleId: 3,
      // GitHub Token状态
      tokenStatus: {
        loading: false,
        status: 'not_configured', // 'valid', 'invalid', 'not_configured'
        message: ''
      },
      courses: [] // 课程列表
    }
  },
  computed: {
    hasRunningTasks() {
      return this.activeTasks.some(task => task.status === '进行中');
    },
    hasPausedTasks() {
      return this.activeTasks.some(task => task.status === '暂停');
    },
    hasActiveTasks() {
      return this.activeTasks.some(task => 
        task.status === '进行中' || task.status === '暂停' || task.status === '等待中'
      );
    }
  },
  mounted() {
    this.initializeCourseId()
    this.loadRepositories()
    this.loadSyncConfig()
    this.refreshStatus()
  },
  watch: {
    repositories: {
      handler(newVal) {
        this.filteredRepositories = [...newVal];
      },
      immediate: true
    },
    selectedRepositories: {
      handler(newVal) {
        if (newVal.length === 0) {
          this.selectAll = false;
          this.isIndeterminate = false;
        } else if (newVal.length === this.filteredRepositories.length) {
          this.selectAll = true;
          this.isIndeterminate = false;
        } else {
          this.selectAll = false;
          this.isIndeterminate = true;
        }
      }
    }
  },
  methods: {
    async loadCourses(retryCount = 0) {
      const maxRetries = 3;
      const cacheKey = 'courses_cache';
      const cacheTimeout = 5 * 60 * 1000; // 5分钟缓存
      
      try {
        // 检查缓存
        const cachedData = this.getCachedData(cacheKey, cacheTimeout);
        if (cachedData) {
          this.courses = cachedData;
          console.log('从缓存加载课程列表:', this.courses);
          return;
        }
        
        const organizationId = this.getOrganizationId()
        const requestParams = {
          organizationId: organizationId,
          pageNum: 1,
          pageSize: 100
        }
        
        const response = await this.$http.get('/courses', {
          params: requestParams
        })
        
        if (response.data.code === '200') {
          const data = response.data.data || {}
          // 检查数据结构，如果是分页对象则提取records，否则直接使用
          if (data.records && Array.isArray(data.records)) {
            this.courses = data.records
            console.log('加载课程列表成功（分页数据）：', this.courses, '总数：', data.total)
          } else if (Array.isArray(data)) {
            this.courses = data
            console.log('加载课程列表成功（数组数据）：', this.courses)
          } else {
            console.warn('课程数据格式异常：', data)
            this.courses = []
          }
          
          // 缓存数据
          this.setCachedData(cacheKey, this.courses);
        } else {
          console.error('加载课程列表失败：', response.data)
          this.courses = []
          
          // API返回错误时也尝试重试
          if (retryCount < maxRetries) {
            console.log(`API返回错误，重试加载课程列表，第${retryCount + 1}次重试`);
            await this.delay(1000 * (retryCount + 1));
            return this.loadCourses(retryCount + 1);
          }
        }
      } catch (error) {
        console.error('加载课程列表失败：', error)
        
        // 重试机制
        if (retryCount < maxRetries) {
          console.log(`重试加载课程列表，第${retryCount + 1}次重试`);
          await this.delay(1000 * (retryCount + 1)); // 递增延迟
          return this.loadCourses(retryCount + 1);
        }
        
        this.$message.error(`加载课程列表失败: ${error.message || '网络错误'}`);
        this.courses = []
      }
    },
    async loadRepositories(retryCount = 0) {
      const maxRetries = 3;
      const cacheKey = 'repositories_cache';
      const cacheTimeout = 3 * 60 * 1000; // 3分钟缓存
      
      try {
        this.repositoriesLoading = true;
        
        // 检查缓存
        const cachedData = this.getCachedData(cacheKey, cacheTimeout);
        if (cachedData) {
          this.repositories = cachedData;
          console.log('从缓存加载仓库列表:', this.repositories);
          return;
        }
        
        // 如果没有课程列表，先加载课程
        if (!this.courses || this.courses.length === 0) {
          console.log('没有课程列表，先加载课程')
          await this.loadCourses()
        }
        
        // 如果仍然没有课程，使用原来的方法
        if (!this.courses || this.courses.length === 0) {
          console.log('没有可用的课程，使用原来的方法加载仓库')
          const response = await this.$http.get('/git-data/repositories/all')
          if (response.data.code === '200') {
            this.repositories = response.data.data || []
            this.setCachedData(cacheKey, this.repositories);
            console.log('加载仓库列表成功：', this.repositories)
          } else {
            throw new Error('获取所有仓库失败: ' + (response.data.message || '未知错误'));
          }
          return
        }
        
        console.log('开始为所有课程加载仓库，课程数量:', this.courses.length)
        
        // 确保courses是数组，如果不是则重新加载
        if (!Array.isArray(this.courses)) {
          console.error('courses不是数组，重新加载课程数据:', this.courses)
          await this.loadCourses()
          // 重新检查，如果还是不是数组则使用备用方法
          if (!Array.isArray(this.courses)) {
            console.log('重新加载后courses仍不是数组，使用备用方法加载仓库')
            const response = await this.$http.get('/git-data/repositories/all')
            if (response.data.code === '200') {
              this.repositories = response.data.data || []
              this.setCachedData(cacheKey, this.repositories);
              console.log('使用备用方法加载仓库列表成功：', this.repositories)
            } else {
              throw new Error('备用方法获取仓库失败: ' + (response.data.message || '未知错误'));
            }
            return
          }
        }
        
        // 为所有课程并发获取仓库（限制并发数量）
        const batchSize = 5; // 限制并发数量
        const allRepositories = [];
        
        for (let i = 0; i < this.courses.length; i += batchSize) {
          const batch = this.courses.slice(i, i + batchSize);
          const repositoryPromises = batch.map(async (course) => {
            try {
              const url = '/git-data/repositories'
              const courseIdValue = course.courseId
              const params = { courseId: courseIdValue }
              
              console.log(`为课程 ${course.courseName}(courseId: ${courseIdValue}) 获取仓库`)
              
              const response = await this.$http.get(url, { params })
              
              if (response.data.code === '200') {
                const repositories = response.data.data || []
                // 为每个仓库添加课程信息
                return repositories.map(repo => ({
                  ...repo,
                  courseName: course.courseName,
                  courseId: courseIdValue
                }))
              } else {
                console.log(`课程 ${course.courseName} API返回失败:`, response.data)
                return []
              }
            } catch (error) {
              console.error(`课程 ${course.courseName} 加载仓库失败:`, error)
              return []
            }
          })
          
          // 等待当前批次完成
          const batchResults = await Promise.all(repositoryPromises);
          allRepositories.push(...batchResults.flat());
          
          // 批次间添加小延迟，避免请求过于频繁
          if (i + batchSize < this.courses.length) {
            await this.delay(200);
          }
        }
        
        this.repositories = allRepositories;
        
        // 缓存数据
        this.setCachedData(cacheKey, this.repositories);
        
        console.log('所有仓库加载完成，总数量:', this.repositories.length)
        console.log('合并后的仓库列表:', this.repositories)
        
      } catch (error) {
        console.error('加载仓库列表失败：', error)
        
        // 重试机制
        if (retryCount < maxRetries) {
          console.log(`重试加载仓库列表，第${retryCount + 1}次重试`);
          await this.delay(2000 * (retryCount + 1)); // 递增延迟
          return this.loadRepositories(retryCount + 1);
        }
        
        this.$message.error(`加载仓库列表失败: ${error.message || '网络错误'}，请检查网络连接`)
      } finally {
        this.repositoriesLoading = false;
      }
    },
    async loadSyncConfig(retryCount = 0) {
      const maxRetries = 3;
      const cacheKey = 'sync_config';
      
      // 尝试从缓存获取
      const cachedConfig = this.getCache(cacheKey);
      if (cachedConfig && retryCount === 0) {
        this.syncConfig = { ...this.syncConfig, ...cachedConfig };
        console.log('从缓存加载同步配置');
        return;
      }
      
      try {
        const response = await this.retryApiCall(
          () => this.$http.get('/data-collection/sync-config'),
          maxRetries
        );
        
        if (response.data.data) {
          this.syncConfig = { ...this.syncConfig, ...response.data.data };
          // 缓存配置数据，缓存10分钟
          this.setCache(cacheKey, response.data.data, 10);
        }
      } catch (error) {
        console.error('加载同步配置失败：', error);
        this.$message.error(`加载同步配置失败: ${error.message || '网络错误'}`);
      }
    },
    async startDataCollection() {
      this.collecting = true
      try {
        // 同步所有仓库数据
        for (const repo of this.repositories) {
          await this.syncRepository(repo)
        }
        this.$message.success('数据采集已启动')
        this.refreshStatus()
      } catch (error) {
        console.error('启动数据采集失败：', error)
        this.$message.error('启动数据采集失败')
      } finally {
        this.collecting = false
      }
    },
    async syncRepository(repo) {
      const maxRetries = 3;
      this.$set(repo, 'syncing', true);
      
      try {
        await this.retryApiCall(
          () => this.$http.post(`/git-data/repositories/${repo.repositoryId}/sync`),
          maxRetries
        );
        
        this.$message.success(`仓库 ${repo.repositoryName} 同步成功`);
        // 清除相关缓存，强制重新加载
        this.clearCache('repositories');
        this.loadRepositories();
      } catch (error) {
        console.error('仓库同步失败：', error);
        this.$message.error(`仓库 ${repo.repositoryName} 同步失败: ${error.message || '网络错误'}`);
      } finally {
        this.$set(repo, 'syncing', false);
      }
    },
    onRepositoryChange(repositoryId) {
      this.selectedRepository = this.repositories.find(repo => repo.repositoryId === repositoryId)
      // 选择仓库后更新课程ID并检查GitHub Token状态
      if (this.selectedRepository) {
        // 从选择的仓库获取课程ID
        this.currentCourseId = this.selectedRepository.courseId
        console.log('选择仓库后更新课程ID：', this.currentCourseId, '仓库：', this.selectedRepository.repositoryName)
        this.checkGitHubTokenStatus()
      }
    },
    goToRepoManagement() {
      // 跳转到Git仓库管理页面
      this.$router.push('/git-repository-management')
    },
    async saveSyncConfig() {
      const maxRetries = 3;
      const cacheKey = 'sync_config';
      
      try {
        await this.retryApiCall(
          () => this.$http.post('/data-collection/sync-config', this.syncConfig),
          maxRetries
        );
        
        this.$message.success('同步配置保存成功');
        // 更新缓存
        this.setCache(cacheKey, this.syncConfig, 10);
      } catch (error) {
        console.error('保存同步配置失败：', error);
        this.$message.error(`保存同步配置失败: ${error.message || '网络错误'}`);
      }
    },
    addPreprocessRule() {
      this.preprocessRules.push({
        id: this.nextRuleId++,
        name: '新规则',
        type: 'cleaning',
        condition: '',
        action: 'remove_empty_commits',
        parameters: '{}',
        enabled: true
      })
    },
    removePreprocessRule(index) {
      this.preprocessRules.splice(index, 1)
    },
    async refreshStatus() {
      const maxRetries = 2; // 状态刷新重试次数较少
      
      try {
        const response = await this.retryApiCall(
          () => this.$http.get('/data-collection/status'),
          maxRetries,
          500 // 较短的重试延迟
        );
        
        if (response.data.data) {
          this.collectionStats = response.data.data.stats || this.collectionStats;
          this.activeTasks = response.data.data.tasks || this.activeTasks;
          this.collectionLogs = response.data.data.logs || this.collectionLogs;
        }
      } catch (error) {
        console.error('刷新状态失败：', error);
        // 状态刷新失败不显示错误消息，避免干扰用户
      }
    },
    getRepoStatusType(status) {
      const statusMap = {
        'connected': 'success',
        'disconnected': 'danger',
        'syncing': 'warning',
        'error': 'danger'
      }
      return statusMap[status] || 'info'
    },
    getRepoStatusText(status) {
      const statusMap = {
        'connected': '已连接',
        'disconnected': '未连接',
        'syncing': '同步中',
        'error': '错误'
      }
      return statusMap[status] || '未知'
    },
    formatDateTime(datetime) {
      if (!datetime) return ''
      return new Date(datetime).toLocaleString('zh-CN')
    },
    formatTime(time) {
      return new Date(time).toLocaleTimeString('zh-CN')
    },
    
    getSyncProgress() {
      if (this.collectionStats.totalRepos === 0) return 0;
      return Math.round((this.collectionStats.syncedRepos / this.collectionStats.totalRepos) * 100);
    },
    
    getTrendIcon(trend) {
      return trend > 0 ? 'el-icon-top' : 'el-icon-bottom';
    },
    
    formatNumber(num) {
      if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k';
      }
      return num.toString();
    },
    
    toggleRealTimeMonitoring(enabled) {
      this.realTimeMonitoring = enabled;
      if (enabled) {
        this.connectionStatus.connected = true;
        this.connectionStatus.message = '实时监控已启用，正在接收数据更新';
        this.connectionStatus.lastUpdate = new Date();
        // 这里可以建立WebSocket连接
      } else {
        this.connectionStatus.connected = false;
        this.connectionStatus.message = '实时监控已关闭';
        this.connectionStatus.lastUpdate = null;
      }
    },
    
    exportCollectionReport() {
      // 导出采集报告的逻辑
      this.$message.success('报告导出功能开发中');
    },
    
    retryFailedTask(error) {
      this.$set(error, 'retrying', true);
      // 模拟重试逻辑
      setTimeout(() => {
        this.$set(error, 'retrying', false);
        this.$message.success('任务重试成功');
        // 从错误列表中移除
        const index = this.collectionStats.errors.indexOf(error);
        if (index > -1) {
          this.collectionStats.errors.splice(index, 1);
        }
      }, 2000);
    },
    getOrganizationId() {
      // 从localStorage或其他地方获取组织ID
      return localStorage.getItem('organizationId') || '1'
    },
    
    // 初始化课程ID
    initializeCourseId() {
      // 从路由参数获取课程ID
      this.currentCourseId = this.$route.params.courseId || 
                            this.$route.query.courseId || 
                            this.$store?.state?.currentCourse?.courseId || 
                            localStorage.getItem('currentCourseId') || 
                            null // 设置为null，等待选择仓库后获取
      console.log('初始化课程ID：', this.currentCourseId)
    },
    
    // 获取Git活动记录
    async loadGitActivities(repositoryId) {
      try {
        const response = await this.$http.get(`/git-data/activities/${repositoryId}`, {
          params: {
            pageNum: 1,
            pageSize: 100
          }
        })
        return response.data.data || []
      } catch (error) {
        console.error('获取Git活动记录失败：', error)
        return []
      }
    },
    
    // 检查GitHub Token状态
    async checkGitHubTokenStatus() {
      // 如果没有课程ID，不进行检查
      if (!this.currentCourseId) {
        console.log('课程ID为空，跳过GitHub Token状态检查')
        this.tokenStatus.status = 'not_configured'
        this.tokenStatus.message = '请先选择仓库'
        return
      }
      
      this.tokenStatus.loading = true
      try {
        console.log('检查GitHub Token状态，课程ID：', this.currentCourseId)
        const response = await this.$http.get(`/github-config/course/${this.currentCourseId}`)
        
        if (response.data.code === '200' && response.data.data) {
          const config = response.data.data
          console.log('获取到GitHub配置：', {
            id: config.id,
            courseId: config.courseId,
            tokenStatus: config.tokenStatus,
            createdAt: config.createdAt || config.created_at,
            updatedAt: config.updatedAt || config.updated_at
          })
          
          // 标准化Token状态
          const normalizedStatus = this.normalizeTokenStatus(config.tokenStatus)
          this.tokenStatus.status = normalizedStatus
          this.tokenStatus.message = this.getTokenStatusMessage(normalizedStatus)
        } else {
          console.log('未找到GitHub配置或配置无效')
          this.tokenStatus.status = 'not_configured'
          this.tokenStatus.message = '未配置GitHub Token'
        }
      } catch (error) {
        console.error('检查GitHub Token状态失败：', error)
        if (error.response && error.response.status === 404) {
          this.tokenStatus.status = 'not_configured'
          this.tokenStatus.message = '未配置GitHub Token'
        } else {
          this.tokenStatus.status = 'invalid'
          this.tokenStatus.message = '检查Token状态失败'
        }
      } finally {
        this.tokenStatus.loading = false
      }
    },
    
    // 标准化Token状态
    normalizeTokenStatus(status) {
      if (!status) return 'not_configured'
      
      const statusStr = status.toString().toLowerCase()
      if (statusStr === 'active' || statusStr === 'valid' || statusStr === '1') {
        return 'valid'
      } else if (statusStr === 'inactive' || statusStr === 'invalid' || statusStr === '0') {
        return 'invalid'
      }
      return 'not_configured'
    },
    
    // 获取Token状态消息
    getTokenStatusMessage(status) {
      const messages = {
        'valid': '可正常进行数据采集',
        'invalid': 'Token已过期或无效，请重新配置',
        'not_configured': '需要配置Token才能进行数据采集'
      }
      return messages[status] || '状态未知'
    },
    
    // 跳转到Token配置页面
    goToTokenConfig() {
      this.$router.push({
        path: '/git-repository-management',
        query: {
          tab: 'github-config',
          courseId: this.currentCourseId
        }
      })
    },
    
    // 任务控制方法
    pauseAllTasks() {
      this.activeTasks.forEach(task => {
        if (task.status === '进行中') {
          task.status = '暂停';
        }
      });
      this.$message.success('已暂停所有任务');
    },
    
    resumeAllTasks() {
      this.activeTasks.forEach(task => {
        if (task.status === '暂停') {
          task.status = '进行中';
        }
      });
      this.$message.success('已恢复所有任务');
    },
    
    cancelAllTasks() {
      this.$confirm('确定要取消所有任务吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.activeTasks.forEach(task => {
          if (task.status !== '已完成') {
            task.status = '已取消';
          }
        });
        this.$message.success('已取消所有任务');
      }).catch(() => {});
    },
    
    toggleTaskStatus(task) {
      if (task.status === '进行中') {
        task.status = '暂停';
        this.$message.info(`任务 "${task.name}" 已暂停`);
      } else if (task.status === '暂停') {
        task.status = '进行中';
        this.$message.info(`任务 "${task.name}" 已恢复`);
      }
    },
    
    cancelTask(task) {
      this.$confirm(`确定要取消任务 "${task.name}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        task.status = '已取消';
        this.$message.success(`任务 "${task.name}" 已取消`);
      }).catch(() => {});
    },
    
    getTaskStatusType(status) {
      const statusMap = {
        '进行中': 'primary',
        '暂停': 'warning',
        '已完成': 'success',
        '已取消': 'danger',
        '等待中': 'info'
      };
      return statusMap[status] || 'info';
    },
    
    // 批量操作相关方法
    handleSelectAll(val) {
      this.selectedRepositories = val ? this.filteredRepositories.map(repo => repo.repositoryId) : [];
      this.isIndeterminate = false;
    },
    
    async batchSyncRepositories() {
      if (this.selectedRepositories.length === 0) {
        this.$message.warning('请先选择要同步的仓库');
        return;
      }
      
      this.batchSyncing = true;
      const totalRepos = this.selectedRepositories.length;
      let successCount = 0;
      let failedCount = 0;
      const failedRepos = [];
      
      try {
        // 限制并发数量，每次处理3个仓库
        const batchSize = 3;
        const selectedRepos = this.selectedRepositories.map(repoId => 
          this.repositories.find(r => r.repositoryId === repoId)
        ).filter(repo => repo); // 过滤掉找不到的仓库
        
        for (let i = 0; i < selectedRepos.length; i += batchSize) {
          const batch = selectedRepos.slice(i, i + batchSize);
          
          // 并发处理当前批次
          const batchPromises = batch.map(async (repo) => {
            try {
              await this.syncRepository(repo);
              successCount++;
            } catch (error) {
              failedCount++;
              failedRepos.push({
                name: repo.repositoryName,
                error: error.message || '同步失败'
              });
            }
          });
          
          await Promise.all(batchPromises);
          
          // 批次间延迟，避免服务器压力过大
          if (i + batchSize < selectedRepos.length) {
            await this.delay(500);
          }
        }
        
        // 显示结果统计
        if (failedCount === 0) {
          this.$message.success(`成功同步 ${successCount} 个仓库`);
        } else {
          this.$message.warning(`同步完成：成功 ${successCount} 个，失败 ${failedCount} 个`);
          
          // 显示失败详情
          if (failedRepos.length > 0) {
            const failedNames = failedRepos.map(repo => repo.name).join('、');
            this.$notify({
              title: '部分仓库同步失败',
              message: `失败的仓库：${failedNames}`,
              type: 'warning',
              duration: 8000
            });
          }
        }
        
        this.selectedRepositories = [];
        this.selectAll = false;
        this.isIndeterminate = false;
        
      } catch (error) {
        console.error('批量同步过程中发生错误：', error);
        this.$message.error('批量同步过程中发生错误');
      } finally {
        this.batchSyncing = false;
      }
    },
    
    batchConfigureTokens() {
      this.$message.info('批量配置Token功能开发中');
    },
    
    batchExportData() {
      if (this.selectedRepositories.length === 0) {
        this.$message.warning('请先选择要导出的仓库');
        return;
      }
      
      const selectedRepos = this.repositories.filter(repo => 
        this.selectedRepositories.includes(repo.repositoryId)
      );
      
      const exportData = {
        exportTime: new Date().toISOString(),
        repositories: selectedRepos.map(repo => ({
          repositoryId: repo.repositoryId,
          repositoryName: repo.repositoryName,
          repositoryUrl: repo.repositoryUrl,
          platform: repo.platform,
          courseName: repo.courseName,
          status: repo.status,
          lastSync: repo.lastSync
        }))
      };
      
      const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `repositories_export_${new Date().toISOString().split('T')[0]}.json`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
      
      this.$message.success('数据导出成功');
    },
    
    viewRepositoryDetails(repo) {
      this.$router.push({
        path: '/git-activity-monitor',
        query: { repositoryId: repo.repositoryId }
      });
    },
    
    getPlatformType(platform) {
      const platformMap = {
        'github': 'success',
        'gitlab': 'warning',
        'gitee': 'primary'
      };
      return platformMap[platform.toLowerCase()] || 'info';
    },
    
    // 高级筛选相关方法
    applyRepositoryFilter() {
      this.filteredRepositories = this.repositories.filter(repo => {
        // 平台筛选
        if (this.repositoryFilter.platform && 
            repo.platform.toLowerCase() !== this.repositoryFilter.platform.toLowerCase()) {
          return false;
        }
        
        // 状态筛选
        if (this.repositoryFilter.status && repo.status !== this.repositoryFilter.status) {
          return false;
        }
        
        // 课程筛选
        if (this.repositoryFilter.courseId && 
            repo.courseId !== this.repositoryFilter.courseId) {
          return false;
        }
        
        // 关键词筛选
        if (this.repositoryFilter.keyword) {
          const keyword = this.repositoryFilter.keyword.toLowerCase();
          const repoName = repo.repositoryName.toLowerCase();
          const repoUrl = repo.repositoryUrl.toLowerCase();
          if (!repoName.includes(keyword) && !repoUrl.includes(keyword)) {
            return false;
          }
        }
        
        return true;
      });
      
      this.$message.success(`筛选完成，找到 ${this.filteredRepositories.length} 个仓库`);
    },
    
    resetRepositoryFilter() {
      this.repositoryFilter = {
        platform: '',
        status: '',
        courseId: '',
        keyword: ''
      };
      this.filteredRepositories = [...this.repositories];
      this.$message.info('筛选条件已重置');
    },
    
    formatDuration(seconds) {
      if (!seconds) return '0秒';
      const hours = Math.floor(seconds / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);
      const secs = seconds % 60;
      
      if (hours > 0) {
        return `${hours}小时${minutes}分钟`;
      } else if (minutes > 0) {
        return `${minutes}分钟${secs}秒`;
      } else {
        return `${secs}秒`;
      }
    },
    
    getProgressStatus(task) {
       if (task.status === '已完成') return 'success';
       if (task.status === '已取消') return 'exception';
       if (task.status === '暂停') return 'warning';
       return null;
     },
    
    formatDuration(seconds) {
      if (!seconds) return '0秒';
      const hours = Math.floor(seconds / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);
      const secs = seconds % 60;
      
      if (hours > 0) {
        return `${hours}小时${minutes}分钟${secs}秒`;
      } else if (minutes > 0) {
        return `${minutes}分钟${secs}秒`;
      } else {
        return `${secs}秒`;
      }
    },

    // 缓存辅助方法
    getCacheKey(key) {
      return `datacollection_${key}`;
    },

    setCache(key, data, ttlMinutes = 5) {
      const cacheData = {
        data,
        timestamp: Date.now(),
        ttl: ttlMinutes * 60 * 1000
      };
      localStorage.setItem(this.getCacheKey(key), JSON.stringify(cacheData));
    },

    getCache(key) {
      try {
        const cached = localStorage.getItem(this.getCacheKey(key));
        if (!cached) return null;
        
        const cacheData = JSON.parse(cached);
        const now = Date.now();
        
        if (now - cacheData.timestamp > cacheData.ttl) {
          localStorage.removeItem(this.getCacheKey(key));
          return null;
        }
        
        return cacheData.data;
      } catch (error) {
        console.error('缓存读取失败:', error);
        return null;
      }
    },

    clearCache(key) {
      localStorage.removeItem(this.getCacheKey(key));
    },

    // 延迟辅助方法
    delay(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    },

    // API重试辅助方法
    async retryApiCall(apiCall, maxRetries = 3, delayMs = 1000) {
      for (let attempt = 1; attempt <= maxRetries; attempt++) {
        try {
          return await apiCall();
        } catch (error) {
          console.warn(`API调用失败 (尝试 ${attempt}/${maxRetries}):`, error.message);
          
          if (attempt === maxRetries) {
            throw error;
          }
          
          // 指数退避延迟
          const backoffDelay = delayMs * Math.pow(2, attempt - 1);
          await this.delay(backoffDelay);
        }
      }
    }
  }
}
</script>

<style scoped>
.data-collection {
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

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 20px;
  display: block;
}

.rules-list {
  display: grid;
  gap: 20px;
}

.rule-card {
  border: 1px solid #ebeef5;
}

.rule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  gap: 15px;
}

.rule-name {
  flex: 1;
}

.status-overview {
  margin-bottom: 30px;
}

.status-card {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.status-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.status-label {
  color: #606266;
  font-size: 14px;
}

.progress-section {
  margin-bottom: 30px;
}

.progress-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.task-progress {
  margin-bottom: 15px;
}

.task-info {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.task-name {
  font-weight: 500;
  color: #303133;
}

.task-status {
  color: #909399;
  font-size: 14px;
}

.logs-section h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.logs-container {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
  background: #fafafa;
}

.log-item {
  display: flex;
  align-items: center;
  padding: 5px 0;
  border-bottom: 1px solid #f0f0f0;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.log-item:last-child {
  border-bottom: none;
}

.log-time {
  color: #909399;
  margin-right: 10px;
  min-width: 80px;
}

.log-level {
  margin-right: 10px;
  min-width: 60px;
  font-weight: bold;
}

.log-item.info .log-level {
  color: #409eff;
}

.log-item.success .log-level {
  color: #67c23a;
}

.log-item.warning .log-level {
  color: #e6a23c;
}

.log-item.error .log-level {
  color: #f56c6c;
}

.log-message {
  flex: 1;
  color: #303133;
}

.danger {
  color: #f56c6c;
}

.danger:hover {
  color: #f56c6c;
  background-color: #fef0f0;
}

.repo-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

/* GitHub Token状态样式 */
.token-loading {
  color: #909399;
}

.token-valid {
  color: #67c23a;
}

.token-invalid {
  color: #e6a23c;
}

.token-not-configured {
  color: #909399;
}

.token-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.config-token-btn {
  margin-left: 8px;
  font-size: 12px;
}

.repo-info p {
  margin: 8px 0;
  color: #606266;
}

.repo-info strong {
  color: #303133;
}

/* Element UI 样式覆盖 */
.data-collection .el-button--primary {
  color: #fff;
  background-color: #131313;
  border-color: #131313;
}

.data-collection .el-button--primary:focus,
.data-collection .el-button--primary:hover {
  background: #404040;
  border-color: #404040;
  color: #fff;
}

.data-collection .el-table th {
  background-color: #fafafa;
  color: #303133;
}

.data-collection .el-tabs--card > .el-tabs__header .el-tabs__nav {
  border: 1px solid #e4e7ed;
  border-bottom: none;
  border-radius: 4px 4px 0 0;
}

.data-collection .el-tabs--card > .el-tabs__header .el-tabs__item {
  border-bottom: 1px solid #e4e7ed;
  border-right: 1px solid #e4e7ed;
}

.data-collection .el-tabs--card > .el-tabs__header .el-tabs__item.is-active {
  background-color: #fff;
  border-bottom-color: #fff;
}

/* 任务进度监控样式 */
.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.progress-controls {
  display: flex;
  gap: 8px;
}

.no-tasks {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.no-tasks i {
  font-size: 48px;
  color: #67C23A;
  margin-bottom: 16px;
}

.task-progress.enhanced {
  background: #fff;
  border: 1px solid #EBEEF5;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.task-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.task-name {
  font-weight: 600;
  font-size: 16px;
  color: #303133;
}

.task-actions {
  display: flex;
  gap: 8px;
}

.task-details {
  margin-top: 16px;
}

.task-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

.task-repository {
  color: #409EFF;
  font-weight: 500;
}

.progress-text {
  font-weight: 600;
}

.progress-detail {
  color: #909399;
  margin-left: 8px;
}

.task-logs {
  margin-top: 12px;
  background: #F5F7FA;
  border-radius: 4px;
  padding: 12px;
}

.task-log {
  display: flex;
  gap: 12px;
  margin-bottom: 4px;
  font-size: 12px;
}

.task-log:last-child {
  margin-bottom: 0;
}

.task-log .log-time {
  color: #909399;
  min-width: 60px;
}

.task-log .log-message {
  color: #606266;
}

/* 连接状态样式 */
.connection-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 12px;
  margin-bottom: 16px;
}

.connection-status.connected {
  background: #F0F9FF;
  color: #409EFF;
  border: 1px solid #B3D8FF;
}

.connection-status.disconnected {
  background: #FDF6EC;
  color: #E6A23C;
  border: 1px solid #F5DAB1;
}

/* 状态卡片增强样式 */
.status-card {
  position: relative;
  overflow: hidden;
}

.status-card.status-loading::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #409EFF, transparent);
  animation: loading 2s infinite;
}

@keyframes loading {
  0% { left: -100%; }
  100% { left: 100%; }
}

.status-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  margin-top: 4px;
}

.status-trend.positive {
  color: #67C23A;
}

.status-trend.negative {
  color: #F56C6C;
}

.status-rate {
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

/* 错误统计样式 */
.error-summary {
  margin-bottom: 20px;
}

.error-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}
</style>