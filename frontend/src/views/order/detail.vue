<template>
  <div class="page-container">
    <el-card v-loading="loading" shadow="never">
      <template #header>
        <div style="display:flex;align-items:center;gap:16px">
          <el-button @click="$router.push('/order/list')">← 返回</el-button>
          <span style="font-size:16px;font-weight:600">订单详情 - {{ order.orderNo }}</span>
          <el-tag :type="statusTag(order.status)" size="large">{{ statusLabel(order.status) }}</el-tag>
        </div>
      </template>

      <el-descriptions :column="3" border>
        <el-descriptions-item label="订单编号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单类型">{{ order.orderType === 'DINE_IN' ? '堂食' : '打包' }}</el-descriptions-item>
        <el-descriptions-item label="取餐号">{{ order.pickupNo }}</el-descriptions-item>
        <el-descriptions-item v-if="order.tableNo" label="桌号">{{ order.tableNo }}</el-descriptions-item>
        <el-descriptions-item label="订单金额">¥{{ order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="创建人">{{ order.createdByName }}</el-descriptions-item>
        <el-descriptions-item label="顾客备注">{{ order.customerRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="内部备注">{{ order.internalRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="order.cancelReason" label="取消原因">{{ order.cancelReason }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ order.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ order.updatedAt }}</el-descriptions-item>
      </el-descriptions>

      <!-- 操作按钮 -->
      <div style="margin-top:16px;display:flex;gap:8px">
        <el-button v-if="order.status === 'PENDING'" type="primary" @click="$router.push(`/order/create?id=${order.id}`)">编辑订单</el-button>
        <el-button v-if="order.status === 'PENDING'" type="success" @click="confirmAction('COOKING')">确认制作</el-button>
        <el-button v-if="order.status === 'COOKING'" type="warning" @click="confirmAction('READY')">制作完成</el-button>
        <el-button v-if="order.status === 'READY'" type="success" @click="confirmAction('COMPLETED')">顾客取餐</el-button>
        <el-button v-if="order.status === 'PENDING'" type="danger" @click="handleCancel">取消订单</el-button>
      </div>

      <!-- 明细 -->
      <el-divider>订单明细</el-divider>
      <el-table :data="order.items" border stripe>
        <el-table-column prop="dishName" label="菜品" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="specName" label="规格" width="100">
          <template #default="{ row }">{{ row.specName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="unitPrice" label="单价(元)" width="100" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="amount" label="小计(元)" width="100" />
      </el-table>

      <!-- 状态时间线 -->
      <el-divider>状态记录</el-divider>
      <el-timeline>
        <el-timeline-item
          v-for="log in order.statusLogs"
          :key="log.id"
          :timestamp="log.createdAt"
          placement="top"
        >
          <span>
            {{ statusLabel(log.afterStatus) }}
            <template v-if="log.beforeStatus">（{{ statusLabel(log.beforeStatus) }} → {{ statusLabel(log.afterStatus) }}）</template>
          </span>
          <div v-if="log.remark" style="color:#909399;font-size:13px">{{ log.remark }}</div>
          <div style="color:#909399;font-size:12px">操作人: {{ log.operatorName }}</div>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const route = useRoute(); const router = useRouter()
const loading = ref(false)
const order = ref<any>({ items: [], statusLogs: [] })

const STATUS_MAP: Record<string, string> = { PENDING: '待确认', COOKING: '制作中', READY: '待取餐', COMPLETED: '已完成', CANCELLED: '已取消' }
const STATUS_TAG: Record<string, string> = { PENDING: 'warning', COOKING: 'primary', READY: 'info', COMPLETED: 'success', CANCELLED: 'danger' }

function statusLabel(s: string) { return STATUS_MAP[s] || s }
function statusTag(s: string) { return STATUS_TAG[s] || '' }

async function fetchDetail() {
  loading.value = true
  try {
    const res = await request.get(`/orders/${route.params.id}`)
    order.value = res.data
  } finally { loading.value = false }
}

async function confirmAction(targetStatus: string) {
  const labelMap: Record<string, string> = { COOKING: '确认开始制作并扣减库存', READY: '确认制作完成', COMPLETED: '确认顾客已取餐' }
  try {
    await ElMessageBox.confirm(`${labelMap[targetStatus]}？`, '操作确认', { type: 'warning' })
    await request.put(`/orders/${order.value.id}/status`, { targetStatus })
    ElMessage.success('操作成功'); fetchDetail()
  } catch { /* cancelled */ }
}

async function handleCancel() {
  try {
    const { value: reason } = await ElMessageBox.prompt('请输入取消原因', '取消订单', { type: 'warning' })
    await request.put(`/orders/${order.value.id}/cancel`, null, { params: { reason } })
    ElMessage.success('订单已取消'); fetchDetail()
  } catch { /* cancelled */ }
}

onMounted(() => fetchDetail())
</script>

<style scoped lang="scss">
.page-container { max-width: 1000px; }
</style>
