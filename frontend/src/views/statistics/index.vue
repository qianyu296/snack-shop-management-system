<template>
  <div class="page-container" v-loading="loading">
    <el-card class="search-card" shadow="never">
      <el-form inline>
        <el-form-item label="日期范围">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
            start-placeholder="开始" end-placeholder="结束" value-format="YYYY-MM-DD" style="width:260px" />
        </el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button></el-form-item>
      </el-form>
    </el-card>

    <!-- 完成率 -->
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="6"><el-card shadow="never"><div class="kpi-label">订单完成率</div><div class="kpi-value">{{ data.completionRate || 0 }}%</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="kpi-label">堂食订单</div><div class="kpi-value">{{ data.orderTypePie?.dineIn || 0 }}</div></el-card></el-col>
      <el-col :span="6"><el-card shadow="never"><div class="kpi-label">打包订单</div><div class="kpi-value">{{ data.orderTypePie?.takeaway || 0 }}</div></el-card></el-col>
    </el-row>

    <!-- 营业额趋势 -->
    <el-card class="section-card" shadow="never"><template #header>营业额趋势</template><div ref="revenueChartRef" style="height:300px"></div></el-card>

    <!-- 订单量趋势 -->
    <el-card class="section-card" shadow="never"><template #header>订单量趋势</template><div ref="orderChartRef" style="height:300px"></div></el-card>

    <!-- 菜品销量排行 -->
    <el-card class="section-card" shadow="never" v-if="data.dishRank?.length"><template #header>菜品销量排行 TOP10</template><div ref="dishChartRef" style="height:300px"></div></el-card>

    <!-- 分类销量占比 -->
    <el-row :gutter="16">
      <el-col :span="12"><el-card class="section-card" shadow="never"><template #header>分类销量占比</template><div ref="catChartRef" style="height:280px"></div></el-card></el-col>
      <el-col :span="12"><el-card class="section-card" shadow="never"><template #header>订单类型占比</template><div ref="typeChartRef" style="height:280px"></div></el-card></el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const loading=ref(false); const data=ref<any>({})
const dateRange=ref<string[]>([])
const revenueChartRef=ref<HTMLElement>(); const orderChartRef=ref<HTMLElement>()
const dishChartRef=ref<HTMLElement>(); const catChartRef=ref<HTMLElement>(); const typeChartRef=ref<HTMLElement>()

async function fetchData(){
  loading.value=true
  try {
    const params:any={}
    if(dateRange.value?.length===2){params.startDate=dateRange.value[0];params.endDate=dateRange.value[1]}
    const res=await request.get('/statistics/business',{params})
    data.value=res.data; await nextTick(); renderCharts()
  } finally { loading.value=false }
}

function renderCharts(){
  if (revenueChartRef.value) {
    const c=echarts.init(revenueChartRef.value)
    c.setOption({
      tooltip:{trigger:'axis'}, xAxis:{type:'category',data:(data.value.revenueTrend||[]).map((t:any)=>t.date)},
      yAxis:{type:'value',name:'元'}, series:[{name:'营业额',type:'line',data:(data.value.revenueTrend||[]).map((t:any)=>t.amount),smooth:true}]
    })
  }
  if (orderChartRef.value) {
    const c=echarts.init(orderChartRef.value)
    const d=data.value.orderCountTrend||[]
    c.setOption({
      tooltip:{trigger:'axis'}, legend:{data:['创建','完成','取消']},
      xAxis:{type:'category',data:d.map((t:any)=>t.date)}, yAxis:{type:'value'},
      series:[
        {name:'创建',type:'line',data:d.map((t:any)=>t.created)},
        {name:'完成',type:'line',data:d.map((t:any)=>t.completed)},
        {name:'取消',type:'line',data:d.map((t:any)=>t.cancelled)},
      ]
    })
  }
  if (dishChartRef.value) {
    const c=echarts.init(dishChartRef.value)
    const d=(data.value.dishRank||[]).slice(0,10)
    c.setOption({
      tooltip:{trigger:'axis'}, xAxis:{type:'value'}, yAxis:{type:'category',data:d.map((t:any)=>t.dishName).reverse(),inverse:true},
      series:[{name:'销量',type:'bar',data:d.map((t:any)=>t.count).reverse(),label:{show:true,position:'right'}}]
    })
  }
  if (catChartRef.value) {
    const c=echarts.init(catChartRef.value)
    const d=data.value.categoryPie||[]
    c.setOption({
      tooltip:{trigger:'item'}, series:[{type:'pie',radius:['40%','70%'],data:d.map((t:any)=>({name:t.categoryName,value:t.value}))}]
    })
  }
  if (typeChartRef.value) {
    const c=echarts.init(typeChartRef.value)
    const tp=data.value.orderTypePie||{}
    c.setOption({
      tooltip:{trigger:'item'}, series:[{type:'pie',radius:'60%',data:[{name:'堂食',value:tp.dineIn||0},{name:'打包',value:tp.takeaway||0}]}]
    })
  }
}

onMounted(()=>fetchData())
</script>

<style scoped lang="scss">
.page-container{display:flex;flex-direction:column;gap:16px}
.section-card{margin-top:0}
.kpi-label{font-size:14px;color:#909399}.kpi-value{font-size:28px;font-weight:700;color:#409eff;margin-top:8px}
</style>
