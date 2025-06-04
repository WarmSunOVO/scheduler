<template>
  <div class="schedule-display-container">
    <el-card shadow="never">
      <el-form :model="filterParams" inline>
        <el-form-item label="选择学期">
          <el-select v-model="filterParams.semesterId" placeholder="请选择学期" style="width: 240px;" @change="handleFilterChange">
            <el-option v-for="semester in semesterList" :key="semester.id" :label="`${semester.academicYear} ${semester.name}`" :value="semester.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="教学对象/班级">
          <el-input
              v-model="filterParams.targetGroup"
              placeholder="输入教学对象/班级名称"
              clearable
              style="width: 220px;"
              @input="handleFilterChange"
              @keyup.enter="fetchOrGenerateSchedule"
          />
          <!-- TODO: 后续可改为班级选择器 -->
        </el-form-item>
        <el-form-item>
          <el-button
              type="primary"
              :icon="SearchIcon"
              @click="fetchOrGenerateSchedule"
              :disabled="!filterParams.semesterId || !filterParams.targetGroup || isLoading"
              :loading="isLoading"
          >
            {{ viewMode === 'generate' ? '生成并查看课表' : '查询课表' }}
          </el-button>
          <!-- <el-switch v-model="viewMode" active-value="generate" inactive-value="query" active-text="实时生成" inactive-text="查询已存" style="margin-left:10px;"/> -->
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 15px;" v-if="scheduleDataLoaded">
      <template #header>
        <div>
          课表: {{ currentSemesterDisplay }} - {{ filterParams.targetGroup }}
          <span v-if="lastScheduleStatus" :style="{color: lastScheduleStatus.success ? 'green' : 'red', marginLeft: '20px'}">
            (上次生成状态: {{ lastScheduleStatus.success ? '成功' : '失败' }} - {{ lastScheduleStatus.message }})
          </span>
        </div>
      </template>

      <div v-if="isLoading" v-loading="isLoading" element-loading-text="正在加载课表数据..." style="min-height: 200px;"></div>

      <div v-else-if="processedSchedule.length > 0">
        <el-table :data="processedSchedule" border :span-method="objectSpanMethod" style="width: 100%">
          <el-table-column prop="period" label="节次" width="80" align="center">
            <template #default="scope">
              <div>第 {{ scope.row.period }} 节</div>
            </template>
          </el-table-column>
          <el-table-column v-for="(day, dayIndex) in daysOfWeek" :key="day.value" :label="day.label" :prop="`day${day.value}`" align="center">
            <template #default="scope">
              <div v-if="scope.row[`day${day.value}`] && scope.row[`day${day.value}`].courseName" class="schedule-cell">
                <div class="course-name"><strong>{{ scope.row[`day${day.value}`].courseName }}</strong></div>
                <div class="teacher-name">{{ scope.row[`day${day.value}`].teacherName }}</div>
                <div class="room-number">{{ scope.row[`day${day.value}`].roomNumber }}</div>
              </div>
              <div v-else class="empty-cell">-</div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-empty v-else description="暂无课表数据，请尝试生成或检查筛选条件。"></el-empty>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { ElMessage, ElNotification } from 'element-plus';
import { Search as SearchIcon } from '@element-plus/icons-vue';
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

const semesterList = ref([]);
const filterParams = reactive({
  semesterId: null,
  targetGroup: '', // 用户输入的班级名称
});

const isLoading = ref(false);
const scheduleDataLoaded = ref(false); // 是否已尝试加载数据
const rawScheduledUnits = ref([]); // 从后端获取的原始排课单元
const lastScheduleStatus = ref(null); // 存储上次生成课表的状态

// const viewMode = ref('generate'); // 'generate' 或 'query' (查询已保存的课表) - 本示例简化为总是生成

const daysOfWeek = ref([
  { label: '星期一', value: 1 }, { label: '星期二', value: 2 }, { label: '星期三', value: 3 },
  { label: '星期四', value: 4 }, { label: '星期五', value: 5 },
  // { label: '星期六', value: 6 }, { label: '星期日', value: 7 } // 根据需要添加
]);

const currentSemesterConfig = computed(() => {
  if (!filterParams.semesterId) return null;
  return semesterList.value.find(s => s.id === filterParams.semesterId);
});

const currentSemesterDisplay = computed(() => {
  const s = currentSemesterConfig.value;
  return s ? `${s.academicYear} ${s.name}` : '未选择学期';
});

const maxPeriods = computed(() => currentSemesterConfig.value?.periodsPerDay || 10); // 默认10节

// 将扁平的 scheduledUnits 转换为表格需要的二维结构
const processedSchedule = computed(() => {
  if (!rawScheduledUnits.value || rawScheduledUnits.value.length === 0 || !currentSemesterConfig.value) {
    return [];
  }
  const scheduleTable = [];
  const periods = maxPeriods.value;

  for (let i = 1; i <= periods; i++) {
    const row = { period: i };
    daysOfWeek.value.forEach(day => {
      row[`day${day.value}`] = null; // 初始化为空单元格
    });
    scheduleTable.push(row);
  }

  rawScheduledUnits.value.forEach(unit => {
    if (unit.targetGroup === filterParams.targetGroup) { // 只显示当前筛选班级的课
      for (let p = unit.startPeriod; p <= unit.endPeriod; p++) {
        if (p >= 1 && p <= periods) { // 确保节次在范围内
          const rowIndex = p - 1;
          // 简单处理，如果一个单元格已有课，则不覆盖（实际可能需要更复杂逻辑处理冲突或多课显示）
          if (!scheduleTable[rowIndex][`day${unit.dayOfWeek}`]) {
            scheduleTable[rowIndex][`day${unit.dayOfWeek}`] = {
              ...unit,
              // 计算 rowspan，用于合并单元格
              // 注意：el-table 的 span-method 更适合这种场景
              // 这里先简单标记，由 span-method 处理
              isStartOfUnit: p === unit.startPeriod,
              span: (unit.endPeriod - unit.startPeriod + 1)
            };
          } else if (p === unit.startPeriod) { // 如果是单元的开始，但格子已被占，记录一个简化信息
            scheduleTable[rowIndex][`day${unit.dayOfWeek}`].conflict = true; // 标记冲突
            scheduleTable[rowIndex][`day${unit.dayOfWeek}`].courseName += ` / ${unit.courseName.substring(0,5)}..`; // 简单显示
          }
        }
      }
    }
  });
  return scheduleTable;
});

// Element Plus Table 合并单元格方法
const objectSpanMethod = ({ row, column, rowIndex, columnIndex }) => {
  // columnIndex 0 是节次列，不需要合并
  if (columnIndex === 0) {
    return { rowspan: 1, colspan: 1 };
  }
  // 获取当前单元格对应的星期 (dayOfWeek)
  const dayValue = daysOfWeek.value[columnIndex - 1]?.value; // columnIndex 从1开始代表星期一
  if (!dayValue) return { rowspan: 1, colspan: 1 };

  const cellData = row[`day${dayValue}`];

  if (cellData && cellData.isStartOfUnit) { // 如果是课程单元的开始节
    return {
      rowspan: cellData.span, // 跨越的行数等于课程的连堂节数
      colspan: 1,
    };
  } else if (cellData && !cellData.isStartOfUnit) { // 如果是课程单元的中间或结束节
    return {
      rowspan: 0, // 不显示此单元格 (已被合并)
      colspan: 0,
    };
  }
  // 其他情况，正常显示单元格
  return { rowspan: 1, colspan: 1 };
};


const fetchSemesters = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/semesters/list`);
    semesterList.value = response.data;
    const current = semesterList.value.find(s => s.isCurrent);
    if (current && !filterParams.semesterId) {
      filterParams.semesterId = current.id;
    } else if (semesterList.value.length > 0 && !filterParams.semesterId) {
      filterParams.semesterId = semesterList.value[0].id;
    }
  } catch (error) {
    ElMessage.error('获取学期列表失败！');
  }
};

const fetchOrGenerateSchedule = async () => {
  if (!filterParams.semesterId || !filterParams.targetGroup) {
    ElMessage.warning('请先选择学期并输入教学对象/班级！');
    return;
  }
  isLoading.value = true;
  scheduleDataLoaded.value = false; // 开始加载时，标记数据未就绪
  rawScheduledUnits.value = []; // 清空旧数据
  lastScheduleStatus.value = null;

  try {
    // 目前总是调用生成API，将来可以根据 viewMode 区分是调用生成还是查询已存课表
    ElNotification({ title: '提示', message: '正在请求后端生成最新课表...', type: 'info', duration: 2000 });
    const response = await axios.post(`${API_BASE_URL}/scheduling/generate`, {
      semesterId: filterParams.semesterId
    });
    lastScheduleStatus.value = { success: response.data.success, message: response.data.message };
    if (response.data.success && response.data.scheduledUnits) {
      rawScheduledUnits.value = response.data.scheduledUnits;
      ElMessage.success(response.data.message || '课表已加载！');
    } else {
      ElMessage.error(response.data.message || '未能成功加载课表。');
      if(response.data.conflictMessages && response.data.conflictMessages.length > 0){
        response.data.conflictMessages.forEach(msg => ElMessage.warning({message: msg, duration: 5000, showClose: true}));
      }
    }
  } catch (error) {
    console.error("Fetch/Generate schedule error:", error);
    let errMsg = '加载或生成课表时发生错误！';
    if (error.response && error.response.data && error.response.data.message) {
      errMsg = `错误: ${error.response.data.message}`;
    }
    ElMessage.error(errMsg);
    lastScheduleStatus.value = { success: false, message: errMsg };
  } finally {
    isLoading.value = false;
    scheduleDataLoaded.value = true; // 无论成功失败，都标记已尝试加载
  }
};

const handleFilterChange = () => {
  scheduleDataLoaded.value = false; // 筛选条件变化，结果变为未加载
  rawScheduledUnits.value = [];
  lastScheduleStatus.value = null;
};

const formatDayOfWeek = (row, column, cellValue) => {
  const dayMap = { 1: "周一", 2: "周二", 3: "周三", 4: "周四", 5: "周五", 6: "周六", 7: "周日" };
  // cellValue 在这里指的是 prop 的值，对于星期列，prop 是 "dayX"，其值是对象或null
  // 这个 formatter 是用于 el-table-column 的，但我们直接在 template 里处理了显示
  // 如果是想格式化某个具体值，则需要看 cellValue 是什么
  // 对于表头，我们直接用 daysOfWeek.label
  // 对于单元格内容，我们已经从 unit 中取 dayOfWeek 数字了
  return dayMap[cellValue] || cellValue || '-'; // 这个 formatter 可能不会被直接用到
};


onMounted(() => {
  fetchSemesters();
});

</script>

<style scoped>
.schedule-display-container {
  padding: 20px;
}
.el-card {
  margin-bottom: 20px;
}
.schedule-cell {
  padding: 8px;
  line-height: 1.4;
  min-height: 60px; /* 给单元格一个最小高度 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center; /* 确保内容居中 */
}
.course-name {
  font-weight: bold;
  margin-bottom: 4px;
}
.teacher-name, .room-number {
  font-size: 0.9em;
  color: #606266;
}
.empty-cell {
  color: #909399;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60px;
}
/* 确保表格文字能正确显示和换行 */
:deep(.el-table .cell) {
  word-break: break-word; /* 或者 break-all */
  white-space: normal; /* 允许自动换行 */
}
</style>