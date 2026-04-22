package com.track.service;

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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private static final String ADMIN_ROLE_CODE = "admin";
    private static final String DEVELOPER_ROLE_CODE = "developer";
    private static final String DASHBOARD_MENU_CODE = "dashboard";
    private static final String DICT_PARAM_MENU_CODE = "system-dict-param";

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RoleMenuRepository roleMenuRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private RoleRepository roleRepository;

    public List<Menu> getMenuTree() {
        List<Menu> allMenus = menuRepository.findByStatusOrderBySortOrder(1);
        return buildTree(allMenus);
    }

    public List<Menu> getFullMenuTree() {
        List<Menu> allMenus = menuRepository.findByStatusOrderBySortOrder(1).stream()
                .filter(menu -> !DICT_PARAM_MENU_CODE.equals(menu.getMenuCode()))
                .collect(Collectors.toList());
        return buildFullTree(allMenus);
    }

    public List<Menu> getMenusByUserId(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        Set<String> roleCodes = getRoleCodes(userRoles);

        if (roleCodes.contains(ADMIN_ROLE_CODE)) {
            return getMenuTree();
        }

        Set<Long> menuIds = new HashSet<>();
        for (UserRole userRole : userRoles) {
            List<RoleMenu> roleMenus = roleMenuRepository.findByRoleId(userRole.getRoleId());
            for (RoleMenu roleMenu : roleMenus) {
                menuIds.add(roleMenu.getMenuId());
            }
        }

        List<Menu> allMenus = menuRepository.findByStatusOrderBySortOrder(1);
        Set<Long> viewablePageIds = collectViewablePageIds(menuIds, allMenus);

        // Dashboard is always visible.
        for (Menu menu : allMenus) {
            if (DASHBOARD_MENU_CODE.equals(menu.getMenuCode()) && menu.getMenuType() == 2) {
                viewablePageIds.add(menu.getId());
                break;
            }
        }

        // Dict parameter page is role-hardcoded for developer, outside role-permission config.
        if (roleCodes.contains(DEVELOPER_ROLE_CODE)) {
            for (Menu menu : allMenus) {
                if (DICT_PARAM_MENU_CODE.equals(menu.getMenuCode()) && menu.getMenuType() == 2) {
                    viewablePageIds.add(menu.getId());
                    break;
                }
            }
        }

        List<Menu> visibleMenus = new ArrayList<>();
        Set<Long> visibleDirIds = new HashSet<>();

        for (Menu menu : allMenus) {
            if (menu.getMenuType() == 2 && viewablePageIds.contains(menu.getId())) {
                visibleMenus.add(menu);
                if (menu.getParentId() != null && menu.getParentId() > 0) {
                    visibleDirIds.add(menu.getParentId());
                }
            }
        }

        for (Menu menu : allMenus) {
            if (menu.getMenuType() == 1 && visibleDirIds.contains(menu.getId())) {
                visibleMenus.add(menu);
            }
        }

        return buildTree(visibleMenus);
    }

    public List<String> getPermissionsByUserId(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);

        for (UserRole userRole : userRoles) {
            Role role = roleRepository.findById(userRole.getRoleId()).orElse(null);
            if (role != null && ADMIN_ROLE_CODE.equalsIgnoreCase(role.getRoleCode())) {
                List<Menu> allMenus = menuRepository.findByStatusOrderBySortOrder(1);
                return allMenus.stream()
                        .filter(menu -> menu.getPerms() != null)
                        .map(Menu::getPerms)
                        .collect(Collectors.toList());
            }
        }

        Set<Long> menuIds = new HashSet<>();
        for (UserRole userRole : userRoles) {
            List<RoleMenu> roleMenus = roleMenuRepository.findByRoleId(userRole.getRoleId());
            for (RoleMenu roleMenu : roleMenus) {
                menuIds.add(roleMenu.getMenuId());
            }
        }

        List<Menu> allMenus = menuRepository.findByStatusOrderBySortOrder(1);
        return allMenus.stream()
                .filter(menu -> menuIds.contains(menu.getId()) && menu.getPerms() != null)
                .map(Menu::getPerms)
                .collect(Collectors.toList());
    }

    private Set<Long> collectViewablePageIds(Set<Long> menuIds, List<Menu> allMenus) {
        Set<Long> viewablePageIds = new HashSet<>();
        for (Menu menu : allMenus) {
            if (menu.getMenuType() == 3
                    && menu.getPerms() != null
                    && menu.getPerms().endsWith(":view")
                    && menuIds.contains(menu.getId())) {
                viewablePageIds.add(menu.getParentId());
            }
        }
        return viewablePageIds;
    }

    private Set<String> getRoleCodes(List<UserRole> userRoles) {
        Set<String> roleCodes = new HashSet<>();
        for (UserRole userRole : userRoles) {
            Role role = roleRepository.findById(userRole.getRoleId()).orElse(null);
            if (role != null && role.getRoleCode() != null) {
                roleCodes.add(role.getRoleCode().toLowerCase());
            }
        }
        return roleCodes;
    }

    private List<Menu> buildTree(List<Menu> menus) {
        Map<Long, List<Menu>> childrenMap = new LinkedHashMap<>();
        List<Menu> roots = new ArrayList<>();

        for (Menu menu : menus) {
            if (menu.getMenuType() == 3) continue;
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                roots.add(menu);
            } else {
                childrenMap.computeIfAbsent(menu.getParentId(), k -> new ArrayList<>()).add(menu);
            }
        }

        for (Menu root : roots) {
            root.setChildren(getChildren(root.getId(), childrenMap));
        }

        return roots;
    }

    private List<Menu> buildFullTree(List<Menu> menus) {
        Map<Long, List<Menu>> childrenMap = new LinkedHashMap<>();
        List<Menu> roots = new ArrayList<>();

        for (Menu menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                roots.add(menu);
            } else {
                childrenMap.computeIfAbsent(menu.getParentId(), k -> new ArrayList<>()).add(menu);
            }
        }

        for (Menu root : roots) {
            root.setChildren(getChildren(root.getId(), childrenMap));
        }

        return roots;
    }

    private List<Menu> getChildren(Long parentId, Map<Long, List<Menu>> childrenMap) {
        List<Menu> children = childrenMap.get(parentId);
        if (children == null) {
            return new ArrayList<>();
        }
        for (Menu child : children) {
            child.setChildren(getChildren(child.getId(), childrenMap));
        }
        return children;
    }
}
