<template>
  <div class="login-page-wrapper">
    <div class="login-container">
      <!-- 左侧内容区域 - 保持你现有的不变 -->
      <div class="left-panel">
        <div class="logo-container">
          <img src="@/assets/logo.png" alt="System Logo" class="logo-image">
          <span class="system-title">智能AI高校排课系统</span>
        </div>
        <div class="tagline">
          <p>"高效排课，智创未来"</p>
        </div>
        <div class="illustration-container">
          <img
              v-if="illustrationExists"
              src="@/assets/education-illustration.svg"
              alt="Education Illustration"
              class="illustration-image"
          >
          <p v-else class="placeholder-illustration">
            <img src="@/assets/education-illustration.svg" alt="Education Illustration" class="illustration-image">
          </p>
        </div>
      </div>

      <!-- 右侧 - 登录表单 (Scolaa 风格调整) -->
      <div class="right-section-scolaa-style">
        <div class="login-card-scolaa-style">
          <div class="card-header-scolaa">
            <el-avatar :size="70" class="avatar-scolaa">
              <!-- 请准备一个头像图片 avatar_default.png 放到 src/assets/ -->
              <img src="@/assets/avatar_default.png" alt="User Avatar" class="avatar-img-scolaa" v-if="avatarExists">
              <el-icon :size="30" v-else><User /></el-icon>
            </el-avatar>
            <h2 class="form-title-scolaa">用户登录</h2>
          </div>

          <el-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              label-position="top"
              class="login-form-content-scolaa"
              @submit.prevent="handleLogin"
          >
            <el-form-item prop="username" class="form-item-scolaa">
              <label for="username-input-scolaa" class="input-label-scolaa">
                <el-icon class="input-icon-scolaa"><User /></el-icon>
                请输入用户名
              </label>
              <el-input
                  id="username-input-scolaa"
                  v-model="loginForm.username"
                  placeholder=""
                  class="input-field-scolaa"
                  size="large"
              />
            </el-form-item>

            <el-form-item prop="password" class="form-item-scolaa">
              <label for="password-input-scolaa" class="input-label-scolaa">
                <el-icon class="input-icon-scolaa"><Lock /></el-icon>
                请输入密码
              </label>
              <el-input
                  id="password-input-scolaa"
                  v-model="loginForm.password"
                  type="password"
                  placeholder=""
                  show-password
                  class="input-field-scolaa"
                  size="large"
                  @keyup.enter="handleLogin"
              />
            </el-form-item>

            <div class="login-options-scolaa">
              <el-checkbox v-model="loginForm.remember" label="记住我" size="small" />
              <el-link type="primary" :underline="false" @click="handleForgotPassword" class="forgot-password-link-scolaa">忘记密码?</el-link>
            </div>

            <el-form-item>
              <el-button type="primary" class="login-button-scolaa" @click="handleLogin" :loading="loading" native-type="submit">
                登 录
              </el-button>
            </el-form-item>

            <div class="signup-link-scolaa">
              新用户? <el-link type="primary" :underline="false" @click="handleRegister">立即注册</el-link>
            </div>
          </el-form>

          <!-- 参考图底部的 "Login as a Tutor" 按钮，如果需要可以取消注释 -->
          <!--
          <div class="alternative-login-scolaa">
             <el-button type="primary" class="tutor-login-button-scolaa">Login as a Tutor</el-button>
          </div>
           -->
        </div>
      </div>
    </div>



  </div>
</template>

<script setup>
// 你的 <script setup> 部分保持不变 (除了可能需要为 avatarExists 添加 ref)
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElCheckbox, ElRow, ElLink, ElAvatar } from 'element-plus'; // 确保 ElAvatar 已导入
import { User, Lock } from '@element-plus/icons-vue';
import { useAuthStore } from '@/stores/authStore';

const router = useRouter();
const authStore = useAuthStore();

const loginFormRef = ref(null);
const loginForm = reactive({
  username: '',
  password: '',
  remember: false, // 对应 Scolaa 的 Remember me
  // role: 'admin' // 你之前的 role 逻辑，这里暂时不显示在表单中，可以保留在 loginForm 中
});
const loading = ref(false);
// const rememberMe = ref(false); // 已移入 loginForm
const illustrationExists = ref(false); // 保持你对插图的处理方式
const avatarExists = ref(false); // 新增：用于判断头像图片是否存在

const loginRules = { // 保持你的校验规则
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
};

onMounted(() => {
  // 检查插图和头像图片是否存在 (简化逻辑，实际应确保文件存在)
  // 如果你已经放置了图片，可以将对应的 Exists ref 设置为 true
  // illustrationExists.value = true;
  // avatarExists.value = true;
  console.warn("请确保 @/assets/education-illustration.svg 和 @/assets/avatar_default.png 文件存在，或在模板中调整图片引用。");
});

const handleLogin = async () => {
  if (!loginFormRef.value) return;
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        await authStore.login(loginForm.username, loginForm.password);
        ElMessage.success('登录成功！');
        const redirectPath = router.currentRoute.value.query.redirect || '/';
        router.push(redirectPath);
      } catch (error) {
        console.error("登录处理失败:", error);
      } finally {
        loading.value = false;
      }
    } else {
      ElMessage.warning('请完整填写登录信息！');
      return false;
    }
  });
};

const handleForgotPassword = () => {
  ElMessage.info('忘记密码功能暂未开放');
};

const handleRegister = () => {
  ElMessage.info('注册功能暂未开放');
  // router.push('/register');
};
</script>

<style scoped>
/* 保留你之前 .login-page-wrapper, .login-container, .left-panel, 和 .page-footer 的样式 */
.login-page-wrapper {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #eef1f5; /* 参考 Scolaa 的整体浅色背景 (可能需要调整为更偏蓝紫的渐变) */
}

.login-container {
  flex-grow: 1;
  display: flex;
  width: 100%;
  max-width: 1200px; /* 或者参考 Scolaa 的整体宽度 */
  margin: auto;
  align-items: center; /* 改为 stretch 可能更好，让左右两栏等高 */
  align-items: stretch;
  padding: 20px;
}

.left-panel { /* 你已有的左侧样式 */
  flex: 1.2;
  padding: 40px 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #333;
  /* background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%); /* 你之前的左侧背景 */
  /* color: white; */
}
.logo-container { /* 你已有的样式 */
  margin-bottom: 30px; display: flex; align-items: center;
}
.logo-image { /* 你已有的样式 */
  height: 50px; margin-right: 15px;
}
.system-title { /* 你已有的样式 */
  font-size: 28px; font-weight: bold; color: #2c3e50;
}
.tagline { /* 你已有的样式 */
  font-size: 24px; line-height: 1.5; margin-bottom: 40px; color: #555;
}
.illustration-container { /* 你已有的样式 */
  text-align: center; flex:1; display:flex; align-items:center; justify-content:center;
}
.illustration-image { /* 你已有的样式 */
  max-width: 80%; height: auto;
}
.placeholder-illustration { /* 你已有的样式 */
  font-size: 18px; color: #909399; padding: 50px 0; border: 2px dashed #dcdfe6; border-radius: 8px; background-color: #f9fafc;
}


/* --- 右侧登录表单 Scolaa 风格调整 --- */
.right-section-scolaa-style {
  flex: 0.8; /* Scolaa 右侧稍窄 */
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px; /* 上下多一点，左右少一点 */
  position: relative; /* 用于弧形背景的定位 */
  background-color: transparent; /* 右侧背景由外层容器控制或透明 */
}

/* 蓝色弧形背景 */
.right-section-scolaa-style::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0; /* 从左边缘开始 */
  right: 0; /* 到右边缘结束 */
  height: 200px; /* 弧形的高度，参考图中约占表单卡片一半 */
  background-color: #2563eb; /* Scolaa 参考图中的深蓝色 */
  border-bottom-left-radius: 50% 60px; /* 调整弧度 (百分比控制水平，像素控制垂直) */
  border-bottom-right-radius: 50% 60px;
  z-index: 0; /* 在卡片之下 */
}

.login-card-scolaa-style {
  width: 100%;
  max-width: 360px; /* Scolaa 卡片宽度 */
  background-color: #ffffff;
  border-radius: 8px; /* Scolaa 卡片圆角 */
  box-shadow: 0px 8px 25px rgba(0, 0, 0, 0.1); /* Scolaa 卡片阴影 */
  padding: 30px 30px 35px 30px; /* Scolaa 卡片内边距 */
  z-index: 1;
  position: relative; /* 确保在弧形背景之上 */
  margin-top: -100px; /* 将卡片向上移动，部分嵌入蓝色弧形中 */
}

.card-header-scolaa {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px; /* 头像和标题与表单的间距 */
}

.avatar-scolaa { /* el-avatar 的容器样式 */
  width: 64px !important; /* Scolaa 头像大小 */
  height: 64px !important;
  border: 3px solid white; /* 白色描边 */
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  margin-bottom: 8px;
  background-color: #d1d5db; /* 默认头像背景 */
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.avatar-img-scolaa {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-scolaa .el-icon { /* Element Plus User Icon 的颜色 */
  color: #6b7280;
}


.form-title-scolaa { /* "Amy Lopez" 或 "用户登录" */
  font-size: 1rem; /* 16px */
  color: #1f2937; /* 深灰色 */
  font-weight: 500; /* medium */
}

.login-form-content-scolaa .el-form-item {
  margin-bottom: 16px; /* Scolaa 表单项间距 */
}

.input-label-scolaa { /* Scolaa 风格的标签 */
  display: flex;
  align-items: center;
  font-size: 0.875rem; /* 14px */
  color: #4b5563; /* 标签颜色 */
  margin-bottom: 4px;
  font-weight: 500;
}
.input-icon-scolaa { /* 标签内的小图标 */
  margin-right: 6px;
  color: #3b82f6; /* Scolaa 主蓝色 */
  font-size: 1rem; /* 16px */
}

/* 自定义 Element Plus 输入框样式，使其更像 Scolaa 的无边框感觉 */
.login-form-content-scolaa :deep(.el-input__wrapper) {
  background-color: #fff !important; /* Scolaa 输入框是白色背景 */
  border-radius: 6px !important;
  box-shadow: none !important;
  border: 1px solid #d1d5db !important; /* 浅灰色边框 */
  padding: 0 12px !important; /* 移除 Element Plus 默认的一些 padding */
}
.login-form-content-scolaa :deep(.el-input__wrapper.is-focus) {
  border-color: #3b82f6 !important; /* 焦点时蓝色边框 */
  box-shadow: 0 0 0 1px #3b82f6 !important; /* 轻微的焦点阴影 */
}
.login-form-content-scolaa :deep(.el-input__inner) {
  color: #1f2937;
  height: 40px !important; /* Scolaa 输入框高度 */
  line-height: 40px !important;
  font-size: 0.875rem;
}
.login-form-content-scolaa :deep(.el-input__prefix) { /* 隐藏 Element Plus 默认的输入框内 prefix icon */
  display: none !important;
}


.login-options-scolaa {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  font-size: 0.875rem; /* 14px */
}
.login-options-scolaa .el-checkbox__label {
  color: #4b5563;
  font-weight: normal;
}
.forgot-password-link-scolaa {
  font-size: 0.875rem; /* 14px */
  color: #3b82f6;
}

.login-button-scolaa { /* "Sign In" 按钮 */
  width: 100%;
  background-color: #3b82f6; /* Scolaa 主蓝色 */
  border-color: #3b82f6;
  color: white;
  padding: 10px 0; /* Scolaa 按钮 padding */
  font-size: 1rem; /* 16px */
  font-weight: 500;
  border-radius: 6px;
  height: 42px; /* 按钮高度 */
}
.login-button-scolaa:hover, .login-button-scolaa:focus {
  background-color: #2563eb;
  border-color: #2563eb;
}

.signup-link-scolaa { /* "New User? Sign up" */
  text-align: center;
  margin-top: 16px;
  font-size: 0.875rem;
  color: #4b5563;
}
.signup-link-scolaa .el-link {
  font-size: 0.875rem;
  color: #3b82f6;
  font-weight: 500;
}

/* Scolaa 底部 "Login as a Tutor" 按钮 (如果需要) */
.alternative-login-scolaa {
  /* 这个按钮在Scolaa图中是在卡片外部，但颜色是蓝色 */
  /* 如果要放在卡片内部，可以像上面signup-link一样处理 */
  /* 如果要完全模拟它在卡片下方，则需要调整HTML结构 */
  /* 这里暂时不实现完全一样的外部按钮，如果需要，可以再调整 */
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #e5e7eb;
  text-align: center;
}
.tutor-login-button-scolaa {
  width: 100%;
  background-color: #2563eb; /* 深蓝色 */
  border-color: #2563eb;
  color: white;
  padding: 10px 0;
  font-size: 1rem;
  font-weight: 500;
  border-radius: 6px;
  height: 42px;
}
.tutor-login-button-scolaa:hover, .tutor-login-button-scolaa:focus {
  background-color: #1e40af;
  border-color: #1e40af;
}

/* 页脚 - 保持你现有的不变 */
.page-footer {
  width: 100%; background-color: #303133; color: #aaa; padding: 20px 0; text-align: center; font-size: 13px; flex-shrink: 0;
}
.footer-links {
  margin-bottom: 8px;
}
.footer-links .el-link {
  color: #aaa; margin: 0 10px; font-weight: normal;
}
.footer-links .el-link:hover {
  color: #fff;
}
.copyright { /* 你已有的样式 */
  font-size: 12px;
}


/* 响应式调整 */
@media (max-width: 992px) { /* 与你之前的类似，但类名可能需要更新 */
  .login-container { /* login-container-scolaa-style 下的 login-container */
    flex-direction: column;
    align-items: center; /* 让左右面板在垂直方向上居中 */
  }
  .left-panel { /* 你已有的左侧 */
    flex: none; width: 100%; max-width: 500px; /* 限制左侧最大宽度 */ padding: 30px 20px; text-align: center; margin-bottom: 30px;
  }
  .right-section-scolaa-style {
    flex: none;
    width: 100%;
    padding: 20px 15px 40px 15px; /* 调整padding */
  }
  .right-section-scolaa-style::before { /* 移动端可以简化或移除弧形 */
    height: 120px;
    border-bottom-left-radius: 0;
    border-bottom-right-radius: 0;
  }
  .login-card-scolaa-style {
    margin-top: -60px; /* 调整嵌入程度 */
    max-width: none; /* 允许卡片宽度适应父容器 */
    width: calc(100% - 30px); /* 留出一些边距 */
  }
  .illustration-image { /* 你已有的 */
    max-width: 60%;
  }
}
</style>