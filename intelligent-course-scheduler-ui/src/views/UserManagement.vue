<template>
  <div class="user-management-container">
    <el-card class="search-action-card" shadow="never">
      <el-form :model="searchParams" ref="searchFormRef" inline>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="searchParams.username" placeholder="搜索用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-select v-model="searchParams.enabled" placeholder="用户状态" clearable style="width: 120px;">
            <el-option label="启用" :value="true"></el-option>
            <el-option label="禁用" :value="false"></el-option>
          </el-select>
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
            新增用户
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
      <el-table-column prop="username" label="用户名" width="150" sortable="custom" />
      <el-table-column prop="fullName" label="姓名/昵称" width="150" />
      <el-table-column prop="email" label="邮箱" width="200" show-overflow-tooltip />
      <el-table-column prop="roles" label="角色" width="200">
        <template #default="scope">
          <el-tag
              v-for="role in scope.row.roles"
              :key="role"
              type="success"
              effect="light"
              size="small"
              style="margin-right: 5px;"
          >
            {{ formatRoleName(role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="enabled" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.enabled ? 'success' : 'danger'">
            {{ scope.row.enabled ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" :formatter="formatDateTime" sortable="custom" />
      <el-table-column label="操作" width="180" fixed="right" align="center">
        <template #default="scope">
          <el-button
              v-if="authStore.hasRole('ROLE_ADMIN')"
              size="small" :icon="Edit" @click="handleOpenEditDialog(scope.row)" circle title="编辑"></el-button>
          <el-button
              v-if="authStore.hasRole('ROLE_ADMIN')"
              size="small" type="danger" :icon="Delete"
              @click="handleDelete(scope.row.id, scope.row.username)" circle title="删除"
              :disabled="scope.row.username === 'admin' || scope.row.username === authStore.user?.username"
          ></el-button>
          <span v-if="!authStore.hasRole('ROLE_ADMIN') && (scope.row.username !== 'admin' && scope.row.username !== authStore.user?.username)">-</span>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
                     :page-sizes="[10, 20, 50, 100]" layout="total, sizes, prev, pager, next, jumper" :total="pagination.total"
                     @size-change="handleSizeChange" @current-change="handleCurrentChange" />
    </div>

    <el-dialog :model-value="dialogVisible" :title="dialogTitle" width="600px" @close="handleCloseDialog" draggable :close-on-click-modal="false">
      <el-form :model="userForm" :rules="userFormRules" ref="userFormRef" label-width="100px" status-icon>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="3-50个字符" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogMode === 'add'">
          <el-input type="password" v-model="userForm.password" placeholder="6-20位密码" show-password />
        </el-form-item>
        <el-form-item label="姓名/昵称" prop="fullName">
          <el-input v-model="userForm.fullName" placeholder="用户真实姓名或昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input type="email" v-model="userForm.email" placeholder="有效的电子邮箱地址" />
        </el-form-item>
        <el-form-item label="角色" prop="roles">
          <el-select v-model="userForm.roles" multiple placeholder="请选择用户角色" style="width: 100%;">
            <el-option v-for="role in availableRoles" :key="role.id" :label="formatRoleName(role.name)" :value="role.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="userForm.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
        <el-form-item label="账户锁定" prop="locked" v-if="dialogMode === 'edit'">
          <el-switch v-model="userForm.locked" active-text="是" inactive-text="否" />
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

const API_USERS_URL = '/api/users';
const authStore = useAuthStore();

const tableData = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const dialogMode = ref('add');
const dialogTitle = ref('新增用户');
const formSubmitting = ref(false);

const searchParams = reactive({ username: '', enabled: null });
const searchFormRef = ref(null);

const initialUserForm = {
  id: null, username: '', password: '', fullName: '', email: '',
  roles: [], enabled: true, locked: false,
};
const userForm = reactive({ ...initialUserForm });
const userFormRef = ref(null);
const availableRoles = ref([]);

const userFormRules = reactive({
  username: [ { required: true, message: '用户名为必填项', trigger: 'blur' }, { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }, ],
  password: [ { required: true, message: '密码为必填项 (新增时)', trigger: 'blur' }, { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }, ],
  email: [{ type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] }],
  roles: [{ required: true, type: 'array', min: 1, message: '至少选择一个角色', trigger: 'change' }]
});

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0,
  sortField: 'id',
  sortOrder: 'desc' // 'asc' 或 'desc'
});

const fetchUsers = async () => {
  loading.value = true;
  try {
    const params = {
      page: pagination.currentPage - 1,
      size: pagination.pageSize,
      ...(searchParams.username && { username: searchParams.username }),
      ...(searchParams.enabled !== null && { enabled: searchParams.enabled }),
    };

    // 构造单一的 sort 字符串参数
    if (pagination.sortField && pagination.sortOrder) {
      // pagination.sortOrder 已经是 'asc' 或 'desc'
      if (pagination.sortField.trim() !== '') {
        params.sort = `${pagination.sortField},${pagination.sortOrder}`;
      }
    }
    // 如果 pagination.sortField 为空，则不发送 sort 参数，让后端使用其默认排序

    console.log("Fetching users with params:", JSON.stringify(params));
    const response = await axios.get(API_USERS_URL, { params });
    tableData.value = response.data.content;
    pagination.total = response.data.totalElements;
  } catch (error) {
    console.error("获取用户列表失败时的错误详情:", error.response || error);
    ElMessage.error(error.response?.data?.message || '获取用户列表失败！');
  } finally {
    loading.value = false;
  }
};

const fetchAvailableRoles = async () => { try { const response = await axios.get(`${API_USERS_URL}/roles`); availableRoles.value = response.data; } catch (error) { ElMessage.error('获取角色列表失败！'); } };
const handleSearch = () => { pagination.currentPage = 1; fetchUsers(); };
const handleResetSearch = () => { searchFormRef.value?.resetFields(); searchParams.username = ''; searchParams.enabled = null; pagination.sortField = 'id'; pagination.sortOrder = 'desc'; handleSearch(); };
const handleOpenAddDialog = () => { dialogMode.value = 'add'; dialogTitle.value = '新增用户'; Object.assign(userForm, { ...initialUserForm, roles: [], enabled: true }); userFormRules.password[0].required = true; dialogVisible.value = true; nextTick(() => userFormRef.value?.clearValidate()); };
const handleOpenEditDialog = (row) => { dialogMode.value = 'edit'; dialogTitle.value = '编辑用户'; const formData = JSON.parse(JSON.stringify(row)); formData.roles = row.roles || []; Object.assign(userForm, formData); userFormRules.password[0].required = false; userForm.password = ''; dialogVisible.value = true; nextTick(() => userFormRef.value?.clearValidate()); };
const handleCloseDialog = () => { dialogVisible.value = false; };
const handleSubmitForm = async () => { if (!userFormRef.value) return; const payload = { ...userForm }; if (dialogMode.value === 'edit' && (!payload.password || payload.password.trim() === '')) { delete payload.password; } await userFormRef.value.validate(async (valid) => { if (valid) { formSubmitting.value = true; try { if (dialogMode.value === 'add') { await axios.post(API_USERS_URL, payload); ElMessage.success('用户创建成功！'); } else { await axios.put(`${API_USERS_URL}/${userForm.id}`, payload); ElMessage.success('用户更新成功！'); } dialogVisible.value = false; await fetchUsers(); } catch (error) { ElMessage.error(error.response?.data?.message || '操作失败，请重试！'); } finally { formSubmitting.value = false; } } }); };
const handleDelete = async (id, username) => { if (username === authStore.user?.username) { ElMessage.warning('不能删除当前登录用户！'); return; } if (username === 'admin' ) { ElMessage.warning('admin 用户受保护，无法删除！'); return; } try { await ElMessageBox.confirm(`确定要删除用户 "${username}" 吗？此操作不可撤销。`, '警告', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning', draggable: true, }); loading.value = true; await axios.delete(`${API_USERS_URL}/${id}`); ElMessage.success('用户删除成功！'); if (tableData.value.length === 1 && pagination.currentPage > 1) pagination.currentPage--; await fetchUsers(); } catch (error) { if (error !== 'cancel') { ElMessage.error(error.response?.data?.message || '删除失败，请重试！'); } } finally { loading.value = false; } };
const handleSizeChange = (val) => { pagination.pageSize = val; pagination.currentPage = 1; fetchUsers(); };
const handleCurrentChange = (val) => { pagination.currentPage = val; fetchUsers(); };

const handleSortChange = ({ column, prop, order }) => {
  if (prop) {
    pagination.sortField = prop;
    // Element Plus 的 order 是 'ascending', 'descending', 或 null
    if (order === 'ascending') {
      pagination.sortOrder = 'asc';
    } else if (order === 'descending') {
      pagination.sortOrder = 'desc';
    } else {
      // 如果 order 是 null (取消排序)，恢复默认或不排序
      pagination.sortField = 'id'; // 或者设为 null 如果后端支持不传 sort
      pagination.sortOrder = 'desc';
    }
  } else {
    // 清除所有排序，恢复默认
    pagination.sortField = 'id';
    pagination.sortOrder = 'desc';
  }
  fetchUsers(); // 重新获取数据
};

const formatDateTime = (row, column, cellValue) => cellValue ? cellValue.replace('T', ' ').substring(0, 19) : '';
const formatRoleName = (roleName) => { if (roleName && roleName.startsWith('ROLE_')) { const simplifiedName = roleName.substring(5); if (simplifiedName === 'ADMIN') return '管理员'; if (simplifiedName === 'TEACHER') return '教师'; if (simplifiedName === 'STUDENT') return '学生'; return simplifiedName; } return roleName; };
onMounted(() => { fetchUsers(); fetchAvailableRoles(); });
</script>

<style scoped>
.user-management-container { padding: 15px; background-color: #f4f6f8; min-height: calc(100vh - 50px); }
.search-action-card { margin-bottom: 15px; }
.search-action-card :deep(.el-card__body) { padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; }
.search-form { display: flex; flex-wrap: wrap; gap: 10px; align-items: center; }
.search-form .el-form-item { margin-bottom: 0 !important; }
.search-action-card :deep(.el-form-item[style*="float: right"]) { margin-left: auto; }
.pagination-container { margin-top: 15px; display: flex; justify-content: flex-end; background-color: #fff; padding: 10px 15px; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.el-table { border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.el-dialog .el-select, .el-dialog .el-input-number { width: 100%; }
.el-dialog .el-form-item { margin-bottom: 20px; }
.el-table :deep(.el-table__body-wrapper) { overflow-y: auto; }
</style>