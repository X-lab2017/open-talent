<template>
  <div class="course-management">
    <NavMenu></NavMenu>
    <el-main>
      <div class="page-header">
        <h2 class="title">课程管理</h2>
        <el-button type="primary" @click="showCreateDialog = true">
          <i class="el-icon-plus"></i> 创建课程
        </el-button>
      </div>

      <!-- 课程列表 -->
      <div class="course-grid">
        <el-card 
          v-for="course in courses" 
          :key="course.courseId" 
          class="course-card"
          shadow="hover"
          @click.native="enterCourse(course)"
        >
          <div class="course-header">
            <h3>{{ course.courseName }}</h3>
            <el-tag :type="getStatusType(course.status)">{{ getStatusText(course.status) }}</el-tag>
          </div>
          <div class="course-description" :title="course.courseDescription">
            {{ course.courseDescription || '暂无描述' }}
          </div>
          <div class="course-info">
            <div class="info-item">
              <i class="el-icon-user"></i>
              <span>{{ course.studentCount || 0 }} 名学生</span>
            </div>
            <div class="info-item">
              <i class="el-icon-date"></i>
              <span>{{ formatDate(course.startDate) }} - {{ formatDate(course.endDate) }}</span>
            </div>
          </div>
          <div class="course-actions">
            <el-button size="mini" type="text" @click.stop="editCourse(course)">
              <i class="el-icon-edit"></i> 编辑
            </el-button>
            <el-button size="mini" type="text" @click.stop="manageCourseStudents(course)">
              <i class="el-icon-user"></i> 学生管理
            </el-button>
            <el-button size="mini" type="text" class="danger" @click.stop="deleteCourse(course)">
              <i class="el-icon-delete"></i> 删除
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 空状态 -->
      <div v-if="courses.length === 0" class="empty-state">
        <i class="el-icon-folder-opened"></i>
        <p>暂无课程，点击上方按钮创建第一个课程</p>
      </div>

      <!-- 创建/编辑课程对话框 -->
      <el-dialog 
        :title="editingCourse ? '编辑课程' : '创建课程'"
        :visible.sync="showCreateDialog"
        width="600px"
      >
        <el-form :model="courseForm" :rules="courseRules" ref="courseForm" label-width="100px">
          <el-form-item label="课程名称" prop="courseName">
            <el-input v-model="courseForm.courseName" placeholder="请输入课程名称"></el-input>
          </el-form-item>
          <el-form-item label="课程描述" prop="courseDescription">
            <el-input 
              type="textarea" 
              v-model="courseForm.courseDescription" 
              placeholder="请输入课程描述"
              :rows="3"
            ></el-input>
          </el-form-item>
          <el-form-item label="开始日期" prop="startDate">
            <el-date-picker
              v-model="courseForm.startDate"
              type="date"
              placeholder="选择开始日期"
              style="width: 100%"
            ></el-date-picker>
          </el-form-item>
          <el-form-item label="结束日期" prop="endDate">
            <el-date-picker
              v-model="courseForm.endDate"
              type="date"
              placeholder="选择结束日期"
              style="width: 100%"
            ></el-date-picker>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="cancelCourseForm">取消</el-button>
          <el-button type="primary" @click="submitCourseForm">确定</el-button>
        </div>
      </el-dialog>

      <!-- 学生管理对话框 -->
      <el-dialog 
        title="学生管理"
        :visible.sync="showStudentDialog"
        width="1000px"
      >
        <div class="student-management">
          <div class="student-actions">
            <el-button type="primary" @click="openAddStudentDialog">
              <i class="el-icon-plus"></i> 添加学生
            </el-button>
            <el-button @click="importStudents">
              <i class="el-icon-upload"></i> 批量导入
            </el-button>
          </div>
          
          <el-table 
            :data="courseStudents" 
            style="width: 100%; margin-top: 20px;"
            border
            stripe
            :header-cell-style="{background: '#f5f7fa', color: '#606266', fontWeight: 'bold'}"
          >
            <el-table-column prop="memberName" label="姓名" min-width="18%" show-overflow-tooltip></el-table-column>
            <el-table-column prop="memberEmail" label="邮箱" min-width="25%" show-overflow-tooltip></el-table-column>
            <el-table-column prop="enrollmentDate" label="加入时间" min-width="22%">
              <template slot-scope="scope">
                {{ formatDateTime(scope.row.enrollmentDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" min-width="20%" align="center">
              <template slot-scope="scope">
                <el-tag :type="scope.row.status === 'active' ? 'success' : 'info'">
                  {{ scope.row.status === 'active' ? '正常' : '其他' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="17%" align="center">
              <template slot-scope="scope">
                <el-button size="mini" type="text" @click="editStudent(scope.row)">
                  编辑
                </el-button>
                <el-button size="mini" type="text" class="danger" @click="removeStudent(scope.row)">
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-dialog>

      <!-- 添加学生对话框 -->
      <el-dialog 
        title="添加学生"
        :visible.sync="showAddStudentDialog"
        width="500px"
      >
        <el-form :model="studentForm" ref="studentForm" label-width="100px">
          <el-form-item label="选择学生">
            <el-select 
              v-model="studentForm.memberIds" 
              multiple 
              placeholder="请选择学生"
              style="width: 100%"
            >
              <el-option
                v-for="member in availableMembers"
                :key="member.memberId"
                :label="member.name"
                :value="member.memberId"
              ></el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="showAddStudentDialog = false">取消</el-button>
          <el-button type="primary" @click="addStudentsToCourse">确定</el-button>
        </div>
      </el-dialog>
    </el-main>
  </div>
</template>

<script>
import NavMenu from '@/components/NavMenu.vue'

export default {
  name: 'CourseManagement',
  components: {
    NavMenu
  },
  data() {
    return {
      courses: [],
      showCreateDialog: false,
      showStudentDialog: false,
      showAddStudentDialog: false,
      editingCourse: null,
      currentCourse: null,
      courseStudents: [],
      availableMembers: [],
      organizationId: null, // 动态获取组织ID
      courseForm: {
        courseName: '',
        courseDescription: '',
        startDate: '',
        endDate: '',
        organizationId: null,
        teacherId: 1, // 从当前用户获取
        status: 'active'
      },
      studentForm: {
        memberIds: []
      },
      courseRules: {
        courseName: [
          { required: true, message: '请输入课程名称', trigger: 'blur' }
        ],
        startDate: [
          { required: true, message: '请选择开始日期', trigger: 'change' }
        ],
        endDate: [
          { required: true, message: '请选择结束日期', trigger: 'change' }
        ]
      }
    }
  },
  mounted() {
    this.organizationId = this.getOrganizationId()
    this.courseForm.organizationId = this.organizationId
    this.fetchCourses()
  },
  methods: {
    async fetchCourses() {
      try {
        const response = await this.$http.get(`/courses?organizationId=${this.organizationId}`)
        this.total = response.data.data?.total || 0
        this.courses = response.data.data?.records || response.data.data || []
        
        console.log('获取课程列表成功:', this.courses)
        console.log('课程学生数量:', this.courses.map(c => ({ name: c.courseName, count: c.studentCount || 0 })))
      } catch (error) {
        console.error('获取课程列表失败：', error)
        this.$message.error('获取课程列表失败')
      }
    },
    async fetchAvailableMembers(courseId) {
      try {
        if (courseId) {
          const response = await this.$http.get(`/member/available/${courseId}`)
          this.availableMembers = response.data.data || []
        } else {
          // 如果没有courseId，获取所有成员（用于初始化）
          const response = await this.$http.get('/member/search')
          this.availableMembers = response.data.data || []
        }
      } catch (error) {
        console.error('获取成员列表失败：', error)
      }
    },
    async fetchCourseStudents(courseId) {
      try {
        const response = await this.$http.get(`/courses/${courseId}/students`)
        this.courseStudents = response.data.data || []
      } catch (error) {
        console.error('获取课程学生失败：', error)
        this.$message.error('获取课程学生失败')
      }
    },
    enterCourse(course) {
      // 直接打开学生管理对话框
      this.manageCourseStudents(course)
    },
    editCourse(course) {
      this.editingCourse = course
      this.courseForm = {
        courseName: course.courseName,
        courseDescription: course.courseDescription,
        startDate: course.startDate,
        endDate: course.endDate
      }
      this.showCreateDialog = true
    },
    async manageCourseStudents(course) {
      this.currentCourse = course
      this.showStudentDialog = true
      await this.fetchCourseStudents(course.courseId)
    },
    async openAddStudentDialog() {
      this.showAddStudentDialog = true
      // 获取当前课程可用的成员列表
      if (this.currentCourse) {
        await this.fetchAvailableMembers(this.currentCourse.courseId)
      }
    },
    async deleteCourse(course) {
      try {
        await this.$confirm('确定要删除这个课程吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await this.$http.delete(`/courses/${course.courseId}`)
        this.$message.success('删除成功')
        this.fetchCourses()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除课程失败：', error)
          this.$message.error('删除课程失败')
        }
      }
    },
    submitCourseForm() {
      this.$refs.courseForm.validate(async (valid) => {
        if (valid) {
          try {
            // 设置组织ID和教师ID
            this.courseForm.organizationId = this.organizationId
            this.courseForm.teacherId = this.getCurrentUserId() // 需要实现获取当前用户ID的方法
            
            // 处理日期格式，避免时区问题
            const formData = { ...this.courseForm }
            if (formData.startDate instanceof Date) {
              formData.startDate = formData.startDate.toLocaleDateString('sv-SE') // YYYY-MM-DD格式
            }
            if (formData.endDate instanceof Date) {
              formData.endDate = formData.endDate.toLocaleDateString('sv-SE') // YYYY-MM-DD格式
            }
            
            // 输出课程相关字段值
            console.log('课程表单数据：', {
              courseName: formData.courseName,
              courseDescription: formData.courseDescription,
              startDate: formData.startDate,
              endDate: formData.endDate,
              organizationId: formData.organizationId,
              teacherId: formData.teacherId,
              status: formData.status
            })
            
            if (this.editingCourse) {
              await this.$http.put(`/courses/${this.editingCourse.courseId}`, formData)
              this.$message.success('更新成功')
            } else {
              await this.$http.post('/courses', formData)
              this.$message.success('创建成功')
            }
            this.cancelCourseForm()
            this.fetchCourses()
          } catch (error) {
            console.error('保存课程失败：', error)
            this.$message.error('保存课程失败')
          }
        }
      })
    },
    cancelCourseForm() {
      this.showCreateDialog = false
      this.editingCourse = null
      this.courseForm = {
        courseName: '',
        courseDescription: '',
        startDate: '',
        endDate: '',
        organizationId: this.getOrganizationId(),
        teacherId: 1,
        status: 'active'
      }
      this.$refs.courseForm && this.$refs.courseForm.resetFields()
    },
    async addStudentsToCourse() {
      try {
        // 确保memberIds是数组且不为空
        if (!this.studentForm.memberIds || this.studentForm.memberIds.length === 0) {
          this.$message.warning('请选择要添加的学生')
          return
        }
        
        console.log('发送到后端的数据：', this.studentForm)
        await this.$http.post(`/courses/${this.currentCourse.courseId}/enroll`, this.studentForm)
        this.$message.success('添加学生成功')
        this.showAddStudentDialog = false
        this.studentForm = { memberIds: [] }
        this.fetchCourseStudents(this.currentCourse.courseId)
        this.fetchCourses() // 刷新课程列表以更新学生人数
      } catch (error) {
        console.error('添加学生失败：', error)
        this.$message.error('添加学生失败')
      }
    },
    async removeStudent(student) {
      try {
        await this.$confirm('确定要移除这个学生吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await this.$http.delete(`/courses/${this.currentCourse.courseId}/students/${student.memberId}`)
        this.$message.success('移除成功')
        this.fetchCourseStudents(this.currentCourse.courseId)
        this.fetchCourses() // 刷新课程列表以更新学生人数
      } catch (error) {
        if (error !== 'cancel') {
          console.error('移除学生失败：', error)
          this.$message.error('移除学生失败')
        }
      }
    },
    importStudents() {
      this.$message.info('批量导入功能开发中')
    },
    editStudent(student) {
      this.$message.info('编辑学生功能开发中')
    },
    getStatusType(status) {
      const statusMap = {
        'active': 'success',
        'inactive': 'info',
        'completed': 'warning'
      }
      return statusMap[status] || 'info'
    },
    getStatusText(status) {
      const statusMap = {
        'active': '进行中',
        'inactive': '未开始',
        'completed': '已结束'
      }
      return statusMap[status] || '未知'
    },
    formatDate(date) {
      if (!date) return ''
      return new Date(date).toLocaleDateString('zh-CN')
    },
    formatDateTime(datetime) {
      if (!datetime) return ''
      return new Date(datetime).toLocaleString('zh-CN')
    },
    
    // 获取当前用户ID（从store或localStorage获取）
    getCurrentUserId() {
      // 这里应该从Vuex store或localStorage获取当前登录用户的ID
      // 暂时返回默认值，实际项目中需要根据登录状态获取
      return this.$store?.state?.user?.id || localStorage.getItem('userId') || 1
    },
    
    // 获取组织ID（从store或localStorage获取）
    getOrganizationId() {
      return this.$store?.state?.user?.organizationId || localStorage.getItem('organizationId') || 1
    }
  }
}
</script>

<style scoped>
.course-management {
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

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.course-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.course-card:hover {
  transform: translateY(-2px);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.course-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.course-description {
  color: #606266;
  margin: 10px 0;
  line-height: 1.6;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 44px;
  cursor: help;
  transition: all 0.3s ease;
  padding: 8px 0;
  border-radius: 4px;
}

.course-description:hover {
  color: #409eff;
  background-color: rgba(64, 158, 255, 0.05);
  padding: 8px 12px;
  margin: 10px -12px;
}

.course-description:empty::before {
  content: '暂无描述';
  color: #c0c4cc;
  font-style: italic;
}

.course-info {
  margin: 15px 0;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  color: #909399;
  font-size: 14px;
}

.info-item i {
  margin-right: 8px;
}

.course-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.course-actions .el-button {
  padding: 5px 8px;
}

.course-actions .danger {
  color: #f56c6c;
}

.course-actions .danger:hover {
  color: #f56c6c;
  background-color: #fef0f0;
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

.student-management {
  min-height: 400px;
}

.student-actions {
  display: flex;
  gap: 10px;
}

.dialog-footer {
  text-align: right;
}

/* Element UI 样式覆盖 */
.course-management .el-button--primary {
  color: #fff;
  background-color: #131313;
  border-color: #131313;
}

.course-management .el-button--primary:focus,
.course-management .el-button--primary:hover {
  background: #404040;
  border-color: #404040;
  color: #fff;
}

.course-management .el-card {
  border: none;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.course-management .el-table th {
  background-color: #fafafa;
  color: #303133;
}
</style>