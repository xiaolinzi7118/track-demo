package com.track.common;

import com.track.entity.Role;
import com.track.entity.UserRole;
import com.track.repository.RoleRepository;
import com.track.repository.UserRoleRepository;
import com.track.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PermissionChecker {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuService menuService;

    private final ConcurrentHashMap<Long, CachedPermissions> permissionCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000;

    public Long getCurrentUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return (Long) request.getAttribute("currentUserId");
    }

    public boolean hasPermission(String permission) {
        Long userId = getCurrentUserId();
        if (userId == null) return false;

        Set<String> permissions = getPermissionsWithCache(userId);

        // 检查是否admin角色
        if (isAdmin(userId)) return true;

        return permissions.contains(permission);
    }

    public void checkPermission(String permission) {
        if (!hasPermission(permission)) {
            throw new RuntimeException("没有权限执行此操作: " + permission);
        }
    }

    private boolean isAdmin(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        for (UserRole ur : userRoles) {
            Role role = roleRepository.findById(ur.getRoleId()).orElse(null);
            if (role != null && "admin".equalsIgnoreCase(role.getRoleCode())) {
                return true;
            }
        }
        return false;
    }

    private Set<String> getPermissionsWithCache(Long userId) {
        CachedPermissions cached = permissionCache.get(userId);
        if (cached != null && System.currentTimeMillis() - cached.timestamp < CACHE_TTL_MS) {
            return cached.permissions;
        }

        List<String> perms = menuService.getPermissionsByUserId(userId);
        Set<String> permSet = new HashSet<>(perms);

        permissionCache.put(userId, new CachedPermissions(permSet, System.currentTimeMillis()));
        return permSet;
    }

    public void clearCache(Long userId) {
        permissionCache.remove(userId);
    }

    private static class CachedPermissions {
        final Set<String> permissions;
        final long timestamp;

        CachedPermissions(Set<String> permissions, long timestamp) {
            this.permissions = permissions;
            this.timestamp = timestamp;
        }
    }
}
