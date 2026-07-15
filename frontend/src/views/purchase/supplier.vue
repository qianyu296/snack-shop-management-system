<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="供应商名称"><el-input v-model="queryForm.name" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="联系人"><el-input v-model="queryForm.contactName" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="电话"><el-input v-model="queryForm.contactPhone" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:100px">
            <el-option label="启用" value="ENABLED" /><el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="table-header"><el-button type="primary" @click="handleAdd">新增供应商</el-button></div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="contactName" label="联系人" width="100" />
        <el-table-column prop="contactPhone" label="电话" width="130" />
        <el-table-column prop="address" label="地址" show-overflow-tooltip />
        <el-table-column prop="supplyMaterials" label="供应原料" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.status==='ENABLED'?'success':'info'">{{ row.status==='ENABLED'?'启用':'停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleToggle(row)">{{ row.status==='ENABLED'?'停用':'启用' }}</el-button>
            <el-popconfirm title="确认删除？" @confirm="handleDelete(row)"><template #reference><el-button type="danger" link>删除</el-button></template></el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="fetchData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>

    <el-dialog :title="isEdit?'编辑供应商':'新增供应商'" v-model="dialogVisible" width="520px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="供应商名称" prop="name"><el-input v-model="form.name" maxlength="100" /></el-form-item>
        <el-form-item label="联系人" prop="contactName"><el-input v-model="form.contactName" maxlength="30" /></el-form-item>
        <el-form-item label="联系电话" prop="contactPhone"><el-input v-model="form.contactPhone" maxlength="20" /></el-form-item>
        <el-form-item label="联系地址"><el-input v-model="form.address" maxlength="200" /></el-form-item>
        <el-form-item label="供应原料"><el-input v-model="form.supplyMaterials" placeholder="如：蔬菜、肉类" maxlength="500" /></el-form-item>
        <el-form-item label="状态" prop="status"><el-radio-group v-model="form.status"><el-radio value="ENABLED">启用</el-radio><el-radio value="DISABLED">停用</el-radio></el-radio-group></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

const loading=ref(false); const submitting=ref(false); const dialogVisible=ref(false)
const isEdit=ref(false); const editId=ref<number>(); const formRef=ref<FormInstance>()
const tableData=ref<any[]>([]); const total=ref(0)
const queryForm = reactive<Record<string,any>>({ pageNum:1, pageSize:10, name:'', contactName:'', contactPhone:'', status:'' })
const form = reactive({ name:'', contactName:'', contactPhone:'', address:'', supplyMaterials:'', status:'ENABLED', remark:'' })
const rules:FormRules = {
  name:[{required:true,message:'请输入供应商名称',trigger:'blur'}],
  contactName:[{required:true,message:'请输入联系人',trigger:'blur'}],
  contactPhone:[{required:true,message:'请输入联系电话',trigger:'blur'}],
}

async function fetchData() {
  loading.value=true
  try {
    const params:any={...queryForm}; Object.keys(params).forEach(k=>!params[k]&&delete params[k])
    const res=await request.get('/suppliers',{params}); tableData.value=res.data.list; total.value=res.data.total
  } finally { loading.value=false }
}
function handleQuery(){queryForm.pageNum=1;fetchData()}
function handleReset(){queryForm.name='';queryForm.contactName='';queryForm.contactPhone='';queryForm.status='';queryForm.pageNum=1;fetchData()}
function handleAdd(){isEdit.value=false;dialogVisible.value=true}
function handleEdit(row:any){
  isEdit.value=true;editId.value=row.id
  Object.assign(form,{name:row.name,contactName:row.contactName,contactPhone:row.contactPhone,address:row.address||'',supplyMaterials:row.supplyMaterials||'',status:row.status,remark:row.remark||''})
  dialogVisible.value=true
}
async function handleToggle(row:any){
  const s=row.status==='ENABLED'?'DISABLED':'ENABLED'
  try{await request.put(`/suppliers/${row.id}/status`,null,{params:{status:s}});ElMessage.success('操作成功');fetchData()}catch{}
}
async function handleDelete(row:any){try{await request.delete(`/suppliers/${row.id}`);ElMessage.success('删除成功');fetchData()}catch{}}
async function handleSubmit(){
  const valid=await formRef.value?.validate().catch(()=>false);if(!valid)return
  submitting.value=true
  try{if(isEdit.value){await request.put(`/suppliers/${editId.value}`,form)}else{await request.post('/suppliers',form)}
    ElMessage.success(isEdit.value?'修改成功':'新增成功');dialogVisible.value=false;fetchData()}finally{submitting.value=false}
}
function resetForm(){formRef.value?.resetFields();Object.assign(form,{name:'',contactName:'',contactPhone:'',address:'',supplyMaterials:'',status:'ENABLED',remark:''})}
onMounted(()=>fetchData())
</script>
<style scoped lang="scss">.page-container{display:flex;flex-direction:column;gap:16px}.table-header{margin-bottom:16px}</style>
