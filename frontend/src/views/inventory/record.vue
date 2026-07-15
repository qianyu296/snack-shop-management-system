<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="变动类型">
          <el-select v-model="queryForm.changeType" placeholder="全部" clearable style="width: 140px">
            <el-option label="采购入库" value="PURCHASE_IN" />
            <el-option label="订单消耗" value="ORDER_CONSUME" />
            <el-option label="盘盈调整" value="SURPLUS_ADJUST" />
            <el-option label="盘亏调整" value="LOSS_ADJUST" />
          </el-select>
        </el-form-item>
        <el-form-item label="业务单号">
          <el-input v-model="queryForm.businessNo" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="materialName" label="原料" width="120" />
        <el-table-column prop="changeType" label="变动类型" width="110">
          <template #default="{ row }">
            <el-tag :type="changeTypeTag(row.changeType)">{{ changeTypeLabel(row.changeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="beforeStock" label="变动前库存" width="120" />
        <el-table-column prop="changeQuantity" label="变动数量" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.changeQuantity > 0 ? '#67c23a' : '#f56c6c' }">
              {{ row.changeQuantity > 0 ? '+' : '' }}{{ row.changeQuantity }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="afterStock" label="变动后库存" width="120" />
        <el-table-column prop="businessNo" label="业务单号" width="200" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="createdAt" label="时间" width="180" />
      </el-table>
      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="fetchData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])

const queryForm = reactive<Record<string, any>>({
  pageNum: 1, pageSize: 10,
  changeType: '', businessNo: '', startDate: '', endDate: '',
})

function changeTypeLabel(type: string) {
  const map: Record<string, string> = { PURCHASE_IN: '采购入库', ORDER_CONSUME: '订单消耗', SURPLUS_ADJUST: '盘盈', LOSS_ADJUST: '盘亏' }
  return map[type] || type
}
function changeTypeTag(type: string) {
  const map: Record<string, string> = { PURCHASE_IN: 'success', ORDER_CONSUME: 'danger', SURPLUS_ADJUST: 'warning', LOSS_ADJUST: 'info' }
  return map[type] || ''
}

async function fetchData() {
  loading.value = true
  try {
    if (dateRange.value?.length === 2) {
      queryForm.startDate = dateRange.value[0]; queryForm.endDate = dateRange.value[1]
    }
    const params: any = { ...queryForm }
    Object.keys(params).forEach(k => !params[k] && delete params[k])
    const res = await request.get('/inventory/records', { params })
    tableData.value = res.data.list; total.value = res.data.total
  } finally { loading.value = false }
}

function handleQuery() { queryForm.pageNum = 1; fetchData() }
function handleReset() {
  queryForm.changeType = ''; queryForm.businessNo = ''; dateRange.value = []
  queryForm.startDate = ''; queryForm.endDate = ''; queryForm.pageNum = 1
  fetchData()
}

onMounted(() => fetchData())
</script>

<style scoped lang="scss">
.page-container { display: flex; flex-direction: column; gap: 16px; }
</style>
