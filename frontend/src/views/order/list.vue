<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="订单号">
          <el-input v-model="queryForm.orderNo" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="取餐号">
          <el-input-number v-model="queryForm.pickupNo" :min="1" placeholder="" style="width:140px" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryForm.orderType" placeholder="全部" clearable style="width:100px">
            <el-option label="堂食" value="DINE_IN" />
            <el-option label="打包" value="TAKEAWAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width:110px">
            <el-option v-for="s in STATUS_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker
            v-model="dateRange" type="daterange" range-separator="至"
            start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <el-button type="primary" @click="$router.push('/order/create')">新建订单</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="pickupNo" label="取餐号" width="80" />
        <el-table-column label="类型" width="70">
          <template #default="{ row }">{{ row.orderType === 'DINE_IN' ? '堂食' : '打包' }}</template>
        </el-table-column>
        <el-table-column prop="tableNo" label="桌号" width="70" />
        <el-table-column prop="totalAmount" label="金额(元)" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdByName" label="创建人" width="100" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="$router.push(`/order/${row.id}`)">详情</el-button>
            <el-button v-if="row.status === 'PENDING'" type="success" link @click="confirmAction(row, 'COOKING')">制作</el-button>
            <el-button v-if="row.status === 'COOKING'" type="warning" link @click="confirmAction(row, 'READY')">备好</el-button>
            <el-button v-if="row.status === 'READY'" type="success" link @click="confirmAction(row, 'COMPLETED')">完成</el-button>
            <el-button v-if="row.status === 'PENDING'" type="danger" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize"
        :total="total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next" @change="fetchData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const STATUS_OPTIONS = [
  { label: '待确认', value: 'PENDING' }, { label: '制作中', value: 'COOKING' },
  { label: '待取餐', value: 'READY' }, { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
]

const STATUS_MAP: Record<string, string> = { PENDING: '待确认', COOKING: '制作中', READY: '待取餐', COMPLETED: '已完成', CANCELLED: '已取消' }
const STATUS_TAG_MAP: Record<string, string> = { PENDING: 'warning', COOKING: 'primary', READY: 'info', COMPLETED: 'success', CANCELLED: 'danger' }

function statusLabel(s: string) { return STATUS_MAP[s] || s }
function statusTag(s: string) { return STATUS_TAG_MAP[s] || '' }

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])

const queryForm = reactive<Record<string, any>>({
  pageNum: 1, pageSize: 10,
  orderNo: '', pickupNo: undefined, orderType: '', status: '', startDate: '', endDate: '',
})

async function fetchData() {
  loading.value = true
  try {
    if (dateRange.value?.length === 2) { queryForm.startDate = dateRange.value[0]; queryForm.endDate = dateRange.value[1] }
    const params: any = { ...queryForm }
    Object.keys(params).forEach(k => (params[k] === '' || params[k] === undefined) && delete params[k])
    const res = await request.get('/orders', { params })
    tableData.value = res.data.list; total.value = res.data.total
  } finally { loading.value = false }
}

function handleQuery() { queryForm.pageNum = 1; fetchData() }
function handleReset() {
  queryForm.orderNo = ''; queryForm.pickupNo = undefined; queryForm.orderType = ''; queryForm.status = ''
  dateRange.value = []; queryForm.startDate = ''; queryForm.endDate = ''
  queryForm.pageNum = 1; fetchData()
}

async function confirmAction(row: any, targetStatus: string) {
  const labelMap: Record<string, string> = { COOKING: '确认开始制作', READY: '确认制作完成', COMPLETED: '确认顾客已取餐' }
  try {
    await ElMessageBox.confirm(`${labelMap[targetStatus]}？`, '操作确认', { type: 'warning' })
    await request.put(`/orders/${row.id}/status`, { targetStatus })
    ElMessage.success('操作成功'); fetchData()
  } catch { /* cancelled */ }
}

async function handleCancel(row: any) {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消订单', { type: 'warning' })
    await request.put(`/orders/${row.id}/cancel`, null, { params: { reason } })
    ElMessage.success('订单已取消'); fetchData()
  } catch { /* cancelled */ }
}

onMounted(() => fetchData())
</script>

<style scoped lang="scss">
.page-container { display: flex; flex-direction: column; gap: 16px; }
.table-header { margin-bottom: 16px; }
</style>
