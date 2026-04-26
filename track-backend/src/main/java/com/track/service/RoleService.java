package com.track.service;

import com.track.common.Result;
import com.track.common.SnowflakeIdGenerator;
import com.track.dto.RoleMenuItemResponse;
import com.track.entity.Menu;
import com.track.entity.Role;
import com.track.entity.RoleMenu;
import com.track.entity.UserRole;
import com.track.repository.MenuRepository;
import com.track.repository.RoleMenuRepository;
import com.track.repository.RoleRepository;
import com.track.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private SnowflakeIdGenerator snowflakeIdGenerator;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(Long id) {
        return roleRepository.findById(id).orElse(null);
    }

    public Result<Role> add(Role role) {
        if (roleRepository.findByRoleCode(role.getRoleCode()) != null) {
            return Result.error("角色编码已存在");
        }
        if (role.getId() == null) {
            role.setId(snowflakeIdGenerator.nextId());
        }
        roleRepository.save(role);
        return Result.success(role);
    }

    public Result<Role> update(Role role) {
        Role existing = roleRepository.findById(role.getId()).orElse(null);
        if (existing == null) {
            return Result.error("角色不存在");
        }
        existing.setRoleName(role.getRoleName());
        existing.setDescription(role.getDescription());
        existing.setStatus(role.getStatus());
        roleRepository.save(existing);
        return Result.success(existing);
    }

    public Result<Void> delete(Long id) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role == null) {
            return Result.error("角色不存在");
        }
        if ("admin".equals(role.getRoleCode())) {
            return Result.error("不能删除管理员角色");
        }

        List<UserRole> users = userRoleRepository.findByRoleId(id);
        if (!users.isEmpty()) {
            return Result.error("该角色下有用户，不能删除");
        }

        roleMenuRepository.deleteByRoleId(id);
        roleRepository.deleteById(id);
        return Result.success();
    }

    public List<RoleMenuItemResponse> getRoleMenus(Long roleId) {
        List<RoleMenu> roleMenus = roleMenuRepository.findByRoleId(roleId);
        List<Long> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
        java.util.Map<Long, Menu> menuMap = menuIds.isEmpty()
                ? new java.util.HashMap<>()
                : menuRepository.findAllById(menuIds).stream()
                .collect(Collectors.toMap(Menu::getId, m -> m, (a, b) -> a));

        List<RoleMenuItemResponse> result = new ArrayList<>();
        for (Long menuId : menuIds) {
            Menu menu = menuMap.get(menuId);
            if (menu == null) {
                continue;
            }
            result.add(new RoleMenuItemResponse(menuId, menu.getMenuCode()));
        }
        return result;
    }

    @Transactional
    public Result<Void> updateRoleMenus(Long roleId, List<Long> menuIds) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            return Result.error("角色不存在");
        }

        List<Long> distinctMenuIds = menuIds == null
                ? java.util.Collections.emptyList()
                : menuIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());

        roleMenuRepository.deleteByRoleId(roleId);
        // Force delete SQL execution before insert SQL in the same transaction.
        roleMenuRepository.flush();

        List<RoleMenu> roleMenus = new ArrayList<>(distinctMenuIds.size());
        for (Long menuId : distinctMenuIds) {
            RoleMenu rm = new RoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            roleMenus.add(rm);
        }
        if (!roleMenus.isEmpty()) {
            roleMenuRepository.saveAll(roleMenus);
        }

        return Result.success();
    }
}
