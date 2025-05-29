<template>
  <div class="classroom-management-container">
    <!-- 搜索与操作栏 -->
    <el-card class="search-action-card" shadow="never">
      <div class="card-content">
        <el-form :model="searchParams" ref="searchFormRef" inline class="search-form">
          <el-form-item label="教室编号" prop="roomNumber">
            <el-input v-model="searchParams.roomNumber" placeholder="模糊搜索教室编号" clearable @keyup.enter="handleSearch" style="width: 200px;" />
          </el-form-item>
          <el-form-item label="教室类型" prop="type">
            <el-select v-model="searchParams.type" placeholder="选择教室类型" clearable style="width: 180px;">
              <el-option label="普通教室" value="普通教室"></el-option>
              <el-option label="多媒体教室" value="多媒体教室"></el-option>
              <el-option label="实验室" value="实验室"></el-option>
              <el-option label="计算机房" value="计算机房"></el-option>
              <el-option label="语音室" value="语音室"></el-option>
              <el-option label="会议室" value="会议室"></el-option>
              <el-option label="画室" value="画室"></el-option>
              <el-option label="琴房" value="琴房"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="searchParams.status" placeholder="选择状态" clearable style="width: 130px;">
              <el-option label="可用" value="可用"></el-option>
              <el-option label="维修中" value="维修中"></el-option>
              <el-option label="停用" value="停用"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="SearchIcon" @click="handleSearch">搜索</el-button>
            <el-button :icon="RefreshIcon" @click="handleResetSearch">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="action-buttons">
          <el-button type="primary" :icon="Plus" @click="handleOpenAddDialog">新增教室</el-button>
        </div>
      </div>
    </el-card>

    <el-table :data="tableData" v-loading="loading" style="width: 100%; margin-top:15px;" border stripe height="calc(100vh - 290px)">
      <el-table-column prop="id" label="ID" width="80" sortable fixed="left" />
      <el-table-column prop="roomNumber" label="教室编号" width="150" sortable />
      <el-table-column prop="capacity" label="容量" width="100" align="center" sortable />
      <el-table-column prop="type" label="类型" width="150" />
      <el-table-column prop="buildingName" label="教学楼" width="180" />
      <el-table-column prop="floorLevel" label="楼层" width="80" align="center" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remarks" label="备注" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="创建时间" width="170" :formatter="formatDateTime" />
      <el-table-column prop="updatedAt" label="更新时间" width="170" :formatter="formatDateTime" />
      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="scope">
          <el-button size="small" :icon="Edit" @click="handleOpenEditDialog(scope.row)" circle title="编辑"></el-button>
          <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(scope.row.id)" circle title="删除"></el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 30, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="clamp(300px, 50%, 600px)"
        @close="handleCloseDialog"
        draggable
        :close-on-click-modal="false"
    >
      <el-form :model="classroomForm" :rules="formRules" ref="classroomFormRef" label-width="100px" status-icon>
        <el-form-item label="教室编号" prop="roomNumber">
          <el-input v-model="classroomForm.roomNumber" placeholder="例如：综教A101" clearable></el-input>
        </el-form-item>
        <el-form-item label="教室容量" prop="capacity">
          <el-input-number v-model="classroomForm.capacity" :min="1" :max="999" placeholder="座位数" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="教室类型" prop="type">
          <el-select v-model="classroomForm.type" placeholder="请选择教室类型" clearable filterable style="width: 100%;">
            <el-option label="普通教室" value="普通教室"></el-option>
            <el-option label="多媒体教室" value="多媒体教室"></el-option>
            <el-option label="实验室" value="实验室"></el-option>
            <el-option label="计算机房" value="计算机房"></el-option>
            <el-option label="语音室" value="语音室"></el-option>
            <el-option label="会议室" value="会议室"></el-option>
            <el-option label="画室" value="画室"></el-option>
            <el-option label="琴房" value="琴房"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="教学楼名称" prop="buildingName">
          <el-input v-model="classroomForm.buildingName" placeholder="例如：综合教学楼A座" clearable></el-input>
        </el-form-item>
        <el-form-item label="所在楼层" prop="floorLevel">
          <el-input-number v-model="classroomForm.floorLevel" :min="1" :max="50" placeholder="例如：1" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="classroomForm.status">
            <el-radio label="可用">可用</el-radio>
            <el-radio label="维修中">维修中</el-radio>
            <el-radio label="停用">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input type="textarea" v-model="classroomForm.remarks" :rows="3" placeholder="选填，最多200字" maxlength="200" show-word-limit></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseDialog">取消</el-button>
          <el-button type="primary" @click="handleSubmitForm" :loading="formSubmitting">
            {{ dialogMode === 'add' ? '创建' : '保存' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Search as SearchIcon, Refresh as RefreshIcon } from '@element-plus/icons-vue';
import axios from 'axios';

const API_BASE_URL = '/api';

const tableData = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogMode = ref('add');
const dialogTitle = ref('新增教室');
const formSubmitting = ref(false);

const initialClassroomForm = {
  id: null,
  roomNumber: '',
  capacity: 1, // Default capacity
  type: '',
  buildingName: '',
  floorLevel: 1, // Default floor
  status: '可用',
  remarks: ''
};
const classroomForm = reactive({ ...initialClassroomForm });
const classroomFormRef = ref(null);

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
  sortField: 'id',
  sortOrder: 'desc'
});

const searchParams = reactive({
  roomNumber: '',
  type: '',
  status: ''
});
const searchFormRef = ref(null);

const formRules = reactive({
  roomNumber: [ { required: true, message: '教室编号不能为空', trigger: 'blur' } ],
  capacity: [
    { required: true, message: '教室容量不能为空', trigger: 'change' },
    { type: 'number', min: 1, message: '容量必须大于0', trigger: 'change' }
  ],
  type: [ { required: true, message: '请选择教室类型', trigger: 'change' } ],
  status: [ { required: true, message: '请选择状态', trigger: 'change' } ]
});

const fetchClassrooms = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      sort: `${pagination.sortField},${pagination.sortOrder}`,
      ...(searchParams.roomNumber && { roomNumber: searchParams.roomNumber }),
      ...(searchParams.type && { type: searchParams.type }),
      ...(searchParams.status && { status: searchParams.status }),
    };
    const response = await axios.get(`${API_BASE_URL}/classrooms`, { params });
    tableData.value = response.data.content;
    pagination.total = response.data.totalElements;
  } catch (error) {
    console.error("获取教室列表失败:", error.response ? error.response.data : error.message);
    ElMessage.error(error.response?.data?.message || '获取教室列表失败！');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.currentPage = 1;
  fetchClassrooms();
};

const handleResetSearch = () => {
  searchFormRef.value?.resetFields(); // This will only reset fields with 'prop'
  // Manual reset for safety or if not all fields have 'prop' in search form
  searchParams.roomNumber = '';
  searchParams.type = '';
  searchParams.status = '';
  pagination.currentPage = 1;
  fetchClassrooms();
};

const handleOpenAddDialog = () => {
  dialogMode.value = 'add';
  dialogTitle.value = '新增教室';
  Object.assign(classroomForm, { ...initialClassroomForm });
  dialogVisible.value = true;
  nextTick(() => {
    classroomFormRef.value?.clearValidate();
  });
};

const handleOpenEditDialog = (row) => {
  dialogMode.value = 'edit';
  dialogTitle.value = '编辑教室';
  Object.assign(classroomForm, JSON.parse(JSON.stringify(row)));
  dialogVisible.value = true;
  nextTick(() => {
    classroomFormRef.value?.clearValidate();
  });
};

const handleCloseDialog = () => {
  dialogVisible.value = false;
};

const handleSubmitForm = async () => {
  if (!classroomFormRef.value) return;
  await classroomFormRef.value.validate(async (valid) => {
    if (valid) {
      formSubmitting.value = true;
      try {
        const payload = { ...classroomForm };
        payload.capacity = payload.capacity === null ? null : Number(payload.capacity);
        payload.floorLevel = payload.floorLevel === null ? null : Number(payload.floorLevel);

        if (dialogMode.value === 'add') {
          await axios.post(`${API_BASE_URL}/classrooms`, payload);
          ElMessage.success('教室创建成功！');
        } else {
          await axios.put(`${API_BASE_URL}/classrooms/${classroomForm.id}`, payload);
          ElMessage.success('教室更新成功！');
        }
        dialogVisible.value = false;
        await fetchClassrooms();
      } catch (error) {
        console.error("操作失败:", error.response ? error.response.data : error.message);
        ElMessage.error(error.response?.data?.message || '操作失败，请重试！');
      } finally {
        formSubmitting.value = false;
      }
    } else {
      ElMessage.warning('请检查表单填写是否正确！');
      return false;
    }
  });
};

const handleDelete = (id) => {
  ElMessageBox.confirm(
      '确定要删除该教室吗？此操作不可撤销。如果该教室已有排课，可能会导致问题。',
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        draggable: true,
      }
  )
      .then(async () => {
        loading.value = true;
        try {
          await axios.delete(`${API_BASE_URL}/classrooms/${id}`);
          ElMessage.success('教室删除成功！');
          if (tableData.value.length === 1 && pagination.currentPage > 1) {
            pagination.currentPage--;
          }
          await fetchClassrooms();
        } catch (error) {
          console.error("删除失败:", error.response ? error.response.data : error.message);
          ElMessage.error(error.response?.data?.message || '删除失败，请重试！');
        } finally {
          loading.value = false;
        }
      })
      .catch(() => {
        ElMessage.info('已取消删除');
      });
};

const handleSizeChange = (newSize) => {
  pagination.pageSize = newSize;
  pagination.currentPage = 1;
  fetchClassrooms();
};

const handleCurrentChange = (newPage) => {
  pagination.currentPage = newPage;
  fetchClassrooms();
};

const getStatusTagType = (status) => {
  if (status === '可用') return 'success';
  if (status === '维修中') return 'warning';
  if (status === '停用') return 'danger';
  return 'info';
};

const formatDateTime = (row, column, cellValue) => {
  if (!cellValue) return '';
  return cellValue.replace('T', ' ').substring(0, 19);
};

onMounted(() => {
  fetchClassrooms();
});
</script>

<style scoped>
.classroom-management-container {
  padding: 15px;
  background-color: #f4f6f8;
  min-height: calc(100vh - 50px);
}

.search-action-card {
  margin-bottom: 15px;
}
/* 新增：用于el-card内部内容的布局 */
.search-action-card :deep(.el-card__body) {
  padding: 15px 20px; /* 根据需要调整 el-card 内部的 padding */
}
.card-content {
  display: flex;
  justify-content: space-between; /* 让搜索表单靠左，操作按钮靠右 */
  align-items: flex-start; /* 垂直方向顶部对齐 */
  flex-wrap: wrap; /* 在小屏幕上允许换行 */
}

.search-form {
  display: flex;
  flex-wrap: wrap; /* 允许表单项换行 */
  gap: 10px; /* 表单项之间的间距 */
  align-items: center; /* 使表单项垂直居中对齐 */
}

.search-form .el-form-item {
  margin-bottom: 0 !important; /* !important 覆盖element-plus默认样式，使其更紧凑 */
}

.action-buttons {
  /* margin-left: auto; */ /* 如果希望按钮始终在最右边，但现在由父级flex控制 */
  /* 如果flex布局下按钮需要一些间距可以加 margin-left */
}


.pagination-container {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
  background-color: #fff;
  padding: 10px 15px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.el-table {
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.el-dialog .el-select,
.el-dialog .el-input-number {
  width: 100%;
}
.el-dialog .el-form-item {
  margin-bottom: 20px;
}

.el-table :deep(.el-table__body-wrapper) {
  overflow-y: auto;
}
</style>