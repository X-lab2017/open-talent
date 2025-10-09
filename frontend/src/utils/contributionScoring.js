/**
 * 智能化贡献度评分算法
 * 基于9种Git活动类型和多维度评分体系
 */

// Git活动类型权重配置
const GIT_ACTIVITY_WEIGHTS = {
  commit: 1.0,           // 提交代码
  push: 0.8,             // 推送代码
  pull_request: 1.5,     // 拉取请求
  merge: 1.2,            // 合并代码
  review: 1.3,           // 代码审查
  issue: 0.9,            // 问题报告
  comment: 0.6,          // 评论
  fork: 0.7,             // 分叉项目
  star: 0.3              // 星标项目
};

// 评分维度权重
const DIMENSION_WEIGHTS = {
  codeContribution: 0.3,    // 代码贡献 30%
  collaboration: 0.25,      // 协作能力 25%
  codeQuality: 0.2,         // 代码质量 20%
  activity: 0.15,           // 活跃度 15%
  innovation: 0.1           // 创新性 10%
};

// 时间衰减因子（天数）
const TIME_DECAY_FACTOR = {
  recent: 1.0,      // 7天内
  week: 0.9,        // 7-30天
  month: 0.7,       // 30-90天
  quarter: 0.5,     // 90-180天
  old: 0.3          // 180天以上
};

/**
 * 计算时间衰减系数
 * @param {Date} activityDate 活动日期
 * @returns {number} 衰减系数
 */
function calculateTimeDecay(activityDate) {
  const now = new Date();
  const daysDiff = Math.floor((now - new Date(activityDate)) / (1000 * 60 * 60 * 24));
  
  if (daysDiff <= 7) return TIME_DECAY_FACTOR.recent;
  if (daysDiff <= 30) return TIME_DECAY_FACTOR.week;
  if (daysDiff <= 90) return TIME_DECAY_FACTOR.month;
  if (daysDiff <= 180) return TIME_DECAY_FACTOR.quarter;
  return TIME_DECAY_FACTOR.old;
}

/**
 * 计算代码贡献得分
 * @param {Array} activities Git活动数据
 * @returns {number} 代码贡献得分 (0-100)
 */
function calculateCodeContribution(activities) {
  const codeActivities = activities.filter(activity => 
    ['commit', 'push', 'pull_request', 'merge'].includes(activity.type)
  );
  
  let totalScore = 0;
  let maxPossibleScore = 0;
  
  codeActivities.forEach(activity => {
    const baseWeight = GIT_ACTIVITY_WEIGHTS[activity.type] || 0;
    const timeDecay = calculateTimeDecay(activity.date);
    const complexityMultiplier = calculateComplexityMultiplier(activity);
    
    const score = baseWeight * timeDecay * complexityMultiplier;
    totalScore += score;
    maxPossibleScore += baseWeight;
  });
  
  return maxPossibleScore > 0 ? Math.min(100, (totalScore / maxPossibleScore) * 100) : 0;
}

/**
 * 计算协作能力得分
 * @param {Array} activities Git活动数据
 * @returns {number} 协作能力得分 (0-100)
 */
function calculateCollaboration(activities) {
  const collaborationActivities = activities.filter(activity => 
    ['review', 'comment', 'pull_request', 'issue'].includes(activity.type)
  );
  
  let totalScore = 0;
  let maxPossibleScore = 0;
  
  collaborationActivities.forEach(activity => {
    const baseWeight = GIT_ACTIVITY_WEIGHTS[activity.type] || 0;
    const timeDecay = calculateTimeDecay(activity.date);
    const interactionMultiplier = calculateInteractionMultiplier(activity);
    
    const score = baseWeight * timeDecay * interactionMultiplier;
    totalScore += score;
    maxPossibleScore += baseWeight;
  });
  
  return maxPossibleScore > 0 ? Math.min(100, (totalScore / maxPossibleScore) * 100) : 0;
}

/**
 * 计算代码质量得分
 * @param {Array} activities Git活动数据
 * @returns {number} 代码质量得分 (0-100)
 */
function calculateCodeQuality(activities) {
  const commits = activities.filter(activity => activity.type === 'commit');
  
  if (commits.length === 0) return 0;
  
  let qualityScore = 0;
  let totalCommits = commits.length;
  
  commits.forEach(commit => {
    let commitQuality = 50; // 基础分数
    
    // 提交信息质量评估
    if (commit.message) {
      const messageQuality = evaluateCommitMessage(commit.message);
      commitQuality += messageQuality * 0.3;
    }
    
    // 代码变更量评估
    if (commit.changes) {
      const changeQuality = evaluateCodeChanges(commit.changes);
      commitQuality += changeQuality * 0.4;
    }
    
    // 测试覆盖率评估
    if (commit.testCoverage) {
      commitQuality += commit.testCoverage * 0.3;
    }
    
    const timeDecay = calculateTimeDecay(commit.date);
    qualityScore += commitQuality * timeDecay;
  });
  
  return Math.min(100, qualityScore / totalCommits);
}

/**
 * 计算活跃度得分
 * @param {Array} activities Git活动数据
 * @returns {number} 活跃度得分 (0-100)
 */
function calculateActivity(activities) {
  const now = new Date();
  const thirtyDaysAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
  
  const recentActivities = activities.filter(activity => 
    new Date(activity.date) >= thirtyDaysAgo
  );
  
  // 计算活跃天数
  const activeDays = new Set(
    recentActivities.map(activity => 
      new Date(activity.date).toDateString()
    )
  ).size;
  
  // 计算活动频率
  const activityFrequency = recentActivities.length;
  
  // 计算连续活跃天数
  const consecutiveDays = calculateConsecutiveActiveDays(activities);
  
  // 综合计算活跃度
  const dayScore = Math.min(100, (activeDays / 30) * 100);
  const frequencyScore = Math.min(100, (activityFrequency / 50) * 100);
  const consistencyScore = Math.min(100, (consecutiveDays / 14) * 100);
  
  return (dayScore * 0.4 + frequencyScore * 0.3 + consistencyScore * 0.3);
}

/**
 * 计算创新性得分
 * @param {Array} activities Git活动数据
 * @returns {number} 创新性得分 (0-100)
 */
function calculateInnovation(activities) {
  let innovationScore = 0;
  
  // 新项目创建
  const newRepos = activities.filter(activity => 
    activity.type === 'create_repo'
  ).length;
  innovationScore += newRepos * 15;
  
  // 新技术使用（通过文件扩展名判断）
  const techDiversity = calculateTechDiversity(activities);
  innovationScore += techDiversity * 20;
  
  // 开源贡献
  const openSourceContributions = activities.filter(activity => 
    activity.isOpenSource
  ).length;
  innovationScore += Math.min(30, openSourceContributions * 2);
  
  // 问题解决能力
  const issuesResolved = activities.filter(activity => 
    activity.type === 'issue' && activity.status === 'closed'
  ).length;
  innovationScore += Math.min(25, issuesResolved * 3);
  
  return Math.min(100, innovationScore);
}

/**
 * 计算复杂度乘数
 * @param {Object} activity 活动对象
 * @returns {number} 复杂度乘数
 */
function calculateComplexityMultiplier(activity) {
  let multiplier = 1.0;
  
  if (activity.linesAdded && activity.linesDeleted) {
    const totalLines = activity.linesAdded + activity.linesDeleted;
    if (totalLines > 500) multiplier += 0.5;
    else if (totalLines > 100) multiplier += 0.3;
    else if (totalLines > 50) multiplier += 0.1;
  }
  
  if (activity.filesChanged) {
    if (activity.filesChanged > 10) multiplier += 0.3;
    else if (activity.filesChanged > 5) multiplier += 0.2;
    else if (activity.filesChanged > 2) multiplier += 0.1;
  }
  
  return Math.min(2.0, multiplier);
}

/**
 * 计算交互乘数
 * @param {Object} activity 活动对象
 * @returns {number} 交互乘数
 */
function calculateInteractionMultiplier(activity) {
  let multiplier = 1.0;
  
  if (activity.participants && activity.participants > 1) {
    multiplier += (activity.participants - 1) * 0.2;
  }
  
  if (activity.comments && activity.comments > 0) {
    multiplier += Math.min(0.5, activity.comments * 0.1);
  }
  
  return Math.min(2.0, multiplier);
}

/**
 * 评估提交信息质量
 * @param {string} message 提交信息
 * @returns {number} 质量得分 (0-50)
 */
function evaluateCommitMessage(message) {
  let score = 0;
  
  // 长度检查
  if (message.length >= 10 && message.length <= 72) score += 15;
  else if (message.length >= 5) score += 10;
  
  // 格式检查
  if (/^(feat|fix|docs|style|refactor|test|chore):/i.test(message)) score += 20;
  else if (/^[A-Z]/.test(message)) score += 10;
  
  // 描述性检查
  if (message.includes('fix') || message.includes('add') || message.includes('update')) score += 10;
  
  // 避免无意义提交
  if (/^(wip|temp|test|.)$/i.test(message)) score -= 20;
  
  return Math.max(0, Math.min(50, score));
}

/**
 * 评估代码变更质量
 * @param {Object} changes 代码变更信息
 * @returns {number} 质量得分 (0-50)
 */
function evaluateCodeChanges(changes) {
  let score = 25; // 基础分数
  
  const { linesAdded = 0, linesDeleted = 0, filesChanged = 0 } = changes;
  
  // 变更规模评估
  const totalLines = linesAdded + linesDeleted;
  if (totalLines > 1000) score -= 10; // 过大的变更可能质量较低
  else if (totalLines > 500) score -= 5;
  else if (totalLines >= 10 && totalLines <= 200) score += 10; // 适中的变更
  
  // 文件数量评估
  if (filesChanged > 20) score -= 10;
  else if (filesChanged > 10) score -= 5;
  else if (filesChanged >= 2 && filesChanged <= 8) score += 10;
  
  // 增删比例评估
  if (linesDeleted > 0) {
    const ratio = linesAdded / linesDeleted;
    if (ratio >= 0.5 && ratio <= 2.0) score += 10; // 合理的增删比例
  }
  
  return Math.max(0, Math.min(50, score));
}

/**
 * 计算连续活跃天数
 * @param {Array} activities 活动数据
 * @returns {number} 连续活跃天数
 */
function calculateConsecutiveActiveDays(activities) {
  const sortedDates = [...new Set(
    activities.map(activity => new Date(activity.date).toDateString())
  )].sort((a, b) => new Date(b) - new Date(a));
  
  if (sortedDates.length === 0) return 0;
  
  let consecutiveDays = 1;
  let currentDate = new Date(sortedDates[0]);
  
  for (let i = 1; i < sortedDates.length; i++) {
    const nextDate = new Date(sortedDates[i]);
    const dayDiff = Math.floor((currentDate - nextDate) / (1000 * 60 * 60 * 24));
    
    if (dayDiff === 1) {
      consecutiveDays++;
      currentDate = nextDate;
    } else {
      break;
    }
  }
  
  return consecutiveDays;
}

/**
 * 计算技术多样性
 * @param {Array} activities 活动数据
 * @returns {number} 技术多样性得分 (0-5)
 */
function calculateTechDiversity(activities) {
  const techStack = new Set();
  
  activities.forEach(activity => {
    if (activity.files) {
      activity.files.forEach(file => {
        const extension = file.split('.').pop();
        if (extension) {
          techStack.add(extension.toLowerCase());
        }
      });
    }
  });
  
  return Math.min(5, techStack.size);
}

/**
 * 主要评分函数 - 计算综合贡献度得分
 * @param {Array} activities Git活动数据
 * @param {Object} customWeights 自定义权重（可选）
 * @returns {Object} 评分结果
 */
export function calculateContributionScore(activities, customWeights = {}) {
  // 合并自定义权重
  const weights = { ...DIMENSION_WEIGHTS, ...customWeights };
  
  // 计算各维度得分
  const scores = {
    codeContribution: calculateCodeContribution(activities),
    collaboration: calculateCollaboration(activities),
    codeQuality: calculateCodeQuality(activities),
    activity: calculateActivity(activities),
    innovation: calculateInnovation(activities)
  };
  
  // 计算加权总分
  const totalScore = Object.keys(scores).reduce((total, dimension) => {
    return total + (scores[dimension] * weights[dimension]);
  }, 0);
  
  // 计算等级
  const grade = getGrade(totalScore);
  
  return {
    totalScore: Math.round(totalScore * 100) / 100,
    grade,
    scores,
    weights,
    recommendations: generateRecommendations(scores)
  };
}

/**
 * 根据总分获取等级
 * @param {number} score 总分
 * @returns {string} 等级
 */
function getGrade(score) {
  if (score >= 90) return 'A+';
  if (score >= 85) return 'A';
  if (score >= 80) return 'A-';
  if (score >= 75) return 'B+';
  if (score >= 70) return 'B';
  if (score >= 65) return 'B-';
  if (score >= 60) return 'C+';
  if (score >= 55) return 'C';
  if (score >= 50) return 'C-';
  return 'D';
}

/**
 * 生成改进建议
 * @param {Object} scores 各维度得分
 * @returns {Array} 建议列表
 */
function generateRecommendations(scores) {
  const recommendations = [];
  
  if (scores.codeContribution < 70) {
    recommendations.push({
      type: 'codeContribution',
      message: '建议增加代码提交频率，参与更多的开发工作',
      priority: 'high'
    });
  }
  
  if (scores.collaboration < 60) {
    recommendations.push({
      type: 'collaboration',
      message: '建议多参与代码审查和团队讨论，提升协作能力',
      priority: 'medium'
    });
  }
  
  if (scores.codeQuality < 65) {
    recommendations.push({
      type: 'codeQuality',
      message: '建议改善提交信息质量，编写更规范的代码',
      priority: 'high'
    });
  }
  
  if (scores.activity < 50) {
    recommendations.push({
      type: 'activity',
      message: '建议保持更稳定的开发节奏，增加日常活跃度',
      priority: 'medium'
    });
  }
  
  if (scores.innovation < 40) {
    recommendations.push({
      type: 'innovation',
      message: '建议尝试新技术，参与开源项目，提升创新能力',
      priority: 'low'
    });
  }
  
  return recommendations;
}

/**
 * 批量计算多个用户的贡献度得分
 * @param {Array} usersActivities 用户活动数据数组
 * @param {Object} customWeights 自定义权重
 * @returns {Array} 用户评分结果数组
 */
export function batchCalculateScores(usersActivities, customWeights = {}) {
  return usersActivities.map(userActivity => ({
    userId: userActivity.userId,
    userName: userActivity.userName,
    ...calculateContributionScore(userActivity.activities, customWeights)
  }));
}

/**
 * 动态调整权重
 * @param {Object} projectType 项目类型配置
 * @returns {Object} 调整后的权重
 */
export function adjustWeightsForProject(projectType) {
  const baseWeights = { ...DIMENSION_WEIGHTS };
  
  switch (projectType) {
    case 'research':
      return {
        ...baseWeights,
        innovation: 0.25,
        codeQuality: 0.25,
        codeContribution: 0.25,
        collaboration: 0.15,
        activity: 0.1
      };
    
    case 'enterprise':
      return {
        ...baseWeights,
        codeQuality: 0.35,
        collaboration: 0.3,
        codeContribution: 0.2,
        activity: 0.1,
        innovation: 0.05
      };
    
    case 'opensource':
      return {
        ...baseWeights,
        collaboration: 0.35,
        innovation: 0.2,
        codeContribution: 0.25,
        activity: 0.15,
        codeQuality: 0.05
      };
    
    default:
      return baseWeights;
  }
}

/**
 * 实时评分更新
 * @param {Array} newActivities 新的活动数据
 * @param {Object} existingScore 现有评分
 * @param {Object} customWeights 自定义权重
 * @returns {Object} 更新后的评分
 */
export function updateScoreRealtime(newActivities, existingScore, customWeights = {}) {
  if (!newActivities || newActivities.length === 0) {
    return existingScore;
  }
  
  // 计算增量评分
  const incrementalScore = calculateContributionScore(newActivities, customWeights);
  
  // 合并评分（加权平均）
  const totalActivities = (existingScore.activityCount || 0) + newActivities.length;
  const existingWeight = (existingScore.activityCount || 0) / totalActivities;
  const newWeight = newActivities.length / totalActivities;
  
  const updatedScores = {};
  Object.keys(incrementalScore.scores).forEach(dimension => {
    updatedScores[dimension] = 
      (existingScore.scores[dimension] || 0) * existingWeight + 
      incrementalScore.scores[dimension] * newWeight;
  });
  
  const updatedTotalScore = Object.keys(updatedScores).reduce((total, dimension) => {
    const weight = customWeights[dimension] || DIMENSION_WEIGHTS[dimension];
    return total + (updatedScores[dimension] * weight);
  }, 0);
  
  return {
    ...incrementalScore,
    totalScore: Math.round(updatedTotalScore * 100) / 100,
    scores: updatedScores,
    activityCount: totalActivities,
    lastUpdated: new Date().toISOString()
  };
}

/**
 * 智能权重推荐
 * @param {Array} activities 活动数据
 * @param {Object} teamProfile 团队特征
 * @returns {Object} 推荐权重
 */
export function recommendWeights(activities, teamProfile = {}) {
  try {
    // 验证输入参数
    if (!Array.isArray(activities)) {
      console.warn('recommendWeights: activities不是数组，使用空数组');
      activities = [];
    }
    
    if (typeof teamProfile !== 'object' || teamProfile === null) {
      console.warn('recommendWeights: teamProfile不是对象，使用默认值');
      teamProfile = {};
    }
    
    const activityStats = analyzeActivityPatterns(activities);
    const recommendedWeights = { ...DIMENSION_WEIGHTS };
  
  // 基于活动模式调整权重
  if (activityStats.collaborationRatio > 0.6) {
    recommendedWeights.collaboration += 0.1;
    recommendedWeights.codeContribution -= 0.05;
    recommendedWeights.activity -= 0.05;
  }
  
  if (activityStats.innovationRatio > 0.3) {
    recommendedWeights.innovation += 0.1;
    recommendedWeights.codeQuality -= 0.05;
    recommendedWeights.activity -= 0.05;
  }
  
  if (activityStats.qualityRatio > 0.7) {
    recommendedWeights.codeQuality += 0.1;
    recommendedWeights.codeContribution -= 0.05;
    recommendedWeights.collaboration -= 0.05;
  }
  
  // 基于团队特征调整
  if (teamProfile.size && teamProfile.size > 10) {
    recommendedWeights.collaboration += 0.05;
    recommendedWeights.activity -= 0.05;
  }
  
    // 确保权重总和为1
    const totalWeight = Object.values(recommendedWeights).reduce((sum, weight) => sum + weight, 0);
    if (totalWeight > 0) {
      Object.keys(recommendedWeights).forEach(key => {
        recommendedWeights[key] = recommendedWeights[key] / totalWeight;
      });
    }
    
    return recommendedWeights;
  } catch (error) {
    console.error('recommendWeights执行失败:', error);
    // 返回默认权重
    return { ...DIMENSION_WEIGHTS };
  }
}

/**
 * 分析活动模式
 * @param {Array} activities 活动数据
 * @returns {Object} 活动统计
 */
function analyzeActivityPatterns(activities) {
  // 验证activities是否为数组
  if (!Array.isArray(activities)) {
    console.warn('analyzeActivityPatterns: activities不是数组，使用空数组', activities);
    activities = [];
  }
  
  const total = activities.length;
  if (total === 0) return { collaborationRatio: 0, innovationRatio: 0, qualityRatio: 0 };
  
  const collaborationActivities = activities.filter(a => 
    ['pull_request', 'review', 'comment', 'issue'].includes(a.type)
  ).length;
  
  const innovationActivities = activities.filter(a => 
    ['fork', 'star', 'release'].includes(a.type) || 
    (a.type === 'commit' && a.message && a.message.includes('feat'))
  ).length;
  
  const qualityActivities = activities.filter(a => 
    a.type === 'commit' && a.message && 
    (a.message.includes('test') || a.message.includes('fix') || a.message.includes('refactor'))
  ).length;
  
  return {
    collaborationRatio: collaborationActivities / total,
    innovationRatio: innovationActivities / total,
    qualityRatio: qualityActivities / total
  };
}

/**
 * 预测评分趋势
 * @param {Array} historicalScores 历史评分数据
 * @param {number} periods 预测周期数
 * @returns {Array} 预测结果
 */
export function predictScoreTrend(historicalScores, periods = 7) {
  if (!historicalScores || historicalScores.length < 3) {
    return [];
  }
  
  const predictions = [];
  const recentScores = historicalScores.slice(-5); // 使用最近5个数据点
  
  // 简单线性回归预测
  for (let i = 0; i < periods; i++) {
    const trend = calculateTrend(recentScores);
    const lastScore = recentScores[recentScores.length - 1];
    const predictedScore = Math.max(0, Math.min(100, lastScore.totalScore + trend * (i + 1)));
    
    predictions.push({
      period: i + 1,
      predictedScore: Math.round(predictedScore * 100) / 100,
      confidence: Math.max(0.3, 0.9 - i * 0.1) // 置信度随预测期数递减
    });
  }
  
  return predictions;
}

/**
 * 计算趋势斜率
 * @param {Array} scores 评分数据
 * @returns {number} 趋势斜率
 */
function calculateTrend(scores) {
  if (scores.length < 2) return 0;
  
  const n = scores.length;
  let sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
  
  scores.forEach((score, index) => {
    sumX += index;
    sumY += score.totalScore;
    sumXY += index * score.totalScore;
    sumXX += index * index;
  });
  
  const slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
  return isNaN(slope) ? 0 : slope;
}

export {
  GIT_ACTIVITY_WEIGHTS,
  DIMENSION_WEIGHTS,
  TIME_DECAY_FACTOR
};