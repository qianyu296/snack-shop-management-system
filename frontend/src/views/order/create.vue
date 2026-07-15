<template>
  <div class="page-container">
    <el-card shadow="never">
      <template #header><span style="font-size:16px;font-weight:600">创建订单</span></template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width:700px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="订单类型" prop="orderType">
              <el-radio-group v-model="form.orderType" @change="onTypeChange">
                <el-radio value="DINE_IN">堂食</el-radio>
                <el-radio value="TAKEAWAY">打包</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.orderType === 'DINE_IN'" label="桌号" prop="tableNo">
              <el-input v-model="form.tableNo" placeholder="请输入桌号" maxlength="10" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="顾客备注">
          <el-input v-model="form.customerRemark" placeholder="如：少辣、不要香菜" maxlength="200" />
        </el-form-item>
        <el-form-item label="内部备注">
          <el-input v-model="form.internalRemark" placeholder="仅后台可见" maxlength="200" />
        </el-form-item>

        <el-divider>菜品明细</el-divider>
        <div v-for="(item, idx) in form.items" :key="idx" style="display:flex;gap:8px;margin-bottom:12px;align-items:center;flex-wrap:wrap">
          <el-select v-model="item.dishId" placeholder="选择菜品" style="width:160px" filterable @change="onDishChange(idx)">
            <el-option v-for="d in onSaleDishes" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
          <el-select v-if="getDishSpecs(idx).length > 0" v-model="item.specId" placeholder="规格" style="width:120px" @change="calcAmount">
            <el-option v-for="s in getDishSpecs(idx)" :key="s.id" :label="s.name + ' ¥' + s.price" :value="s.id" />
          </el-select>
          <el-input-number v-model="item.quantity" :min="1" style="width:100px" @change="calcAmount" />
          <span style="min-width:80px;text-align:right">
            ¥{{ getItemAmount(item).toFixed(2) }}
          </span>
          <el-button type="danger" @click="removeItem(idx)" :disabled="form.items.length <= 1" circle size="small">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
        <el-button type="primary" link @click="addItem">+ 添加菜品</el-button>

        <el-divider />
        <div style="text-align:right;font-size:18px;margin:16px 0">
          合计: <span style="color:#f56c6c;font-weight:700">¥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <div style="text-align:right">
          <el-button @click="$router.back()">取消</el-button>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">创建订单</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const onSaleDishes = ref<any[]>([])

const form = reactive<Record<string, any>>({
  orderType: 'DINE_IN', tableNo: '',
  customerRemark: '', internalRemark: '',
  items: [{ dishId: null, specId: null, quantity: 1 }],
})

const rules: FormRules = {
  orderType: [{ required: true, message: '请选择订单类型', trigger: 'change' }],
  tableNo: [{ required: true, message: '堂食请输入桌号', trigger: 'blur' }],
}

const totalAmount = computed(() => {
  return form.items.reduce((sum: number, item: any) => sum + getItemAmount(item), 0)
})

function getDishSpecs(idx: number) {
  const item = form.items[idx]
  if (!item.dishId) return []
  const dish = onSaleDishes.value.find((d: any) => d.id === item.dishId)
  return dish?.specs || []
}

function getItemAmount(item: any): number {
  if (!item.dishId) return 0
  const dish = onSaleDishes.value.find((d: any) => d.id === item.dishId)
  if (!dish) return 0
  let price = dish.basePrice
  if (item.specId) {
    const spec = dish.specs?.find((s: any) => s.id === item.specId)
    if (spec) price = spec.price
  }
  return price * (item.quantity || 0)
}

function onTypeChange() { if (form.orderType === 'TAKEAWAY') form.tableNo = '' }
function calcAmount() { /* totalAmount is computed, auto-reacts */ }
function onDishChange(idx: number) { form.items[idx].specId = null; calcAmount() }
function addItem() { form.items.push({ dishId: null, specId: null, quantity: 1 }) }
function removeItem(idx: number) { form.items.splice(idx, 1) }

async function fetchDishes() {
  const res = await request.get('/dishes/on-sale')
  onSaleDishes.value = res.data
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  // 校验至少有一个菜品
  const validItems = form.items.filter((i: any) => i.dishId)
  if (validItems.length === 0) { ElMessage.warning('请选择至少一个菜品'); return }

  submitting.value = true
  try {
    const res = await request.post('/orders', {
      orderType: form.orderType,
      tableNo: form.tableNo || undefined,
      customerRemark: form.customerRemark || undefined,
      internalRemark: form.internalRemark || undefined,
      items: validItems.map((i: any) => ({
        dishId: i.dishId,
        specId: i.specId || undefined,
        quantity: i.quantity,
      })),
    })
    ElMessage.success('订单创建成功')
    router.push(`/order/${res.data.id}`)
  } finally { submitting.value = false }
}

onMounted(() => fetchDishes())
</script>

<style scoped lang="scss">
.page-container { max-width: 900px; }
</style>
