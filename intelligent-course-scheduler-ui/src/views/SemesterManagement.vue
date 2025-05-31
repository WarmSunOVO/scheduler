<template>
  <div class="semester-management-container">
    <el-card class="search-action-card" shadow="never">
      <el-form :model="searchParams" ref="searchFormRef" inline>
        <el-form-item label="学年/名称" prop="query">
          <el-input v-model="searchParams.query" placeholder="搜索学年或学期名称" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="SearchIcon" @click="handleSearch">搜索</el-button>
          <el-button :icon="RefreshIcon" @click="handleResetSearch">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right;">
          <el-button
              v-if="authStore.hasRole('ROLE_ADMIN')"
              type="primary"
              :icon="Plus"
              @click="handleOpenAddDialog"
          >
            新增学期
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%; margin-top:15px;"
        border
        stripe
        height="calc(100vh - 290px)"
        @sort-change="handleSortChange"
        :default-sort="{ prop: pagination.sortField, order: pagination.sortOrder === 'asc' ? 'ascending' : 'descending' }"
    >
      <el-table-column prop="id" label="ID" width="80" sortable="custom" />
      <el-table-column prop="academicYear" label="学年" width="150" sortable="custom" />
      <el-table-column prop="name" label="学期名称" width="150" sortable="custom" />
      <el-table-column prop="startDate" label="开始日期" width="150" :formatter="formatDate" />
      <el-table-column prop="endDate" label="结束日期" width="150" :formatter="formatDate" />
      <el-table-column prop="isCurrent" label="当前学期" width="120" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.isCurrent ? 'success' : 'info'">
            {{ scope.row.isCurrent ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remarks" label="备注" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="创建时间" width="170" :formatter="formatDateTime" />
      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="scope">
          <el-button
              v-if="authStore.hasRole('ROLE_ADMIN')"
              size="small" :icon="Edit" @click="handleOpenEditDialog(scope.row)" circle title="编辑"></el-button>
          <el-button
              v-if="authStore.hasRole('ROLE_ADMIN')"
              size="small" type="danger" :icon="Delete" @click="handleDelete(scope.row.id, `${scope.row.academicYear} ${scope.row.name}`)" circle title="删除"></el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
                     :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="pagination.total"
                     @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <el-dialog :model-value="dialogVisible" :title="dialogTitle" width="600px" @close="handleCloseDialog" draggable :close-on-click-modal="false">
      <el-form :model="semesterForm" :rules="semesterFormRules" ref="semesterFormRef" label-width="100px" status-icon>
        <el-form-item label="学年" prop="academicYear">
          <el-input v-model="semesterForm.academicYear" placeholder="例如：2024-2025" />
        </el-form-item>
        <el-form-item label="学期名称" prop="name">
          <el-select v-model="semesterForm.name" placeholder="选择学期" style="width:100%;">
            <el-option label="第一学期" value="第一学期"></el-option>
            <el-option label="第二学期" value="第二学期"></el-option>
            <el-option label="夏季学期" value="夏季学期"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker v-model="semesterForm.startDate" type="date" placeholder="选择开始日期" style="width:100%;" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker v-model="semesterForm.endDate" type="date" placeholder="选择结束日期" style="width:100%;" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="设为当前" prop="isCurrent">
          <el-switch v-model="semesterForm.isCurrent" />
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input type="textarea" :rows="3" v-model="semesterForm.remarks" placeholder="可选备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmitForm" :loading="formSubmitting">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus, Edit, Delete, Search as SearchIcon, Refresh as RefreshIcon } from '@element-plus/icons-vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';

const API_SEMESTERS_URL = '/api/semesters'; // API端点
const authStore = useAuthStore();

const tableData = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogMode = ref('add');
const dialogTitle = ref('新增学期');
const formSubmitting = ref(false);

const searchParams = reactive({ query: '' });
const searchFormRef = ref(null);

const initialSemesterForm = {
  id: null, academicYear: '', name: '', startDate: null, endDate: null,
  isCurrent: false, remarks: '',
};
const semesterForm = reactive({ ...initialSemesterForm });
const semesterFormRef = ref(null);

const semesterFormRules = reactive({
  academicYear: [{ required: true, message: '学年为必填项', trigger: 'blur' }],
  name: [{ required: true, message: '学期名称为必填项', trigger: 'change' }],
  startDate: [{ type: 'date', message: '开始日期格式不正确', trigger: 'change' }],
  endDate: [{ type: 'date', message: '结束日期格式不正确', trigger: 'change' }],
});

const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0, sortField: 'academicYear', sortOrder: 'desc' });

const fetchSemesters = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      query: searchParams.query || null, // ★★★ 确保这里使用了 searchParams.query ★★★
      // status: searchParams.status || null, // 如果学期管理也有状态搜索
    };
    if (pagination.sortField) {
      let direction = pagination.sortOrder === 'ascending' ? 'asc' : 'desc';
      params.sort = `${pagination.sortField},${direction}`;
    }

    console.log(`Fetching ${API_SEMESTERS_URL} with params:`, JSON.stringify(params)); // 这行日志很有用
    const response = await axios.get(API_SEMESTERS_URL, { params });
    tableData.value = response.data.content;
    pagination.total = response.data.totalElements;
  } catch (error) {
    console.error("获取学期列表失败时的错误详情:", error.response || error);
    ElMessage.error(error.response?.data?.message || '获取学期列表失败！');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  console.log('handleSearch called. Current search query:', searchParams.query); // <--- 添加日志
  pagination.currentPage = 1; fetchSemesters(); };
const handleResetSearch = () => {
  searchFormRef.value?.resetFields(); // 只重置带prop的表单项
  searchParams.query = ''; // 手动清空
  pagination.sortField = 'academicYear'; pagination.sortOrder = 'desc';
  handleSearch();
};

const handleOpenAddDialog = () => {
  dialogMode.value = 'add';
  dialogTitle.value = '新增学期';
  Object.assign(semesterForm, { ...initialSemesterForm, isCurrent: false });
  dialogVisible.value = true;
  nextTick(() => semesterFormRef.value?.clearValidate());
};

const handleOpenEditDialog = (row) => {
  dialogMode.value = 'edit';
  dialogTitle.value = '编辑学期';
  Object.assign(semesterForm, JSON.parse(JSON.stringify(row)));
  dialogVisible.value = true;
  nextTick(() => semesterFormRef.value?.clearValidate());
};

const handleCloseDialog = () => { dialogVisible.value = false; };

const handleSubmitForm = async () => {
  if (!semesterFormRef.value) return;
  await semesterFormRef.value.validate(async (valid) => {
    if (valid) {
      formSubmitting.value = true;
      try {
        const payload = { ...semesterForm };
        // 日期可能需要特定格式化，但如果后端能处理 YYYY-MM-DD 就还好
        if (dialogMode.value === 'add') {
          await axios.post(API_SEMESTERS_URL, payload);
          ElMessage.success('学期创建成功！');
        } else {
          await axios.put(`${API_SEMESTERS_URL}/${semesterForm.id}`, payload);
          ElMessage.success('学期更新成功！');
        }
        dialogVisible.value = false;
        await fetchSemesters();
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '操作失败，请重试！');
      } finally {
        formSubmitting.value = false;
      }
    }
  });
};

const handleDelete = async (id, name) => {
  try {
    await ElMessageBox.confirm(`确定要删除学期 "${name}" 吗？`, '警告', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning', draggable: true,
    });
    loading.value = true;
    await axios.delete(`${API_SEMESTERS_URL}/${id}`);
    ElMessage.success('学期删除成功！');
    if (tableData.value.length === 1 && pagination.currentPage > 1) pagination.currentPage--;
    await fetchSemesters();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败，请重试！');
    }
  } finally {
    loading.value = false;
  }
};

const handleSizeChange = (val) => { pagination.pageSize = val; pagination.currentPage = 1; fetchSemesters(); };
const handleCurrentChange = (val) => { pagination.currentPage = val; fetchSemesters(); };
const handleSortChange = ({ prop, order }) => {
  if (prop) {
    pagination.sortField = prop;
    pagination.sortOrder = order || 'ascending';
  } else {
    pagination.sortField = 'academicYear'; pagination.sortOrder = 'desc';
  }
  fetchSemesters();
};

const formatDateTime = (row, column, cellValue) => cellValue ? cellValue.replace('T', ' ').substring(0, 16) : '';
const formatDate = (row, column, cellValue) => cellValue ? cellValue : ''; // 日期类型直接显示


onMounted(() => {
  fetchSemesters();
});
</script>

<style scoped>
/* 复用或微调 UserManagement 的样式 */
.semester-management-container { padding: 15px; background-color: #f4f6f8; min-height: calc(100vh - 50px); }
.search-action-card { margin-bottom: 15px; }
.search-action-card :deep(.el-card__body) { padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; }
.search-form { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
.search-form .el-form-item { margin-bottom: 0 !important; }
.search-action-card :deep(.el-form-item[style*="float: right"]) { margin-left: auto; }
.pagination-container { margin-top: 15px; display: flex; justify-content: flex-end; background-color: #fff; padding: 10px 15px; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.el-table { border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.el-dialog .el-select, .el-dialog .el-input-number, .el-dialog .el-date-picker { width: 100%; }
.el-dialog .el-form-item { margin-bottom: 20px; }
.el-table :deep(.el-table__body-wrapper) { overflow-y: auto; }
</style>