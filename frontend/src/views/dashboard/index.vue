<template>
  <div class="dashboard" v-loading="loading">
    <!-- 今日概览 -->
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in todayCards" :key="card.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 基础数据 -->
    <el-card class="section-card" shadow="never">
      <template #header>基础数据</template>
      <el-row :gutter="16">
        <el-col :span="6"><div class="base-item">上架菜品<span>{{ data.baseStats?.onSaleDishCount || 0 }}</span></div></el-col>
        <el-col :span="6"><div class="base-item">原料种类<span>{{ data.baseStats?.materialCount || 0 }}</span></div></el-col>
        <el-col :span="6"><div class="base-item">低库存原料<span style="color:#f56c6c">{{ data.baseStats?.lowStockMaterialCount || 0 }}</span></div></el-col>
        <el-col :span="6"><div class="base-item">启用供应商<span>{{ data.baseStats?.enabledSupplierCount || 0 }}</span></div></el-col>
      </el-row>
    </el-card>

    <!-- 待办事项 -->
    <el-card class="section-card" shadow="never" v-if="authStore.isAdmin">
      <template #header>待办事项</template>
      <el-row :gutter="16">
        <el-col :span="8">
          <div class="todo-item" @click="$router.push('/system/audit')">待审核账号 <el-tag type="warning" size="small">{{ data.todo?.pendingAuditCount || 0 }}</el-tag></div>
        </el-col>
        <el-col :span="8">
          <div class="todo-item" @click="$router.push('/order/list')">待确认订单 <el-tag type="warning" size="small">{{ data.todo?.pendingOrderCount || 0 }}</el-tag></div>
        </el-col>
        <el-col :span="8">
          <div class="todo-item" @click="$router.push('/inventory/material')">低库存预警 <el-tag type="danger" size="small">{{ data.todo?.lowStockCount || 0 }}</el-tag></div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 订单趋势图 -->
    <el-card class="section-card" shadow="never">
      <template #header>近7天订单趋势</template>
      <div ref="trendChartRef" style="height:300px"></div>
    </el-card>

    <!-- 库存预警 -->
    <el-card class="section-card" shadow="never" v-if="data.lowStockMaterials?.length">
      <template #header>库存预警</template>
      <el-table :data="data.lowStockMaterials" size="small" border stripe>
        <el-table-column prop="name" label="原料" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="currentStock" label="当前库存" width="100" />
        <el-table-column prop="safeStock" label="安全库存" width="100" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const data = ref<any>({ today: {}, baseStats: {}, orderTrend: [], lowStockMaterials: [], todo: {} })
const trendChartRef = ref<HTMLElement>()

const todayCards = computed(() => {
  const t = data.value.today || {}
  return [
    { label: '今日完成订单', value: t.completedOrderCount || 0, color: '#67c23a' },
    { label: '今日营业额(元)', value: (t.todayRevenue || 0).toFixed(2), color: '#409eff' },
    { label: '待确认订单', value: t.pendingOrderCount || 0, color: '#e6a23c' },
    { label: '制作中订单', value: t.cookingOrderCount || 0, color: '#909399' },
  ]
})

async function fetchData() {
  loading.value = true
  try {
    const res = await request.get('/dashboard/summary')
    data.value = res.data
    await nextTick()
    renderTrendChart()
  } finally { loading.value = false }
}

function renderTrendChart() {
  if (!trendChartRef.value) return
  const chart = echarts.init(trendChartRef.value)
  const trend = data.value.orderTrend || []
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '营业额(元)'] },
    xAxis: { type: 'category', data: trend.map((t: any) => t.date) },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '营业额(元)' },
    ],
    series: [
      { name: '订单数', type: 'line', data: trend.map((t: any) => t.orderCount) },
      { name: '营业额(元)', type: 'line', yAxisIndex: 1, data: trend.map((t: any) => t.revenue) },
    ],
  })
}

onMounted(() => fetchData())
</script>

<style scoped lang="scss">
.dashboard { display: flex; flex-direction: column; gap: 16px; }
.stat-card { text-align: center; .stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; } .stat-value { font-size: 28px; font-weight: 700; } }
.section-card { margin-top: 0; }
.base-item { text-align: center; font-size: 14px; color: #606266; span { display: block; font-size: 24px; font-weight: 600; margin-top: 4px; color: #303133; } }
.todo-item { cursor: pointer; text-align: center; display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 14px; &:hover { color: #409eff; } }
</style>
