<template>
  <div class="teaching-task-management-container">
    <el-card shadow="never">
      <el-form :model="filterParams" inline>
        <el-form-item label="选择学期">
          <el-select
              v-model="filterParams.semesterId"
              placeholder="请选择学期"
              style="width: 200px;"
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
          <el-input v-model="filterParams.targetGroup" placeholder="输入班级名称/代码" clearable @change="handleSemesterOrGroupChange" @keyup.enter="handleSemesterOrGroupChange"/>

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

      <el-table :data="teachingTaskList" v-loading="loadingTasks" border stripe>
        <el-table-column prop="courseCode" label="课程代码" width="120" />
        <el-table-column prop="courseName" label="课程名称" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="hoursPerWeek" label="周学时" width="100" align="center" />
        <el-table-column prop="sessionsPerWeek" label="周次" width="80" align="center" />
        <el-table-column prop="sessionLength" label="连读" width="80" align="center" />
        <el-table-column prop="requiredRoomType" label="教室类型要求" width="150" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" :icon="Edit" @click="handleOpenTaskDialog(scope.row)" circle title="编辑" v-if="authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_TEACHER'])"></el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDeleteTask(scope.row)" circle title="删除" v-if="authStore.hasRole('ROLE_ADMIN')"></el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loadingTasks && teachingTaskList.length === 0" style="text-align: center; padding: 20px; color: #909399;">
        暂无教学任务，请点击右上角新增。
      </div>
    </el-card>

    <!-- 新增/编辑教学任务对话框 -->
    <el-dialog :model-value="taskDialogVisible" :title="taskDialogTitle" width="750px" @close="taskDialogVisible = false" draggable :close-on-click-modal="false">
      <el-form :model="taskForm" :rules="taskFormRules" ref="taskFormRef" label-width="120px" status-icon>
        <el-alert title="提示：周学时应等于每周上课次数乘以每次连堂节数。" type="info" show-icon :closable="false" style="margin-bottom:20px;" v-if="taskForm.hoursPerWeek !== (taskForm.sessionsPerWeek * taskForm.sessionLength) && taskForm.sessionsPerWeek > 0 && taskForm.sessionLength > 0"/>
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
        <el-button @click="taskDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitTaskForm" :loading="taskFormSubmitting">提交</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, computed, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Search as SearchIcon } from '@element-plus/icons-vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';

const API_BASE_URL = '/api';
const authStore = useAuthStore();

// --- 筛选参数 ---
const filterParams = reactive({ semesterId: null, targetGroup: '' });
const semesterList = ref([]);
const tasksLoaded = ref(false); // 用于控制任务列表卡片是否显示

// --- 教学任务列表 ---
const teachingTaskList = ref([]);
const loadingTasks = ref(false);

// --- 教学任务表单 ---
const taskDialogVisible = ref(false);
const taskDialogMode = ref('add'); // 'add' or 'edit'
const taskDialogTitle = ref('新增教学任务');
const taskFormSubmitting = ref(false);
const taskFormRef = ref(null);
const initialTaskForm = {
  id: null, courseId: null, teacherId: null, hoursPerWeek: 2, sessionsPerWeek: 1,
  sessionLength: 2, requiredRoomType: '', remarks: ''
};
const taskForm = reactive({ ...initialTaskForm });

const courseList = ref([]); // 用于对话框中选择课程
const teacherList = ref([]); // 用于对话框中选择教师

const taskFormRules = reactive({
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择授课教师', trigger: 'change' }],
  hoursPerWeek: [{ required: true, type: 'number', min:1, message: '周学时不能为空且大于0', trigger: 'blur' }],
  sessionsPerWeek: [{ required: true, type: 'number', min:1, message: '每周次数不能为空且大于0', trigger: 'blur' }],
  sessionLength: [{ required: true, type: 'number', min:1, message: '连堂节数不能为空且大于0', trigger: 'blur' }],
});

// --- 计算属性 ---
const selectedSemesterDisplay = computed(() => {
  const selected = semesterList.value.find(s => s.id === filterParams.semesterId);
  return selected ? `${selected.academicYear} ${selected.name}` : '';
});

// --- 方法 ---
const fetchSemesters = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/semesters/list`); // 获取不分页的学期列表
    semesterList.value = response.data;
    // (可选) 默认选中第一个或标记为isCurrent的学期
    // if (semesterList.value.length > 0) {
    //   const current = semesterList.value.find(s => s.isCurrent);
    //   filterParams.semesterId = current ? current.id : semesterList.value[0].id;
    // }
  } catch (error) { ElMessage.error('获取学期列表失败！'); }
};

const fetchCoursesForSelect = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/courses/list`);
    courseList.value = response.data;
  } catch (error) { ElMessage.error('获取课程列表失败！'); }
};

const fetchTeachersForSelect = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/teachers/all`); // 假设获取所有教师用于选择
    teacherList.value = response.data;
  } catch (error) { ElMessage.error('获取教师列表失败！'); }
};

const fetchTeachingTasksForGroup = async () => {
  if (!filterParams.semesterId || !filterParams.targetGroup) {
    // ElMessage.warning('请先选择学期并输入教学对象/班级');
    teachingTaskList.value = []; // 清空列表
    tasksLoaded.value = true; // 标记已尝试加载（即使为空）
    return;
  }
  loadingTasks.value = true;
  tasksLoaded.value = false;
  try {
    const response = await axios.get(`${API_BASE_URL}/teaching-tasks/by-semester-group`, {
      params: {
        semesterId: filterParams.semesterId,
        targetGroup: filterParams.targetGroup,
      }
    });
    teachingTaskList.value = response.data;
  } catch (error) {
    ElMessage.error('获取教学任务列表失败！');
    teachingTaskList.value = []; // 清空列表
  } finally {
    loadingTasks.value = false;
    tasksLoaded.value = true;
  }
};

// 当学期或班级输入框变化时，可以自动清空或重新加载任务列表
const handleSemesterOrGroupChange = () => {
  teachingTaskList.value = []; // 清空之前的任务
  tasksLoaded.value = false;   // 重置加载状态，等待用户点击查询
  if (filterParams.semesterId && filterParams.targetGroup) {
    // fetchTeachingTasksForGroup(); // 或者让用户手动点击查询按钮
  }
};


watch(() => taskForm.sessionsPerWeek, (newVal) => {
  if (newVal && taskForm.sessionLength) taskForm.hoursPerWeek = newVal * taskForm.sessionLength;
});
watch(() => taskForm.sessionLength, (newVal) => {
  if (newVal && taskForm.sessionsPerWeek) taskForm.hoursPerWeek = newVal * taskForm.sessionsPerWeek;
});


const handleCourseChange = (courseId) => {
  const selectedCourse = courseList.value.find(c => c.id === courseId);
  if (selectedCourse) {
    taskForm.requiredRoomType = selectedCourse.requiredRoomType || '';
    if(selectedCourse.hoursPerWeek && selectedCourse.sessionsPerWeek && selectedCourse.sessionLength){
      taskForm.hoursPerWeek = selectedCourse.hoursPerWeek;
      // 还需要根据周学时和节数推荐每周次数，或者让用户自己填
      // 例如，如果周学时是4，连堂2节，则每周次数是2
    }
  }
};


const handleOpenTaskDialog = (task) => {
  taskDialogMode.value = task ? 'edit' : 'add';
  taskDialogTitle.value = task ? '编辑教学任务' : `为 ${filterParams.targetGroup} 新增教学任务 (${selectedSemesterDisplay.value})`;

  if (task) {
    // 编辑，注意后端返回的DTO和表单需要的字段可能不完全一致
    taskForm.id = task.id;
    taskForm.courseId = task.courseId;
    taskForm.teacherId = task.teacherId;
    taskForm.hoursPerWeek = task.hoursPerWeek;
    taskForm.sessionsPerWeek = task.sessionsPerWeek;
    taskForm.sessionLength = task.sessionLength;
    taskForm.requiredRoomType = task.requiredRoomType || '';
    taskForm.remarks = task.remarks || '';
  } else {
    Object.assign(taskForm, { ...initialTaskForm });
    // 新增时，自动填充当前筛选的学期和班级 (这些字段在CreateRequest中)
  }
  taskDialogVisible.value = true;
  nextTick(() => taskFormRef.value?.clearValidate());
};

const handleSubmitTaskForm = async () => {
  if (!taskFormRef.value) return;
  await taskFormRef.value.validate(async (valid) => {
    if (valid) {
      if (taskForm.hoursPerWeek !== (taskForm.sessionsPerWeek * taskForm.sessionLength)) {
        ElMessage.warning('周学时、每周上课次数和每次连堂节数不匹配，请调整！');
        return;
      }
      taskFormSubmitting.value = true;
      try {
        const payload = {
          ...taskForm,
          semesterId: filterParams.semesterId, // ★★★ 从筛选器获取
          targetGroup: filterParams.targetGroup, // ★★★ 从筛选器获取
        };
        if (taskDialogMode.value === 'add') {
          await axios.post(`${API_BASE_URL}/teaching-tasks`, payload);
          ElMessage.success('教学任务创建成功！');
        } else {
          await axios.put(`${API_BASE_URL}/teaching-tasks/${taskForm.id}`, payload);
          ElMessage.success('教学任务更新成功！');
        }
        taskDialogVisible.value = false;
        await fetchTeachingTasksForGroup(); // 刷新列表
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '操作失败！');
      } finally {
        taskFormSubmitting.value = false;
      }
    }
  });
};

const handleDeleteTask = async (task) => {
  try {
    await ElMessageBox.confirm(`确定要删除课程 "${task.courseName}" 的教学任务吗？`, '警告', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning', draggable: true,
    });
    loadingTasks.value = true; // 可以用一个独立的loading状态
    await axios.delete(`${API_BASE_URL}/teaching-tasks/${task.id}`);
    ElMessage.success('教学任务删除成功！');
    await fetchTeachingTasksForGroup();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败！');
    }
  } finally {
    loadingTasks.value = false;
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