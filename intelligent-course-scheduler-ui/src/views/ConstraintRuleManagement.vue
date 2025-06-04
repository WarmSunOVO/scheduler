<template>
  <div class="constraint-rule-management-container">
    <el-card shadow="never">
      <el-form :model="filterParams" inline>
        <el-form-item label="规则名称">
          <el-input v-model="filterParams.name" placeholder="输入规则名称搜索" clearable />
        </el-form-item>
        <el-form-item label="规则编码">
          <el-input v-model="filterParams.constraintCode" placeholder="输入规则编码搜索" clearable />
        </el-form-item>
        <el-form-item label="约束类型">
          <el-select v-model="filterParams.constraintType" placeholder="选择约束类型" clearable style="width: 120px;">
            <el-option label="硬约束" value="HARD" />
            <el-option label="软约束" value="SOFT" />
          </el-select>
        </el-form-item>
        <el-form-item label="适用学期">
          <el-select v-model="filterParams.semesterId" placeholder="选择学期" clearable style="width: 200px;">
            <el-option label="全局约束" value="null"></el-option>
            <el-option v-for="semester in semesterList" :key="semester.id" :label="`${semester.academicYear} ${semester.name}`" :value="semester.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="SearchIcon" @click="fetchData(1)">查询</el-button>
          <el-button :icon="Plus" type="success" @click="handleOpenDialog(null)" v-if="authStore.hasRole('ROLE_ADMIN')">新增规则</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 15px;">
      <el-table :data="ruleList" v-loading="loadingData" border stripe>
        <!-- 表格列定义 (与您之前的一致) -->
        <el-table-column prop="name" label="规则名称" width="200" show-overflow-tooltip />
        <el-table-column prop="constraintCode" label="规则编码" width="200" show-overflow-tooltip />
        <el-table-column prop="constraintType" label="约束类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.constraintType === 'HARD' ? 'danger' : 'warning'">
              {{ scope.row.constraintType === 'HARD' ? '硬约束' : '软约束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetEntityType" label="目标实体类型" width="150" :formatter="formatTargetEntityType" />
        <el-table-column prop="targetEntityId" label="目标实体ID" width="120" />
        <el-table-column prop="semesterDisplay" label="适用学期" width="180" />
        <el-table-column prop="isActive" label="是否激活" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isActive ? 'success' : 'info'">{{ scope.row.isActive ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="penaltyWeight" label="惩罚权重" width="100" align="center" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" :icon="ViewIcon" @click="handleOpenDialog(scope.row, true)" circle title="查看"></el-button>
            <el-button size="small" :icon="Edit" @click="handleOpenDialog(scope.row)" circle title="编辑" v-if="authStore.hasRole('ROLE_ADMIN')"></el-button>
            <el-button size="small" type="danger" :icon="Delete" @click="handleDelete(scope.row.id)" circle title="删除" v-if="authStore.hasRole('ROLE_ADMIN')"></el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
          style="margin-top: 15px; justify-content: flex-end;"
          v-if="totalItems > 0"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalItems"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 新增/编辑/查看 对话框 (与您之前的一致) -->
    <el-dialog :model-value="dialogVisible" :title="dialogTitle" width="750px" @close="handleCloseDialog" draggable :close-on-click-modal="false">
      <!-- ... (您原有的对话框表单内容) ... -->
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" status-icon :disabled="isViewMode">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="规则名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入规则名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规则编码" prop="constraintCode">
              <el-select v-model="form.constraintCode" filterable placeholder="选择或输入规则编码" style="width:100%;" @change="handleConstraintCodeChange" :allow-create="authStore.hasRole('ROLE_ADMIN')">
                <el-option v-for="item in PREDEFINED_CONSTRAINT_CODES" :key="item.value" :label="`${item.label} (${item.value})`" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="约束类型" prop="constraintType">
              <el-select v-model="form.constraintType" placeholder="选择约束类型" style="width:100%;">
                <el-option label="硬约束 (HARD)" value="HARD" />
                <el-option label="软约束 (SOFT)" value="SOFT" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目标实体类型" prop="targetEntityType">
              <el-select v-model="form.targetEntityType" placeholder="选择目标实体类型" style="width:100%;" @change="handleTargetEntityTypeChange">
                <el-option label="全局 (GLOBAL)" value="GLOBAL" />
                <el-option label="教师 (TEACHER)" value="TEACHER" />
                <el-option label="班级/教学对象 (CLASS_GROUP)" value="CLASS_GROUP" />
                <el-option label="课程 (COURSE)" value="COURSE" />
                <el-option label="教室 (ROOM)" value="ROOM" />
                <el-option label="教学任务 (TEACHING_TASK)" value="TEACHING_TASK" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="目标实体ID" prop="targetEntityId" v-if="form.targetEntityType !== 'GLOBAL'">
              <el-input-number v-model="form.targetEntityId" :min="1" placeholder="输入目标实体ID" style="width:100%;" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="适用学期" prop="semesterId" v-if="form.targetEntityType !== 'GLOBAL'">
              <el-select v-model="form.semesterId" placeholder="选择适用学期 (可为空)" clearable style="width:100%;">
                <el-option v-for="semester in semesterList" :key="semester.id" :label="`${semester.academicYear} ${semester.name}`" :value="semester.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="参数 (JSON)" prop="parametersJsonString">
          <el-input v-model="form.parametersJsonString" type="textarea" :rows="4" placeholder='请输入JSON格式的参数, 例如: {"dayOfWeek": 1, "preferenceScore": -2}' />
          <small>提示: 确保输入合法的JSON格式。后续版本将提供更友好的参数输入方式。</small>
        </el-form-item>
        <el-form-item label="惩罚权重" prop="penaltyWeight" v-if="form.constraintType === 'SOFT'">
          <el-input-number v-model="form.penaltyWeight" :min="0" placeholder="输入软约束的惩罚权重" style="width:100%;" controls-position="right"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="请输入规则描述 (可选)" />
        </el-form-item>
        <el-form-item label="是否激活" prop="isActive">
          <el-switch v-model="form.isActive" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCloseDialog">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="formSubmitting" v-if="!isViewMode">提交</el-button>
      </template>
    </el-dialog>

    <!-- VVVVVVVV 新增 AI 辅助输入部分 VVVVVVVV -->
    <el-card shadow="never" style="margin-top: 20px;">
      <template #header>
        <div class="clearfix">
          <span>AI 辅助输入约束 (实验性功能)</span>
        </div>
      </template>
      <el-input
          v-model="naturalLanguageInput"
          type="textarea"
          :rows="3"
          placeholder="请用自然语言描述您的排课偏好或约束，例如：李老师周三下午不想上课，计算机基础这门课需要多媒体教室..."
      />
      <el-button type="primary" @click="submitToAI" :loading="aiLoading" style="margin-top: 10px;">
        <el-icon style="margin-right: 5px;"><ChatDotRound /></el-icon>
        获取AI建议
      </el-button>
      <div v-if="aiSuggestion" style="margin-top: 15px; padding: 10px; border: 1px solid #dcdfe6; border-radius: 4px; background-color: #f9f9f9;">
        <p><strong>您的输入:</strong></p>
        <p style="white-space: pre-wrap;">{{ aiSuggestion.originalText }}</p>
        <el-divider />
        <p><strong>AI 理解:</strong></p>
        <p style="white-space: pre-wrap;">{{ aiSuggestion.interpretation }}</p>
        <div v-if="aiSuggestion.structuredSuggestion && Object.keys(aiSuggestion.structuredSuggestion).length > 0">
          <p><strong>AI解析出的结构化信息 (参考):</strong></p>
          <pre style="background-color: #282c34; color: #abb2bf; padding: 10px; border-radius: 4px; overflow-x: auto;">{{ JSON.stringify(aiSuggestion.structuredSuggestion, null, 2) }}</pre>
          <small>提示：您可以参考以上结构化信息，在上方“新增规则”或编辑规则时填写“规则编码”和“参数(JSON)”。</small>
        </div>
        <p v-if="aiSuggestion.humanReadableSuggestion" style="margin-top: 10px;"><strong>AI 操作提示:</strong> {{ aiSuggestion.humanReadableSuggestion }}</p>
      </div>
    </el-card>
    <!-- ^^^^^^^^^^ 新增 AI 辅助输入部分 ^^^^^^^^^^ -->

  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
// 更新图标导入，添加 ChatDotRound
import { Plus, Edit, Delete, Search as SearchIcon, View as ViewIcon, ChatDotRound } from '@element-plus/icons-vue';
import axios from 'axios';
import { useAuthStore } from '@/stores/authStore';

const PREDEFINED_CONSTRAINT_CODES = [
  { value: 'TEACHER_TIME_PREFERENCE', label: '教师时间偏好', description: '设置教师对特定时间段的偏好程度 (软约束)', defaultConstraintType: 'SOFT', defaultTargetEntityType: 'TEACHER', parameterFields: [ { name: 'dayOfWeek', label: '星期几', type: 'select', options: [{value:1, label:'周一'},{value:2, label:'周二'},{value:3, label:'周三'},{value:4, label:'周四'},{value:5, label:'周五'},{value:6, label:'周六'},{value:7, label:'周日'}], required: true }, { name: 'startPeriod', label: '开始节次', type: 'number', min: 1, required: true }, { name: 'endPeriod', label: '结束节次', type: 'number', min: 1, required: true }, { name: 'preferenceScore', label: '偏好分值', type: 'number', placeholder: '-2 到 2', required: true }]},
  { value: 'COURSE_FIXED_SCHEDULE', label: '课程固定排课', description: '将特定课程固定在某个时间段和教室 (硬约束)', defaultConstraintType: 'HARD', defaultTargetEntityType: 'COURSE', parameterFields: [ { name: 'dayOfWeek', label: '星期几', type: 'select', options: [{value:1, label:'周一'},{value:2, label:'周二'},{value:3, label:'周三'},{value:4, label:'周四'},{value:5, label:'周五'},{value:6, label:'周六'},{value:7, label:'周日'}], required: true }, { name: 'startPeriod', label: '开始节次', type: 'number', min: 1, required: true }, { name: 'endPeriod', label: '结束节次', type: 'number', min: 1, required: true }, { name: 'roomId', label: '教室ID (可选)', type: 'number', required: false }]},
  { value: 'ROOM_TYPE_FOR_COURSE', label: '课程教室类型要求', description: '指定某门课程必须使用特定类型的教室 (硬约束)', defaultConstraintType: 'HARD', defaultTargetEntityType: 'COURSE', parameterFields: [ { name: 'requiredRoomType', label: '要求教室类型', type: 'text', placeholder: '例如: 多媒体教室', required: true }]},
  { value: 'AVOID_CLASS_CONSECUTIVE_EVENTS', label: '班级避免连续活动', description: '一个班级一天内最多连续上多少节课 (软约束)', defaultConstraintType: 'SOFT', defaultTargetEntityType: 'CLASS_GROUP', parameterFields: [ { name: 'maxConsecutivePeriods', label: '最大连续节次数', type: 'number', min: 1, required: true }]},
  { value: 'GLOBAL_NO_FRIDAY_AFTERNOON', label: '全局周五下午无课', description: '全局设置周五下午不安排任何课程 (硬约束)', defaultConstraintType: 'HARD', defaultTargetEntityType: 'GLOBAL', parameterFields: [{ name: 'afternoonStartPeriod', label: '下午开始节次', type: 'number', min: 1, required: true, defaultValue: 5 }]},
  { value: 'GLOBAL_NO_WEDNESDAY', label: '全局周三无课', description: '全局设置周三不安排任何课程 (硬约束)', defaultConstraintType: 'HARD', defaultTargetEntityType: 'GLOBAL', parameterFields: []},
  { value: 'TEACHER_MAX_DAILY_PERIODS', // 规则编码，需要与后端 AutoSchedulingServiceImpl 中 evaluateGenericHardConstraint 的 case 匹配
    label: '教师每日最大课时',
    description: '限制一个教师一天内最多可以上的总节数 (硬约束)',
    defaultConstraintType: 'HARD',
    defaultTargetEntityType: 'TEACHER', // 这个约束通常是针对特定教师的
    parameterFields: [
      { name: 'maxPeriods', label: '每日最大节数', type: 'number', min: 1, required: true, placeholder: '例如: 4' }
    ]
  }
];

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';
const authStore = useAuthStore();

// --- 您已有的筛选、列表、分页、对话框相关 ref 和 reactive 定义 (保持不变) ---
const filterParams = reactive({ name: '', constraintCode: '', constraintType: null, semesterId: null });
const semesterList = ref([]);
const ruleList = ref([]);
const loadingData = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalItems = ref(0);
const dialogVisible = ref(false);
const dialogMode = ref('add');
const isViewMode = computed(() => dialogMode.value === 'view');
const dialogTitle = ref('新增约束规则');
const initialForm = { id: null, name: '', constraintCode: '', constraintType: 'HARD', targetEntityType: 'GLOBAL', targetEntityId: null, semesterId: null, parametersJsonString: '{}', penaltyWeight: 0, description: '', isActive: true };
const form = reactive({ ...initialForm });
const formRef = ref(null);
const formSubmitting = ref(false);
const rules = reactive({ /* ... 您已有的 rules ... */
  name: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }],
  constraintCode: [{ required: true, message: '规则编码不能为空', trigger: ['blur', 'change'] }],
  constraintType: [{ required: true, message: '约束类型不能为空', trigger: 'change' }],
  targetEntityType: [{ required: true, message: '目标实体类型不能为空', trigger: 'change' }],
  targetEntityId: [ { validator: (rule, value, callback) => { if (form.targetEntityType !== 'GLOBAL' && (value === null || value === undefined || value === '')) { callback(new Error('非全局约束的目标实体ID不能为空')); } else { callback(); } }, trigger: 'blur' } ],
  parametersJsonString: [ { validator: (rule, value, callback) => { try { JSON.parse(value); callback(); } catch (e) { callback(new Error('参数必须是合法的JSON格式')); } }, trigger: 'blur' } ],
  penaltyWeight: [ { validator: (rule, value, callback) => { if (form.constraintType === 'SOFT' && (value === null || value === undefined || value < 0)) { callback(new Error('软约束的惩罚权重不能为空且大于等于0')); } else { callback(); } }, trigger: 'blur'} ]
});


// --- VVVVVVVV 新增 AI 相关 ref 和方法 VVVVVVVV ---
const naturalLanguageInput = ref('');
const aiLoading = ref(false);
const aiSuggestion = ref(null); // 用于存储 AiSuggestionResponseDto

const submitToAI = async () => {
  if (!naturalLanguageInput.value.trim()) {
    ElMessage.warning('请输入您的描述后再提交给AI。');
    return;
  }
  aiLoading.value = true;
  aiSuggestion.value = null; // 清空上次建议
  try {
    const response = await axios.post(`${API_BASE_URL}/ai/process-text`, {
      inputText: naturalLanguageInput.value
    });
    aiSuggestion.value = response.data;
    ElMessage.success('AI建议已获取！请参考下方信息。');
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '调用AI服务失败！');
    console.error("AI process error:", error);
    aiSuggestion.value = {
      originalText: naturalLanguageInput.value,
      interpretation: "调用AI服务时出错，请检查后端日志或网络连接。",
      suggestionType: "ERROR",
      structuredSuggestion: null,
      humanReadableSuggestion: "无法从AI获取有效建议。"
    };
  } finally {
    aiLoading.value = false;
  }
};
// --- ^^^^^^^^ 新增 AI 相关 ref 和方法 ^^^^^^^^ ---


// --- 您已有的方法 (fetchSemesters, fetchData, handleSizeChange, etc.) 保持不变 ---
const fetchSemesters = async () => { /* ... */ try { const response = await axios.get(`${API_BASE_URL}/semesters/list`); semesterList.value = response.data; } catch (error) { ElMessage.error('获取学期列表失败！'); }};
const fetchData = async (page = currentPage.value, size = pageSize.value) => { /* ... */ loadingData.value = true; const params = { page: page - 1, size: size, sort: 'id,desc', ...filterParams, }; if (params.semesterId === 'null') { params.semesterId = null; } Object.keys(params).forEach(key => { if (params[key] === null || params[key] === '') { delete params[key]; } }); try { const response = await axios.get(`${API_BASE_URL}/constraint-rules`, { params }); ruleList.value = response.data.content; totalItems.value = response.data.totalElements; currentPage.value = response.data.number + 1; } catch (error) { ElMessage.error('获取约束规则列表失败！'); console.error("Fetch constraint rules error:", error); ruleList.value = []; totalItems.value = 0; } finally { loadingData.value = false; } };
const handleSizeChange = (val) => { pageSize.value = val; fetchData(1, val); };
const handleCurrentChange = (val) => { currentPage.value = val; fetchData(val, pageSize.value); };
const formatTargetEntityType = (row, column, cellValue) => { const map = { GLOBAL: '全局', TEACHER: '教师', CLASS_GROUP: '班级/教学对象', COURSE: '课程', ROOM: '教室', TEACHING_TASK: '教学任务' }; return map[cellValue] || cellValue; };
const handleConstraintCodeChange = (selectedCode) => { const selectedDefinition = PREDEFINED_CONSTRAINT_CODES.find(c => c.value === selectedCode); if (selectedDefinition) { if (dialogMode.value === 'add') { form.name = selectedDefinition.label; form.constraintType = selectedDefinition.defaultConstraintType || 'HARD'; form.targetEntityType = selectedDefinition.defaultTargetEntityType || 'GLOBAL'; if (selectedDefinition.parameterFields && selectedDefinition.parameterFields.length > 0) { const paramsTemplate = {}; selectedDefinition.parameterFields.forEach(field => { paramsTemplate[field.name] = field.defaultValue !== undefined ? field.defaultValue : `_REPLACE_WITH_${field.type.toUpperCase()}_VALUE_`; }); form.parametersJsonString = JSON.stringify(paramsTemplate, null, 2); } else { form.parametersJsonString = '{}'; } } } };
const handleTargetEntityTypeChange = () => { if(form.targetEntityType === 'GLOBAL') { form.targetEntityId = null; form.semesterId = null; }};
const handleOpenDialog = (rowData = null, viewMode = false) => { dialogMode.value = viewMode ? 'view' : (rowData ? 'edit' : 'add'); if (rowData) { dialogTitle.value = viewMode ? `查看规则: ${rowData.name}` : `编辑规则: ${rowData.name}`; Object.assign(form, JSON.parse(JSON.stringify(rowData))); form.parametersJsonString = JSON.stringify(rowData.parametersJson || {}, null, 2); } else { dialogTitle.value = '新增约束规则'; Object.assign(form, JSON.parse(JSON.stringify(initialForm))); } dialogVisible.value = true; nextTick(() => { formRef.value?.clearValidate(); }); };
const handleCloseDialog = () => { dialogVisible.value = false; Object.assign(form, JSON.parse(JSON.stringify(initialForm))); };
const handleSubmit = async () => { if (!formRef.value) return; await formRef.value.validate(async (valid) => { if (valid) { formSubmitting.value = true; let paramsJsonObj; try { paramsJsonObj = JSON.parse(form.parametersJsonString); } catch (e) { ElMessage.error('参数JSON格式不正确！'); formSubmitting.value = false; return; } const payload = { ...form, parametersJson: paramsJsonObj }; delete payload.parametersJsonString; const currentId = form.id; if(dialogMode.value === 'add') { delete payload.id; } if (payload.targetEntityType === 'GLOBAL') { payload.targetEntityId = null; payload.semesterId = null; } if (payload.constraintType === 'HARD') { delete payload.penaltyWeight; } try { if (dialogMode.value === 'add') { await axios.post(`${API_BASE_URL}/constraint-rules`, payload); ElMessage.success('新增成功！'); } else { await axios.put(`${API_BASE_URL}/constraint-rules/${currentId}`, payload); ElMessage.success('更新成功！'); } dialogVisible.value = false; await fetchData(); } catch (error) { ElMessage.error(error.response?.data?.message || '操作失败！'); console.error("Submit constraint rule error:", error); } finally { formSubmitting.value = false; } } }); };
const handleDelete = async (id) => { try { await ElMessageBox.confirm('确定要删除这条约束规则吗？', '警告', { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'warning', }); await axios.delete(`${API_BASE_URL}/constraint-rules/${id}`); ElMessage.success('删除成功！'); await fetchData(); } catch (error) { if (error !== 'cancel') { ElMessage.error(error.response?.data?.message || '删除失败！'); console.error("Delete constraint rule error:", error); } } };

onMounted(() => {
  fetchSemesters();
  fetchData();
});

</script>

<style scoped>
/* 您已有的样式保持不变 */
.constraint-rule-management-container { padding: 15px; }
.el-card { margin-bottom: 15px; }
.clearfix:before,
.clearfix:after { display: table; content: ""; }
.clearfix:after { clear: both }
/* 为 AI 建议部分的 pre 标签添加样式 */
pre {
  background-color: #f4f4f5;
  border: 1px solid #e9e9eb;
  padding: 10px;
  border-radius: 4px;
  white-space: pre-wrap;       /* CSS3 */
  white-space: -moz-pre-wrap;  /* Mozilla, since 1999 */
  white-space: -pre-wrap;      /* Opera 4-6 */
  white-space: -o-pre-wrap;    /* Opera 7 */
  word-wrap: break-word;       /* Internet Explorer 5.5+ */
  font-family: monospace;
}
</style>