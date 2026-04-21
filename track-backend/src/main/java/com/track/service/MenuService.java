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
        List<Menu> allMenus = menuRepository.findByStatusOrderBySortOrder(1);
        return buildFullTree(allMenus);
    }

    public List<Menu> getMenusByUserId(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);

        // 检查是否是admin角色
        for (UserRole ur : userRoles) {
            Role role = roleRepository.findById(ur.getRoleId()).orElse(null);
            if (role != null && "admin".equalsIgnoreCase(role.getRoleCode())) {
                return getMenuTree();
            }
        }

        // 获取用户角色关联的所有菜单ID
        Set<Long> menuIds = new HashSet<>();
        for (UserRole ur : userRoles) {
            List<RoleMenu> roleMenus = roleMenuRepository.findByRoleId(ur.getRoleId());
            for (RoleMenu rm : roleMenus) {
                menuIds.add(rm.getMenuId());
            }
        }

        if (menuIds.isEmpty()) return new ArrayList<>();

        // 查找用户有 "xxx:view" 权限的页面
        List<Menu> allMenus = menuRepository.findByStatusOrderBySortOrder(1);
        Set<String> userPerms = new HashSet<>();
        for (Menu m : allMenus) {
            if (menuIds.contains(m.getId()) && m.getPerms() != null) {
                userPerms.add(m.getPerms());
            }
        }

        // 找出有查看权限的页面（perms以:view结尾或页面本身被授权且其子按钮有:view）
        Set<Long> viewablePageIds = new HashSet<>();
        for (Menu m : allMenus) {
            if (m.getMenuType() == 3 && m.getPerms() != null && m.getPerms().endsWith(":view")) {
                if (menuIds.contains(m.getId())) {
                    viewablePageIds.add(m.getParentId());
                }
            }
        }

        // 仪表盘默认对所有角色可见
        for (Menu m : allMenus) {
            if ("dashboard".equals(m.getMenuCode()) && m.getMenuType() == 2) {
                viewablePageIds.add(m.getId());
            }
        }

        // 构建用户可见的菜单树：只包含可见的页面和其所属目录
        List<Menu> visibleMenus = new ArrayList<>();
        Set<Long> visibleDirIds = new HashSet<>();

        for (Menu m : allMenus) {
            if (m.getMenuType() == 2 && viewablePageIds.contains(m.getId())) {
                visibleMenus.add(m);
                if (m.getParentId() != null && m.getParentId() > 0) {
                    visibleDirIds.add(m.getParentId());
                }
            }
        }

        // 加入目录
        for (Menu m : allMenus) {
            if (m.getMenuType() == 1 && visibleDirIds.contains(m.getId())) {
                visibleMenus.add(m);
            }
        }

        return buildTree(visibleMenus);
    }

    public List<String> getPermissionsByUserId(Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);

        for (UserRole ur : userRoles) {
            Role role = roleRepository.findById(ur.getRoleId()).orElse(null);
            if (role != null && "admin".equalsIgnoreCase(role.getRoleCode())) {
                List<Menu> allMenus = menuRepository.findByStatusOrderBySortOrder(1);
                return allMenus.stream()
                        .filter(m -> m.getPerms() != null)
                        .map(Menu::getPerms)
                        .collect(Collectors.toList());
            }
        }

        Set<Long> menuIds = new HashSet<>();
        for (UserRole ur : userRoles) {
            List<RoleMenu> roleMenus = roleMenuRepository.findByRoleId(ur.getRoleId());
            for (RoleMenu rm : roleMenus) {
                menuIds.add(rm.getMenuId());
            }
        }

        List<Menu> allMenus = menuRepository.findByStatusOrderBySortOrder(1);
        return allMenus.stream()
                .filter(m -> menuIds.contains(m.getId()) && m.getPerms() != null)
                .map(Menu::getPerms)
                .collect(Collectors.toList());
    }

    private List<Menu> buildTree(List<Menu> menus) {
        Map<Long, List<Menu>> childrenMap = new LinkedHashMap<>();
        List<Menu> roots = new ArrayList<>();

        for (Menu m : menus) {
            if (m.getMenuType() == 3) continue; // 按钮不参与菜单树
            if (m.getParentId() == null || m.getParentId() == 0) {
                roots.add(m);
            } else {
                childrenMap.computeIfAbsent(m.getParentId(), k -> new ArrayList<>()).add(m);
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

        for (Menu m : menus) {
            if (m.getParentId() == null || m.getParentId() == 0) {
                roots.add(m);
            } else {
                childrenMap.computeIfAbsent(m.getParentId(), k -> new ArrayList<>()).add(m);
            }
        }

        for (Menu root : roots) {
            root.setChildren(getChildren(root.getId(), childrenMap));
        }

        return roots;
    }

    private List<Menu> getChildren(Long parentId, Map<Long, List<Menu>> childrenMap) {
        List<Menu> children = childrenMap.get(parentId);
        if (children == null) return new ArrayList<>();
        for (Menu child : children) {
            child.setChildren(getChildren(child.getId(), childrenMap));
        }
        return children;
    }
}
