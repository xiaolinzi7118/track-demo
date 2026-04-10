package com.track.controller;

import com.track.common.Result;
import com.track.entity.Menu;
import com.track.entity.Role;
import com.track.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping("/list")
    public Result<List<Role>> list() {
        return roleService.list();
    }

    @PostMapping("/add")
    public Result<Role> add(@RequestBody Role role) {
        return roleService.add(role);
    }

    @PostMapping("/update")
    public Result<Role> update(@RequestBody Role role) {
        return roleService.update(role);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestBody Map<String, Long> request) {
        return roleService.delete(request.get("id"));
    }

    @GetMapping("/permission-tree")
    public Result<List<Menu>> getPermissionTree(@RequestParam Long roleId) {
        return roleService.getPermissionTree(roleId);
    }

    @PostMapping("/assign-permissions")
    public Result<Void> assignPermissions(@RequestBody Map<String, Object> request) {
        Long roleId = ((Number) request.get("roleId")).longValue();
        @SuppressWarnings("unchecked")
        List<Number> menuIds = (List<Number>) request.get("menuIds");
        List<Long> ids = new java.util.ArrayList<>();
        for (Number n : menuIds) {
            ids.add(n.longValue());
        }
        return roleService.assignPermissions(roleId, ids);
    }
}
