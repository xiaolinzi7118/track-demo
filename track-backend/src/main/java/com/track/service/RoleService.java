package com.track.service;

import com.track.common.Result;
import com.track.entity.Menu;
import com.track.entity.Role;
import com.track.entity.RoleMenu;
import com.track.repository.MenuRepository;
import com.track.repository.RoleMenuRepository;
import com.track.repository.RoleRepository;
import com.track.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private MenuService menuService;

    public Result<List<Role>> list() {
        return Result.success(roleRepository.findAll());
    }

    public Result<Role> add(Role role) {
        if (roleRepository.findByRoleCode(role.getRoleCode()) != null) {
            return Result.error("角色编码已存在");
        }
        role.setCreateTime(LocalDateTime.now());
        role.setUpdateTime(LocalDateTime.now());
        if (role.getStatus() == null) {
            role.setStatus(1);
        }
        return Result.success(roleRepository.save(role));
    }

    public Result<Role> update(Role role) {
        Role existing = roleRepository.findById(role.getId()).orElse(null);
        if (existing == null) {
            return Result.error("角色不存在");
        }
        existing.setRoleName(role.getRoleName());
        existing.setDescription(role.getDescription());
        if (role.getStatus() != null) {
            existing.setStatus(role.getStatus());
        }
        existing.setUpdateTime(LocalDateTime.now());
        return Result.success(roleRepository.save(existing));
    }

    public Result<Void> delete(Long id) {
        List<com.track.entity.UserRole> userRoles = userRoleRepository.findByRoleId(id);
        if (!userRoles.isEmpty()) {
            return Result.error("该角色下有用户，不能删除");
        }
        roleMenuRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
        return Result.success();
    }

    public Result<Void> assignPermissions(Long roleId, List<Long> menuIds) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            return Result.error("角色不存在");
        }
        roleMenuRepository.deleteByRoleId(roleId);
        for (Long menuId : menuIds) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenuRepository.save(rm);
        }
        return Result.success();
    }

    public Result<List<Menu>> getPermissionTree(Long roleId) {
        List<Menu> allMenus = menuService.getMenuTree();
        List<RoleMenu> roleMenus = roleMenuRepository.findByRoleId(roleId);
        List<Long> assignedIds = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
        markChecked(allMenus, assignedIds);
        return Result.success(allMenus);
    }

    private void markChecked(List<Menu> menus, List<Long> assignedIds) {
        for (Menu menu : menus) {
            menu.setChecked(assignedIds.contains(menu.getId()));
            if (menu.getChildren() != null) {
                markChecked(menu.getChildren(), assignedIds);
            }
        }
    }
}
