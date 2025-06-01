<template>
  <div class="class-group-unavailability-management-container">
    <el-card shadow="never">
      <el-form :model="filterParams" inline>
        <el-form-item label="教学对象/班级">
          <el-input
              v-model="filterParams.targetGroup"
              placeholder="输入教学对象/班级名称"
              clearable
              style="width: 220px;"
              @input="handleTargetGroupOrSemesterChange"
              @keyup.enter="fetchUnavailabilities"
          />
          <!-- 如果将来有班级列表接口，可以换成 el-select -->
        </el-form-item>
        <el-form-item label="选择学期">
          <el-select v-model="filterParams.semesterId" placeholder="请选择学期" style="width: 240px;" @change="handleTargetGroupOrSemesterChange">
            <el-option v-for="semester in semesterList" :key="semester.id" :label="`${semester.academicYear} ${semester.name}`" :value="semester.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="SearchIcon" @click="fetchUnavailabilities" :disabled="!filterParams.targetGroup || !filterParams.semesterId">
            查询不可用时间
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 15px;" v-if="filterParams.targetGroup && filterParams.semesterId && dataLoaded">
      <template #header>
        <div class="clearfix">
          <span>
            教学对象: {{ filterParams.targetGroup }} | 学期: {{ selectedSemesterDisplay }} 的不可用时间
          </span>
          <el-button
              style="float: right; padding: 3px 0"
              type="primary"
              text
              :icon="Plus"
              @click="handleOpenDialog(null)"
              v-if="authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_MANAGER'])"
              :disabled="!filterParams.targetGroup || !filterParams.semesterId"
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
        当前教学对象在此学期暂无不可用时间记录，可点击右上角新增。
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

const filterParams = reactive({
  targetGroup: '', // 改为 targetGroup
  semesterId: null,
});

const semesterList = ref([]);
const unavailabilityList = ref([]);
const loadingData = ref(false);
const dataLoaded = ref(false);

const dialogVisible = ref(false);
const dialogMode = ref('add');
const dialogTitle = ref('新增不可用时间');
const form = reactive({
  id: null,
  targetGroup: '', // 将在提交时从 filterParams 获取
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

const selectedSemesterDisplay = computed(() => {
  const semester = semesterList.value.find(s => s.id === filterParams.semesterId);
  return semester ? `${semester.academicYear} ${semester.name}` : '未知学期';
});
const currentSemesterPeriodsPerDay = computed(() => {
  const semester = semesterList.value.find(s => s.id === filterParams.semesterId);
  return semester?.periodsPerDay || 12;
});

const fetchSemesters = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/semesters/list`);
    semesterList.value = response.data;
  } catch (error) {
    ElMessage.error('获取学期列表失败！');
    console.error("Fetch semesters error:", error);
  }
};

const fetchUnavailabilities = async () => {
  if (!filterParams.targetGroup || !filterParams.semesterId) {
    unavailabilityList.value = [];
    dataLoaded.value = true;
    return;
  }
  loadingData.value = true;
  dataLoaded.value = false;
  try {
    // 修改API路径
    const response = await axios.get(`${API_BASE_URL}/class-group-unavailabilities/target-group/${encodeURIComponent(filterParams.targetGroup)}/semester/${filterParams.semesterId}`);
    unavailabilityList.value = response.data;
  } catch (error) {
    ElMessage.error('获取教学对象不可用时间列表失败！');
    unavailabilityList.value = [];
    console.error("Fetch class group unavailabilities error:", error);
  } finally {
    loadingData.value = false;
    dataLoaded.value = true;
  }
};

const handleTargetGroupOrSemesterChange = () => {
  unavailabilityList.value = [];
  dataLoaded.value = false;
};

const formatDayOfWeek = (row, column, cellValue) => {
  const days = ["", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"];
  return days[cellValue] || '未知';
};

const handleOpenDialog = (rowData = null) => {
  if (!filterParams.targetGroup || !filterParams.semesterId) {
    ElMessage.warning('请先输入教学对象并选择学期才能新增不可用时间。');
    return;
  }
  if (rowData) {
    dialogMode.value = 'edit';
    dialogTitle.value = `编辑 ${filterParams.targetGroup} 的不可用时间`;
    form.id = rowData.id;
    form.dayOfWeek = rowData.dayOfWeek;
    form.startPeriod = rowData.startPeriod;
    form.endPeriod = rowData.endPeriod;
    form.reason = rowData.reason || '';
    form.isActive = typeof rowData.isActive === 'boolean' ? rowData.isActive : true;
  } else {
    dialogMode.value = 'add';
    dialogTitle.value = `为 ${filterParams.targetGroup} 新增不可用时间 (${selectedSemesterDisplay.value})`;
    form.id = null;
    form.dayOfWeek = null;
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
  formRef.value?.resetFields();
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
        // id 不在 request DTO 中，在 form 中是为了编辑时知道是哪个记录
        targetGroup: filterParams.targetGroup, // 从筛选条件中获取
        semesterId: filterParams.semesterId, // 从筛选条件中获取
        dayOfWeek: form.dayOfWeek,
        startPeriod: form.startPeriod,
        endPeriod: form.endPeriod,
        reason: form.reason,
        isActive: form.isActive,
      };

      try {
        if (dialogMode.value === 'add') {
          await axios.post(`${API_BASE_URL}/class-group-unavailabilities`, payload);
          ElMessage.success('新增成功！');
        } else {
          await axios.put(`${API_BASE_URL}/class-group-unavailabilities/${form.id}`, payload);
          ElMessage.success('更新成功！');
        }
        dialogVisible.value = false;
        await fetchUnavailabilities();
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '操作失败！');
        console.error("Submit class group unavailability error:", error);
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
    await axios.delete(`${API_BASE_URL}/class-group-unavailabilities/${id}`);
    ElMessage.success('删除成功！');
    await fetchUnavailabilities();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败！');
      console.error("Delete class group unavailability error:", error);
    }
  }
};

onMounted(() => {
  fetchSemesters();
});
</script>

<style scoped>
.class-group-unavailability-management-container {
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