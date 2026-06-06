import { ref, reactive } from 'vue'

/**
 * 通用分页 composable
 * 用法: const { list, page, pageSize, total, loadPage, getParams } = usePagination(fetchFn)
 *       loadPage()  // 加载数据
 *       getParams() // 返回 { page, pageSize } 用于 axios params
 */
export function usePagination(fetchFn) {
  const list = ref([])
  const page = ref(1)
  const pageSize = ref(10)
  const total = ref(0)

  const loadPage = async (extraParams = {}) => {
    const res = await fetchFn({ page: page.value, pageSize: pageSize.value, ...extraParams })
    if (res.data.code === 200) {
      list.value = res.data.data.records || []
      total.value = res.data.data.total || 0
    }
    return res
  }

  const onPageChange = (newPage) => {
    page.value = newPage
    loadPage()
  }

  const getParams = () => ({ page: page.value, pageSize: pageSize.value })

  return { list, page, pageSize, total, loadPage, onPageChange, getParams }
}
