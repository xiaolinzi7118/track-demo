package com.track.controller;

import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.dto.RoleMenuItemResponse;
import com.track.entity.Role;
import com.track.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionChecker permissionChecker;

    @GetMapping("/list")
    public Result<Page<Role>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        permissionChecker.checkPermission("system-role:view");
        return Result.success(roleService.pageList(pageNum, pageSize));
    }

    @GetMapping("/detail")
    public Result<Role> detail(@RequestParam Long id) {
        permissionChecker.checkPermission("system-role:view");
        Role role = roleService.findById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success(role);
    }

    @PostMapping("/add")
    public Result<Role> add(@RequestBody Role role) {
        permissionChecker.checkPermission("system-role:add");
        return roleService.add(role);
    }

    @PostMapping("/update")
    public Result<Role> update(@RequestBody Role role) {
        permissionChecker.checkPermission("system-role:edit");
        return roleService.update(role);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> request) {
        permissionChecker.checkPermission("system-role:delete");
        return roleService.delete(request.get("id"));
    }

    @GetMapping("/menus")
    public Result<List<RoleMenuItemResponse>> getRoleMenus(@RequestParam Long id) {
        permissionChecker.checkPermission("system-role:view");
        return Result.success(roleService.getRoleMenus(id));
    }

    @PostMapping("/update-menus")
    public Result<Void> updateRoleMenus(@RequestBody Map<String, Object> request) {
        permissionChecker.checkPermission("system-role:edit");
        Long roleId = parseLong(request.get("roleId"));
        if (roleId == null) {
            return Result.error("roleId is required");
        }
        List<Long> menuIds = parseLongList(request.get("menuIds"));
        return roleService.updateRoleMenus(roleId, menuIds);
    }

    private List<Long> parseLongList(Object value) {
        java.util.ArrayList<Long> result = new java.util.ArrayList<>();
        if (value == null) {
            return result;
        }
        if (value instanceof List<?>) {
            for (Object item : (List<?>) value) {
                Long parsed = parseLong(item);
                if (parsed != null) {
                    result.add(parsed);
                }
            }
            return result;
        }
        Long parsed = parseLong(value);
        if (parsed != null) {
            result.add(parsed);
        }
        return result;
    }

    private Long parseLong(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(value.toString());
        } catch (Exception ignored) {
            return null;
        }
    }
}
