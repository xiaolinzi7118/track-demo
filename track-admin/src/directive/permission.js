import { useUserStore } from '../store/user'

export default {
  mounted(el, binding) {
    const userStore = useUserStore()
    const requiredPermission = binding.value
    if (requiredPermission && !userStore.hasPermission(requiredPermission)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
