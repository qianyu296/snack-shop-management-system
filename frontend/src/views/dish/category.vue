<template>
  <div class="page-container">
    <!-- 查询区 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" inline>
        <el-form-item label="分类名称">
          <el-input v-model="queryForm.name" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="启用" value="ENABLED" />
            <el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作 + 表格 -->
    <el-card class="table-card" shadow="never">
      <div class="table-header">
        <el-button type="primary" @click="handleAdd">新增分类</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="sort" label="排序值" width="100" />
        <el-table-column prop="dishCount" label="菜品数量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ row.status === 'ENABLED' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 'ENABLED'"
              type="warning" link @click="handleToggleStatus(row, 'DISABLED')"
            >停用</el-button>
            <el-button
              v-else
              type="success" link @click="handleToggleStatus(row, 'ENABLED')"
            >启用</el-button>
            <el-popconfirm title="确认删除该分类？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link :disabled="row.dishCount > 0">
                  删除
                </el-button>
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
    <el-dialog
      :title="isEdit ? '编辑分类' : '新增分类'"
      v-model="dialogVisible"
      width="520px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="2-20个字符" maxlength="20" />
        </el-form-item>
        <el-form-item label="排序值" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number>()
const formRef = ref<FormInstance>()
const tableData = ref<any[]>([])
const total = ref(0)

const pageQuery = reactive({ pageNum: 1, pageSize: 10 })
const queryForm = reactive({ name: '', status: '' })

const form = reactive({
  name: '',
  sort: 0,
  status: 'ENABLED',
  remark: '',
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序值', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { ...pageQuery, ...queryForm }
    Object.keys(params).forEach(k => !params[k] && delete params[k])
    const res = await request.get('/dish-categories', { params })
    tableData.value = res.data.list
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  pageQuery.pageNum = 1
  fetchData()
}

function handleReset() {
  queryForm.name = ''
  queryForm.status = ''
  pageQuery.pageNum = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  editId.value = undefined
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  editId.value = row.id
  form.name = row.name
  form.sort = row.sort
  form.status = row.status
  form.remark = row.remark || ''
  dialogVisible.value = true
}

async function handleToggleStatus(row: any, status: string) {
  try {
    await request.put(`/dish-categories/${row.id}/status`, null, { params: { status } })
    ElMessage.success('操作成功')
    fetchData()
  } catch { /* handled */ }
}

async function handleDelete(row: any) {
  try {
    await request.delete(`/dish-categories/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* handled */ }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await request.put(`/dish-categories/${editId.value}`, form)
      ElMessage.success('修改成功')
    } else {
      await request.post('/dish-categories', form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
  form.name = ''
  form.sort = 0
  form.status = 'ENABLED'
  form.remark = ''
}

onMounted(() => fetchData())
</script>

<style scoped lang="scss">
.page-container { display: flex; flex-direction: column; gap: 16px; }
.table-header { margin-bottom: 16px; }
</style>
