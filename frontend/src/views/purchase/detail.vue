<template>
  <div class="page-container" v-loading="loading">
    <el-card shadow="never">
      <template #header>
        <div style="display:flex;align-items:center;gap:16px">
          <el-button @click="$router.push('/purchase/list')">← 返回</el-button>
          <span style="font-size:16px;font-weight:600">采购单详情 - {{ po.purchaseNo }}</span>
          <el-tag :type="po.status==='DRAFT'?'info':po.status==='WAREHOUSED'?'success':'danger'">{{ statusLabel(po.status) }}</el-tag>
        </div>
      </template>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="采购单号">{{ po.purchaseNo }}</el-descriptions-item>
        <el-descriptions-item label="供应商">{{ po.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="采购日期">{{ po.purchaseDate }}</el-descriptions-item>
        <el-descriptions-item label="采购总额">¥{{ po.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ po.createdByName }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ po.createdAt }}</el-descriptions-item>
        <el-descriptions-item v-if="po.cancelReason" label="作废原因">{{ po.cancelReason }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ po.remark || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div v-if="po.status==='DRAFT'" style="margin-top:16px;display:flex;gap:8px">
        <el-button type="success" @click="doWarehouse">确认入库</el-button>
        <el-button type="danger" @click="doCancel">作废采购单</el-button>
      </div>

      <el-divider>采购明细</el-divider>
      <el-table :data="po.items" border stripe>
        <el-table-column prop="materialName" label="原料" />
        <el-table-column prop="materialUnit" label="单位" width="80" />
        <el-table-column prop="quantity" label="采购数量" width="120" />
        <el-table-column prop="unitPrice" label="单价(元)" width="100" />
        <el-table-column prop="amount" label="小计(元)" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const route=useRoute(); const router=useRouter()
const loading=ref(false); const po=ref<any>({items:[]})

function statusLabel(s:string){const m:Record<string,string>={DRAFT:'草稿',WAREHOUSED:'已入库',CANCELLED:'已作废'};return m[s]||s}

async function fetchDetail(){loading.value=true;try{const res=await request.get(`/purchase-orders/${route.params.id}`);po.value=res.data}finally{loading.value=false}}

async function doWarehouse(){
  try{await ElMessageBox.confirm('确认入库？','提示',{type:'warning'});await request.post(`/purchase-orders/${po.value.id}/warehouse`);ElMessage.success('入库成功');fetchDetail()}catch{}
}

async function doCancel(){
  try{const {value:reason}=await ElMessageBox.prompt('请输入作废原因','作废采购单',{type:'warning'});await request.put(`/purchase-orders/${po.value.id}/cancel`,null,{params:{reason}});ElMessage.success('已作废');fetchDetail()}catch{}
}

onMounted(()=>fetchDetail())
</script>
<style scoped lang="scss">.page-container{max-width:900px}</style>
