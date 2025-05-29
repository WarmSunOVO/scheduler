<template>
  <div class="teacher-management-container">
    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" :icon="Plus" @click="handleOpenAddDialog">新增教师</el-button>
      <!-- TODO: 搜索/筛选功能后续添加 -->
    </div>

    <!-- 教师列表表格 -->
    <el-table :data="tableData" v-loading="loading" style="width: 100%" border stripe height="calc(100vh - 220px)">
      <el-table-column prop="id" label="ID" width="80" sortable fixed="left" />
      <el-table-column prop="teacherIdNumber" label="工号" width="120" sortable />
      <el-table-column prop="name" label="姓名" width="120" sortable />
      <el-table-column prop="gender" label="性别" width="80" align="center" />
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column prop="department" label="院系/部门" width="180" />
      <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip />
      <el-table-column prop="phoneNumber" label="电话" width="130" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getStatusTagType(scope.row.status)">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="创建时间" width="170" :formatter="formatDateTime" />
      <el-table-column prop="updatedAt" label="更新时间" width="170" :formatter="formatDateTime" />
      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="scope">
          <el-button size="small" :icon="Edit" @click="handleOpenEditDialog(scope.row)" circle title="编辑"></el-button>
          <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(scope.row.id)" circle title="删除"></el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
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

    <!-- 新增/编辑教师对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="clamp(400px, 60%, 800px)"
        @close="handleCloseDialog"
        draggable
        :close-on-click-modal="false"
    >
      <el-form :model="teacherForm" :rules="formRules" ref="teacherFormRef" label-width="120px" status-icon>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="教师工号" prop="teacherIdNumber">
              <el-input v-model="teacherForm.teacherIdNumber" placeholder="例如：T001" clearable :disabled="dialogMode === 'edit'"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="教师姓名" prop="name">
              <el-input v-model="teacherForm.name" placeholder="教师真实姓名" clearable></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="teacherForm.gender" placeholder="请选择性别" clearable style="width: 100%;">
                <el-option label="男" value="男"></el-option>
                <el-option label="女" value="女"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-select v-model="teacherForm.title" placeholder="请选择职称" clearable filterable style="width: 100%;">
                <el-option label="助教" value="助教"></el-option>
                <el-option label="讲师" value="讲师"></el-option>
                <el-option label="副教授" value="副教授"></el-option>
                <el-option label="教授" value="教授"></el-option>
                <el-option label="研究员" value="研究员"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属院系/部门" prop="department">
              <el-input v-model="teacherForm.department" placeholder="例如：计算机学院软件工程系" clearable></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="teacherForm.status">
                <el-radio label="在职">在职</el-radio>
                <el-radio label="休假">休假</el-radio>
                <el-radio label="离职">离职</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="teacherForm.email" placeholder="常用电子邮箱" clearable type="email"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phoneNumber">
          <el-input v-model="teacherForm.phoneNumber" placeholder="手机或办公电话" clearable></el-input>
        </el-form-item>
        <el-form-item label="可授课程" prop="teachableCourses">
          <el-input
              type="textarea"
              v-model="teacherForm.teachableCourses"
              :rows="3"
              :placeholder="teachableCoursesPlaceholder"
          ></el-input>
          <small class="form-item-help">提示：可输入课程名列表或JSON数组字符串。</small>
        </el-form-item>
        <el-form-item label="不便排课时间" prop="unavailableSlots">
          <el-input
              type="textarea"
              v-model="teacherForm.unavailableSlots"
              :rows="3"
              :placeholder="unavailableSlotsPlaceholder"
          ></el-input>
          <small class="form-item-help">提示：可输入文本描述或JSON格式。AI后续可辅助解析。</small>
        </el-form-item>
        <el-form-item label="备注" prop="remarks">
          <el-input type="textarea" v-model="teacherForm.remarks" :rows="2" placeholder="选填，最多200字" maxlength="200" show-word-limit></el-input>
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
import { Plus, Edit, Delete } from '@element-plus/icons-vue';
import axios from 'axios';

// --- API 基础 URL (确保 Vite 代理配置正确) ---
const API_BASE_URL = '/api';

// --- Placeholder 文本定义 ---
const teachableCoursesPlaceholder = `输入课程名，多个课程用英文逗号或换行分隔，例如：高等数学,线性代数
或JSON格式: ["高等数学", "线性代数"]`;

const unavailableSlotsPlaceholder = `描述不方便排课的时间段，例如：周一下午,周三上午1-2节
或JSON格式: [{"dayOfWeek": 1, "slots": ["下午"]}]`;


// --- 响应式数据 ---
const tableData = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogMode = ref('add');
const dialogTitle = ref('新增教师');
const formSubmitting = ref(false);

const initialTeacherForm = {
  id: null,
  teacherIdNumber: '',
  name: '',
  gender: '',
  title: '',
  department: '',
  email: '',
  phoneNumber: '',
  teachableCourses: '',
  unavailableSlots: '',
  status: '在职',
  remarks: ''
};
const teacherForm = reactive({ ...initialTeacherForm });
const teacherFormRef = ref(null);

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
  sortField: 'id',
  sortOrder: 'desc'
});

// --- 表单校验规则 ---
const validateEmail = (rule, value, callback) => {
  if (value && !/^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i.test(value)) {
    callback(new Error('请输入有效的邮箱地址'));
  } else {
    callback();
  }
};

const formRules = reactive({
  teacherIdNumber: [
    { required: true, message: '教师工号不能为空', trigger: 'blur' },
  ],
  name: [
    { required: true, message: '教师姓名不能为空', trigger: 'blur' }
  ],
  email: [
    { validator: validateEmail, trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
});

// --- 方法 ---

const fetchTeachers = async () => {
  loading.value = true;
  try {
    const response = await axios.get(`${API_BASE_URL}/teachers`, {
      params: {
        page: pagination.currentPage - 1,
        size: pagination.pageSize,
        sort: `${pagination.sortField},${pagination.sortOrder}`
      }
    });
    tableData.value = response.data.content;
    pagination.total = response.data.totalElements;
  } catch (error) {
    console.error("获取教师列表失败:", error.response ? error.response.data : error.message);
    ElMessage.error(error.response?.data?.message || '获取教师列表失败！');
  } finally {
    loading.value = false;
  }
};

const handleOpenAddDialog = () => {
  dialogMode.value = 'add';
  dialogTitle.value = '新增教师';
  Object.assign(teacherForm, { ...initialTeacherForm, status: '在职' });
  dialogVisible.value = true;
  nextTick(() => {
    teacherFormRef.value?.clearValidate();
  });
};

const handleOpenEditDialog = (row) => {
  dialogMode.value = 'edit';
  dialogTitle.value = '编辑教师';
  Object.assign(teacherForm, JSON.parse(JSON.stringify(row)));
  dialogVisible.value = true;
  nextTick(() => {
    teacherFormRef.value?.clearValidate();
  });
};

const handleCloseDialog = () => {
  dialogVisible.value = false;
};

const handleSubmitForm = async () => {
  if (!teacherFormRef.value) return;
  await teacherFormRef.value.validate(async (valid) => {
    if (valid) {
      formSubmitting.value = true;
      try {
        let response;
        const payload = { ...teacherForm };

        if (dialogMode.value === 'add') {
          response = await axios.post(`${API_BASE_URL}/teachers`, payload);
          ElMessage.success('教师创建成功！');
        } else {
          response = await axios.put(`${API_BASE_URL}/teachers/${teacherForm.id}`, payload);
          ElMessage.success('教师更新成功！');
        }
        dialogVisible.value = false;
        await fetchTeachers();
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
      '确定要删除该教师吗？此操作不可撤销。如果该教师已有排课任务，删除可能导致数据不一致。',
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
          await axios.delete(`${API_BASE_URL}/teachers/${id}`);
          ElMessage.success('教师删除成功！');
          if (tableData.value.length === 1 && pagination.currentPage > 1) {
            pagination.currentPage--;
          }
          await fetchTeachers();
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
  fetchTeachers();
};

const handleCurrentChange = (newPage) => {
  pagination.currentPage = newPage;
  fetchTeachers();
};

const getStatusTagType = (status) => {
  if (status === '在职') return 'success';
  if (status === '休假') return 'warning';
  if (status === '离职') return 'danger';
  return 'info';
};

const formatDateTime = (row, column, cellValue) => {
  if (!cellValue) return '';
  return cellValue.replace('T', ' ').substring(0, 19);
};

onMounted(() => {
  fetchTeachers();
});

</script>

<style scoped>
.teacher-management-container {
  padding: 15px;
  background-color: #f4f6f8;
  min-height: calc(100vh - 50px);
}

.action-bar {
  margin-bottom: 15px;
  display: flex;
  justify-content: flex-start;
  background-color: #fff;
  padding: 10px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.pagination-container {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
  background-color: #fff;
  padding: 10px;
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
.form-item-help {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  display: block;
  margin-top: 2px;
}
</style>