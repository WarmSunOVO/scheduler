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
      <el-table-column prop="startDate" label="开始日期" width="120" :formatter="formatDate" />
      <el-table-column prop="endDate" label="结束日期" width="120" :formatter="formatDate" />
      <!-- VVVVVV 新增的列 VVVVVV -->
      <el-table-column prop="totalWeeks" label="总周数" width="90" align="center"/>
      <el-table-column prop="periodsPerDay" label="每日节次" width="100" align="center"/>
      <el-table-column prop="daysPerWeek" label="每周天数" width="100" align="center"/>
      <!-- ^^^^^^ 新增的列 ^^^^^^ -->
      <el-table-column prop="isCurrent" label="当前学期" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.isCurrent ? 'success' : 'info'">
            {{ scope.row.isCurrent ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remarks" label="备注" min-width="180" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="创建时间" width="170" :formatter="formatDateTime" />
      <el-table-column label="操作" width="130" fixed="right" align="center">
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

    <el-dialog :model-value="dialogVisible" :title="dialogTitle" width="650px" @close="handleCloseDialog" draggable :close-on-click-modal="false">
      <el-form :model="semesterForm" :rules="semesterFormRules" ref="semesterFormRef" label-width="120px" status-icon>
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
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker v-model="semesterForm.startDate" type="date" placeholder="选择开始日期" style="width:100%;" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker v-model="semesterForm.endDate" type="date" placeholder="选择结束日期" style="width:100%;" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- VVVVVV 新增的表单项 VVVVVV -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="总周数" prop="totalWeeks">
              <el-input-number v-model="semesterForm.totalWeeks" :min="1" :max="52" placeholder="例如: 18" style="width: 100%;" controls-position="right"/>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="每日总节次" prop="periodsPerDay">
              <el-input-number v-model="semesterForm.periodsPerDay" :min="1" :max="20" placeholder="例如: 10" style="width: 100%;" controls-position="right"/>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="每周上课天数" prop="daysPerWeek">
              <el-input-number v-model="semesterForm.daysPerWeek" :min="1" :max="7" placeholder="例如: 5" style="width: 100%;" controls-position="right"/>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- ^^^^^^ 新增的表单项 ^^^^^^ -->
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
import dayjs from 'dayjs'; // 引入 dayjs

const API_SEMESTERS_URL = '/api/semesters';
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
  id: null,
  academicYear: dayjs().format('YYYY') + '-' + (dayjs().year() + 1), // 默认当年-下一年
  name: '', // 学期名称让用户选择或输入
  startDate: dayjs().format('YYYY-MM-DD'), // 默认今天
  endDate: dayjs().add(4, 'month').format('YYYY-MM-DD'), // 默认四个月后
  isCurrent: false,
  remarks: '',
  // VVVVVV 新增字段的默认值 VVVVVV
  totalWeeks: 18,
  periodsPerDay: 10,
  daysPerWeek: 5,
  // ^^^^^^ 新增字段的默认值 ^^^^^^
};
const semesterForm = reactive({ ...initialSemesterForm });
const semesterFormRef = ref(null);

const semesterFormRules = reactive({
  academicYear: [{ required: true, message: '学年为必填项', trigger: 'blur' }],
  name: [{ required: true, message: '学期名称为必填项', trigger: 'change' }],
  startDate: [
    { required: true, message: '开始日期为必填项', trigger: 'change' },
    { type: 'date', message: '开始日期格式不正确', trigger: 'change' }
  ],
  endDate: [
    { required: true, message: '结束日期为必填项', trigger: 'change' },
    { type: 'date', message: '结束日期格式不正确', trigger: 'change' },
    { validator: (rule, value, callback) => {
        if (semesterForm.startDate && value && dayjs(value).isBefore(dayjs(semesterForm.startDate))) {
          callback(new Error('结束日期不能早于开始日期'));
        } else {
          callback();
        }
      }, trigger: 'change' }
  ],
  // VVVVVV 新增字段的校验规则 VVVVVV
  totalWeeks: [
    { required: true, message: '总周数不能为空', trigger: 'blur' },
    { type: 'integer', min: 1, message: '总周数必须是大于0的整数', trigger: 'blur'}
  ],
  periodsPerDay: [
    { required: true, message: '每日总节次不能为空', trigger: 'blur' },
    { type: 'integer', min: 1, max: 20, message: '每日总节次必须是1-20之间的整数', trigger: 'blur'}
  ],
  daysPerWeek: [
    { required: true, message: '每周上课天数不能为空', trigger: 'blur' },
    { type: 'integer', min: 1, max: 7, message: '每周上课天数必须是1-7之间的整数', trigger: 'blur'}
  ],
  // ^^^^^^ 新增字段的校验规则 ^^^^^^
});

const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0, sortField: 'academicYear', sortOrder: 'desc' });

const fetchSemesters = async () => {
  // ... (您已有的 fetchSemesters 逻辑保持不变) ...
  loading.value = true;
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      query: searchParams.query || null,
    };
    if (pagination.sortField) {
      let direction = pagination.sortOrder === 'ascending' ? 'asc' : 'desc';
      params.sort = `${pagination.sortField},${direction}`;
    }
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

const handleSearch = () => { pagination.currentPage = 1; fetchSemesters(); };
const handleResetSearch = () => {
  searchFormRef.value?.resetFields();
  searchParams.query = '';
  pagination.sortField = 'academicYear'; pagination.sortOrder = 'desc';
  handleSearch();
};

const handleOpenAddDialog = () => {
  dialogMode.value = 'add';
  dialogTitle.value = '新增学期';
  // 使用深拷贝确保 initialSemesterForm 不被意外修改，并且每次新增都是干净的初始值
  Object.assign(semesterForm, JSON.parse(JSON.stringify(initialSemesterForm)));
  // isCurrent 在 initialForm 中已设为 false，如果需要特定逻辑可以再调整
  dialogVisible.value = true;
  nextTick(() => semesterFormRef.value?.clearValidate());
};

const handleOpenEditDialog = (row) => {
  dialogMode.value = 'edit';
  dialogTitle.value = '编辑学期';
  // 深拷贝行数据到表单，确保日期等能被正确处理和回显
  Object.assign(semesterForm, JSON.parse(JSON.stringify(row)));
  dialogVisible.value = true;
  nextTick(() => semesterFormRef.value?.clearValidate());
};

const handleCloseDialog = () => {
  dialogVisible.value = false;
  // resetFields 会将表单项重置为初始值（如果 form-item 有 prop），并清除校验
  // 如果 initialSemesterForm 动态变化，或者希望更彻底重置为最新的 initialSemesterForm，下面的 assign 更好
  // semesterFormRef.value?.resetFields();
  Object.assign(semesterForm, JSON.parse(JSON.stringify(initialSemesterForm)));
};

const handleSubmitForm = async () => {
  // ... (您已有的 handleSubmitForm 逻辑保持不变，它会提交整个 semesterForm) ...
  if (!semesterFormRef.value) return;
  await semesterFormRef.value.validate(async (valid) => {
    if (valid) {
      formSubmitting.value = true;
      try {
        const payload = { ...semesterForm };
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

const handleDelete = async (id, name) => { /* ... (您已有的 handleDelete 逻辑保持不变) ... */
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
    pagination.sortOrder = order || 'ascending'; // el-table 的 order 是 'ascending' 或 'descending'
  } else { // 默认排序
    pagination.sortField = 'academicYear'; pagination.sortOrder = 'desc';
  }
  fetchSemesters();
};

// 日期格式化函数 (保持不变)
const formatDateTime = (row, column, cellValue) => cellValue ? dayjs(cellValue).format('YYYY-MM-DD HH:mm') : ''; // 稍微修改格式
const formatDate = (row, column, cellValue) => cellValue ? dayjs(cellValue).format('YYYY-MM-DD') : '';


onMounted(() => {
  fetchSemesters();
});
</script>

<style scoped>
/* 您已有的样式保持不变 */
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