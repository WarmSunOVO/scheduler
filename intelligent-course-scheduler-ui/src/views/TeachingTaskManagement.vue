<template>
  <div class="teaching-task-management-container">
    <el-card shadow="never">
      <el-form :model="filterParams" inline>
        <el-form-item label="选择学期">
          <el-select
              v-model="filterParams.semesterId"
              placeholder="请选择学期"
              style="width: 200px;"
              clearable
              @change="handleSemesterOrGroupChange"
          >
            <el-option
                v-for="item in semesterList"
                :key="item.id"
                :label="`${item.academicYear} ${item.name}`"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="教学对象/班级">
          <el-input v-model="filterParams.targetGroup" placeholder="输入班级名称/代码" clearable @input="handleSemesterOrGroupChange" @keyup.enter="fetchTeachingTasksForGroup"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="SearchIcon" @click="fetchTeachingTasksForGroup" :disabled="!filterParams.semesterId || !filterParams.targetGroup">
            查询任务
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 15px;" v-if="filterParams.semesterId && filterParams.targetGroup && tasksLoaded">
      <template #header>
        <div class="clearfix">
          <span>
            学期: {{ selectedSemesterDisplay }} | 教学对象: {{ filterParams.targetGroup }} 的教学任务
          </span>
          <el-button
              style="float: right; padding: 3px 0"
              type="primary"
              text
              :icon="Plus"
              @click="handleOpenTaskDialog(null)"
              v-if="authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_TEACHER'])"
          >
            新增教学任务
          </el-button>
        </div>
      </template>

      <el-table :data="teachingTaskList" v-loading="loadingTasks" border stripe empty-text="暂无教学任务数据">
        <el-table-column prop="courseCode" label="课程代码" width="120" />
        <el-table-column prop="courseName" label="课程名称" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="hoursPerWeek" label="周学时" width="100" align="center" />
        <el-table-column prop="sessionsPerWeek" label="周上课次数" width="100" align="center" />
        <el-table-column prop="sessionLength" label="每次连堂" width="100" align="center" />
        <el-table-column prop="requiredRoomType" label="教室类型要求" width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="handleOpenTaskDialog(scope.row)" circle title="编辑" v-if="authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_TEACHER'])"></el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDeleteTask(scope.row)" circle title="删除" v-if="authStore.hasRole('ROLE_ADMIN')"></el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loadingTasks && teachingTaskList.length === 0 && tasksLoaded" style="text-align: center; padding: 20px; color: #909399;">
        当前筛选条件下暂无教学任务，请尝试调整筛选或点击右上角新增。
      </div>
    </el-card>

    <el-dialog :model-value="taskDialogVisible" :title="taskDialogTitle" width="750px" @close="handleCloseDialog" draggable :close-on-click-modal="false">
      <el-form :model="taskForm" :rules="taskFormRules" ref="taskFormRef" label-width="120px" status-icon>
        <el-alert title="提示：周学时应等于每周上课次数 × 每次连堂节数。" type="info" show-icon :closable="false" style="margin-bottom:20px;" v-if="typeof taskForm.hoursPerWeek === 'number' && typeof taskForm.sessionsPerWeek === 'number' && typeof taskForm.sessionLength === 'number' && taskForm.hoursPerWeek !== (taskForm.sessionsPerWeek * taskForm.sessionLength)"/>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程" prop="courseId">
              <el-select v-model="taskForm.courseId" filterable placeholder="选择课程" style="width:100%;" @change="handleCourseChange">
                <el-option v-for="course in courseList" :key="course.id" :label="`${course.name} (${course.courseCode})`" :value="course.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="授课教师" prop="teacherId">
              <el-select v-model="taskForm.teacherId" filterable placeholder="选择教师" style="width:100%;">
                <el-option v-for="teacher in teacherList" :key="teacher.id" :label="teacher.name" :value="teacher.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="周学时" prop="hoursPerWeek">
              <el-input-number v-model="taskForm.hoursPerWeek" :min="1" placeholder="总周学时" style="width:100%;"/>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="每周上课次数" prop="sessionsPerWeek">
              <el-input-number v-model="taskForm.sessionsPerWeek" :min="1" placeholder="周几次" style="width:100%;"/>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="每次连堂节数" prop="sessionLength">
              <el-input-number v-model="taskForm.sessionLength" :min="1" placeholder="节/次" style="width:100%;"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="教室类型要求" prop="requiredRoomType">
          <el-select v-model="taskForm.requiredRoomType" placeholder="选择或输入所需教室类型" clearable filterable allow-create default-first-option style="width:100%;">
            <el-option label="无特殊要求" value=""></el-option>
            <el-option label="普通教室" value="普通教室"></el-option>
            <el-option label="多媒体教室" value="多媒体教室"></el-option>
            <el-option label="计算机房" value="计算机房"></el-option>
            <el-option label="实验室" value="实验室"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input type="textarea" :rows="2" v-model="taskForm.remarks" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmitTaskForm" :loading="taskFormSubmitting">提交</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'; // 移除了 watch
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Search as SearchIcon } from '@element-plus/icons-vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';
const authStore = useAuthStore();

const filterParams = reactive({ semesterId: null, targetGroup: '' });
const semesterList = ref([]);
const tasksLoaded = ref(false);

const teachingTaskList = ref([]);
const loadingTasks = ref(false);

const taskDialogVisible = ref(false);
const taskDialogMode = ref('add');
const taskDialogTitle = ref('新增教学任务');
const taskFormSubmitting = ref(false);
const taskFormRef = ref(null);
const initialTaskForm = {
  id: null, courseId: null, teacherId: null, hoursPerWeek: 2, sessionsPerWeek: 1,
  sessionLength: 2, requiredRoomType: '', remarks: ''
};
const taskForm = reactive({ ...initialTaskForm });

const courseList = ref([]);
const teacherList = ref([]);

const validateHoursConsistency = (rule, value, callback) => {
  const h = taskForm.hoursPerWeek;
  const s = taskForm.sessionsPerWeek;
  const l = taskForm.sessionLength;

  if (typeof h === 'number' && typeof s === 'number' && typeof l === 'number') {
    if (h !== (s * l)) {
      callback(new Error('周学时应等于 周上课次数 × 每次连堂节数'));
    } else {
      callback();
    }
  } else {
    // 如果有任何一个值不是数字（可能是空、null、undefined），
    // 暂时不报一致性错误，让各自的必填或类型校验去处理。
    callback();
  }
};

const taskFormRules = reactive({
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择授课教师', trigger: 'change' }],
  hoursPerWeek: [
    { required: true, message: '周学时不能为空', trigger: 'blur' },
    { type: 'number', min: 1, message: '周学时必须为数字且大于0', trigger: 'blur' },
    { validator: validateHoursConsistency, trigger: 'blur' }
  ],
  sessionsPerWeek: [
    { required: true, message: '每周上课次数不能为空', trigger: 'blur' },
    { type: 'number', min: 1, message: '每周上课次数必须为数字且大于0', trigger: 'blur' },
    { validator: validateHoursConsistency, trigger: 'blur' }
  ],
  sessionLength: [
    { required: true, message: '每次连堂节数不能为空', trigger: 'blur' },
    { type: 'number', min: 1, message: '每次连堂节数必须为数字且大于0', trigger: 'blur' },
    { validator: validateHoursConsistency, trigger: 'blur' }
  ],
});

const selectedSemesterDisplay = computed(() => {
  const selected = semesterList.value.find(s => s.id === filterParams.semesterId);
  return selected ? `${selected.academicYear} ${selected.name}` : '未知学期';
});

const fetchSemesters = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/semesters/list`);
    semesterList.value = response.data;
  } catch (error) {
    console.error('获取学期列表失败:', error);
    ElMessage.error('获取学期列表失败！');
  }
};

const fetchCoursesForSelect = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/courses/list`);
    courseList.value = response.data;
  } catch (error) {
    console.error('获取课程列表失败:', error);
    ElMessage.error('获取课程列表失败！');
  }
};

const fetchTeachersForSelect = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/teachers/all`);
    teacherList.value = response.data;
  } catch (error) {
    console.error('获取教师列表失败:', error);
    ElMessage.error('获取教师列表失败！');
  }
};

const fetchTeachingTasksForGroup = async () => {
  if (!filterParams.semesterId || !filterParams.targetGroup) {
    teachingTaskList.value = [];
    tasksLoaded.value = true;
    return;
  }
  loadingTasks.value = true;
  tasksLoaded.value = false;
  try {
    const response = await axios.get(`${API_BASE_URL}/teaching-tasks/by-semester-group`, {
      params: {
        semesterId: filterParams.semesterId,
        targetGroup: filterParams.targetGroup.trim(),
      }
    });
    teachingTaskList.value = response.data.map(task => ({
      ...task,
      courseName: task.course?.name || task.courseName || '未知课程',
      courseCode: task.course?.courseCode || task.courseCode || 'N/A',
      teacherName: task.teacher?.name || task.teacherName || '待分配',
    }));
  } catch (error) {
    console.error('获取教学任务列表失败:', error);
    ElMessage.error(error.response?.data?.message || '获取教学任务列表失败！');
    teachingTaskList.value = [];
  } finally {
    loadingTasks.value = false;
    tasksLoaded.value = true;
  }
};

const handleSemesterOrGroupChange = () => {
  teachingTaskList.value = [];
  tasksLoaded.value = false;
};

// 移除了所有 watch 侦听器

const handleCourseChange = (courseId) => {
  const selectedCourse = courseList.value.find(c => c.id === courseId);
  if (selectedCourse) {
    taskForm.requiredRoomType = selectedCourse.requiredRoomType || '';
    const isHoursInitial = taskForm.hoursPerWeek === initialTaskForm.hoursPerWeek;
    const isSessionsInitial = taskForm.sessionsPerWeek === initialTaskForm.sessionsPerWeek;
    const isLengthInitial = taskForm.sessionLength === initialTaskForm.sessionLength;

    if (selectedCourse.hasOwnProperty('hoursPerWeek') &&
        selectedCourse.hasOwnProperty('sessionsPerWeek') &&
        selectedCourse.hasOwnProperty('sessionLength')) {
      if (isHoursInitial && isSessionsInitial && isLengthInitial) {
        if (selectedCourse.hoursPerWeek && selectedCourse.sessionsPerWeek && selectedCourse.sessionLength) {
          taskForm.hoursPerWeek = selectedCourse.hoursPerWeek;
          taskForm.sessionsPerWeek = selectedCourse.sessionsPerWeek;
          taskForm.sessionLength = selectedCourse.sessionLength;
          // 由于移除了 watch 自动计算和主动校验，这里不再需要 nextTick(validateField)
          // 用户需要失焦后，blur 触发的校验才会生效
        }
      }
    }
  }
};

const handleOpenTaskDialog = (task = null) => {
  taskDialogMode.value = task ? 'edit' : 'add';
  taskDialogTitle.value = task
      ? `编辑教学任务 - ${task.courseName || '课程'} (${task.teacherName || '教师'})`
      : `为 ${filterParams.targetGroup} 新增教学任务 (${selectedSemesterDisplay.value})`;

  if (task) {
    taskForm.id = task.id;
    taskForm.courseId = task.courseId;
    taskForm.teacherId = task.teacherId;
    taskForm.hoursPerWeek = (typeof task.hoursPerWeek === 'number') ? task.hoursPerWeek : initialTaskForm.hoursPerWeek;
    taskForm.sessionsPerWeek = (typeof task.sessionsPerWeek === 'number') ? task.sessionsPerWeek : initialTaskForm.sessionsPerWeek;
    taskForm.sessionLength = (typeof task.sessionLength === 'number') ? task.sessionLength : initialTaskForm.sessionLength;
    taskForm.requiredRoomType = task.requiredRoomType || '';
    taskForm.remarks = task.remarks || '';
  } else {
    Object.assign(taskForm, { ...initialTaskForm });
  }
  taskDialogVisible.value = true;
  nextTick(() => taskFormRef.value?.clearValidate());
};

const handleCloseDialog = () => {
  taskDialogVisible.value = false;
};

const handleSubmitTaskForm = async () => {
  if (!taskFormRef.value) return;
  await taskFormRef.value.validate(async (valid) => {
    if (valid) {
      // 检查学时一致性，因为没有 watch 自动更新了，提交前最好再确认一次
      if (typeof taskForm.hoursPerWeek === 'number' &&
          typeof taskForm.sessionsPerWeek === 'number' &&
          typeof taskForm.sessionLength === 'number' &&
          taskForm.hoursPerWeek !== (taskForm.sessionsPerWeek * taskForm.sessionLength)) {
        ElMessage.warning('周学时、每周上课次数和每次连堂节数不匹配，请调整！');
        return; // 阻止提交
      }

      taskFormSubmitting.value = true;
      try {
        const payload = {
          ...taskForm,
          semesterId: filterParams.semesterId,
          targetGroup: filterParams.targetGroup.trim(),
        };
        if (taskDialogMode.value === 'add') {
          await axios.post(`${API_BASE_URL}/teaching-tasks`, payload);
          ElMessage.success('教学任务创建成功！');
        } else {
          await axios.put(`${API_BASE_URL}/teaching-tasks/${taskForm.id}`, payload);
          ElMessage.success('教学任务更新成功！');
        }
        taskDialogVisible.value = false;
        await fetchTeachingTasksForGroup();
      } catch (error) {
        console.error('提交教学任务失败:', error);
        // 安全地尝试获取后端返回的错误消息
        let userErrorMessage = '操作失败，请检查输入或联系管理员！';
        if (error && error.response && error.response.data && typeof error.response.data.message === 'string' && error.response.data.message.trim() !== '') {
          userErrorMessage = error.response.data.message;
        }
        ElMessage.error(userErrorMessage);
      } finally {
        taskFormSubmitting.value = false;
      }
    } else {
      ElMessage.warning('表单校验未通过，请检查红色标记的字段！');
      return false;
    }
  });
};

const handleDeleteTask = async (task) => {
  try {
    await ElMessageBox.confirm(
        `确定要删除课程 "${task.courseName || '未知课程'}" (教师: ${task.teacherName || '未知教师'}) 的教学任务吗？此操作不可恢复。`,
        '警告：删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          draggable: true,
        }
    );
    taskFormSubmitting.value = true; // 复用提交状态作为加载指示
    await axios.delete(`${API_BASE_URL}/teaching-tasks/${task.id}`);
    ElMessage.success('教学任务删除成功！');
    await fetchTeachingTasksForGroup();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除教学任务失败:', error);
      let userErrorMessage = '删除失败！';
      if (error && error.response && error.response.data && typeof error.response.data.message === 'string' && error.response.data.message.trim() !== '') {
        userErrorMessage = error.response.data.message;
      }
      ElMessage.error(userErrorMessage);
    }
  } finally {
    taskFormSubmitting.value = false;
  }
};

onMounted(() => {
  fetchSemesters();
  fetchCoursesForSelect();
  fetchTeachersForSelect();
});
</script>

<style scoped>
.teaching-task-management-container { padding: 15px; }
.el-card { margin-bottom: 15px; }
.clearfix:before,
.clearfix:after { display: table; content: ""; }
.clearfix:after { clear: both }
.pagination-container { margin-top: 15px; display: flex; justify-content: flex-end; }
</style>