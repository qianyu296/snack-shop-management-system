<template>
  <div class="page-container">
    <el-card shadow="never" style="max-width:600px">
      <template #header><span style="font-size:16px;font-weight:600">个人中心</span></template>
      <el-tabs>
        <el-tab-pane label="基本信息">
          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="80px">
            <el-form-item label="用户名"><el-input :model-value="authStore.username" disabled /></el-form-item>
            <el-form-item label="姓名" prop="realName"><el-input v-model="profileForm.realName" /></el-form-item>
            <el-form-item label="手机号" prop="phone"><el-input v-model="profileForm.phone" /></el-form-item>
            <el-form-item label="角色">
              <el-tag v-if="authStore.isAdmin" type="danger">管理员</el-tag>
              <el-tag v-else type="primary">店长</el-tag>
            </el-form-item>
            <el-form-item><el-button type="primary" :loading="profileSubmitting" @click="saveProfile">保存</el-button></el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="修改密码">
          <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
            <el-form-item label="旧密码" prop="oldPassword"><el-input v-model="pwdForm.oldPassword" type="password" /></el-form-item>
            <el-form-item label="新密码" prop="newPassword"><el-input v-model="pwdForm.newPassword" type="password" /></el-form-item>
            <el-form-item><el-button type="primary" :loading="pwdSubmitting" @click="savePassword">修改密码</el-button></el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import { useAuthStore } from '@/stores/auth'

const authStore=useAuthStore()
const profileFormRef=ref<FormInstance>(); const pwdFormRef=ref<FormInstance>()
const profileSubmitting=ref(false); const pwdSubmitting=ref(false)

const profileForm=reactive({realName:authStore.userInfo?.realName||'',phone:authStore.userInfo?.phone||''})
const profileRules:FormRules={realName:[{required:true,message:'请输入姓名'}],phone:[{required:true,message:'请输入手机号',pattern:/^1[3-9]\d{9}$/}]}
const pwdForm=reactive({oldPassword:'',newPassword:''})
const pwdRules:FormRules={oldPassword:[{required:true,message:'请输入旧密码'}],newPassword:[{required:true,min:6,message:'密码至少6位'}]}

async function saveProfile(){
  const valid=await profileFormRef.value?.validate().catch(()=>false);if(!valid)return
  profileSubmitting.value=true
  try{await request.put('/auth/profile',{realName:profileForm.realName,phone:profileForm.phone});await authStore.fetchUserInfo();ElMessage.success('修改成功')}finally{profileSubmitting.value=false}
}
async function savePassword(){
  const valid=await pwdFormRef.value?.validate().catch(()=>false);if(!valid)return
  pwdSubmitting.value=true
  try{await request.put('/auth/change-password',pwdForm);ElMessage.success('密码修改成功，请重新登录');authStore.logout()}finally{pwdSubmitting.value=false}
}
</script>
<style scoped lang="scss">.page-container{max-width:700px}</style>
