package com.track.service;

import com.track.common.Result;
import com.track.entity.Role;
import com.track.entity.RoleMenu;
import com.track.entity.UserRole;
import com.track.repository.RoleMenuRepository;
import com.track.repository.RoleRepository;
import com.track.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

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

    public List<Long> getRoleMenuIds(Long roleId) {
        List<RoleMenu> roleMenus = roleMenuRepository.findByRoleId(roleId);
        return roleMenus.stream().map(RoleMenu::getMenuId).collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public Result<Void> updateRoleMenus(Long roleId, List<Long> menuIds) {
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
}
