<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">404</h1>
      <p class="error-message">页面不存在</p>
      <el-button type="primary" @click="$router.push('/dashboard')">返回首页</el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.error-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-content {
  text-align: center;

  .error-code {
    font-size: 80px;
    font-weight: 700;
    color: #c0c4cc;
    margin-bottom: 8px;
  }

  .error-message {
    font-size: 18px;
    color: #909399;
    margin-bottom: 24px;
  }
}
</style>
