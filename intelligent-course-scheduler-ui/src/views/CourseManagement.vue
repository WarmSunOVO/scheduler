<template>
  <div class="course-management-container">
    <!-- 搜索栏 -->
    <el-card class="search-action-card" shadow="never">
      <el-form :model="searchParams" ref="searchFormRef" inline>
        <el-form-item label="课程名称/代码" prop="query">
          <el-input v-model="searchParams.query" placeholder="搜索课程名称或代码" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="searchParams.status" placeholder="课程状态" clearable style="width: 120px;">
            <el-option label="激活" value="激活"></el-option>
            <el-option label="停用" value="停用"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="SearchIcon" @click="handleSearch">搜索</el-button>
          <el-button :icon="RefreshIcon" @click="handleResetSearch">重置</el-button>
        </el-form-item>
        <el-form-item style="float: right;">
          <el-button
              v-if="authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_TEACHER'])"
              type="primary"
              :icon="Plus"
              @click="handleOpenAddDialog"
          >
            新增课程
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 课程列表表格 -->
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
      <el-table-column prop="courseCode" label="课程代码" width="150" sortable="custom" />
      <el-table-column prop="name" label="课程名称" width="200" sortable="custom" show-overflow-tooltip />
      <el-table-column prop="credits" label="学分" width="100" align="center" />
      <el-table-column prop="type" label="课程类型" width="120" />
      <el-table-column prop="departmentName" label="开课院系" width="150" />
      <el-table-column prop="requiredRoomType" label="教室类型要求" width="150" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === '激活' ? 'success' : 'info'">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" :formatter="formatDateTime" sortable="custom" />
      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="scope">
          <el-button
              v-if="authStore.hasAnyRole(['ROLE_ADMIN', 'ROLE_TEACHER'])"
              size="small" :icon="Edit" @click="handleOpenEditDialog(scope.row)" circle title="编辑"></el-button>
          <el-button
              v-if="authStore.hasRole('ROLE_ADMIN')"
              size="small" type="danger" :icon="Delete" @click="handleDelete(scope.row.id, scope.row.name)" circle title="删除"></el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
                     :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="pagination.total"
                     @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <!-- 新增/编辑课程对话框 -->
    <el-dialog :model-value="dialogVisible" :title="dialogTitle" width="700px" @close="handleCloseDialog" draggable :close-on-click-modal="false">
      <el-form :model="courseForm" :rules="courseFormRules" ref="courseFormRef" label-width="120px" status-icon>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="课程代码" prop="courseCode">
              <el-input v-model="courseForm.courseCode" placeholder="例如：CS101" :disabled="dialogMode === 'edit'" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程名称" prop="name">
              <el-input v-model="courseForm.name" placeholder="例如：计算机组成原理" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学分" prop="credits">
              <el-input-number v-model="courseForm.credits" :precision="1" :step="0.5" :min="0.5" placeholder="例如：3.0" style="width:100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程类型" prop="type">
              <el-select v-model="courseForm.type" placeholder="选择课程类型" clearable style="width:100%;">
                <el-option label="必修课" value="必修课"></el-option>
                <el-option label="选修课" value="选修课"></el-option>
                <el-option label="理论课" value="理论课"></el-option>
                <el-option label="实验课" value="实验课"></el-option>
                <el-option label="实践课" value="实践课"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="周学时" prop="hoursPerWeek">
              <el-input-number v-model="courseForm.hoursPerWeek" :min="0" placeholder="每周课时数" style="width:100%;"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="总学时" prop="totalHours">
              <el-input-number v-model="courseForm.totalHours" :min="0" placeholder="总课时数" style="width:100%;"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="开课院系" prop="departmentName">
          <el-input v-model="courseForm.departmentName" placeholder="例如：计算机学院" />
        </el-form-item>
        <el-form-item label="教室类型要求" prop="requiredRoomType">
          <el-select v-model="courseForm.requiredRoomType" placeholder="选择所需教室类型" clearable style="width:100%;">
            <el-option label="无特殊要求" value=""></el-option>
            <el-option label="普通教室" value="普通教室"></el-option>
            <el-option label="多媒体教室" value="多媒体教室"></el-option>
            <el-option label="计算机房" value="计算机房"></el-option>
            <el-option label="实验室" value="实验室"></el-option>
            <el-option label="语音室" value="语音室"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input type="textarea" :rows="3" v-model="courseForm.description" placeholder="课程简介、教学大纲等" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="courseForm.status" active-value="激活" inactive-value="停用" active-text="激活" inactive-text="停用" />
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

const API_COURSES_URL = '/api/courses'; // API端点
const authStore = useAuthStore();

const tableData = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogMode = ref('add');
const dialogTitle = ref('新增课程');
const formSubmitting = ref(false);

const searchParams = reactive({ query: '', status: '' }); // query 用于代码或名称搜索
const searchFormRef = ref(null);

const initialCourseForm = {
  id: null, courseCode: '', name: '', credits: null, hoursPerWeek: null, totalHours: null,
  type: '', requiredRoomType: '', departmentName: '', description: '', status: '激活',
};
const courseForm = reactive({ ...initialCourseForm });
const courseFormRef = ref(null);

// 表单校验规则
const courseFormRules = reactive({
  courseCode: [{ required: true, message: '课程代码为必填项', trigger: 'blur' }],
  name: [{ required: true, message: '课程名称为必填项', trigger: 'blur' }],
  credits: [{ required: true, type: 'number', message: '学分必须为数字且不能为空', trigger: 'blur' }],
});

const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0, sortField: 'courseCode', sortOrder: 'asc' });

// --- 方法 ---
const fetchCourses = async () => { /* 与 UserManagement 类似，但API和参数不同 */
  loading.value = true;
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      query: searchParams.query || null, // 后端接收的参数名
      // status: searchParams.status || null, // 如果后端支持按状态搜索
    };
    if (pagination.sortField) {
      let direction = pagination.sortOrder === 'ascending' ? 'asc' : 'desc';
      params.sort = `${pagination.sortField},${direction}`;
    }
    if (searchParams.status) { // 单独处理status，如果后端按此参数名
      params.status = searchParams.status;
    }

    console.log("Fetching courses with params:", JSON.stringify(params));
    const response = await axios.get(API_COURSES_URL, { params });
    tableData.value = response.data.content;
    pagination.total = response.data.totalElements;
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '获取课程列表失败！');
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => { pagination.currentPage = 1; fetchCourses(); };
const handleResetSearch = () => {
  searchFormRef.value?.resetFields();
  searchParams.query = '';
  searchParams.status = '';
  pagination.sortField = 'courseCode'; pagination.sortOrder = 'asc';
  handleSearch();
};

const handleOpenAddDialog = () => {
  dialogMode.value = 'add';
  dialogTitle.value = '新增课程';
  Object.assign(courseForm, { ...initialCourseForm, status: '激活' });
  dialogVisible.value = true;
  nextTick(() => courseFormRef.value?.clearValidate());
};

const handleOpenEditDialog = (row) => {
  dialogMode.value = 'edit';
  dialogTitle.value = '编辑课程';
  Object.assign(courseForm, JSON.parse(JSON.stringify(row)));
  dialogVisible.value = true;
  nextTick(() => courseFormRef.value?.clearValidate());
};

const handleCloseDialog = () => { dialogVisible.value = false; };

const handleSubmitForm = async () => {
  if (!courseFormRef.value) return;
  await courseFormRef.value.validate(async (valid) => {
    if (valid) {
      formSubmitting.value = true;
      try {
        const payload = { ...courseForm };
        if (dialogMode.value === 'add') {
          await axios.post(API_COURSES_URL, payload);
          ElMessage.success('课程创建成功！');
        } else {
          await axios.put(`${API_COURSES_URL}/${courseForm.id}`, payload);
          ElMessage.success('课程更新成功！');
        }
        dialogVisible.value = false;
        await fetchCourses();
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
    await ElMessageBox.confirm(`确定要删除课程 "${name}" 吗？`, '警告', {
      confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning', draggable: true,
    });
    loading.value = true;
    await axios.delete(`${API_COURSES_URL}/${id}`);
    ElMessage.success('课程删除成功！');
    if (tableData.value.length === 1 && pagination.currentPage > 1) pagination.currentPage--;
    await fetchCourses();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败，请重试！');
    }
  } finally {
    loading.value = false;
  }
};

const handleSizeChange = (val) => { pagination.pageSize = val; pagination.currentPage = 1; fetchCourses(); };
const handleCurrentChange = (val) => { pagination.currentPage = val; fetchCourses(); };
const handleSortChange = ({ prop, order }) => { /* ...与 UserManagement 类似 ... */
  if (prop) {
    pagination.sortField = prop;
    pagination.sortOrder = order || 'ascending'; // Element Plus的order可能是null
  } else {
    pagination.sortField = 'courseCode'; pagination.sortOrder = 'ascending';
  }
  fetchCourses();
};

const formatDateTime = (row, column, cellValue) => cellValue ? cellValue.replace('T', ' ').substring(0, 16) : ''; // 显示到分钟
// formatRoleName 在这里不需要

onMounted(() => {
  fetchCourses();
  // 如果有其他需要加载的，例如课程类型列表，可以在这里调用
});
</script>

<style scoped>
/* 大部分样式可以复用 UserManagement 的，这里只列出可能的微调 */
.course-management-container { padding: 15px; background-color: #f4f6f8; min-height: calc(100vh - 50px); }
.search-action-card { margin-bottom: 15px; }
.search-action-card :deep(.el-card__body) { padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; }
.search-form { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
.search-form .el-form-item { margin-bottom: 0 !important; }
.search-action-card :deep(.el-form-item[style*="float: right"]) { margin-left: auto; }
.pagination-container { margin-top: 15px; display: flex; justify-content: flex-end; background-color: #fff; padding: 10px 15px; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.el-table { border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.el-dialog .el-select, .el-dialog .el-input-number, .el-dialog .el-cascader { width: 100%; } /* 确保对话框内选择器等宽度正确 */
.el-dialog .el-form-item { margin-bottom: 20px; }
.el-table :deep(.el-table__body-wrapper) { overflow-y: auto; }
</style>