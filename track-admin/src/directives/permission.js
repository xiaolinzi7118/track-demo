import { useUserStore } from '../store/user'

export const permission = {
  mounted(el, binding) {
    const { value } = binding
    if (!value) return

    const userStore = useUserStore()
    if (!userStore.hasPermission(value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

export function setupPermissionDirective(app) {
  app.directive('permission', permission)
}
