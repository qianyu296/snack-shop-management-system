<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="操作模块"><el-input v-model="queryForm.module" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="结果">
          <el-select v-model="queryForm.result" placeholder="全部" clearable style="width:100px">
            <el-option label="成功" value="SUCCESS" /><el-option label="失败" value="FAIL" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:240px" />
        </el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="operation" label="操作" width="100" />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="operatorIp" label="IP" width="140" />
        <el-table-column prop="result" label="结果" width="80">
          <template #default="{row}"><el-tag :type="row.result==='SUCCESS'?'success':'danger'" size="small">{{ row.result==='SUCCESS'?'成功':'失败' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="80" />
        <el-table-column prop="requestUrl" label="请求URL" show-overflow-tooltip width="200" />
        <el-table-column prop="errorMsg" label="错误信息" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="180" />
      </el-table>
      <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="fetchData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

const loading=ref(false); const tableData=ref<any[]>([]); const total=ref(0); const dateRange=ref<string[]>([])
const queryForm=reactive<Record<string,any>>({pageNum:1,pageSize:10,module:'',result:'',startDate:'',endDate:''})

async function fetchData(){
  loading.value=true
  try{if(dateRange.value?.length===2){queryForm.startDate=dateRange.value[0];queryForm.endDate=dateRange.value[1]}
    const params:any={...queryForm};Object.keys(params).forEach(k=>(params[k]===''||params[k]===undefined)&&delete params[k])
    const res=await request.get('/system/logs',{params});tableData.value=res.data.list;total.value=res.data.total
  }finally{loading.value=false}
}
function handleQuery(){queryForm.pageNum=1;fetchData()}
function handleReset(){queryForm.module='';queryForm.result='';dateRange.value=[];queryForm.startDate='';queryForm.endDate='';queryForm.pageNum=1;fetchData()}
onMounted(()=>fetchData())
</script>
<style scoped lang="scss">.page-container{display:flex;flex-direction:column;gap:16px}</style>
