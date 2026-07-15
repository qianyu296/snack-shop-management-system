<template>
  <div class="page-container">
    <!-- 查询区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="菜品名称">
          <el-input v-model="queryForm.name" placeholder="请输入" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.categoryId" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="销售状态">
          <el-select v-model="queryForm.saleStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已上架" value="ON_SALE" />
            <el-option label="已下架" value="OFF_SALE" />
          </el-select>
        </el-form-item>
        <el-form-item label="推荐状态">
          <el-select v-model="queryForm.recommendStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="普通" value="NORMAL" />
            <el-option label="推荐" value="RECOMMENDED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <el-button type="primary" @click="handleAdd">新增菜品</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image v-if="row.imageUrl" :src="row.imageUrl" style="width:48px;height:48px" fit="cover" />
            <span v-else style="color:#ccc">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="菜品名称" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="basePrice" label="基础价格(元)" width="120" />
        <el-table-column prop="taste" label="口味" width="80" />
        <el-table-column label="推荐" width="70">
          <template #default="{ row }">
            <el-tag :type="row.recommendStatus === 'RECOMMENDED' ? 'warning' : 'info'" size="small">
              {{ row.recommendStatus === 'RECOMMENDED' ? '推荐' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="销售状态" width="90">
          <template #default="{ row }">
            <el-tag :type="saleStatusTag(row.saleStatus)" size="small">
              {{ saleStatusLabel(row.saleStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="row.saleStatus === 'DRAFT' || row.saleStatus === 'OFF_SALE'"
              type="success" link @click="updateSaleStatus(row, 'ON_SALE')">上架</el-button>
            <el-button v-if="row.saleStatus === 'ON_SALE'"
              type="warning" link @click="updateSaleStatus(row, 'OFF_SALE')">下架</el-button>
            <el-button type="warning" link
              @click="toggleRecommend(row)">{{ row.recommendStatus === 'NORMAL' ? '设为推荐' : '取消推荐' }}</el-button>
            <el-popconfirm title="确认删除该菜品？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageQuery.pageNum" v-model:page-size="pageQuery.pageSize"
        :total="total" :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next" @change="fetchData"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="isEdit ? '编辑菜品' : '新增菜品'" v-model="dialogVisible" width="720px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜品名称" prop="name">
              <el-input v-model="form.name" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜品分类" prop="categoryId">
              <el-select v-model="form.categoryId" style="width:100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="基础价格" prop="basePrice">
              <el-input-number v-model="form.basePrice" :min="0" :precision="2" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="口味">
              <el-input v-model="form.taste" maxlength="50" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="推荐状态">
              <el-radio-group v-model="form.recommendStatus">
                <el-radio value="NORMAL">普通</el-radio>
                <el-radio value="RECOMMENDED">推荐</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="销售状态">
              <el-radio-group v-model="form.saleStatus">
                <el-radio value="DRAFT">草稿</el-radio>
                <el-radio value="ON_SALE">上架</el-radio>
                <el-radio value="OFF_SALE">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="菜品图片">
          <el-upload
            :action="uploadUrl" :headers="uploadHeaders" :show-file-list="false"
            :on-success="onUploadSuccess" :before-upload="beforeUpload"
          >
            <el-button type="primary">选择图片</el-button>
            <template #tip>
              <span class="upload-tip"> 支持 JPG/PNG/WebP，不超过5MB</span>
            </template>
          </el-upload>
          <el-image v-if="form.imageUrl" :src="form.imageUrl" style="width:120px;height:80px;margin-top:8px" fit="cover" />
        </el-form-item>
        <el-form-item label="菜品描述">
          <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>

        <!-- 规格 -->
        <el-divider>菜品规格</el-divider>
        <div v-for="(spec, idx) in form.specs" :key="idx" style="display:flex;gap:8px;margin-bottom:8px;">
          <el-input v-model="spec.name" placeholder="规格名称" style="width:150px" />
          <el-input-number v-model="spec.price" :min="0" :precision="2" placeholder="价格" style="width:130px" />
          <el-button type="danger" @click="form.specs.splice(idx, 1)" :disabled="form.specs.length <= 1">删除</el-button>
        </div>
        <el-button type="primary" link @click="form.specs.push({ name: '', price: 0 })">+ 添加规格</el-button>

        <!-- 配方 -->
        <el-divider>菜品配方（原料用量）</el-divider>
        <div v-for="(mat, idx) in form.materials" :key="idx" style="display:flex;gap:8px;margin-bottom:8px;align-items:center">
          <el-select v-model="mat.materialId" placeholder="选择原料" style="width:180px" filterable>
            <el-option v-for="m in enabledMaterials" :key="m.id" :label="m.name + '(' + m.unit + ')'" :value="m.id" />
          </el-select>
          <el-input-number v-model="mat.quantity" :min="0.001" :precision="3" placeholder="用量" style="width:130px" />
          <el-button type="danger" @click="form.materials.splice(idx, 1)" :disabled="form.materials.length <= 1">删除</el-button>
        </div>
        <el-button type="primary" link @click="form.materials.push({ materialId: null as any, quantity: 0 })">+ 添加原料</el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const uploadUrl = '/api/dishes/upload-image'
const uploadHeaders = { Authorization: `Bearer ${authStore.token}` }

const loading = ref(false); const submitting = ref(false)
const dialogVisible = ref(false); const isEdit = ref(false); const editId = ref<number>()
const formRef = ref<FormInstance>()
const tableData = ref<any[]>([]); const total = ref(0)
const categories = ref<any[]>([])
const enabledMaterials = ref<any[]>([])

const pageQuery = reactive({ pageNum: 1, pageSize: 10 })
const queryForm = reactive<Record<string, any>>({ name: '', categoryId: '', saleStatus: '', recommendStatus: '' })

const form = reactive<Record<string, any>>({
  categoryId: '', name: '', basePrice: 0, imageUrl: '', taste: '', description: '',
  recommendStatus: 'NORMAL', saleStatus: 'DRAFT',
  specs: [{ name: '', price: 0 }],
  materials: [{ materialId: null, quantity: 0 }],
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  basePrice: [{ required: true, message: '请输入基础价格', trigger: 'blur' }],
}

function saleStatusLabel(s: string) {
  const m: Record<string, string> = { DRAFT: '草稿', ON_SALE: '已上架', OFF_SALE: '已下架' }; return m[s] || s
}
function saleStatusTag(s: string) {
  const m: Record<string, string> = { DRAFT: 'info', ON_SALE: 'success', OFF_SALE: 'warning' }; return m[s] || ''
}

async function fetchCategories() {
  const res = await request.get('/dish-categories', { params: { pageSize: 100 } })
  categories.value = res.data.list.filter((c: any) => c.status === 'ENABLED')
}

async function fetchMaterials() {
  const res = await request.get('/materials', { params: { status: 'ENABLED', pageSize: 200 } })
  enabledMaterials.value = res.data.list
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { ...pageQuery, ...queryForm }
    Object.keys(params).forEach(k => !params[k] && delete params[k])
    const res = await request.get('/dishes', { params })
    tableData.value = res.data.list; total.value = res.data.total
  } finally { loading.value = false }
}

function handleQuery() { pageQuery.pageNum = 1; fetchData() }
function handleReset() { queryForm.name = ''; queryForm.categoryId = ''; queryForm.saleStatus = ''; queryForm.recommendStatus = ''; pageQuery.pageNum = 1; fetchData() }

function handleAdd() { isEdit.value = false; dialogVisible.value = true }

function handleEdit(row: any) {
  isEdit.value = true; editId.value = row.id
  ;(async () => {
    const res = await request.get(`/dishes/${row.id}`)
    const d = res.data
    Object.assign(form, {
      categoryId: d.categoryId, name: d.name, basePrice: d.basePrice,
      imageUrl: d.imageUrl || '', taste: d.taste || '', description: d.description || '',
      recommendStatus: d.recommendStatus, saleStatus: d.saleStatus,
      specs: d.specs?.length ? d.specs.map((s: any) => ({ name: s.name, price: s.price })) : [{ name: '', price: 0 }],
      materials: d.materials?.length ? d.materials.map((m: any) => ({ materialId: m.materialId, quantity: m.quantity })) : [{ materialId: null, quantity: 0 }],
    })
  })()
  dialogVisible.value = true
}

async function updateSaleStatus(row: any, status: string) {
  try { await request.put(`/dishes/${row.id}/sale-status`, null, { params: { saleStatus: status } }); ElMessage.success('操作成功'); fetchData() } catch { /* handled */ }
}

async function toggleRecommend(row: any) {
  const newStatus = row.recommendStatus === 'NORMAL' ? 'RECOMMENDED' : 'NORMAL'
  try { await request.put(`/dishes/${row.id}/recommend-status`, null, { params: { recommendStatus: newStatus } }); ElMessage.success('操作成功'); fetchData() } catch { /* handled */ }
}

async function handleDelete(row: any) {
  try { await request.delete(`/dishes/${row.id}`); ElMessage.success('删除成功'); fetchData() } catch { /* handled */ }
}

function onUploadSuccess(res: any) { form.imageUrl = res.data }
function beforeUpload(file: File) {
  const allowed = ['image/jpeg', 'image/png', 'image/webp']
  if (!allowed.includes(file.type)) { ElMessage.error('仅支持 JPG/PNG/WebP'); return false }
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('文件不超过5MB'); return false }
  return true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false); if (!valid) return
  submitting.value = true
  try {
    const payload = { ...form }
    // 清理空规格和空配方
    payload.specs = payload.specs.filter((s: any) => s.name)
    payload.materials = payload.materials.filter((m: any) => m.materialId)
    if (payload.specs.length === 0) payload.specs = undefined
    if (isEdit.value) {
      await request.put(`/dishes/${editId.value}`, payload)
    } else {
      await request.post('/dishes', payload)
    }
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, {
    categoryId: '', name: '', basePrice: 0, imageUrl: '', taste: '', description: '',
    recommendStatus: 'NORMAL', saleStatus: 'DRAFT',
    specs: [{ name: '', price: 0 }], materials: [{ materialId: null, quantity: 0 }],
  })
}

onMounted(() => { fetchData(); fetchCategories(); fetchMaterials() })
</script>

<style scoped lang="scss">
.page-container { display: flex; flex-direction: column; gap: 16px; }
.table-header { margin-bottom: 16px; }
.upload-tip { font-size: 12px; color: #909399; margin-left: 8px; }
</style>
