<template>
  <div class="room-unavailability-management-container">
    <el-card shadow="never">
      <el-form :model="filterParams" inline>
        <el-form-item label="选择教室">
          <el-select v-model="filterParams.roomId" filterable placeholder="请选择教室" style="width: 200px;" @change="handleRoomOrSemesterChange">
            <el-option v-for="room in roomList" :key="room.id" :label="`${room.roomNumber} (${room.type || 'N/A'})`" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="选择学期">
          <el-select v-model="filterParams.semesterId" placeholder="请选择学期" style="width: 240px;" @change="handleRoomOrSemesterChange">
            <el-option v-for="semester in semesterList" :key="semester.id" :label="`${semester.academicYear} ${semester.name}`" :value="semester.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="SearchIcon" @click="fetchUnavailabilities" :disabled="!filterParams.roomId || !filterParams.semesterId">
            查询不可用时间
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 15px;" v-if="filterParams.roomId && filterParams.semesterId && dataLoaded">
      <template #header>
        <div class="clearfix">
          <span>
            教室: {{ selectedRoomDisplay }} | 学期: {{ selectedSemesterDisplay }} 的不可用时间
          </span>
          <el-button
              style="float: right; padding: 3px 0"
              type="primary"
              text
              :icon="Plus"
              @click="handleOpenDialog(null)"
              v-if="authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_MANAGER'])"
              :disabled="!filterParams.roomId || !filterParams.semesterId"
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
        当前教室在此学期暂无不可用时间记录，可点击右上角新增。
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
  roomId: null, // 改为 roomId
  semesterId: null,
});

const roomList = ref([]); // 新增教室列表
const semesterList = ref([]);
const unavailabilityList = ref([]);
const loadingData = ref(false);
const dataLoaded = ref(false);

const dialogVisible = ref(false);
const dialogMode = ref('add');
const dialogTitle = ref('新增不可用时间');
const form = reactive({
  id: null,
  roomId: null, // 将在提交时从 filterParams 获取
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

const selectedRoomDisplay = computed(() => {
  const room = roomList.value.find(r => r.id === filterParams.roomId);
  return room ? `${room.roomNumber} (${room.type || 'N/A'})` : '未知教室';
});
const selectedSemesterDisplay = computed(() => {
  const semester = semesterList.value.find(s => s.id === filterParams.semesterId);
  return semester ? `${semester.academicYear} ${semester.name}` : '未知学期';
});
const currentSemesterPeriodsPerDay = computed(() => {
  const semester = semesterList.value.find(s => s.id === filterParams.semesterId);
  return semester?.periodsPerDay || 12;
});

const fetchRooms = async () => { // 新增获取教室列表方法
  try {
    const response = await axios.get(`${API_BASE_URL}/classrooms`);
    console.log('Fetched rooms response:', response.data); // <<--- 添加这行来查看数据结构
    // 根据 response.data 的结构来赋值
    if (Array.isArray(response.data)) {
      roomList.value = response.data;
    } else if (response.data && Array.isArray(response.data.content)) { // 检查是否是分页对象
      roomList.value = response.data.content;
    } else {
      // 如果结构未知或不符合预期，给一个空数组并打印警告
      roomList.value = [];
      console.warn('Unexpected room list data structure:', response.data);
    }
  } catch (error) {
    ElMessage.error('获取教室列表失败！');
    console.error("Fetch rooms error:", error);
    roomList.value = []; // 出错时也给空数组
  }
};

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
  if (!filterParams.roomId || !filterParams.semesterId) {
    unavailabilityList.value = [];
    dataLoaded.value = true;
    return;
  }
  loadingData.value = true;
  dataLoaded.value = false;
  try {
    // 修改API路径
    const response = await axios.get(`${API_BASE_URL}/room-unavailabilities/room/${filterParams.roomId}/semester/${filterParams.semesterId}`);
    unavailabilityList.value = response.data;
  } catch (error) {
    ElMessage.error('获取教室不可用时间列表失败！');
    unavailabilityList.value = [];
    console.error("Fetch room unavailabilities error:", error);
  } finally {
    loadingData.value = false;
    dataLoaded.value = true;
  }
};

const handleRoomOrSemesterChange = () => {
  unavailabilityList.value = [];
  dataLoaded.value = false;
};

const formatDayOfWeek = (row, column, cellValue) => {
  const days = ["", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"];
  return days[cellValue] || '未知';
};

const handleOpenDialog = (rowData = null) => {
  if (!filterParams.roomId || !filterParams.semesterId) {
    ElMessage.warning('请先选择教室和学期才能新增不可用时间。');
    return;
  }
  if (rowData) {
    dialogMode.value = 'edit';
    dialogTitle.value = `编辑 ${selectedRoomDisplay.value} 的不可用时间`;
    form.id = rowData.id;
    form.dayOfWeek = rowData.dayOfWeek;
    form.startPeriod = rowData.startPeriod;
    form.endPeriod = rowData.endPeriod;
    form.reason = rowData.reason || '';
    form.isActive = typeof rowData.isActive === 'boolean' ? rowData.isActive : true;
  } else {
    dialogMode.value = 'add';
    dialogTitle.value = `为 ${selectedRoomDisplay.value} 新增不可用时间 (${selectedSemesterDisplay.value})`;
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
        roomId: filterParams.roomId,
        semesterId: filterParams.semesterId,
        dayOfWeek: form.dayOfWeek,
        startPeriod: form.startPeriod,
        endPeriod: form.endPeriod,
        reason: form.reason,
        isActive: form.isActive,
      };

      try {
        if (dialogMode.value === 'add') {
          await axios.post(`${API_BASE_URL}/room-unavailabilities`, payload);
          ElMessage.success('新增成功！');
        } else {
          await axios.put(`${API_BASE_URL}/room-unavailabilities/${form.id}`, payload);
          ElMessage.success('更新成功！');
        }
        dialogVisible.value = false;
        await fetchUnavailabilities();
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '操作失败！');
        console.error("Submit room unavailability error:", error);
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
    await axios.delete(`${API_BASE_URL}/room-unavailabilities/${id}`);
    ElMessage.success('删除成功！');
    await fetchUnavailabilities();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败！');
      console.error("Delete room unavailability error:", error);
    }
  }
};

onMounted(() => {
  fetchRooms(); // 加载教室列表
  fetchSemesters();
});
</script>

<style scoped>
.room-unavailability-management-container {
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