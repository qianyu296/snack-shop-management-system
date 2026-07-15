<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="用户名"><el-input v-model="queryForm.username" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="queryForm.realName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="queryForm.phone" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:100px">
            <el-option v-for="s in STATUS_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}"><el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="rejectReason" label="驳回原因" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status==='ENABLED'" type="warning" link @click="toggleStatus(row,'DISABLED')">禁用</el-button>
            <el-button v-if="row.status==='DISABLED'" type="success" link @click="toggleStatus(row,'ENABLED')">启用</el-button>
            <el-button type="primary" link @click="handleResetPwd(row)">重置密码</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="fetchData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>

    <el-dialog title="重置密码" v-model="pwdVisible" width="400px">
      <el-form><el-form-item label="新密码"><el-input v-model="newPassword" type="password" placeholder="6-20个字符" /></el-form-item></el-form>
      <template #footer><el-button @click="pwdVisible=false">取消</el-button><el-button type="primary" :loading="pwdSubmitting" @click="doResetPwd">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const STATUS_OPTIONS=[{label:'待审核',value:'PENDING'},{label:'已启用',value:'ENABLED'},{label:'已禁用',value:'DISABLED'},{label:'已驳回',value:'REJECTED'}]
const STATUS_MAP:Record<string,string>={PENDING:'待审核',ENABLED:'已启用',DISABLED:'已禁用',REJECTED:'已驳回'}
const STATUS_TAG:Record<string,string>={PENDING:'warning',ENABLED:'success',DISABLED:'info',REJECTED:'danger'}
function statusLabel(s:string){return STATUS_MAP[s]||s}
function statusTag(s:string){return STATUS_TAG[s]||''}

const loading=ref(false); const tableData=ref<any[]>([]); const total=ref(0)
const queryForm=reactive<Record<string,any>>({pageNum:1,pageSize:10,username:'',realName:'',phone:'',status:''})
const pwdVisible=ref(false); const pwdSubmitting=ref(false); const pwdUserId=ref<number>(); const newPassword=ref('')

async function fetchData(){
  loading.value=true
  try{const params:any={...queryForm};Object.keys(params).forEach(k=>!params[k]&&delete params[k]);const res=await request.get('/users',{params});tableData.value=res.data.list;total.value=res.data.total}finally{loading.value=false}
}
function handleQuery(){queryForm.pageNum=1;fetchData()}
function handleReset(){queryForm.username='';queryForm.realName='';queryForm.phone='';queryForm.status='';queryForm.pageNum=1;fetchData()}
async function toggleStatus(row:any,status:string){try{await request.put(`/users/${row.id}/status`,null,{params:{status}});ElMessage.success('操作成功');fetchData()}catch{}}
function handleResetPwd(row:any){pwdUserId.value=row.id;newPassword.value='';pwdVisible.value=true}
async function doResetPwd(){if(!newPassword.value){ElMessage.warning('请输入新密码');return};pwdSubmitting.value=true;try{await request.put(`/users/${pwdUserId.value}/reset-password`,{newPassword:newPassword.value});ElMessage.success('密码重置成功');pwdVisible.value=false}finally{pwdSubmitting.value=false}}
onMounted(()=>fetchData())
</script>
<style scoped lang="scss">.page-container{display:flex;flex-direction:column;gap:16px}.table-header{margin-bottom:16px}</style>
