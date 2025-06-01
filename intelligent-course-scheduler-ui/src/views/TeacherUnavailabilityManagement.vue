<template>
  <div class="teacher-unavailability-management-container">
    <el-card shadow="never">
      <el-form :model="filterParams" inline>
        <el-form-item label="选择教师">
          <el-select v-model="filterParams.teacherId" filterable placeholder="请选择教师" style="width: 200px;" @change="handleTeacherOrSemesterChange">
            <el-option v-for="teacher in teacherList" :key="teacher.id" :label="teacher.name" :value="teacher.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择学期">
          <el-select v-model="filterParams.semesterId" placeholder="请选择学期" style="width: 240px;" @change="handleTeacherOrSemesterChange">
            <el-option v-for="semester in semesterList" :key="semester.id" :label="`${semester.academicYear} ${semester.name}`" :value="semester.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="SearchIcon" @click="fetchUnavailabilities" :disabled="!filterParams.teacherId || !filterParams.semesterId">
            查询不可用时间
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 15px;" v-if="filterParams.teacherId && filterParams.semesterId && dataLoaded">
      <template #header>
        <div class="clearfix">
          <span>
            教师: {{ selectedTeacherName }} | 学期: {{ selectedSemesterDisplay }} 的不可用时间
          </span>
          <el-button
              style="float: right; padding: 3px 0"
              type="primary"
              text
              :icon="Plus"
              @click="handleOpenDialog(null)"
              v-if="authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_MANAGER'])"
          :disabled="!filterParams.teacherId || !filterParams.semesterId"
          >
          新增不可用时间
          </el-button>
        </div>
      </template>

      <el-table :data="unavailabilityList" v-loading="loadingData" border stripe empty-text="暂无不可用时间记录">
        <el-table-column prop="dayOfWeek" label="星期几" width="100" :formatter="formatDayOfWeek" />
        <el-table-column prop="startPeriod" label="开始节次" width="100" align="center" />
        <el-table-column prop="endPeriod" label="结束节次" width="100" align="center" />
        <el-table-column prop="reason" label="原因" show-overflow-tooltip />
        <el-table-column prop="isActive" label="是否激活" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isActive ? 'success' : 'info'">{{ scope.row.isActive ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button
                size="small"
                :icon="Edit"
                @click="handleOpenDialog(scope.row)"
                circle
                title="编辑"
                v-if="authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_MANAGER'])"
            />
            <el-button
                size="small"
                type="danger"
                :icon="Delete"
                @click="handleDelete(scope.row.id)"
                circle
                title="删除"
                v-if="authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_MANAGER'])"
            />
          </template>
        </el-table-column>
      </el-table>
      <div v-if="!loadingData && unavailabilityList.length === 0 && dataLoaded" style="text-align: center; padding: 20px; color: #909399;">
        当前教师在此学期暂无不可用时间记录，可点击右上角新增。
      </div>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :model-value="dialogVisible" :title="dialogTitle" width="600px" @close="handleCloseDialog" draggable :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" status-icon>
        <el-form-item label="星期几" prop="dayOfWeek">
          <el-select v-model="form.dayOfWeek" placeholder="请选择星期几" style="width: 100%;">
            <el-option label="星期一" :value="1" />
            <el-option label="星期二" :value="2" />
            <el-option label="星期三" :value="3" />
            <el-option label="星期四" :value="4" />
            <el-option label="星期五" :value="5" />
            <el-option label="星期六" :value="6" />
            <el-option label="星期日" :value="7" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始节次" prop="startPeriod">
          <el-input-number v-model="form.startPeriod" :min="1" :max="currentSemesterPeriodsPerDay" placeholder="开始节次" style="width: 100%;"/>
        </el-form-item>
        <el-form-item label="结束节次" prop="endPeriod">
          <el-input-number v-model="form.endPeriod" :min="form.startPeriod || 1" :max="currentSemesterPeriodsPerDay" placeholder="结束节次" style="width: 100%;"/>
          <div v-if="form.startPeriod && form.endPeriod && form.startPeriod > form.endPeriod" class="el-form-item__error" style="margin-top: 2px;">
            结束节次不能小于开始节次
          </div>
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="2" placeholder="请输入原因 (可选)" />
        </el-form-item>
        <el-form-item label="是否激活" prop="isActive">
          <el-switch v-model="form.isActive" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="formSubmitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Search as SearchIcon } from '@element-plus/icons-vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';
const authStore = useAuthStore();
console.log('当前用户角色:', authStore.userRoles);
console.log('是否有ADMIN或MANAGER角色:', authStore.hasAnyRole(['ADMIN', 'MANAGER']));

// 筛选参数
const filterParams = reactive({
  teacherId: null,
  semesterId: null,
});

const teacherList = ref([]);
const semesterList = ref([]);
const unavailabilityList = ref([]);
const loadingData = ref(false);
const dataLoaded = ref(false); // 用于控制结果卡片是否显示

// 对话框相关
const dialogVisible = ref(false);
const dialogMode = ref('add'); // 'add' or 'edit'
const dialogTitle = ref('新增不可用时间');
const form = reactive({
  id: null,
  teacherId: null, // 将在提交时从 filterParams 获取
  semesterId: null, // 将在提交时从 filterParams 获取
  dayOfWeek: null,
  startPeriod: 1,
  endPeriod: 1,
  reason: '',
  isActive: true,
});
const formRef = ref(null);
const formSubmitting = ref(false);

const rules = reactive({
  dayOfWeek: [{ required: true, message: '请选择星期几', trigger: 'change' }],
  startPeriod: [{ required: true, type: 'number', message: '请输入开始节次', trigger: 'blur' }],
  endPeriod: [{ required: true, type: 'number', message: '请输入结束节次', trigger: 'blur' },
    { validator: (rule, value, callback) => {
        if (form.startPeriod && value < form.startPeriod) {
          callback(new Error('结束节次不能小于开始节次'));
        } else {
          callback();
        }
      }, trigger: ['blur', 'change'] }
  ],
});

// 计算属性
const selectedTeacherName = computed(() => {
  const teacher = teacherList.value.find(t => t.id === filterParams.teacherId);
  return teacher ? teacher.name : '未知教师';
});
const selectedSemesterDisplay = computed(() => {
  const semester = semesterList.value.find(s => s.id === filterParams.semesterId);
  return semester ? `${semester.academicYear} ${semester.name}` : '未知学期';
});
const currentSemesterPeriodsPerDay = computed(() => {
  const semester = semesterList.value.find(s => s.id === filterParams.semesterId);
  return semester?.periodsPerDay || 12; // 默认最大12节，如果学期未定义
});


// 方法
const fetchTeachers = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/teachers/all`); // 假设获取所有教师用于选择
    teacherList.value = response.data;
  } catch (error) {
    ElMessage.error('获取教师列表失败！');
    console.error("Fetch teachers error:", error);
  }
};

const fetchSemesters = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/semesters/list`);
    semesterList.value = response.data;
    // 可以在这里设置默认选中的学期，例如isCurrent=true的
    const currentSemester = semesterList.value.find(s => s.isCurrent);
    if (currentSemester && !filterParams.semesterId) { // 避免覆盖用户已选
      // filterParams.semesterId = currentSemester.id; // 取消注释以自动选择当前学期
    }
  } catch (error) {
    ElMessage.error('获取学期列表失败！');
    console.error("Fetch semesters error:", error);
  }
};

const fetchUnavailabilities = async () => {
  if (!filterParams.teacherId || !filterParams.semesterId) {
    // ElMessage.warning('请先选择教师和学期');
    unavailabilityList.value = [];
    dataLoaded.value = true; // 允许显示空状态卡片
    return;
  }
  loadingData.value = true;
  dataLoaded.value = false;
  try {
    const response = await axios.get(`${API_BASE_URL}/teacher-unavailabilities/teacher/${filterParams.teacherId}/semester/${filterParams.semesterId}`);
    unavailabilityList.value = response.data;
  } catch (error) {
    ElMessage.error('获取教师不可用时间列表失败！');
    unavailabilityList.value = [];
    console.error("Fetch unavailabilities error:", error);
  } finally {
    loadingData.value = false;
    dataLoaded.value = true;
  }
};

const handleTeacherOrSemesterChange = () => {
  unavailabilityList.value = [];
  dataLoaded.value = false; // 重置加载状态，等待用户点击查询
  // 如果希望在选择教师或学期后自动加载，可以在这里调用 fetchUnavailabilities()
  // if (filterParams.teacherId && filterParams.semesterId) {
  //   fetchUnavailabilities();
  // }
};

const formatDayOfWeek = (row, column, cellValue) => {
  const days = ["", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"];
  return days[cellValue] || '未知';
};

const handleOpenDialog = (rowData = null) => {
  if (!filterParams.teacherId || !filterParams.semesterId) {
    ElMessage.warning('请先选择教师和学期才能新增不可用时间。');
    return;
  }
  if (rowData) {
    dialogMode.value = 'edit';
    dialogTitle.value = `编辑 ${selectedTeacherName.value} 的不可用时间`;
    // 注意：isActive 可能在后端是 boolean，前端 switch 也需要 boolean
    form.id = rowData.id;
    form.dayOfWeek = rowData.dayOfWeek;
    form.startPeriod = rowData.startPeriod;
    form.endPeriod = rowData.endPeriod;
    form.reason = rowData.reason || '';
    form.isActive = typeof rowData.isActive === 'boolean' ? rowData.isActive : true;
  } else {
    dialogMode.value = 'add';
    dialogTitle.value = `为 ${selectedTeacherName.value} 新增不可用时间 (${selectedSemesterDisplay.value})`;
    form.id = null;
    form.dayOfWeek = null; // 或者给一个默认值，比如 1
    form.startPeriod = 1;
    form.endPeriod = 1;
    form.reason = '';
    form.isActive = true;
  }
  dialogVisible.value = true;
  nextTick(() => {
    formRef.value?.clearValidate();
  });
};

const handleCloseDialog = () => {
  dialogVisible.value = false;
  formRef.value?.resetFields(); // 重置表单和校验状态
};

const handleSubmit = async () => {
  if (!formRef.value) return;
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (form.startPeriod > form.endPeriod) {
        ElMessage.error('结束节次不能小于开始节次！');
        return;
      }

      formSubmitting.value = true;
      const payload = {
        ...form,
        teacherId: filterParams.teacherId, // 从筛选条件中获取
        semesterId: filterParams.semesterId, // 从筛选条件中获取
      };
      delete payload.id; // 创建时不应传递id

      try {
        if (dialogMode.value === 'add') {
          await axios.post(`${API_BASE_URL}/teacher-unavailabilities`, payload);
          ElMessage.success('新增成功！');
        } else {
          // 更新时URL需要ID
          await axios.put(`${API_BASE_URL}/teacher-unavailabilities/${form.id}`, payload);
          ElMessage.success('更新成功！');
        }
        dialogVisible.value = false;
        await fetchUnavailabilities(); // 刷新列表
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '操作失败！');
        console.error("Submit error:", error);
      } finally {
        formSubmitting.value = false;
      }
    }
  });
};

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条不可用时间记录吗？', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await axios.delete(`${API_BASE_URL}/teacher-unavailabilities/${id}`);
    ElMessage.success('删除成功！');
    await fetchUnavailabilities(); // 刷新列表
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败！');
      console.error("Delete error:", error);
    }
  }
};

onMounted(() => {
  fetchTeachers();
  fetchSemesters();
});
</script>

<style scoped>
.teacher-unavailability-management-container {
  padding: 15px;
}
.el-card {
  margin-bottom: 15px;
}
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}
</style>