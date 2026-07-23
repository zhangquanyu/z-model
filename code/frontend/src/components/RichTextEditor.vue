<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

const props = defineProps<{
  modelValue: string
  placeholder?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const content = ref(props.modelValue)

watch(() => props.modelValue, (newVal) => {
  content.value = newVal
})

const handleUpdate = (val: string) => {
  emit('update:modelValue', val)
}

const modules = {
  toolbar: [
    ['bold', 'italic', 'underline', 'strike'],
    ['blockquote', 'code-block'],
    [{ 'header': 1 }, { 'header': 2 }],
    [{ 'list': 'ordered' }, { 'list': 'bullet' }],
    [{ 'script': 'sub' }, { 'script': 'super' }],
    [{ 'indent': '-1' }, { 'indent': '+1' }],
    [{ 'direction': 'rtl' }],
    [{ 'size': ['small', false, 'large', 'huge'] }],
    [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
    [{ 'color': [] }, { 'background': [] }],
    [{ 'font': [] }],
    [{ 'align': [] }],
    ['clean'],
    ['link', 'image']
  ]
}

const theme = 'snow'
</script>

<template>
  <QuillEditor
    v-model:content="content"
    :modules="modules"
    :theme="theme"
    :placeholder="placeholder || '请输入内容'"
    content-type="html"
    @update:content="handleUpdate"
    class="rich-text-editor"
  />
</template>

<style lang="scss" scoped>
.rich-text-editor {
  :deep(.ql-container) {
    font-size: 14px;
    min-height: 200px;
    border-radius: 10px;
  }

  :deep(.ql-editor) {
    min-height: 200px;
    padding: 16px;
    background-color: white;
  }

  :deep(.ql-toolbar) {
    border-radius: 10px 10px 0 0;
    background-color: #fafbfc;
    border-color: #e8ebf0;
  }

  :deep(.ql-container) {
    border-radius: 0 0 10px 10px;
    border-color: #e8ebf0;
  }

  :deep(.ql-toolbar.ql-snow) {
    border-color: #e8ebf0;
    border-bottom: none;
  }
}
</style>