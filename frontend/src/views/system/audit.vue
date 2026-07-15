<template>
  <div class="page-container">
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}"><el-tag type="warning" size="small">待审核</el-tag></template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button type="success" @click="handleAudit(row, true)">通过</el-button>
            <el-button type="danger" @click="handleAudit(row, false)">驳回</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageQuery.pageNum" v-model:page-size="pageQuery.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="fetchData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading=ref(false); const tableData=ref<any[]>([]); const total=ref(0)
const pageQuery=reactive({pageNum:1,pageSize:10})

async function fetchData(){
  loading.value=true
  try{const res=await request.get('/users',{params:{...pageQuery,status:'PENDING'}});tableData.value=res.data.list;total.value=res.data.total}finally{loading.value=false}
}

async function handleAudit(row:any,approved:boolean){
  if(approved){
    try{await ElMessageBox.confirm('确认通过该申请？','审核确认',{type:'warning'});await request.put(`/users/${row.id}/audit`,{approved:true});ElMessage.success('已通过');fetchData()}catch{}
  }else{
    try{
      const {value:reason}=await ElMessageBox.prompt('请输入驳回原因','驳回申请',{type:'warning',confirmButtonText:'确定驳回'})
      await request.put(`/users/${row.id}/audit`,{approved:false,rejectReason:reason});ElMessage.success('已驳回');fetchData()
    }catch{}
  }
}

onMounted(()=>fetchData())
</script>
<style scoped lang="scss">.page-container{display:flex;flex-direction:column;gap:16px}</style>
