package com.track.controller;

import com.track.common.PermissionChecker;
import com.track.common.Result;
import com.track.entity.Role;
import com.track.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    public Result<List<Role>> list() {
        permissionChecker.checkPermission("system-role:view");
        return Result.success(roleService.findAll());
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
    public Result<List<Long>> getRoleMenus(@RequestParam Long id) {
        permissionChecker.checkPermission("system-role:view");
        return Result.success(roleService.getRoleMenuIds(id));
    }

    @PostMapping("/update-menus")
    public Result<Void> updateRoleMenus(@RequestBody Map<String, Object> request) {
        permissionChecker.checkPermission("system-role:edit");
        Long roleId = Long.valueOf(request.get("roleId").toString());
        @SuppressWarnings("unchecked")
        List<Integer> menuIdInts = (List<Integer>) request.get("menuIds");
        List<Long> menuIds = new ArrayList<>();
        for (Integer id : menuIdInts) {
            menuIds.add(id.longValue());
        }
        return roleService.updateRoleMenus(roleId, menuIds);
    }
}
