<template>
  <div class="automated-scheduling-container">
    <el-card shadow="never">
      <el-form inline>
        <el-form-item label="选择学期进行排课">
          <el-select v-model="selectedSemesterId" placeholder="请选择学期" style="width: 240px;">
            <el-option v-for="semester in semesterList" :key="semester.id" :label="`${semester.academicYear} ${semester.name}`" :value="semester.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button
              type="primary"
              @click="triggerScheduling"
              :loading="isScheduling"
              :disabled="!selectedSemesterId || isScheduling"
          >
            <el-icon style="vertical-align: middle; margin-right: 5px;"><Cpu /></el-icon>
            开始自动排课
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 15px;" v-if="schedulingAttempted">
      <template #header>
        <div>排课结果 - {{ currentSemesterDisplay }}</div>
      </template>
      <div v-if="isScheduling" v-loading="isScheduling" element-loading-text="智能排课引擎正在高速运转中，请稍候..." style="min-height: 100px;"></div>

      <div v-if="!isScheduling && scheduleResult">
        <el-alert
            :title="scheduleResult.success ? '排课成功！' : '排课失败或部分成功'"
            :type="scheduleResult.success ? 'success' : 'error'"
            :description="scheduleResult.message || (scheduleResult.success ? '所有教学任务已成功安排。' : '部分教学任务未能安排，或存在冲突。')"
            show-icon
            :closable="false"
            style="margin-bottom: 15px;"
        />

        <div v-if="scheduleResult.conflictMessages && scheduleResult.conflictMessages.length > 0" style="margin-bottom:15px;">
          <h4>冲突或未安排信息：</h4>
          <el-alert type="warning" :closable="false" v-for="(msg, index) in scheduleResult.conflictMessages" :key="`conflict-${index}`" style="margin-bottom: 5px;">
            {{ msg }}
          </el-alert>
        </div>

        <div v-if="scheduleResult.scheduledUnits && scheduleResult.scheduledUnits.length > 0">
          <h4>已安排课程单元 (按班级分组 - 简化展示):</h4>
          <div v-for="(groupUnits, groupName) in groupedScheduledUnits" :key="groupName" style="margin-bottom: 20px;">
            <el-divider content-position="left"><strong>班级/教学对象: {{ groupName }}</strong></el-divider>
            <el-table :data="groupUnits" border stripe size="small">
              <el-table-column prop="courseName" label="课程名称" />
              <el-table-column prop="teacherName" label="授课教师" />
              <el-table-column prop="dayOfWeek" label="星期几" :formatter="formatDayOfWeek" width="100"/>
              <el-table-column prop="startPeriod" label="开始节次" width="100" align="center"/>
              <el-table-column prop="endPeriod" label="结束节次" width="100" align="center"/>
              <el-table-column prop="roomNumber" label="教室" />
            </el-table>
          </div>
        </div>
        <el-empty v-else-if="scheduleResult.success" description="虽然排课成功，但没有需要安排的教学任务或没有课程被排上。"></el-empty>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElNotification } from 'element-plus'; // 使用 Notification
import { Cpu } from '@element-plus/icons-vue'; // 图标
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

const semesterList = ref([]);
const selectedSemesterId = ref(null);
const isScheduling = ref(false);
const schedulingAttempted = ref(false); // 是否已尝试过排课
const scheduleResult = ref(null); // 存储后端返回的 ScheduleSolutionDto

// 计算属性
const currentSemesterDisplay = computed(() => {
  if (!selectedSemesterId.value) return '未选择学期';
  const semester = semesterList.value.find(s => s.id === selectedSemesterId.value);
  return semester ? `${semester.academicYear} ${semester.name}` : '未知学期';
});

const groupedScheduledUnits = computed(() => {
  if (!scheduleResult.value || !scheduleResult.value.scheduledUnits) {
    return {};
  }
  return scheduleResult.value.scheduledUnits.reduce((acc, unit) => {
    const group = unit.targetGroup || '未知班级';
    if (!acc[group]) {
      acc[group] = [];
    }
    acc[group].push(unit);
    // 可以按星期和节次排序
    acc[group].sort((a, b) => {
      if (a.dayOfWeek !== b.dayOfWeek) return a.dayOfWeek - b.dayOfWeek;
      return a.startPeriod - b.startPeriod;
    });
    return acc;
  }, {});
});

// 方法
const fetchSemesters = async () => {
  try {
    const response = await axios.get(`${API_BASE_URL}/semesters/list`);
    semesterList.value = response.data;
    const current = semesterList.value.find(s => s.isCurrent);
    if (current) {
      selectedSemesterId.value = current.id;
    } else if (semesterList.value.length > 0) {
      selectedSemesterId.value = semesterList.value[0].id;
    }
  } catch (error) {
    ElMessage.error('获取学期列表失败！');
    console.error("Fetch semesters error:", error);
  }
};

const triggerScheduling = async () => {
  if (!selectedSemesterId.value) {
    ElMessage.warning('请先选择一个学期！');
    return;
  }
  isScheduling.value = true;
  schedulingAttempted.value = true;
  scheduleResult.value = null; // 清空上次结果

  ElNotification({
    title: '排课任务已启动',
    message: `正在为学期 [${currentSemesterDisplay.value}] 进行智能排课，请耐心等待...`,
    type: 'info',
    duration: 3000, // 3秒后自动关闭，或者根据排课时长调整
  });

  try {
    const response = await axios.post(`${API_BASE_URL}/scheduling/generate`, {
      semesterId: selectedSemesterId.value
    });
    scheduleResult.value = response.data;
    if (response.data.success) {
      ElNotification({
        title: '排课成功',
        message: response.data.message || '课表已成功生成！',
        type: 'success',
        duration: 4500,
      });
    } else {
      ElNotification({
        title: '排课提示',
        message: response.data.message || '排课未完全成功，请查看详情。',
        type: 'warning',
        duration: 4500,
      });
    }
  } catch (error) {
    console.error("Trigger scheduling error:", error);
    let errMsg = '排课过程中发生未知错误！';
    if (error.response && error.response.data && error.response.data.message) {
      errMsg = `排课失败: ${error.response.data.message}`;
    }
    scheduleResult.value = { success: false, message: errMsg, scheduledUnits: [], conflictMessages: [errMsg] };
    ElNotification({
      title: '排课出错',
      message: errMsg,
      type: 'error',
      duration: 4500,
    });
  } finally {
    isScheduling.value = false;
  }
};

const formatDayOfWeek = (row, column, cellValue) => {
  const days = ["", "周一", "周二", "周三", "周四", "周五", "周六", "周日"]; // 更简洁的中文
  return days[cellValue] || '未知';
};

onMounted(() => {
  fetchSemesters();
});
</script>

<style scoped>
.automated-scheduling-container {
  padding: 20px;
}
.el-card {
  margin-bottom: 20px;
}
/* 可以添加一些加载状态的样式 */
</style>