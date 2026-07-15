<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="采购单号"><el-input v-model="queryForm.purchaseNo" placeholder="请输入" clearable /></el-form-item>
        <el-form-item label="供应商">
          <el-select v-model="queryForm.supplierId" placeholder="全部" clearable style="width:160px" filterable>
            <el-option v-for="s in suppliers" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:110px">
            <el-option label="草稿" value="DRAFT" /><el-option label="已入库" value="WAREHOUSED" /><el-option label="已作废" value="CANCELLED" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:240px" />
        </el-form-item>
        <el-form-item><el-button type="primary" @click="handleQuery">查询</el-button><el-button @click="handleReset">重置</el-button></el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="table-header"><el-button type="primary" @click="dialogVisible=true">新建采购单</el-button></div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="purchaseNo" label="采购单号" width="200" />
        <el-table-column prop="supplierName" label="供应商" />
        <el-table-column prop="purchaseDate" label="采购日期" width="110" />
        <el-table-column prop="totalAmount" label="总额(元)" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status==='DRAFT'?'info':row.status==='WAREHOUSED'?'success':'danger'" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="创建人" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{row}">
            <el-button type="primary" link @click="$router.push(`/purchase/${row.id}`)">详情</el-button>
            <el-button v-if="row.status==='DRAFT'" type="success" link @click="doWarehouse(row)">入库</el-button>
            <el-button v-if="row.status==='DRAFT'" type="danger" link @click="doCancel(row)">作废</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" @change="fetchData" style="margin-top:16px;justify-content:flex-end" />
    </el-card>

    <!-- 新建采购单对话框 -->
    <el-dialog title="新建采购单" v-model="dialogVisible" width="660px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12"><el-form-item label="供应商" prop="supplierId"><el-select v-model="form.supplierId" style="width:100%" filterable><el-option v-for="s in enabledSuppliers" :key="s.id" :label="s.name" :value="s.id" /></el-select></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="采购日期" prop="purchaseDate"><el-date-picker v-model="form.purchaseDate" type="date" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="备注"><el-input v-model="form.remark" maxlength="200" /></el-form-item>
        <el-divider>采购明细</el-divider>
        <div v-for="(item,idx) in form.items" :key="idx" style="display:flex;gap:8px;margin-bottom:8px;align-items:center">
          <el-select v-model="item.materialId" placeholder="选择原料" style="width:180px" filterable><el-option v-for="m in enabledMaterials" :key="m.id" :label="m.name+'('+m.unit+')'" :value="m.id" /></el-select>
          <el-input-number v-model="item.quantity" :min="0.001" :precision="3" placeholder="数量" style="width:120px" />
          <el-input-number v-model="item.unitPrice" :min="0" :precision="2" placeholder="单价" style="width:110px" />
          <span style="min-width:60px">¥{{((item.quantity||0)*(item.unitPrice||0)).toFixed(2)}}</span>
          <el-button type="danger" @click="form.items.splice(idx,1)" :disabled="form.items.length<=1" circle size="small"><el-icon><Delete /></el-icon></el-button>
        </div>
        <el-button type="primary" link @click="form.items.push({materialId:null,quantity:0,unitPrice:0})">+ 添加原料</el-button>
        <el-divider />
        <div style="text-align:right;font-size:16px">合计: <span style="color:#f56c6c;font-weight:700">¥{{totalAmount.toFixed(2)}}</span></div>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" :loading="submitting" @click="handleSubmit">创建</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

const loading=ref(false); const submitting=ref(false); const dialogVisible=ref(false)
const formRef=ref<FormInstance>(); const tableData=ref<any[]>([]); const total=ref(0)
const suppliers=ref<any[]>([]); const enabledSuppliers=ref<any[]>([]); const enabledMaterials=ref<any[]>([])
const dateRange=ref<string[]>([])
const queryForm = reactive<Record<string,any>>({ pageNum:1, pageSize:10, purchaseNo:'', supplierId:'', status:'', startDate:'', endDate:'' })
const form=reactive<Record<string,any>>({ supplierId:'', purchaseDate:new Date().toISOString().slice(0,10), remark:'', items:[{materialId:null,quantity:0,unitPrice:0}] })
const rules:FormRules = { supplierId:[{required:true,message:'请选择供应商',trigger:'change'}] }
const totalAmount=computed(()=>form.items.reduce((s:number,i:any)=>s+(i.quantity||0)*(i.unitPrice||0),0))

async function fetchData(){
  loading.value=true
  try{if(dateRange.value?.length===2){queryForm.startDate=dateRange.value[0];queryForm.endDate=dateRange.value[1]}
    const params:any={...queryForm}; Object.keys(params).forEach(k=>(params[k]===''||params[k]===undefined)&&delete params[k])
    const res=await request.get('/purchase-orders',{params}); tableData.value=res.data.list; total.value=res.data.total
  } finally{loading.value=false}
}
async function fetchSuppliers(){const r=await request.get('/suppliers',{params:{pageSize:200}});suppliers.value=r.data.list;enabledSuppliers.value=r.data.list.filter((s:any)=>s.status==='ENABLED')}
async function fetchMaterials(){const r=await request.get('/materials',{params:{status:'ENABLED',pageSize:200}});enabledMaterials.value=r.data.list}
function handleQuery(){queryForm.pageNum=1;fetchData()}
function handleReset(){queryForm.purchaseNo='';queryForm.supplierId='';queryForm.status='';dateRange.value=[];queryForm.startDate='';queryForm.endDate='';queryForm.pageNum=1;fetchData()}
function statusLabel(s:string){const m:Record<string,string>={DRAFT:'草稿',WAREHOUSED:'已入库',CANCELLED:'已作废'};return m[s]||s}
async function doWarehouse(row:any){try{await ElMessageBox.confirm('确认入库？入库后库存将增加','确认',{type:'warning'});await request.post(`/purchase-orders/${row.id}/warehouse`);ElMessage.success('入库成功');fetchData()}catch{}}
async function doCancel(row:any){try{const {value:reason}=await ElMessageBox.prompt('请输入作废原因','作废采购单',{type:'warning'});await request.put(`/purchase-orders/${row.id}/cancel`,null,{params:{reason}});ElMessage.success('已作废');fetchData()}catch{}}
async function handleSubmit(){
  const valid=await formRef.value?.validate().catch(()=>false);if(!valid)return
  const validItems=form.items.filter((i:any)=>i.materialId&&i.quantity>0)
  if(validItems.length===0){ElMessage.warning('请添加采购明细');return}
  submitting.value=true
  try{await request.post('/purchase-orders',{supplierId:form.supplierId,purchaseDate:form.purchaseDate,remark:form.remark||undefined,items:validItems.map((i:any)=>({materialId:i.materialId,quantity:i.quantity,unitPrice:i.unitPrice}))})
    ElMessage.success('创建成功');dialogVisible.value=false;fetchData()}finally{submitting.value=false}
}
function resetForm(){formRef.value?.resetFields();Object.assign(form,{supplierId:'',purchaseDate:new Date().toISOString().slice(0,10),remark:'',items:[{materialId:null,quantity:0,unitPrice:0}]})}
onMounted(()=>{fetchData();fetchSuppliers();fetchMaterials()})
</script>
<style scoped lang="scss">.page-container{display:flex;flex-direction:column;gap:16px}.table-header{margin-bottom:16px}</style>
