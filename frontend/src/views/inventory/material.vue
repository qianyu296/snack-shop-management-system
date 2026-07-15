<template>
  <div class="page-container">
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="原料名称">
          <el-input v-model="queryForm.name" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.category" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="item in CATEGORY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="queryForm.lowStock" :true-value="true" :false-value="undefined">仅低库存预警</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <el-button type="primary" @click="handleAdd">新增原料</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="原料名称" />
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">{{ CATEGORY_MAP[row.category] || row.category }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="currentStock" label="当前库存" width="120" />
        <el-table-column prop="safeStock" label="安全库存" width="100" />
        <el-table-column label="库存状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.lowStock ? 'danger' : 'success'">
              {{ row.lowStock ? '低库存' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ row.status === 'ENABLED' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleAdjust(row)">库存调整</el-button>
            <el-button type="warning" link @click="handleToggleStatus(row)">{{ row.status === 'ENABLED' ? '停用' : '启用' }}</el-button>
            <el-popconfirm title="确认删除该原料？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageQuery.pageNum"
        v-model:page-size="pageQuery.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="fetchData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="isEdit ? '编辑原料' : '新增原料'" v-model="dialogVisible" width="520px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="原料名称" prop="name">
          <el-input v-model="form.name" placeholder="2-50个字符" maxlength="50" />
        </el-form-item>
        <el-form-item label="原料分类" prop="category">
          <el-select v-model="form.category" style="width: 100%">
            <el-option v-for="item in CATEGORY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="计量单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如：克、千克、个、份" maxlength="20" />
        </el-form-item>
        <el-form-item label="安全库存" prop="safeStock">
          <el-input-number v-model="form.safeStock" :min="0" :precision="3" style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio value="ENABLED">启用</el-radio>
            <el-radio value="DISABLED">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 库存调整对话框 -->
    <el-dialog title="库存调整" v-model="adjustVisible" width="460px">
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="80px">
        <el-form-item label="调整类型" prop="changeType">
          <el-radio-group v-model="adjustForm.changeType">
            <el-radio value="SURPLUS_ADJUST">盘盈 (+)</el-radio>
            <el-radio value="LOSS_ADJUST">盘亏 (-)</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="调整数量" prop="quantity">
          <el-input-number v-model="adjustForm.quantity" :min="0.001" :precision="3" style="width: 200px" />
        </el-form-item>
        <el-form-item label="调整原因" prop="remark">
          <el-input v-model="adjustForm.remark" type="textarea" :rows="2" placeholder="请填写调整原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="primary" :loading="adjustSubmitting" @click="submitAdjust">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

const CATEGORY_OPTIONS = [
  { label: '主食', value: 'STAPLE' }, { label: '蔬菜', value: 'VEGETABLE' },
  { label: '肉类', value: 'MEAT' }, { label: '调料', value: 'SEASONING' },
  { label: '包装材料', value: 'PACKAGING' }, { label: '其他', value: 'OTHER' },
]
const CATEGORY_MAP: Record<string, string> = Object.fromEntries(CATEGORY_OPTIONS.map(o => [o.value, o.label]))

const loading = ref(false); const submitting = ref(false); const dialogVisible = ref(false)
const isEdit = ref(false); const editId = ref<number>()
const formRef = ref<FormInstance>(); const tableData = ref<any[]>([]); const total = ref(0)
const pageQuery = reactive({ pageNum: 1, pageSize: 10 })
const queryForm = reactive<Record<string, any>>({ name: '', category: '', status: '', lowStock: undefined })

const form = reactive({ name: '', category: '', unit: '', safeStock: 0, status: 'ENABLED', remark: '' })
const rules: FormRules = {
  name: [{ required: true, message: '请输入原料名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择原料分类', trigger: 'change' }],
  unit: [{ required: true, message: '请输入计量单位', trigger: 'blur' }],
  safeStock: [{ required: true, message: '请输入安全库存', trigger: 'blur' }],
}

const adjustVisible = ref(false); const adjustSubmitting = ref(false)
const adjustFormRef = ref<FormInstance>(); const adjustMaterialId = ref<number>()
const adjustForm = reactive({ changeType: 'SURPLUS_ADJUST', quantity: 0, remark: '' })
const adjustRules: FormRules = {
  changeType: [{ required: true, message: '请选择调整类型', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入调整数量', trigger: 'blur' }],
  remark: [{ required: true, message: '请填写调整原因', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { ...pageQuery, ...queryForm }
    Object.keys(params).forEach(k => (params[k] === '' || params[k] === undefined) && delete params[k])
    const res = await request.get('/materials', { params })
    tableData.value = res.data.list; total.value = res.data.total
  } finally { loading.value = false }
}

function handleQuery() { pageQuery.pageNum = 1; fetchData() }
function handleReset() { queryForm.name = ''; queryForm.category = ''; queryForm.status = ''; queryForm.lowStock = undefined; pageQuery.pageNum = 1; fetchData() }

function handleAdd() { isEdit.value = false; dialogVisible.value = true }
function handleEdit(row: any) {
  isEdit.value = true; editId.value = row.id
  Object.assign(form, { name: row.name, category: row.category, unit: row.unit, safeStock: row.safeStock, status: row.status, remark: row.remark || '' })
  dialogVisible.value = true
}

async function handleToggleStatus(row: any) {
  const newStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  try { await request.put(`/materials/${row.id}/status`, null, { params: { status: newStatus } }); ElMessage.success('操作成功'); fetchData() } catch { /* handled */ }
}

async function handleDelete(row: any) {
  try { await request.delete(`/materials/${row.id}`); ElMessage.success('删除成功'); fetchData() } catch { /* handled */ }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false); if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) { await request.put(`/materials/${editId.value}`, form) } else { await request.post('/materials', form) }
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); dialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

function resetForm() { formRef.value?.resetFields(); Object.assign(form, { name: '', category: '', unit: '', safeStock: 0, status: 'ENABLED', remark: '' }) }

function handleAdjust(row: any) { adjustMaterialId.value = row.id; adjustForm.changeType = 'SURPLUS_ADJUST'; adjustForm.quantity = 0; adjustForm.remark = ''; adjustVisible.value = true }

async function submitAdjust() {
  const valid = await adjustFormRef.value?.validate().catch(() => false); if (!valid) return
  adjustSubmitting.value = true
  try {
    await request.post('/inventory/adjustments', { materialId: adjustMaterialId.value, changeType: adjustForm.changeType, quantity: adjustForm.quantity, remark: adjustForm.remark, businessNo: 'ADJ' + Date.now() })
    ElMessage.success('库存调整成功'); adjustVisible.value = false; fetchData()
  } finally { adjustSubmitting.value = false }
}

onMounted(() => fetchData())
</script>

<style scoped lang="scss">
.page-container { display: flex; flex-direction: column; gap: 16px; }
.table-header { margin-bottom: 16px; }
</style>
