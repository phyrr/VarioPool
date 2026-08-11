<template>
  <el-dialog
    :model-value="visible"
    title="编辑线程池参数"
    width="640px"
    class="pool-edit-dialog"
    @close="emit('update:visible', false)"
  >
    <el-form :model="form" label-width="140px">
      <el-form-item label="Pool ID">
        <el-input v-model="form.poolId" disabled />
      </el-form-item>
      <el-form-item label="核心线程数">
        <el-input-number v-model="form.corePoolSize" :min="1" :max="512" />
      </el-form-item>
      <el-form-item label="最大线程数">
        <el-input-number v-model="form.maximumPoolSize" :min="1" :max="512" />
      </el-form-item>
      <el-form-item label="队列容量">
        <el-input-number v-model="form.queueCapacity" :min="0" :max="9999999" />
      </el-form-item>
      <el-form-item label="队列类型">
        <el-select v-model="form.workQueue" style="width: 100%">
          <el-option label="ResizableCapacityLinkedBlockingQueue" value="ResizableCapacityLinkedBlockingQueue" />
          <el-option label="LinkedBlockingQueue" value="LinkedBlockingQueue" />
          <el-option label="ArrayBlockingQueue" value="ArrayBlockingQueue" />
          <el-option label="SynchronousQueue" value="SynchronousQueue" />
        </el-select>
      </el-form-item>
      <el-form-item label="拒绝策略">
        <el-select v-model="form.rejectedHandler" style="width: 100%">
          <el-option label="CallerRunsPolicy" value="CallerRunsPolicy" />
          <el-option label="AbortPolicy" value="AbortPolicy" />
          <el-option label="DiscardPolicy" value="DiscardPolicy" />
          <el-option label="DiscardOldestPolicy" value="DiscardOldestPolicy" />
        </el-select>
      </el-form-item>
      <el-form-item label="KeepAlive(秒)">
        <el-input-number v-model="form.keepAliveSeconds" :min="1" :max="3600" />
      </el-form-item>
      <el-form-item label="核心线程超时">
        <el-switch v-model="form.allowCoreThreadTimeout" />
      </el-form-item>
      <el-form-item label="同步到 Redis">
        <el-switch v-model="form.syncRedis" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button round @click="emit('update:visible', false)">取消</el-button>
        <el-button type="primary" round :loading="loading" @click="submit">保存并发布</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { updateThreadPool } from '../api/pool'

const props = defineProps({
  visible: Boolean,
  pool: Object
})
const emit = defineEmits(['update:visible', 'saved'])
const loading = ref(false)
const form = reactive({
  appName: '',
  dataId: '',
  group: '',
  poolId: '',
  corePoolSize: 1,
  maximumPoolSize: 1,
  queueCapacity: 100,
  workQueue: 'ResizableCapacityLinkedBlockingQueue',
  rejectedHandler: 'CallerRunsPolicy',
  keepAliveSeconds: 60,
  allowCoreThreadTimeout: false,
  syncRedis: true
})

watch(() => props.pool, (pool) => {
  if (!pool) return
  Object.assign(form, {
    appName: pool.appName,
    dataId: pool.dataId,
    group: pool.group,
    poolId: pool.poolId,
    corePoolSize: pool.corePoolSize,
    maximumPoolSize: pool.maximumPoolSize,
    queueCapacity: pool.queueCapacity,
    workQueue: pool.workQueue,
    rejectedHandler: pool.rejectedHandler,
    keepAliveSeconds: pool.keepAliveSeconds,
    allowCoreThreadTimeout: pool.allowCoreThreadTimeout,
    syncRedis: true
  })
}, { immediate: true })

async function submit() {
  if (form.corePoolSize > form.maximumPoolSize) {
    ElMessage.error('核心线程数不能大于最大线程数')
    return
  }
  loading.value = true
  try {
    await updateThreadPool(form)
    ElMessage.success('参数已发布到 Nacos / Redis')
    emit('saved')
    emit('update:visible', false)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
