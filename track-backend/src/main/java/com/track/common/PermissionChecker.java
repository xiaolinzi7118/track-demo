package com.track.common;

import com.track.entity.Role;
import com.track.entity.User;
import com.track.entity.UserRole;
import com.track.repository.RoleRepository;
import com.track.repository.UserRepository;
import com.track.repository.UserRoleRepository;
import com.track.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class PermissionChecker {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuService menuService;

    private final ConcurrentHashMap<Long, CachedPermissions> permissionCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 5 * 60 * 1000;

    public Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        HttpServletRequest request = attributes.getRequest();
        return (Long) request.getAttribute("currentUserId");
    }

    public boolean hasPermission(String permission) {
        Long userId = getCurrentUserId();
        if (userId == null) return false;

        if (isBuiltInSuperAdmin(userId)) {
            return true;
        }

        Set<String> permissions = getPermissionsWithCache(userId);

        return permissions.contains(permission);
    }

    public void checkPermission(String permission) {
        if (!hasPermission(permission)) {
            throw new RuntimeException("No permission: " + permission);
        }
    }

    public boolean hasAnyRole(String... roleCodes) {
        Long userId = getCurrentUserId();
        if (userId == null || roleCodes == null || roleCodes.length == 0) {
            return false;
        }

        Set<String> targetRoles = Arrays.stream(roleCodes)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        if (targetRoles.isEmpty()) {
            return false;
        }
        if (isBuiltInSuperAdmin(userId)) {
            return true;
        }

        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        for (UserRole userRole : userRoles) {
            Role role = roleRepository.findById(userRole.getRoleId()).orElse(null);
            if (role != null && role.getRoleCode() != null && targetRoles.contains(role.getRoleCode().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public void checkAnyRole(String... roleCodes) {
        if (!hasAnyRole(roleCodes)) {
            throw new RuntimeException("No permission for current role");
        }
    }

    private boolean isBuiltInSuperAdmin(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        return user != null && Integer.valueOf(1).equals(user.getIsBuiltinSuperAdmin());
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
