package com.track.service;

import com.track.entity.Menu;
import com.track.entity.RoleMenu;
import com.track.repository.MenuRepository;
import com.track.repository.RoleMenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    public List<Menu> getMenuTree() {
        List<Menu> allMenus = menuRepository.findByStatusOrderBySortOrder(1);
        return buildTree(allMenus, 0L);
    }

    public List<Menu> getMenuTreeByRoleIds(List<Long> roleIds) {
        List<Long> menuIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            List<RoleMenu> roleMenus = roleMenuRepository.findByRoleId(roleId);
            menuIds.addAll(roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList()));
        }
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Menu> menus = menuRepository.findByStatusOrderBySortOrder(1);
        List<Long> finalMenuIds = menuIds.stream().distinct().collect(Collectors.toList());
        List<Menu> authorizedMenus = menus.stream()
                .filter(m -> finalMenuIds.contains(m.getId()))
                .collect(Collectors.toList());
        return buildTree(authorizedMenus, 0L);
    }

    public List<String> getPermissionCodesByRoleIds(List<Long> roleIds) {
        List<Long> menuIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            List<RoleMenu> roleMenus = roleMenuRepository.findByRoleId(roleId);
            menuIds.addAll(roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList()));
        }
        if (menuIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Menu> menus = menuRepository.findByStatusOrderBySortOrder(1);
        List<Long> finalMenuIds = menuIds.stream().distinct().collect(Collectors.toList());
        return menus.stream()
                .filter(m -> finalMenuIds.contains(m.getId()))
                .map(Menu::getMenuCode)
                .collect(Collectors.toList());
    }

    private List<Menu> buildTree(List<Menu> menus, Long parentId) {
        List<Menu> tree = new ArrayList<>();
        for (Menu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                menu.setChildren(buildTree(menus, menu.getId()));
                tree.add(menu);
            }
        }
        return tree;
    }
}
